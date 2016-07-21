package just4fun.kotlin.schemify.production

import just4fun.kotlin.schemify.core.*


object DefaultFactory : ReaderFactory<String>, WriterFactory<String> {
	override fun invoke(input: String): Reader<String> 	= DefaultReader(input)
	override fun invoke(): Writer<String> = DefaultWriter()
}


/* READER */

class DefaultReader(override val input: String) : Reader<String>() {
	val chars: CharArray = input.toCharArray()
	var cursor = 0
	val lastIndex = chars.size - 1
	var char: Char = if (lastIndex < 0) '\u0000' else chars[0]

	override fun readRootEntry(entryBuilder: EntryBuilder): Entry = readNextEntry(entryBuilder, true)
	override fun readNextEntry(entryBuilder: EntryBuilder, contextIsSequence: Boolean): Entry {
		skipSpacesSafely()
		val entry = if (cursor >= lastIndex - 1) entryBuilder.EndEntry()
		else if (char == ']' || char == '}') {
			stepSafely()
			skipSpacesSafely()
			if (char == ',') step()
			entryBuilder.EndEntry()
		}
		else {
			val name = if (contextIsSequence) null else readName()
			//
			when (char) {
				'[' -> run { step(); entryBuilder.SequenceEntry(name) }
				'{' -> run { step(); entryBuilder.ObjectEntry(name) }
				else -> {
					val entry = when (char) {
						'"' -> entryBuilder.AtomicEntry(nextString(), name)
						in '0'..'9', '-' -> nextNum(entryBuilder, name)
						else -> nextLiteral(entryBuilder, name)
					}
					skipSpaces()
					if (char == ',') step()
					entry
				}
			}
		}
		//
//		println("NXT>> ${if(currentContainerIsObject())"input" else "input"};  $entry;")
		return entry
	}

	private fun readName(): String {
		val name = if (char == '"') nextString()
		else {
			val buff = StringBuilder()
			do {
				buff.append(char)
				step()
			} while (char != ':' && char != ' ')
			buff.toString()
		}
		while (char != ':') step()
		step()
		skipSpaces()
		return name
	}

	private fun nextString(): String {
		step()
		val buff = StringBuilder()
		while (char != '"') {
			if (char == '\\') chars[cursor + 1].let { if (it == '"' || it == '\\') step() }
			buff.append(char)
			step()
		}
		step()
		return buff.toString()
	}

	private fun nextNum(entryBuilder: EntryBuilder, name:String?): Entry {
		var buff = StringBuilder()
		var frac = false
		var valid = true
		val neg = if (char == '-') run { step(); true } else false
		val n = if (neg) 1 else 0
		//
		while (char != ',' && char != ']' && char != '}' && char != ' ') {
			when {
				char >= '0' && char <= '9' -> if (!frac && cursor - n > 17) valid = false// long overflow
				char == '.' -> if (frac) valid = false else frac = true
				else -> valid = false
			}
			buff.append(char)
			step()
		}
		val v = buff.toString()
		return if (!valid) entryBuilder.AtomicEntry(if (neg) "-$v" else v, name)
		else if (v.length == 0 || v == ".") entryBuilder.AtomicEntry(0, name)
		else if (frac) if (neg) entryBuilder.AtomicEntry(-v.toDouble(), name) else entryBuilder.AtomicEntry(v.toDouble(), name)
		else if (v.length < 10) if (neg) entryBuilder.AtomicEntry(-v.toInt(), name) else  entryBuilder.AtomicEntry(v.toInt(), name)
		else if (neg)  entryBuilder.AtomicEntry(-v.toLong(), name) else entryBuilder.AtomicEntry(v.toLong(), name)
	}

