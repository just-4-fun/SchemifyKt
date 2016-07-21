package just4fun.kotlin.schemify.core

import kotlin.reflect.KProperty


/*CONSTRUCTION*/
class Produce : EntryBuilder, Entry, EnclosingEntries {
	companion object {
		operator fun <IN : Any, OUT : Any> invoke(reader: Reader<IN>, writer: Writer<OUT>): OUT? {
			return Produce().processRoot(reader, writer)
		}
	}

	private lateinit var reader: Reader<*>
	private lateinit var writer: Writer<*>
	private var isContainer = false
	private var isSequence = true
	private var entryName: String? = null
	private var redirectReader: Reader<*>? = null
	private var index = -2
	private var called = false
	private var end = false
		set(v: Boolean): Unit {
			field = v
		}
		get(): Boolean {
			val v = field
			if (field) field = false
			return v
		}

	private fun <IN : Any, OUT : Any> processRoot(reader: Reader<IN>, writer: Writer<OUT>): OUT? {
		this.reader = reader
		this.writer = writer
		val rootBuilder = object : EntryBuilder {
			override fun EndEntry(): Entry = this@Produce.apply { end = true }
			override fun ObjectEntry(name: String?): Entry = this@Produce.apply { isSequence = false }
			override fun SequenceEntry(name: String?): Entry = this@Produce.apply { isSequence = true }
		}
		//
		reader.readRootEntry(rootBuilder)
		called = false
		writer.writeRootEntry(isSequence, this)
		// todo specific exception
		if (!called) throw Exception("Method content.write(...) should be called inside Writer.writeRootEntry(..., content).")
		return if (index == -2) null else writer.result()
	}

	private fun processContainer(): Unit {
		val isSeq = isSequence
		var n = 0
		called = true
		do {
			index = n++
			isSequence = isSeq
			reader.readNextEntry(this, isSequence)
			//
			if (isContainer) {
				isContainer = false
				called = false
				val temp = reader
				if (redirectReader != null) reader = redirectReader!!
				if (isSequence) writer.writeSequenceEntry(entryName, index, this)
				else writer.writeObjectEntry(entryName, index, this)
				reader = temp
				// todo specific exception
				if (!called) throw Exception("Method content.write(...) should be called inside Writer.${if (isSequence) "writeSequenceEntry" else "writeObjectEntry"}(..., content).")
			}
		} while (!end)
	}

	/* sub content interface */

	override fun write() = processContainer()
	override fun <T : Any> write(redirectTo: Writer<T>): T? {
		val temp = writer
		writer = redirectTo
		processContainer()
		writer = temp
		return redirectTo.result()
	}

	/* entry builder interface */

	fun context(container: Boolean, sequence: Boolean, name: String?, redirect: Reader<*>?) {
		entryName = if (isSequence) null else name
		redirectReader = redirect
		isContainer = container
		isSequence = sequence
	}

	override fun ObjectEntry(name: String?): Entry = apply { context(true, false, name, null) }
	override fun ObjectEntry(name: String?, redirectTo: Reader<*>): Entry = apply { context(true, false, name, redirectTo) }
	override fun SequenceEntry(name: String?): Entry = apply { context(true, true, name, null) }
	override fun SequenceEntry(name: String?, redirectTo: Reader<*>): Entry = apply { context(true, true, name, redirectTo) }
	override fun AtomicEntry(value: String, name: String?): Entry = apply {
		writer.writeAtomicEntry(value, if (isSequence) null else name, index)
	}

	override fun AtomicEntry(value: ByteArray, name: String?): Entry = apply {
		writer.writeAtomicEntry(value, if (isSequence) null else name, index)
	}

	override fun AtomicEntry(value: Long, name: String?): Entry = apply {
		writer.writeAtomicEntry(value, if (isSequence) null else name, index)
	}

	override fun AtomicEntry(value: Int, name: String?): Entry = apply {
		writer.writeAtomicEntry(value, if (isSequence) null else name, index)
	}

	override fun AtomicEntry(value: Short, name: String?): Entry = apply {
		writer.writeAtomicEntry(value, if (isSequence) null else name, index)
	}

	override fun AtomicEntry(value: Byte, name: String?): Entry = apply {
		writer.writeAtomicEntry(value, if (isSequence) null else name, index)
	}

	override fun AtomicEntry(value: Char, name: String?): Entry = apply {
		writer.writeAtomicEntry(value, if (isSequence) null else name, index)
	}

	override fun AtomicEntry(value: Double, name: String?): Entry = apply {
		writer.writeAtomicEntry(value, if (isSequence) null else name, index)
	}

	override fun AtomicEntry(value: Float, name: String?): Entry = apply {
		writer.writeAtomicEntry(value, if (isSequence) null else name, index)
	}

