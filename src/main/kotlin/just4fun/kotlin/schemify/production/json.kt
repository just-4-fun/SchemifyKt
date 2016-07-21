package just4fun.kotlin.schemify.production

import com.fasterxml.jackson.core.JsonFactory as JFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.JsonToken.*
import just4fun.kotlin.schemify.core.*
import java.io.StringWriter




object JsonFactory : ReaderFactory<String>, WriterFactory<String> {
	private val factory = JFactory()
	override fun invoke(input: String): Reader<String> 	= JsonReader(input, factory.createParser(input))
	override fun invoke(): Writer<String> = JsonWriter(factory.createGenerator(StringWriter()))
}


/* READER */

class JsonReader(override val input: String, val parser: JsonParser) : Reader<String>() {
	override fun readRootEntry(entryBuilder: EntryBuilder): Entry = readNextEntry(entryBuilder, true)
	override fun readNextEntry(entryBuilder: EntryBuilder, contextIsSequence: Boolean): Entry {
		val token = parser.nextValue() ?: return entryBuilder.EndEntry()
		val name = parser.currentName
		return when {
			token >= VALUE_EMBEDDED_OBJECT -> when (token) {
				VALUE_STRING -> entryBuilder.AtomicEntry(parser.text, name)
				VALUE_NUMBER_INT -> entryBuilder.AtomicEntry(parser.valueAsLong, name)
				VALUE_NUMBER_FLOAT -> entryBuilder.AtomicEntry(parser.valueAsDouble, name)
				VALUE_NULL -> entryBuilder.AtomicNullEntry(name)
				VALUE_FALSE -> entryBuilder.AtomicEntry(parser.valueAsBoolean, name)
				VALUE_TRUE -> entryBuilder.AtomicEntry(parser.valueAsBoolean, name)
				else -> entryBuilder.AtomicEntry(parser.text, name)
			}
			token == START_ARRAY -> entryBuilder.SequenceEntry(name)
			token == START_OBJECT -> entryBuilder.ObjectEntry(name)
			else -> entryBuilder.EndEntry()
		}
	}
}


/* WRITER */

class JsonWriter(val generator: JsonGenerator) : Writer<String>() {
	override fun result(): String? {
		generator.close()
		return generator.outputTarget.toString()
	}

	override fun writeRootEntry(isSequence: Boolean, content: EnclosingEntries) {
		if (isSequence) {
			generator.writeStartArray()
			content.write()
			generator.writeEndArray()
		} else {
			generator.writeStartObject()
			content.write()
			generator.writeEndObject()
		}
	}

	override fun writeObjectEntry(name: String?, index: Int, content: EnclosingEntries) {
		if (name != null) generator.writeFieldName(name)
		generator.writeStartObject()
		content.write()
		generator.writeEndObject()
	}

	override fun writeSequenceEntry(name: String?, index: Int, content: EnclosingEntries) {
		if (name != null) generator.writeFieldName(name)
		generator.writeStartArray()
		content.write()
		generator.writeEndArray()
	}

	override fun writeAtomicEntry(value: String, name: String?, index: Int) {
		if (name != null) generator.writeFieldName(name)
		generator.writeString(value)
	}

	override fun writeAtomicEntry(value: Long, name: String?, index: Int) {
		if (name != null) generator.writeFieldName(name)
		generator.writeNumber(value)
	}

	override fun writeAtomicEntry(value: Int, name: String?, index: Int) {
		if (name != null) generator.writeFieldName(name)
		generator.writeNumber(value)
	}

	override fun writeAtomicEntry(value: Double, name: String?, index: Int) {
		if (name != null) generator.writeFieldName(name)
		generator.writeNumber(value)
	}

	override fun writeAtomicEntry(value: Float, name: String?, index: Int) {
		if (name != null) generator.writeFieldName(name)
		generator.writeNumber(value)
	}

	override fun writeAtomicEntry(value: Boolean, name: String?, index: Int) {
		if (name != null) generator.writeFieldName(name)
		generator.writeBoolean(value)
	}

	override fun writeAtomicNullEntry(name: String?, index: Int) {
		if (name != null) generator.writeFieldName(name)
		generator.writeNull()
	}

//	override fun writeAtomicEntry(value: ByteArray, name: String?, index: Int) {
//		if (name != null) generator.writeFieldName(name)
//		// todo ???
//		generator.writeStartArray()
//		value.forEachIndexed { ix, b ->  writeAtomicEntry(b, null, ix)}
//		generator.writeEndArray()
//	}
}