	private fun nextLiteral(entryBuilder: EntryBuilder, name:String?): Entry = when (char) {
		'n' -> if (step() == 'u' && step() == 'l' && step() == 'l' && stepIsEnd()) entryBuilder.AtomicNullEntry(name) else fail()
		'f' -> if (step() == 'a' && step() == 'l' && step() == 's' && step() == 'e' && stepIsEnd()) entryBuilder.AtomicEntry(false, name) else fail()
		't' -> if (step() == 'r' && step() == 'u' && step() == 'e' && stepIsEnd())  entryBuilder.AtomicEntry(true, name) else fail()
		else -> {
			val buff = StringBuilder()
			do buff.append(char) while (!stepIsEnd())
			entryBuilder.AtomicEntry(buff.toString(), name)
		}
	}

	private fun stepIsEnd(): Boolean {
		if (cursor < lastIndex) char = chars[++cursor] else fail()
		return char == ',' || char == ']' || char == '}' || char == ' '
	}

	private fun skipSpaces(): Char {
		while (char == ' ') char = chars[++cursor]
		return char
	}

	private fun skipSpacesSafely(): Char {
		while (char == ' ') if (cursor < lastIndex) char = chars[++cursor] else char = '\u0000'
		return char
	}

	private fun stepSafely(): Char {
		if (cursor < lastIndex) char = chars[++cursor] else char = '\u0000'
		return char
	}

	private fun step(): Char {
		char = chars[++cursor]
		//		if (cursor < lastIndex) char = chars[++cursor] else fail()
		return char
	}

	private fun fail(): Nothing {
		val msg = if (cursor >= lastIndex) "Unexpected end of input" else "Parsing failed at position: $cursor; char: '$char'."
		throw Exception(msg)
	}
}


/* WRITER */

class DefaultWriter : Writer<String>() {
	val buff: StringBuilder = StringBuilder()

	override fun result(): String? =buff.toString()

	override fun writeRootEntry(isSequence: Boolean, content: EnclosingEntries) {
		if (isSequence) {
			buff.append('[')
			content.write()
			buff.append(']')
		} else {
			buff.append('{')
			content.write()
			buff.append('}')
		}
	}

	override fun writeObjectEntry(name: String?, index: Int, content: EnclosingEntries) {
		if (index > 0) buff.append(',')
		if (name != null) buff.append('"').append(name).append('"').append(':')
		buff.append('{')
		content.write()
		buff.append('}')
	}

	override fun writeSequenceEntry(name: String?, index: Int, content: EnclosingEntries) {
		if (index > 0) buff.append(',')
		if (name != null) buff.append('"').append(name).append('"').append(':')
		buff.append('[')
		content.write()
		buff.append(']')
	}

	override fun writeAtomicEntry(value: String, name: String?, index: Int) {
		if (index > 0) buff.append(',')
		if (name != null) buff.append('"').append(name).append('"').append(':')
		buff.append('"').append(value).append('"')
	}

	override fun writeAtomicEntry(value: Long, name: String?, index: Int) {
		if (index > 0) buff.append(',')
		if (name != null) buff.append('"').append(name).append('"').append(':')
		buff.append(value)
	}

	override fun writeAtomicEntry(value: Int, name: String?, index: Int) {
		if (index > 0) buff.append(',')
		if (name != null) buff.append('"').append(name).append('"').append(':')
		buff.append(value)
	}

	override fun writeAtomicEntry(value: Double, name: String?, index: Int) {
		if (index > 0) buff.append(',')
		if (name != null) buff.append('"').append(name).append('"').append(':')
		buff.append(value)
	}

	override fun writeAtomicEntry(value: Float, name: String?, index: Int) {
		if (index > 0) buff.append(',')
		if (name != null) buff.append('"').append(name).append('"').append(':')
		buff.append(value)
	}

	override fun writeAtomicEntry(value: Boolean, name: String?, index: Int) {
		if (index > 0) buff.append(',')
		if (name != null) buff.append('"').append(name).append('"').append(':')
		buff.append(value)
	}

	override fun writeAtomicNullEntry(name: String?, index: Int) {
		if (index > 0) buff.append(',')
		if (name != null) buff.append('"').append(name).append('"').append(':')
		buff.append("null")
	}
}

