package just4fun.kotlin.schemify.production

import just4fun.kotlin.schemify.core.*
import just4fun.kotlin.schemify.core.PropType.*


interface TypeProducer<T:Any> {
	fun <D: Any> read(input: D, factory: ReaderFactory<D>): T?
	fun <D: Any> write(input: T, factory: WriterFactory<D>): D?
}


// todo make them anonymous classes ?

/* SCHEME PRODUCTION */
/* reader */
class SchemeReader<T : Any>(override val input: T, val schema: SCHEMAof<T>, val compact: Boolean? = null) : Reader<T>() {
	private var index = 0
	private var hasAlias = false

	override fun readRootEntry(entryBuilder: EntryBuilder): Entry =
		if (compact ?: schema.compact) entryBuilder.SequenceEntry()
		else entryBuilder.ObjectEntry()

	override fun readNextEntry(entryBuilder: EntryBuilder, contextIsSequence: Boolean): Entry {
		return if (index >= schema.propSize) entryBuilder.EndEntry()
		else {
			val prop = schema.props[index]
			val v = prop.get(input)
			if (hasAlias) {
				hasAlias = false
				index++
				if (v == null) entryBuilder.AtomicNullEntry(prop.alias!!)
				else prop.readEntry(v, prop.alias!!, entryBuilder, compact)
			}
			else {
				if (prop.alias == null || contextIsSequence) index++
				else hasAlias = true
				if (v == null) entryBuilder.AtomicNullEntry(prop.name)
				else prop.readEntry(v, prop.name, entryBuilder, compact)
			}
		}
	}
}

/* writer */
class SchemeWriter<T : Any>(val schema: SCHEMAof<T>) : Writer<T>() {
	val hasConstructor = schema.hasConstructor
	lateinit var instance: T
	lateinit var values: Array<Any?>
	var index = 0

	init {
		if (hasConstructor) values = arrayOfNulls<Any?>(schema.propSize).apply { for (n in 0 until schema.propSize) this[n] = Unit }
		else instance = schema.instance()
	}

	override fun result(): T? {
		if (hasConstructor) instance = schema.instance(values)
		return instance
	}

	private fun getProp(name: String?): Prop<*>? {
		return if (name != null) schema.propMap[name]
		else if (index < schema.propSize) schema.props[index++]
		else null
	}

	override fun writeRootEntry(isSequence: Boolean, content: EnclosingEntries) = content.write()

	override fun writeObjectEntry(name: String?, index: Int, content: EnclosingEntries) {
		getProp(name)?.let { prop ->
			val v = content.write(prop.objectWriter() ?: DummyWriter)// todo check if called
			writeValue(v, prop)
		} ?: content.write(DummyWriter)
	}

	override fun writeSequenceEntry(name: String?, index: Int, content: EnclosingEntries) {
		getProp(name)?.let { prop ->
			val v = content.write(prop.sequenceWriter() ?: DummyWriter)// todo check if called
			writeValue(v, prop)
		} ?: content.write(DummyWriter)
	}

	private fun writeValue(value: Any?, prop: Prop<*>) {
		if (hasConstructor) values[prop.index] = value
		else prop.set(instance, value)
	}

	private fun writeValue(value: Any?, name: String?) {
		getProp(name)?.let { prop ->
			if (hasConstructor) values[prop.index] = value
			else prop.set(instance, value)
		}
	}

	override fun writeAtomicEntry(value: ByteArray, name: String?, index: Int): Unit = writeValue(value, name)
	override fun writeAtomicEntry(value: String, name: String?, index: Int): Unit = writeValue(value, name)
	override fun writeAtomicEntry(value: Long, name: String?, index: Int): Unit = writeValue(value, name)
	override fun writeAtomicEntry(value: Int, name: String?, index: Int): Unit = writeValue(value, name)
	override fun writeAtomicEntry(value: Double, name: String?, index: Int): Unit = writeValue(value, name)
	override fun writeAtomicEntry(value: Float, name: String?, index: Int): Unit = writeValue(value, name)
	override fun writeAtomicEntry(value: Boolean, name: String?, index: Int): Unit = writeValue(value, name)
	override fun writeAtomicNullEntry(name: String?, index: Int): Unit = writeValue(null, name)
}


