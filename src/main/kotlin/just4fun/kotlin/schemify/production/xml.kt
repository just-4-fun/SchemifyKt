package just4fun.kotlin.schemify.production

import just4fun.kotlin.schemify.core.*
import  just4fun.kotlin.schemify.production.XmlToken.*


object XmlFactory : ReaderFactory<String>, WriterFactory<String> {
	override fun invoke(input: String): Reader<String> 	= XmlReader(input)
	override fun invoke(): Writer<String> = XmlWriter()
}


/*Reader*/

class XmlReader(override val input: String) : Reader<String>() {
	val chars: CharArray = input.toCharArray()
	val ITEM_NAME = "i"
	var cursor = 0
	val lastIndex = chars.size - 1
	var char: Char = if (lastIndex < 0) '\u0000' else chars[0]
	internal var root: XmlToken? = null
	internal var currentToken: XmlToken? = null

	// todo problem when read input with 1 element by SchemaType. Will try to read as Obj and fail to match name
	init {
		// todo skip whole xml header
		root = nextToken()
		currentToken = root?.next ?: root
		while (currentToken != null) {
			val next = nextToken()
			currentToken!!.next = next
			currentToken = next?.next ?: next
		}
		currentToken = root

		// todo remove
		var token: XmlToken? = root
		while (token != null) {
			val t = token!!
//			prnToken(t, "*")
			token = t.next
		}
	}

	private fun prnToken(t: XmlToken?, msg: String? = null) {
		when (t) {
			is ValueTk -> println("${t.value} > ${if(msg==null) "" else " // $msg"}")
			is StartTk -> print("< ${t.name}${if(msg==null) "" else " // $msg"}${if (t.next is ValueTk) "= " else "\n"}")
			is EndTk -> println("> ${if(msg==null) "" else " // $msg"}")
			null -> println("null ${if(msg==null) "" else " // $msg"}")
		}
	}

	override fun readRootEntry(entryBuilder: EntryBuilder): Entry {
		return readNextEntry(entryBuilder, true)
	}

	override fun readNextEntry(entryBuilder: EntryBuilder, contextIsSequence: Boolean): Entry {
		val curr = currentToken
//		prnToken(curr)
		var next = currentToken?.next
		val e= when {
			curr is StartTk -> {
				val nxt = next!!
				when (nxt) {
					is ValueTk -> {
//						prnToken(nxt, "nxt")
						next = nxt.next
						if (nxt.value == null) entryBuilder.AtomicNullEntry(curr.name)
						else entryBuilder.AtomicEntry(nxt.value, curr.name)
					}
					is EndTk -> {
//						prnToken(nxt, "nxt")
						next = nxt.next
						entryBuilder.SequenceEntry(curr.name)
					}
					is StartTk ->
						if (isObject(nxt)) entryBuilder.ObjectEntry(curr.name)
						else entryBuilder.SequenceEntry(curr.name)
				}
			}
			curr is EndTk || curr == null -> entryBuilder.EndEntry()
			else ->  throw Exception("Unexpected curr: $curr") //todo message ?
		}
		currentToken = next
		return e
	}

	private fun isObject(current: StartTk): Boolean {
		val next = nextSibling(current)
		return when (next) {
			is StartTk -> current.name != next.name
			else -> current.name != ITEM_NAME// todo still could be 1 element sequence with other name
		}
	}

	private fun nextSibling(current: StartTk): XmlToken? {
		var level = 0
		var token: XmlToken = current
		do {
			token = token.next!!
			if (token is StartTk) level++ else level--
		} while (level >= 0)
		return token.next
	}

	private fun nextToken(): XmlToken? {
		while (cursor < lastIndex && char != '<') step()
		return if (cursor >= lastIndex - 1) null
		else {
			step()
			if (char == '/') {
				while (char != '>') step()
				step()
				EndTk()
			} else {
				val name = StringBuilder()
				while (char != '>') run { name.append(char); step() }
				step()
				if (name.last() == '/') {
					// empty tag
					StartTk(name.substring(0, name.length - 1)).apply { next = ValueTk(null) }
				} else {
					StartTk(name.toString()).apply {
						if (char != '<') {
							val value = StringBuilder()
							while (char != '<') run { value.append(char); step() }
							if (chars[cursor + 1] == '/') {
								next = ValueTk(value.toString())
								while (char != '>') step()// skip end tag as value is end itself
								step()
							}
						}
					}
				}
			}
		}
	}

	private fun step(): Char {
		if (cursor < lastIndex) char = chars[++cursor]
		//		if (cursor < lastIndex) char = chars[++cursor] else fail()
		return char
	}

	private fun fail() {
		val msg = if (cursor >= lastIndex) "Unexpected end of input" else "Parsing failed at position: $cursor; char: '$char'."
		throw Exception(msg)
	}
}


internal sealed class XmlToken {
	var next: XmlToken? = null

	class StartTk(val name: String) : XmlToken()

	class ValueTk(val value: String?) : XmlToken()

	class EndTk : XmlToken()
}


/*Deconstructor*/

class XmlWriter : Writer<String>() {
	val ITEM_NAME = "i"
	private val buff: StringBuilder = StringBuilder()

	override fun result(): String = buff.toString()

	override fun writeRootEntry(isSequence: Boolean, content: EnclosingEntries) {
		buff.append('<').append("root").append('>')//.append('\n')// todo no newlines
		content.write()
		buff.append("</").append("root").append('>')//.append('\n')
	}

	override fun writeObjectEntry(name: String?, index: Int, content: EnclosingEntries) {
		writeSequenceEntry(name, index, content)
	}

	override fun writeSequenceEntry(name: String?, index: Int, content: EnclosingEntries) {
		val nm = name ?: ITEM_NAME
		buff.append('<').append(nm).append('>')//.append('\n')
		content.write()
		buff.append("</").append(nm).append('>')//.append('\n')
	}

	override fun writeAtomicEntry(value: String, name: String?, index: Int) = run { wrap(value, name) }
	override fun writeAtomicEntry(value: Long, name: String?, index: Int) = run { wrap(value, name) }
	override fun writeAtomicEntry(value: Int, name: String?, index: Int) = run { wrap(value, name) }
	override fun writeAtomicEntry(value: Double, name: String?, index: Int) = run { wrap(value, name) }
	override fun writeAtomicEntry(value: Float, name: String?, index: Int) = run { wrap(value, name) }
	override fun writeAtomicEntry(value: Boolean, name: String?, index: Int) = run { wrap(value, name) }
	override fun writeAtomicNullEntry(name: String?, index: Int) = run { wrap("null", name) }

	private fun wrap(v: Any?, name: String?) {
		val nm = name ?: ITEM_NAME
		buff.append('<').append(nm).append('>').append(v.toString()).append("</").append(nm).append('>')//.append('\n')
	}
}