	override fun AtomicEntry(value: Boolean, name: String?): Entry = apply {
		writer.writeAtomicEntry(value, if (isSequence) null else name, index)
	}

	override fun AtomicNullEntry(name: String?): Entry = apply {
		writer.writeAtomicNullEntry(if (isSequence) null else name, index)
	}

	override fun EndEntry(): Entry = apply { end = true }
}


/* ENTRY */
interface Entry


/* READER */

interface ReaderFactory<T : Any> {
	operator fun invoke(input: T) : Reader<T>
}


abstract class Reader<T : Any> {
	abstract val input: T
	abstract fun readRootEntry(entryBuilder: EntryBuilder): Entry
	abstract fun readNextEntry(entryBuilder: EntryBuilder, contextIsSequence: Boolean): Entry
}


/* WRITER */

interface WriterFactory<T : Any> {
	operator fun invoke() : Writer<T>
}

abstract class Writer<T : Any> {
	abstract fun result(): T? //.TODO should it ? and how can it be ?
	abstract fun writeRootEntry(isSequence: Boolean, content: EnclosingEntries): Unit
	abstract fun writeObjectEntry(name: String?, index: Int, content: EnclosingEntries): Unit
	abstract fun writeSequenceEntry(name: String?, index: Int, content: EnclosingEntries): Unit
	abstract fun writeAtomicEntry(value: String, name: String?, index: Int): Unit
	abstract fun writeAtomicEntry(value: Long, name: String?, index: Int): Unit
	abstract fun writeAtomicEntry(value: Int, name: String?, index: Int): Unit
	abstract fun writeAtomicEntry(value: Double, name: String?, index: Int): Unit
	abstract fun writeAtomicEntry(value: Float, name: String?, index: Int): Unit
	abstract fun writeAtomicEntry(value: Boolean, name: String?, index: Int): Unit
	abstract fun writeAtomicNullEntry(name: String?, index: Int): Unit

	open fun writeAtomicEntry(value: Short, name: String?, index: Int): Unit = writeAtomicEntry(value.toInt(), name, index)
	open fun writeAtomicEntry(value: Byte, name: String?, index: Int): Unit = writeAtomicEntry(value.toInt(), name, index)
	open fun writeAtomicEntry(value: Char, name: String?, index: Int): Unit = writeAtomicEntry(value.toInt(), name, index)
	open fun writeAtomicEntry(value: ByteArray, name: String?, index: Int): Unit = writeSequenceEntry(name, index, writeBytes(value))

	private val writeBytes by lazy(LazyThreadSafetyMode.NONE) {
		object : EnclosingEntries {
			lateinit var value: ByteArray
			operator fun invoke(v: ByteArray) = apply { value = v }
			override fun <T : Any> write(redirectTo: Writer<T>): T? = throw UnsupportedOperationException()
			override fun write() {
				var count = 0
				for (n in value) writeAtomicEntry(n, null, count++)
			}
		}
	}
}


/* CONTENT WRITERS */
interface EnclosingEntries {
	fun <T : Any> write(redirectTo: Writer<T>): T?
	fun write()
}


/* ENTRY BUILDER */
interface EntryBuilder {
	fun EndEntry(): Entry
	fun ObjectEntry(name: String? = null): Entry
	fun ObjectEntry(name: String? = null, redirectTo: Reader<*>): Entry = throw UnsupportedOperationException()
	fun SequenceEntry(name: String? = null): Entry
	fun SequenceEntry(name: String? = null, redirectTo: Reader<*>): Entry = throw UnsupportedOperationException()
	fun AtomicEntry(value: String, name: String? = null): Entry = throw UnsupportedOperationException()
	fun AtomicEntry(value: ByteArray, name: String? = null): Entry = throw UnsupportedOperationException()
	fun AtomicEntry(value: Long, name: String? = null): Entry = throw UnsupportedOperationException()
	fun AtomicEntry(value: Int, name: String? = null): Entry = throw UnsupportedOperationException()
	fun AtomicEntry(value: Short, name: String? = null): Entry = throw UnsupportedOperationException()
	fun AtomicEntry(value: Byte, name: String? = null): Entry = throw UnsupportedOperationException()
	fun AtomicEntry(value: Char, name: String? = null): Entry = throw UnsupportedOperationException()
	fun AtomicEntry(value: Double, name: String? = null): Entry = throw UnsupportedOperationException()
	fun AtomicEntry(value: Float, name: String? = null): Entry = throw UnsupportedOperationException()
	fun AtomicEntry(value: Boolean, name: String? = null): Entry = throw UnsupportedOperationException()
	fun AtomicNullEntry(name: String? = null): Entry = throw UnsupportedOperationException()
}