/* SEQUENCE PRODUCTION */
/* reader */
class SequenceReader<T : Any, E : Any>(override val input: T, val seqType: SEQUENCEof<T, E>) : Reader<T>() {
	private val iterator = seqType.iterator(input)

	override fun readRootEntry(entryBuilder: EntryBuilder): Entry = entryBuilder.SequenceEntry()
	override fun readNextEntry(entryBuilder: EntryBuilder, contextIsSequence: Boolean): Entry {
		return if (!iterator.hasNext()) entryBuilder.EndEntry()
		else {
			val v = iterator.next()
			if (v == null) entryBuilder.AtomicNullEntry()
			else seqType.elementType.readEntry(v, null, entryBuilder, null)
		}
	}
}

/* writer */
class SequenceWriter<T : Any, E : Any>(val seqType: SEQUENCEof<T, E>) : Writer<T>() {
	var instance: T = seqType.instance()
	var size = 0

	override fun result(): T? {
		return seqType.onComplete(instance, size)
	}

	override fun writeRootEntry(isSequence: Boolean, content: EnclosingEntries) = content.write()

	override fun writeObjectEntry(name: String?, index: Int, content: EnclosingEntries) {
		val v = content.write( seqType.elementType.objectWriter() ?: DummyWriter)// todo check if called
		writeValue(v)
	}

	override fun writeSequenceEntry(name: String?, index: Int, content: EnclosingEntries) {
		val v = content.write(seqType.elementType.sequenceWriter() ?: DummyWriter)// todo check if called
		writeValue(v)
	}

	private fun writeValue(value: Any?) {
		val v = seqType.elementType.asInstance(value, false)!!
		instance = seqType.addElement(v, size, instance)
		size++
	}

	override fun writeAtomicEntry(value: ByteArray, name: String?, index: Int): Unit = writeValue(value)
	override fun writeAtomicEntry(value: String, name: String?, index: Int): Unit = writeValue(value)
	override fun writeAtomicEntry(value: Long, name: String?, index: Int): Unit = writeValue(value)
	override fun writeAtomicEntry(value: Int, name: String?, index: Int): Unit = writeValue(value)
	override fun writeAtomicEntry(value: Double, name: String?, index: Int): Unit = writeValue(value)
	override fun writeAtomicEntry(value: Float, name: String?, index: Int): Unit = writeValue(value)
	override fun writeAtomicEntry(value: Boolean, name: String?, index: Int): Unit = writeValue(value)
	override fun writeAtomicNullEntry(name: String?, index: Int): Unit = writeValue(null)
}


/* RAW COLLECTION PRODUCTION */
/* reader */
class RawCollectionReader(override val input: Collection<Any?>) : Reader<Collection<Any?>>() {
	private val iterator = input.iterator()

	override fun readRootEntry(entryBuilder: EntryBuilder): Entry = entryBuilder.SequenceEntry()
	override fun readNextEntry(entryBuilder: EntryBuilder, contextIsSequence: Boolean): Entry {
		return if (!iterator.hasNext()) entryBuilder.EndEntry()
		else {
			val v = iterator.next()
			if (v == null) entryBuilder.AtomicNullEntry()
			else {
				val type = PropType.detectType(v)
				if (type == null) STRING.readEntry(v.toString(), null, entryBuilder, null)
				else type.readEntry(v, null, entryBuilder, null)
			}
		}
	}
}

/* writer */
// todo typeUtils detect type?
class RawCollectionWriter : Writer<Collection<Any?>>() {
	var instance: Collection<Any?> = COLLECTION.instance()

	override fun result(): Collection<Any?> {
		return COLLECTION.onComplete(instance, instance.size)
	}

	override fun writeRootEntry(isSequence: Boolean, content: EnclosingEntries) = content.write()

	override fun writeObjectEntry(name: String?, index: Int, content: EnclosingEntries) {
		val v = content.write(RawMapWriter())// todo check if called
		writeValue(v)
	}

	override fun writeSequenceEntry(name: String?, index: Int, content: EnclosingEntries) {
		val v = content.write(RawCollectionWriter())// todo check if called
		writeValue(v)
	}

	private fun writeValue(value: Any?) {
		instance = COLLECTION.addElement(value, instance.size, instance)
	}

	override fun writeAtomicEntry(value: ByteArray, name: String?, index: Int): Unit = writeValue(value)
	override fun writeAtomicEntry(value: String, name: String?, index: Int): Unit = writeValue(value)
	override fun writeAtomicEntry(value: Long, name: String?, index: Int): Unit = writeValue(value)
	override fun writeAtomicEntry(value: Int, name: String?, index: Int): Unit = writeValue(value)
	override fun writeAtomicEntry(value: Double, name: String?, index: Int): Unit = writeValue(value)
	override fun writeAtomicEntry(value: Float, name: String?, index: Int): Unit = writeValue(value)
	override fun writeAtomicEntry(value: Boolean, name: String?, index: Int): Unit = writeValue(value)
	override fun writeAtomicNullEntry(name: String?, index: Int): Unit = writeValue(null)
}


/* RAW ARRAY PRODUCTION */
/* reader */
class RawArrayReader(override val input: Array<Any?>) : Reader<Array<Any?>>() {
	private val iterator = input.iterator()

	override fun readRootEntry(entryBuilder: EntryBuilder): Entry = entryBuilder.SequenceEntry()
	override fun readNextEntry(entryBuilder: EntryBuilder, contextIsSequence: Boolean): Entry {
		return if (!iterator.hasNext()) entryBuilder.EndEntry()
		else {
			val v = iterator.next()
			if (v == null) entryBuilder.AtomicNullEntry()
			else {
				val type = PropType.detectType(v)
				if (type == null) STRING.readEntry(v.toString(), null, entryBuilder, null)
				else type.readEntry(v, null, entryBuilder, null)
			}
		}
	}
}

/* writer */
// todo typeUtils detect type?
class RawArrayWriter : Writer<Array<Any?>>() {
	var instance = ARRAY.instance()
	var size = 0

	override fun result(): Array<Any?> {
		return ARRAY.onComplete(instance, size)
	}

	override fun writeRootEntry(isSequence: Boolean, content: EnclosingEntries) = content.write()

	override fun writeObjectEntry(name: String?, index: Int, content: EnclosingEntries) {
		val v = content.write(RawMapWriter())// todo check if called
		writeValue(v)
	}

	override fun writeSequenceEntry(name: String?, index: Int, content: EnclosingEntries) {
		val v = content.write(RawCollectionWriter())// todo check if called
		writeValue(v)
	}

	private fun writeValue(value: Any?) {
		instance = ARRAY.addElement(value, size, instance)
		size++
	}

	override fun writeAtomicEntry(value: ByteArray, name: String?, index: Int): Unit = writeValue(value)
	override fun writeAtomicEntry(value: String, name: String?, index: Int): Unit = writeValue(value)
	override fun writeAtomicEntry(value: Long, name: String?, index: Int): Unit = writeValue(value)
	override fun writeAtomicEntry(value: Int, name: String?, index: Int): Unit = writeValue(value)
	override fun writeAtomicEntry(value: Double, name: String?, index: Int): Unit = writeValue(value)
	override fun writeAtomicEntry(value: Float, name: String?, index: Int): Unit = writeValue(value)
	override fun writeAtomicEntry(value: Boolean, name: String?, index: Int): Unit = writeValue(value)
	override fun writeAtomicNullEntry(name: String?, index: Int): Unit = writeValue(null)
}


/* RAW MAP PRODUCTION */
/* reader */
class RawMapReader(override val input: MutableMap<String, Any?>) : Reader<MutableMap<String, Any?>>() {
	private val iterator = input.iterator() as Iterator<Map.Entry<Any?, Any?>>

	override fun readRootEntry(entryBuilder: EntryBuilder): Entry = entryBuilder.ObjectEntry()
	override fun readNextEntry(entryBuilder: EntryBuilder, contextIsSequence: Boolean): Entry {
		return if (!iterator.hasNext()) entryBuilder.EndEntry()
		else {
			val pair = iterator.next()
			val v = pair.value
			if (v == null) entryBuilder.AtomicNullEntry()
			else {
				val type = PropType.detectType(v)
				if (type == null) STRING.readEntry(v.toString(), pair.key.toString(), entryBuilder, null)
				else type.readEntry(v, pair.key.toString(), entryBuilder, null)
			}
		}
	}
}

/*writer*/
// todo typeUtils detect type?
class RawMapWriter : Writer<MutableMap<String, Any?>>() {
	var instance = MAP.instance()

	override fun result() = instance

	override fun writeRootEntry(isSequence: Boolean, content: EnclosingEntries) = content.write()

	override fun writeObjectEntry(name: String?, index: Int, content: EnclosingEntries) {
		val v = content.write(RawMapWriter())// todo check if called
		writeValue(v, name)
	}

	override fun writeSequenceEntry(name: String?, index: Int, content: EnclosingEntries) {
		val v = content.write(RawCollectionWriter())// todo check if called
		writeValue(v, name)
	}

	private fun writeValue(value: Any?, name: String?) {
		if (name != null) instance.put(name, value)
	}

	override fun writeAtomicEntry(value: ByteArray, name: String?, index: Int): Unit = writeValue(value, name)
	override fun writeAtomicEntry(value: String, name: String?, index: Int): Unit = writeValue(value, name)
	override fun writeAtomicEntry(value: Long, name: String?, index: Int): Unit = writeValue(value, name)
	override fun writeAtomicEntry(value: Int, name: String?, index: Int): Unit = writeValue(value, name)
	override fun writeAtomicEntry(value: Double, name: String?, index: Int): Unit = writeValue(value, name)
	override fun writeAtomicEntry(value: Float, name: String?, index: Int): Unit = writeValue(value, name)
	override fun writeAtomicEntry(value: Boolean, name: String?, index: Int): Unit = writeValue(value, name)
	override fun writeAtomicNullEntry(name: String?, index: Int): Unit = writeValue(null, name)
}


/* DUMMY WRITER */
object DummyWriter : Writer<Any>() {
	override fun result(): Any? = null
	override fun writeRootEntry(isSequence: Boolean, content: EnclosingEntries) = content.write()
	override fun writeObjectEntry(name: String?, index: Int, content: EnclosingEntries) = content.write()
	override fun writeSequenceEntry(name: String?, index: Int, content: EnclosingEntries) = content.write()
	override fun writeAtomicEntry(value: String, name: String?, index: Int) = Unit
	override fun writeAtomicEntry(value: Long, name: String?, index: Int) = Unit
	override fun writeAtomicEntry(value: Int, name: String?, index: Int) = Unit
	override fun writeAtomicEntry(value: Double, name: String?, index: Int) = Unit
	override fun writeAtomicEntry(value: Float, name: String?, index: Int) = Unit
	override fun writeAtomicEntry(value: Boolean, name: String?, index: Int) = Unit
	override fun writeAtomicEntry(value: ByteArray, name: String?, index: Int) = Unit
	override fun writeAtomicNullEntry(name: String?, index: Int) = Unit
}


