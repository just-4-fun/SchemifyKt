package just4fun.kotlin.schemify.core

import just4fun.kotlin.schemify.production.*
import java.util.*
import kotlin.reflect.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmName
import just4fun.kotlin.schemify.core.PropType.*
import just4fun.kotlin.schemify.hide
import java.lang.reflect.Array as JArray


sealed class PropType<T : Any>(override val typeKClass: KClass<T>) : iPropType<T> {
	// todo uncomment if cache types needed
	//	override final val typeKClass: KClass<T> by lazy(LazyThreadSafetyMode.NONE) { typeKClass() }// because of early access
	//	abstract protected fun typeKClass(): KClass<T>
	override val typeName = typeKClass.defaultType.toString()
	
	
	/* COMPANION*/
	companion object {
		//	var defaulStringFactory: ConstructionFactory<String> = JsonFactory2
		val NumPattern = """[\D&&[^\.,\-]]*(\-?[\D&&[^\.,]]*)(\d*)([\.,]*)(\d*).*""".toRegex()
		// todo if implement type cache > update MapType.writeValue_i to detect more types
		private val scheMap: MutableMap<Class<*>, SCHEMAof<*>> = mutableMapOf()
		internal val propClassName = Prop::class.java.name
		
		fun register(schema: SCHEMAof<*>) = run { scheMap[schema.typeKClass.java] = schema }
		fun unregister(schema: SCHEMAof<*>) = run { scheMap.remove(schema.typeKClass.java) }
		
		@Suppress("UNCHECKED_CAST")
		fun <T : Any> schemaFor(objClass: Class<T>): SCHEMAof<T>? {
			return (scheMap[objClass] ?: run {
				// object class may be subclassed but superclass schema is used
				// todo still actual schema may not be created yet
				scheMap.entries.find { it.key.isAssignableFrom(objClass) }?.let {
					scheMap[objClass] = it.value
					it.value
				}
			}) as SCHEMAof<T>?
		}
		
		inline fun <T : Any> string2number(expr: String, fromString: String.() -> T, fromDouble: Double.() -> T): T = expr.toNumber(fromString, fromDouble)
		
		inline fun <T : Any> String.toNumber(fromString: String.() -> T, fromDouble: Double.() -> T): T {
			return try {
				fromString()
			} catch (e: NumberFormatException) {
				// call cost 30000 ns
				NumPattern.matchEntire(this)?.run {
					var (sig, r, pt, f) = destructured
					var mult = if (sig.endsWith("-")) -1 else 1
					if (r.length == 0) r = "0"
					if (f.length == 0) f = "0"
					if (pt.length > 1) {
						if (r != "0") f = "0" else mult = 1
					}
					val n = "$r.$f".toDouble() * mult
					//		println("string2double: $v VS $sig$r $pt $f  >  $n")
					n.fromDouble()
				} ?: 0.0.fromDouble()
			}
		}
		
		fun <T : Any> evalError(v: Any?, type: iPropType<T>, e: Throwable? = null): T? {
			printError("Cast of $v to ${type.typeKClass} failed${if (e == null) "" else ".  Caused by\n    " + e.toString()}")
			return type.default()
		}
		
		fun printError(msg: String) {
			println(msg)
		}
		
		internal fun pureTypeName(type: KType): String {
			return type.toString().let { val c = it.last(); if (c == '?' || c == '!') it.substring(0, it.length - 1) else it }
		}
		
		fun <T : Any> copy(v: T?, deep: Boolean): T? = if (v == null) null else detectType(v)?.copy(v, deep) ?: v
		
		fun toString(v: Any?, sequenceLimit: Int): String {
			return if (v == null) "null"
			else detectType(v)?.toString(v, sequenceLimit) ?: v.toString()
		}
		
		fun equal(v1: Any?, v2: Any?): Boolean = when {
			v1 == null -> v2 == null
			v2 == null -> false
			else -> {
				val type = detectType(v1)
				when (type) {
					null -> v1 == v2
					else -> type.equal(type.asInstance(v1), type.asInstance(v2))
				}
			}
		}
		
		fun <T : Any> detectType(v: T): PropType<T>? {
			val typ = when (v) {
				is String -> STRING
				is Number -> when (v) {
					is Int -> INT
					is Long -> LONG
					is Double -> DOUBLE
					is Float -> FLOAT
					is Short -> SHORT
					is Byte -> BYTE
					else -> null //as PropType<T>?// impossible
				}
				is SchemaObject<*> -> PropType.schemaFor(v.javaClass)
				is Map<*, *> -> MAP
				is Collection<*> -> COLLECTION
				is Cloneable -> when (v) {
					is Array<*> -> ARRAY
					is ByteArray -> BYTES
					is IntArray -> INTS
					is LongArray -> LONGS
					is DoubleArray -> DOUBLES
					is FloatArray -> FLOATS
					is BooleanArray -> BOOLEANS
					is ShortArray -> SHORTS
					is CharArray -> CHARS
					else -> null //as PropType<T>?
				}
				is Boolean -> BOOLEAN
				is Char -> CHAR
				else -> null //as PropType<T>?
			// todo more ???
			}
			if (typ == null) printError("Can not detect type of ${v.javaClass}")
			@Suppress("UNCHECKED_CAST")
			return typ as PropType<T>?
		}
	}
	
	
	/* TYPES */
	
	object LONG : PropType<Long>(Long::class) {
		override fun instance(): Long = 0L
		override fun default(): Long = 0L
		override fun isInstance(v: Any): Boolean = v is Long
		override fun asInstance(v: Any): Long? = when (v) {
			is Long -> v
			is Number -> v.toLong()
			is String -> v.toNumber(String::toLong, Double::toLong)
			is Boolean -> if (v) 1L else 0L
			is Char -> v.toLong()
			else -> PropType.evalError(v, this)
		}
		
		override fun readEntry(v: Long, name: String?, entryBuilder: EntryBuilder, compact: Boolean?): Entry = entryBuilder.AtomicEntry(v, name)
	}
	
	object INT : PropType<Int>(Int::class) {
		override fun instance(): Int = 0
		override fun default(): Int = 0
		override fun isInstance(v: Any): Boolean = v is Int
		override fun asInstance(v: Any): Int? = when (v) {
			is Int -> v
			is Number -> v.toInt()
			is String -> v.toNumber(String::toInt, Double::toInt)
			is Boolean -> if (v) 1 else 0
			is Char -> v.toInt()
			else -> PropType.evalError(v, this)
		}
		
		override fun readEntry(v: Int, name: String?, entryBuilder: EntryBuilder, compact: Boolean?): Entry = entryBuilder.AtomicEntry(v, name)
	}
	
	object SHORT : PropType<Short>(Short::class) {
		override fun instance(): Short = 0
		override fun default(): Short = 0
		override fun isInstance(v: Any): Boolean = v is Short
		override fun asInstance(v: Any): Short? = when (v) {
			is Short -> v
			is Number -> v.toShort()
			is String -> v.toNumber(String::toShort, Double::toShort)
			is Boolean -> if (v) 1 else 0
			is Char -> v.toShort()
			else -> PropType.evalError(v, this)
		}
		
		override fun readEntry(v: Short, name: String?, entryBuilder: EntryBuilder, compact: Boolean?): Entry = entryBuilder.AtomicEntry(v, name)
	}
	
	object BYTE : PropType<Byte>(Byte::class) {
		override fun instance(): Byte = 0
		override fun default(): Byte = 0
		override fun isInstance(v: Any): Boolean = v is Byte
		override fun asInstance(v: Any): Byte? = when (v) {
			is Byte -> v
			is Number -> v.toByte()
			is String -> v.toNumber(String::toByte, Double::toByte)
			is Boolean -> if (v) 1 else 0
			is Char -> v.toByte()
			else -> PropType.evalError(v, this)
		}
		
		override fun readEntry(v: Byte, name: String?, entryBuilder: EntryBuilder, compact: Boolean?): Entry = entryBuilder.AtomicEntry(v, name)
	}
	
	object DOUBLE : PropType<Double>(Double::class) {
		override fun instance(): Double = 0.0
		override fun default(): Double = 0.0
		override fun isInstance(v: Any): Boolean = v is Double
		override fun asInstance(v: Any): Double? = when (v) {
			is Double -> v
			is Number -> v.toDouble()
			is String -> v.toNumber(String::toDouble, Double::toDouble)
			is Boolean -> if (v) 1.0 else 0.0
			is Char -> v.toDouble()
			else -> PropType.evalError(v, this)
		}
		
		override fun readEntry(v: Double, name: String?, entryBuilder: EntryBuilder, compact: Boolean?): Entry = entryBuilder.AtomicEntry(v, name)
	}
	
	object FLOAT : PropType<Float>(Float::class) {
		override fun instance(): Float = 0f
		override fun default(): Float = 0f
		override fun isInstance(v: Any): Boolean = v is Float
		override fun asInstance(v: Any): Float? = when (v) {
			is Float -> v
			is Number -> v.toFloat()
			is String -> v.toNumber(String::toFloat, Double::toFloat)
			is Boolean -> if (v) 1.0f else 0.0f
			is Char -> v.toFloat()
			else -> PropType.evalError(v, this)
		}
		
		override fun readEntry(v: Float, name: String?, entryBuilder: EntryBuilder, compact: Boolean?): Entry = entryBuilder.AtomicEntry(v, name)
	}
	
	object CHAR : PropType<Char>(Char::class) {
		override fun instance(): Char = '\u0000'
		override fun default(): Char = '\u0000'
		override fun isInstance(v: Any): Boolean = v is Char
		override fun asInstance(v: Any): Char? = when (v) {
			is Char -> v
			is Int -> v.toChar()// glitch : is Number fails
			is String -> if (v.isEmpty()) '\u0000' else try {
				Integer.parseInt(v).toChar()
			} catch(e: Throwable) {
				v[0]
			}
			is Long -> v.toChar()
			is Double -> v.toChar()
			is Boolean -> if (v) '1' else '0'
			is Float -> v.toChar()
			is Short -> v.toChar()
			is Byte -> v.toChar()
			else -> PropType.evalError(v, this)
		}
		
		override fun readEntry(v: Char, name: String?, entryBuilder: EntryBuilder, compact: Boolean?): Entry = entryBuilder.AtomicEntry(v, name)
	}
	
	object BOOLEAN : PropType<Boolean>(Boolean::class) {
		var falseLike = listOf("", "0", "null", "0.0", "0,0")
		override fun instance(): Boolean = false
		override fun default(): Boolean = false
		override fun isInstance(v: Any): Boolean = v is Boolean
		override fun asInstance(v: Any): Boolean? = when (v) {
			is Boolean -> v
			is Number -> v.toInt() != 0
			"false" -> false
			"true" -> true
			is String -> v !in falseLike
			is Char -> v != '0'
			else -> PropType.evalError(v, this)
		}
		
		override fun readEntry(v: Boolean, name: String?, entryBuilder: EntryBuilder, compact: Boolean?): Entry = entryBuilder.AtomicEntry(v, name)
	}
	
	object STRING : PropType<String>(String::class) {
		val asciiEncoded = false// todo remove
		override fun instance(): String = ""
		override fun isInstance(v: Any): Boolean = v is String
		override fun asInstance(v: Any): String? = when (v) {
			is String -> v
			is Number -> v.toString()
			is Boolean -> v.toString()
			is Char -> v.toString()
			else -> PropType.evalError(v, this)
		}
		
		override fun readEntry(v: String, name: String?, entryBuilder: EntryBuilder, compact: Boolean?): Entry = entryBuilder.AtomicEntry(v, name)
	}
	
	//todo reasoning for that Unit values
	object STUB : PropType<Unit>(Unit::class) {
		override fun instance(): Unit = Unit
		override fun default(): Unit? = Unit
		override fun asInstance(v: Any): Unit? = Unit
		override fun copy(v: Unit?, deep: Boolean): Unit? = Unit
		override fun equal(v1: Unit?, v2: Unit?): Boolean = true
		override fun isInstance(v: Any): Boolean = v == Unit
		override fun readEntry(v: Unit, name: String?, entryBuilder: EntryBuilder, compact: Boolean?): Entry = entryBuilder.AtomicNullEntry(name)
	}
	
	
	/* VALUE TYPES */
	
	private interface ValueType<V : Any, T : Any> : iPropType<T> {
		val valueType: PropType<V>
		fun fromValue(v: V): T?
		fun toValue(v: T?): V?
		override fun asInstance(v: Any): T? = if (isInstance(v)) v as T else fromValue(valueType.asInstance(v, false)!!)
		override fun copy(v: T?, deep: Boolean): T? = if (!deep || v == null) v else valueType.copy(toValue(v), true)?.let { fromValue(it) }
		override fun equal(v1: T?, v2: T?): Boolean = v1 === v2 || (v1 != null && v2 != null && valueType.equal(toValue(v1), toValue(v2)))
		override fun hash(v: T): Int = toValue(v)?.let { valueType.hash(it) } ?: 0
		override fun readEntry(v: T, name: String?, entryBuilder: EntryBuilder, compact: Boolean?): Entry {
			return toValue(v)?.let { valueType.readEntry(it, name, entryBuilder, compact) } ?: entryBuilder.AtomicNullEntry(name)
		}
	}
	
	abstract class STRINGof<T : Any>(typeKClass: KClass<T>) : PropType<T>(typeKClass), ValueType<String, T> {
		override val valueType: PropType<String> = STRING
	}
	
	abstract class BYTESof<T : Any>(typeKClass: KClass<T>) : PropType<T>(typeKClass), ValueType<ByteArray, T> {
		override val valueType: PropType<ByteArray> = BYTES
	}
	
	abstract class LONGof<T : Any>(typeKClass: KClass<T>) : PropType<T>(typeKClass), ValueType<Long, T> {
		override val valueType: PropType<Long> = LONG
	}
	
	
	/* SEQUENCE TYPE */
	
	interface SEQUENCEof<T : Any, E : Any> : iPropType<T>, TypeProducer<T> {
		val elementType: PropType<E>
		
		override fun <D : Any> read(input: D, factory: ReaderFactory<D>): T? = Produce(factory(input), SequenceWriter(this))
		override fun <D : Any> write(input: T, factory: WriterFactory<D>): D? = Produce(SequenceReader(input, this), factory())
		
		fun iterator(seq: T): Iterator<E>
		fun addElement(e: E, index: Int, seq: T): T
		fun onComplete(seq: T, expectedSize: Int): T = seq
		fun buffSize() = 100
		override fun sequenceWriter(): Writer<T>? = SequenceWriter(this)
		override fun readEntry(v: T, name: String?, entryBuilder: EntryBuilder, compact: Boolean?): Entry {
			return entryBuilder.SequenceEntry(name, SequenceReader(v, this))
		}
	}
	
	/* COLLECTION TYPE */
	abstract class COLLECTIONof<T : Collection<E>, E : Any>(override val elementType: PropType<E>, typeKClass: KClass<T>) : PropType<T>(typeKClass), SEQUENCEof<T, E> {
		override val typeName by lazy(LazyThreadSafetyMode.NONE) { "${typeKClass.defaultType.toString().substringBefore('<')}<${elementType.typeName}>" }
		override fun iterator(seq: T): Iterator<E> = seq.iterator()
		@Suppress("UNCHECKED_CAST")
		override fun asInstance(v: Any): T? {
			var index = -1
			return when {
				isInstance(v) -> {
					// still can miss if non-first element is null
					if ((v as Collection<*>).isNotEmpty() && v.first().let { e -> e == null || !elementType.isInstance(e) }) {
						var coll = instance()
						for (e in v) coll = addElement(elementType.asInstance(e, false)!!, index++, coll)
						onComplete(coll, coll.size)
					} else v as T
				}
				v is Collection<*> -> {
					var coll = instance()
					for (e in v) coll = addElement(elementType.asInstance(e, false)!!, index++, coll)
					onComplete(coll, coll.size)
				}
				v is Array<*> -> {
					var coll = instance()
					for (e in v) coll = addElement(elementType.asInstance(e, false)!!, index++, coll)
					onComplete(coll, coll.size)
				}
			// case: xml node wrongly detected as Object due to limited info
				v is Map<*, *> && v.size <= 1 -> {
					var coll = instance()
					for (e in v) coll = addElement(elementType.asInstance(e.value, false)!!, index++, coll)
					onComplete(coll, coll.size)
				}
				else -> PropType.evalError(v, this, Exception("${v.javaClass.kotlin.javaObjectType} is not ${typeKClass.javaObjectType}"))
			}
		}
		
		@Suppress("UNCHECKED_CAST")
		override fun copy(v: T?, deep: Boolean): T? = if (!deep || v == null) v else (v.map { elementType.copy(it, true) } as T)
		
		override fun equal(v1: T?, v2: T?): Boolean = v1 === v2 || (v1 != null && v2 != null && v1.size == v2.size && run {
			val itr1 = v1.iterator()
			val itr2 = v2.iterator()
			while (itr1.hasNext()) if (!elementType.equal(itr1.next(), itr2.next())) return false
			true
		})
		
		override fun hash(v: T): Int {
			var code = 1
			v.forEach { code = code * 31 + elementType.hash(it) }
			return code
		}
		
		override fun toString(v: T?, sequenceLimit: Int): String = if (v == null) "null" else v.map { elementType.toString(it, sequenceLimit) }.joinToString(", ", "[", "]", sequenceLimit)
	}
	
	
	/* ARRAY TYPE */
	open class ARRAYof<E : Any>(override val elementType: PropType<E>) : PropType<Array<E>>(JArray.newInstance(elementType.typeKClass.javaObjectType, 0).javaClass.kotlin as KClass<Array<E>>), SEQUENCEof<Array<E>, E> {
		override fun iterator(seq: Array<E>): Iterator<E> = seq.iterator()
		//	override val typeKClass = Array<Any>::class as KClass<Array<E>>// WARN cant create prop of Array<Array<E>>
		// due to glitch: https://youtrack.jetbrains.com/issue/KT-11754
		override val typeName = "kotlin.Array<${elementType.typeName}>"
		
		override fun instance(): Array<E> = instance(buffSize())
		@Suppress("UNCHECKED_CAST")
		fun instance(size: Int): Array<E> = JArray.newInstance(elementType.typeKClass.javaObjectType, size) as Array<E>
		
		@Suppress("CAST_NEVER_SUCCEEDS")
		override fun asInstance(v: Any): Array<E>? = when (v) {
			is Array<*> ->
				if (v.javaClass.componentType.let { it == elementType.typeKClass.java }) v as Array<E>
				else instance(v.size).apply { v.forEachIndexed { i, item -> this[i] = elementType.asInstance(item, false)!! } }
			is Collection<*> -> instance(v.size).apply { v.forEachIndexed { i, item -> this[i] = elementType.asInstance(item, false)!! } }
		// todo from spec arrays ?
		// case: xml node wrongly detected as Object due to limited info
			is Map<*, *> -> instance(v.size).apply { for (e in v) this[0] = elementType.asInstance(e.value, false)!! }
			else -> PropType.evalError(v, this, Exception("${v.javaClass.kotlin.javaObjectType} is not ${typeKClass.javaObjectType}"))
		}
		
		override fun copy(v: Array<E>?, deep: Boolean): Array<E>? = if (!deep || v == null) v else {
			val seq = instance(v.size)
			v.forEachIndexed { ix, e -> seq[ix] = elementType.copy(e, true)!! }
			seq
		}
		
		override fun equal(v1: Array<E>?, v2: Array<E>?): Boolean = v1 === v2 || (v1 != null && v2 != null && v1.size == v2.size && run {
			for (n in 0 until v1.size) if (!elementType.equal(v1[n], v2[n])) return false
			true
		})
		
		override fun hash(v: Array<E>): Int {
			var code = 1
			for (e in v) {
				code = code * 31 + elementType.hash(e)
			}
			return code
		}
		
		override fun toString(v: Array<E>?, sequenceLimit: Int): String = if (v == null) "null" else v.map { elementType.toString(it, sequenceLimit) }.joinToString(", ", "[", "]", sequenceLimit)
		
		override fun addElement(e: E, index: Int, seq: Array<E>): Array<E> {
			val newSeq = if (index == seq.size) Arrays.copyOf(seq, seq.size + buffSize()) else seq
			newSeq[index] = e
			return newSeq
		}
		
		override fun onComplete(seq: Array<E>, expectedSize: Int): Array<E> {
			return if (expectedSize < seq.size) Arrays.copyOf(seq, expectedSize) else seq
		}
	}
	
	
	/* SPECIALIZED ARRAYS */
	private interface ArrayType<T : Any, E : Any> : SEQUENCEof<T, E> {
		fun instance(size: Int): T
		fun set(v: T, index: Int, e: E): Unit
		fun get(v: T, index: Int): E
		fun size(v: T): Int
		fun copy(v: T, expectSize: Int): T
		
		override fun instance(): T = instance(buffSize())
		@hide fun elementLike(v: Any?): Boolean = v is Number || v is String || v is Boolean || v is Char
		
		fun inlineCopy(v: Collection<*>): T? {
			return if (v.isEmpty() || elementLike(v.first()))
				instance(v.size).apply { v.forEachIndexed { i, item -> set(this, i, elementType.asInstance(item, false)!!) } }
			else run { PropType.evalError(v, this); null }
		}
		
		fun inlineCopy(v: Array<*>): T? {
			return if (v.isEmpty() || elementLike(v.first()))
				instance(v.size).apply { v.forEachIndexed { i, item -> set(this, i, elementType.asInstance(item, false)!!) } }
			else run { PropType.evalError(v, this); null }
		}
		
		override fun asInstance(v: Any): T? = when {
			isInstance(v) -> v as T
			v is Collection<*> -> inlineCopy(v)
			v is Array<*> -> inlineCopy(v)
		// todo from spec arrays ?
		// case: xml node wrongly detected as Object due to limited info
			v is Map<*, *> -> instance(v.size).apply { for (e in v) set(this, 0, elementType.asInstance(e.value, false)!!) }
			else -> PropType.evalError(v, this)
		}
		
		override fun addElement(e: E, index: Int, seq: T): T {
			val size = size(seq)
			val newSeq = if (index == size) copy(seq, size + buffSize()) else seq
			set(newSeq, index, e)
			return newSeq
		}
		
		override fun onComplete(seq: T, expectedSize: Int): T = if (expectedSize < size(seq)) copy(seq, expectedSize) else seq
		
		override fun copy(v: T?, deep: Boolean): T? = if (!deep || v == null) v else copy(v, size(v))
		
		override fun toString(v: T?, sequenceLimit: Int): String = if (v == null) "null" else {
			val buff = StringBuilder("[")
			var first = true
			iterator(v).forEach {
				if (first) first = false else buff.append(",")
				buff.append(it)
			}
			buff.append("]")
			buff.toString()
		}
	}
	
	
	/*BYTE*/
	object BYTES : PropType<ByteArray>(ByteArray::class), ArrayType<ByteArray, Byte> {
		override val typeName = typeKClass.defaultType.toString()
		override val elementType = BYTE
		override fun iterator(seq: ByteArray): Iterator<Byte> = seq.iterator()
		override fun instance(size: Int): ByteArray = ByteArray(size)
		override fun isInstance(v: Any): Boolean = v is ByteArray
		override fun set(v: ByteArray, index: Int, e: Byte) = run { v[index] = e }
		override fun get(v: ByteArray, index: Int): Byte = v[index]
		override fun size(v: ByteArray): Int = v.size
		override fun copy(v: ByteArray, expectSize: Int): ByteArray = Arrays.copyOf(v, expectSize)
		override fun equal(v1: ByteArray?, v2: ByteArray?): Boolean = Arrays.equals(v1, v2)
		override fun hash(v: ByteArray): Int = Arrays.hashCode(v)
		override fun readEntry(v: ByteArray, name: String?, entryBuilder: EntryBuilder, compact: Boolean?): Entry = entryBuilder.AtomicEntry(v, name)
	}
	
	
	/*LONG*/
	object LONGS : PropType<LongArray>(LongArray::class), ArrayType<LongArray, Long> {
		override val typeName = typeKClass.defaultType.toString()
		override val elementType = LONG
		override fun iterator(seq: LongArray): Iterator<Long> = seq.iterator()
		override fun instance(size: Int): LongArray = LongArray(size)
		override fun isInstance(v: Any): Boolean = v is LongArray
		override fun set(v: LongArray, index: Int, e: Long) = run { v[index] = e }
		override fun get(v: LongArray, index: Int): Long = v[index]
		override fun size(v: LongArray): Int = v.size
		override fun copy(v: LongArray, expectSize: Int): LongArray = Arrays.copyOf(v, expectSize)
		override fun equal(v1: LongArray?, v2: LongArray?): Boolean = Arrays.equals(v1, v2)
		override fun hash(v: LongArray): Int = Arrays.hashCode(v)
	}
	
	
	/*INT*/
	object INTS : PropType<IntArray>(IntArray::class), ArrayType<IntArray, Int> {
		override val typeName = typeKClass.defaultType.toString()
		override val elementType = INT
		override fun iterator(seq: IntArray): Iterator<Int> = seq.iterator()
		override fun instance(size: Int): IntArray = IntArray(size)
		override fun isInstance(v: Any): Boolean = v is IntArray
		override fun set(v: IntArray, index: Int, e: Int) = run { v[index] = e }
		override fun get(v: IntArray, index: Int): Int = v[index]
		override fun size(v: IntArray): Int = v.size
		override fun copy(v: IntArray, expectSize: Int): IntArray = Arrays.copyOf(v, expectSize)
		override fun equal(v1: IntArray?, v2: IntArray?): Boolean = Arrays.equals(v1, v2)
		override fun hash(v: IntArray): Int = Arrays.hashCode(v)
	}
	
	
	/*SHORT*/
	object SHORTS : PropType<ShortArray>(ShortArray::class), ArrayType<ShortArray, Short> {
		override val typeName = typeKClass.defaultType.toString()
		override val elementType = SHORT
		override fun iterator(seq: ShortArray): Iterator<Short> = seq.iterator()
		override fun instance(size: Int): ShortArray = ShortArray(size)
		override fun isInstance(v: Any): Boolean = v is ShortArray
		override fun set(v: ShortArray, index: Int, e: Short) = run { v[index] = e }
		override fun get(v: ShortArray, index: Int): Short = v[index]
		override fun size(v: ShortArray): Int = v.size
		override fun copy(v: ShortArray, expectSize: Int): ShortArray = Arrays.copyOf(v, expectSize)
		override fun equal(v1: ShortArray?, v2: ShortArray?): Boolean = Arrays.equals(v1, v2)
		override fun hash(v: ShortArray): Int = Arrays.hashCode(v)
	}
	
	
	/*CHAR*/
	object CHARS : PropType<CharArray>(CharArray::class), ArrayType<CharArray, Char> {
		override val typeName = typeKClass.defaultType.toString()
		override val elementType = CHAR
		override fun iterator(seq: CharArray): Iterator<Char> = seq.iterator()
		override fun instance(size: Int): CharArray = CharArray(size)
		override fun isInstance(v: Any): Boolean = v is CharArray
		override fun set(v: CharArray, index: Int, e: Char) = run { v[index] = e }
		override fun get(v: CharArray, index: Int): Char = v[index]
		override fun size(v: CharArray): Int = v.size
		override fun copy(v: CharArray, expectSize: Int): CharArray = Arrays.copyOf(v, expectSize)
		override fun equal(v1: CharArray?, v2: CharArray?): Boolean = Arrays.equals(v1, v2)
		override fun hash(v: CharArray): Int = Arrays.hashCode(v)
	}
	
	
	/*DOUBLE*/
	object DOUBLES : PropType<DoubleArray>(DoubleArray::class), ArrayType<DoubleArray, Double> {
		override val typeName = typeKClass.defaultType.toString()
		override val elementType = DOUBLE
		override fun iterator(seq: DoubleArray): Iterator<Double> = seq.iterator()
		override fun instance(size: Int): DoubleArray = DoubleArray(size)
		override fun isInstance(v: Any): Boolean = v is DoubleArray
		override fun set(v: DoubleArray, index: Int, e: Double) = run { v[index] = e }
		override fun get(v: DoubleArray, index: Int): Double = v[index]
		override fun size(v: DoubleArray): Int = v.size
		override fun copy(v: DoubleArray, expectSize: Int): DoubleArray = Arrays.copyOf(v, expectSize)
		override fun equal(v1: DoubleArray?, v2: DoubleArray?): Boolean = Arrays.equals(v1, v2)
		override fun hash(v: DoubleArray): Int = Arrays.hashCode(v)
	}
	
	
	/*FLOAT*/
	object FLOATS : PropType<FloatArray>(FloatArray::class), ArrayType<FloatArray, Float> {
		override val typeName = typeKClass.defaultType.toString()
		override val elementType = FLOAT
		override fun iterator(seq: FloatArray): Iterator<Float> = seq.iterator()
		override fun instance(size: Int): FloatArray = FloatArray(size)
		override fun isInstance(v: Any): Boolean = v is FloatArray
		override fun set(v: FloatArray, index: Int, e: Float) = run { v[index] = e }
		override fun get(v: FloatArray, index: Int): Float = v[index]
		override fun size(v: FloatArray): Int = v.size
		override fun copy(v: FloatArray, expectSize: Int): FloatArray = Arrays.copyOf(v, expectSize)
		override fun equal(v1: FloatArray?, v2: FloatArray?): Boolean = Arrays.equals(v1, v2)
		override fun hash(v: FloatArray): Int = Arrays.hashCode(v)
	}
	
	/*BOOLEAN*/
	object BOOLEANS : PropType<BooleanArray>(BooleanArray::class), ArrayType<BooleanArray, Boolean> {
		override val typeName = typeKClass.defaultType.toString()
		override val elementType = BOOLEAN
		override fun iterator(seq: BooleanArray): Iterator<Boolean> = seq.iterator()
		override fun instance(size: Int): BooleanArray = BooleanArray(size)
		override fun isInstance(v: Any): Boolean = v is BooleanArray
		override fun set(v: BooleanArray, index: Int, e: Boolean) = run { v[index] = e }
		override fun get(v: BooleanArray, index: Int): Boolean = v[index]
		override fun size(v: BooleanArray): Int = v.size
		override fun copy(v: BooleanArray, expectSize: Int): BooleanArray = Arrays.copyOf(v, expectSize)
		override fun equal(v1: BooleanArray?, v2: BooleanArray?): Boolean = Arrays.equals(v1, v2)
		override fun hash(v: BooleanArray): Int = Arrays.hashCode(v)
	}
	
	
	/* RAW COLLECTION */
	object COLLECTION : PropType<Collection<Any?>>(Collection::class), TypeProducer<Collection<Any?>> {
		override val typeName = "kotlin.collections.Collection<kotlin.Any?>"
		
		override fun <D : Any> read(input: D, factory: ReaderFactory<D>): Collection<Any?>? = Produce(factory(input), RawCollectionWriter())
		override fun <D : Any> write(input: Collection<Any?>, factory: WriterFactory<D>): D? = Produce(RawCollectionReader(input), factory())
		
		override fun instance(): MutableList<Any?> = mutableListOf()
		
		override fun asInstance(v: Any): Collection<*>? = when (v) {
			is Collection<*> -> v
			is Array<*> -> instance().apply { for (e in v) add(e) }
		// case: xml node wrongly detected as Object due to limited info
			is Map<*, *> -> {
				var coll = instance()
				for (e in v) coll.add(0, e.value)
				coll
			}
			else -> PropType.evalError(v, this)
		}
		
		fun addElement(e: Any?, index: Int, seq: Collection<Any?>): Collection<Any?> {
			return seq + e
		}
		
		fun onComplete(seq: Collection<Any?>, expectedSize: Int): Collection<Any?> = seq
		
		override fun copy(v: Collection<*>?, deep: Boolean): Collection<*>? {
			return if (v == null || !deep) v
			else instance().apply { for (e in v) add(PropType.copy(e, deep)) }
		}
		
		override fun equal(v1: Collection<*>?, v2: Collection<*>?): Boolean = v1 === v2 || (v1 != null && v2 != null && v1.size == v2.size && run {
			val itr1 = v1.iterator()
			val itr2 = v2.iterator()
			while (itr1.hasNext()) if (!PropType.equal(itr1.next(), itr2.next())) return false
//			while (itr1.hasNext()) {
//				val v1 = itr1.next()
//				val v2 = itr2.next()
//				if (!PropType.equal(v1,v2 )) {
//					println("NE  V1:${PropType.detectType(v1!!)!!.typeName}= ${v1};  V2:${PropType.detectType(v2!!)!!.typeName}= ${v2}")
//					return false
//				}
//			}
			true
		})
		
		override fun toString(v: Collection<*>?, sequenceLimit: Int): String {
			return if (v == null) "null"
			else v.map { e -> PropType.toString(e, sequenceLimit) }.joinToString(",", "[", "]", sequenceLimit)
		}
		
		override fun sequenceWriter() = RawCollectionWriter()
		override fun readEntry(v: Collection<Any?>, name: String?, entryBuilder: EntryBuilder, compact: Boolean?): Entry {
			return entryBuilder.SequenceEntry(name, RawCollectionReader(v))
		}
	}
	
	
	/* RAW ARRAY */
	object ARRAY : PropType<Array<Any?>>(JArray.newInstance(Any::class.java, 0).javaClass.kotlin as KClass<Array<Any?>>), TypeProducer<Array<Any?>> {
		var buffSize = 100
		override val typeName = "kotlin.Array<kotlin.Any?>"
		
		override fun <D : Any> read(input: D, factory: ReaderFactory<D>): Array<Any?>? = Produce(factory(input), RawArrayWriter())
		override fun <D : Any> write(input: Array<Any?>, factory: WriterFactory<D>): D? = Produce(RawArrayReader(input), factory())
		
		override fun instance(): Array<Any?> = instance(buffSize)
		@Suppress("UNCHECKED_CAST")
		fun instance(size: Int): Array<Any?> = JArray.newInstance(Any::class.java, size) as Array<Any?>
		
		fun addElement(e: Any?, index: Int, seq: Array<Any?>): Array<Any?> {
			val newSeq = if (index == seq.size) Arrays.copyOf(seq, seq.size + buffSize) else seq
			newSeq[index] = e
			return newSeq
		}
		
		fun onComplete(seq: Array<Any?>, expectedSize: Int): Array<Any?> {
			return if (expectedSize < seq.size) Arrays.copyOf(seq, expectedSize) else seq
		}
		
		override fun asInstance(v: Any): Array<Any?>? = when (v) {
			is Array<*> -> v as Array<Any?>
			is Collection<*> -> instance(v.size).apply { v.forEachIndexed { i, item -> this[i] = item } }
		// case: xml node wrongly detected as Object due to limited info
			is Map<*, *> -> {
				var coll = instance()
				for (e in v) coll[0] = e.value
				coll
			}
			else -> PropType.evalError(v, this)
		}
		
		override fun copy(v: Array<Any?>?, deep: Boolean): Array<Any?>? {
			return if (v == null || !deep) v
			else instance(v.size).apply { v.forEachIndexed { ix, e -> this[ix] = PropType.copy(e, deep) } }
		}
		
		override fun equal(v1: Array<Any?>?, v2: Array<Any?>?): Boolean = v1 === v2 || (v1 != null && v2 != null && v1.size == v2.size && run {
			val itr1 = v1.iterator()
			val itr2 = v2.iterator()
			while (itr1.hasNext()) if (!PropType.equal(itr1.next(), itr2.next())) return false
			true
		})
		
		override fun toString(v: Array<Any?>?, sequenceLimit: Int): String {
			return if (v == null) "null"
			else v.map { e -> PropType.toString(e, sequenceLimit) }.joinToString(",", "[", "]", sequenceLimit)
		}
		
		override fun sequenceWriter() = RawArrayWriter()
		override fun readEntry(v: Array<Any?>, name: String?, entryBuilder: EntryBuilder, compact: Boolean?): Entry {
			return entryBuilder.SequenceEntry(name, RawArrayReader(v))
		}
	}
	
	
	/* RAW MAP */
	object MAP : PropType<MutableMap<String, Any?>>(MutableMap::class as KClass<MutableMap<String, Any?>>), TypeProducer<MutableMap<String, Any?>> {
		override val typeName = "kotlin.collections.MutableMap<kotlin.String, kotlin.Any?>"
		//todo override hashCode ?
		
		override fun <D : Any> read(input: D, factory: ReaderFactory<D>): MutableMap<String, Any?>? = Produce(factory(input), RawMapWriter())
		override fun <D : Any> write(input: MutableMap<String, Any?>, factory: WriterFactory<D>): D? = Produce(RawMapReader(input), factory())
		
		override fun instance(): MutableMap<String, Any?> = mutableMapOf()
		
		override fun asInstance(v: Any): MutableMap<String, Any?>? = when (v) {
			is Map<*, *> -> {
				if (v.keys.all { it is String }) v as? MutableMap<String, Any?>
				else mutableMapOf<String, Any?>().apply { v.entries.forEach { this[it.key.toString()] = it.value } }
			}
			else -> PropType.evalError(v, this)
		}
		
		override fun equal(m1: MutableMap<String, Any?>?, m2: MutableMap<String, Any?>?): Boolean {
			return m1 === m2 || (m1 != null && m2 != null && m1.size == m2.size && run {
				val m1 = m1 as Map<Any?, Any?>
				val m2 = m2 as Map<Any?, Any?>
				m1.entries.all { entry -> m2.containsKey(entry.key) && PropType.equal(entry.value, m2[entry.key]) }
			})
		}
		
		override fun copy(m1: MutableMap<String, Any?>?, deep: Boolean) = if (!deep || m1 == null) m1 else {
			val m2 = instance()
			(m1 as Map<Any?, Any?>).forEach { entry -> m2[entry.key.toString()] = PropType.copy(entry.value, deep) }
			m2
		}
		
		override fun toString(m: MutableMap<String, Any?>?, sequenceLimit: Int): String {
			return if (m == null) "null"
			else (m as Map<Any?, Any?>).map { e -> "${e.key}=${PropType.toString(e.value, sequenceLimit)}" }.joinToString(",", "{", "}", sequenceLimit)
		}
		
		override fun objectWriter() = RawMapWriter()
		override fun readEntry(v: MutableMap<String, Any?>, name: String?, entryBuilder: EntryBuilder, compact: Boolean?): Entry {
			return entryBuilder.ObjectEntry(name, RawMapReader(v))
		}
	}
	
	
	/*SCHEMA TYPE*/
	
	// todo impl eval to convert from sub/super classes
// todo asInstance convert string to object by DefaultProducer
	abstract class SCHEMAof<T : Any>(typeKClass: KClass<T>) : PropType<T>(typeKClass), TypeProducer<T> {
		var compact = true
		var propSize: Int = 0
			internal set(value) = run { field = value }
		val props: List<Prop<Any>> by lazy { init(); pps }
		val propMap: Map<String, Prop<Any>> by lazy { initPropMap() }
		internal var pps = mutableListOf<Prop<Any>>()
		internal var hasConstructor = false
		private var constructor: KFunction<T>? = null // todo test accessors for inline fun
		private var constrProps: Array<Prop<Any>>? = null
		private var nonConstrProps: Array<Prop<Any>>? = null
		internal var inited = false
		
		init {
			if (SchemaObject::class.java.isAssignableFrom(typeKClass.java)) PropType.register(this)
		}
		
		/* predef props */
		
		fun PROP_STUB(): Prop<Unit> = PROP_of(STUB)
		fun PROP_Long(hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<Long> = PROP_of(LONG, hasNoAccessors, writeProtected)
		fun PROP_Int(hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<Int> = PROP_of(INT, hasNoAccessors, writeProtected)
		fun PROP_Short(hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<Short> = PROP_of(SHORT, hasNoAccessors, writeProtected)
		fun PROP_Byte(hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<Byte> = PROP_of(BYTE, hasNoAccessors, writeProtected)
		fun PROP_Char(hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<Char> = PROP_of(CHAR, hasNoAccessors, writeProtected)
		fun PROP_Double(hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<Double> = PROP_of(DOUBLE, hasNoAccessors, writeProtected)
		fun PROP_Float(hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<Float> = PROP_of(FLOAT, hasNoAccessors, writeProtected)
		fun PROP_Boolean(hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<Boolean> = PROP_of(BOOLEAN, hasNoAccessors, writeProtected)
		fun PROP_String(hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<String> = PROP_of(STRING, hasNoAccessors, writeProtected)
		fun PROP_Longs(hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<LongArray> = PROP_of(LONGS, hasNoAccessors, writeProtected)
		fun PROP_Ints(hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<IntArray> = PROP_of(INTS, hasNoAccessors, writeProtected)
		fun PROP_Shorts(hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<ShortArray> = PROP_of(SHORTS, hasNoAccessors, writeProtected)
		fun PROP_Bytes(hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<ByteArray> = PROP_of(BYTES, hasNoAccessors, writeProtected)
		fun PROP_Chars(hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<CharArray> = PROP_of(CHARS, hasNoAccessors, writeProtected)
		fun PROP_Doubles(hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<DoubleArray> = PROP_of(DOUBLES, hasNoAccessors, writeProtected)
		fun PROP_Floats(hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<FloatArray> = PROP_of(FLOATS, hasNoAccessors, writeProtected)
		fun PROP_Booleans(hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<BooleanArray> = PROP_of(BOOLEANS, hasNoAccessors, writeProtected)
		fun PROP_Map(hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<MutableMap<String, Any?>> = PROP_of(MAP, hasNoAccessors, writeProtected)
		fun PROP_Collection(hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<Collection<Any?>> = PROP_of(COLLECTION, hasNoAccessors, writeProtected)
		fun PROP_Array(hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<Array<Any?>> = PROP_of(ARRAY, hasNoAccessors, writeProtected)
		fun <E : Any> PROP_ArrayOf(elementType: PropType<E>, hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<Array<E>> = PROP_of(ARRAYof(elementType), hasNoAccessors, writeProtected)
		fun <E : Any> PROP_ListOf(elementType: PropType<E>, hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<List<E>> = PROP_of(LISTof(elementType), hasNoAccessors, writeProtected)
		fun <E : Any> PROP_MutableListOf(elementType: PropType<E>, hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<MutableList<E>> = PROP_of(MLISTof(elementType), hasNoAccessors, writeProtected)
		fun <E : Any> PROP_SetOf(elementType: PropType<E>, hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<Set<E>> = PROP_of(SETof(elementType), hasNoAccessors, writeProtected)
		fun <E : Any> PROP_MutableSetOf(elementType: PropType<E>, hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<MutableSet<E>> = PROP_of(MSETof(elementType), hasNoAccessors, writeProtected)
		inline fun <reified R : Any> PROP_AutoSchemaOf(hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<R> = PROP_of(AUTOSCHEMAof<R>(), hasNoAccessors, writeProtected)
		
		@Suppress("CAST_NEVER_SUCCEEDS")
		fun <T : Any> PROP_of(type: PropType<T>, hasNoAccessors: Boolean = false, writeProtected: Boolean? = null): Prop<T> {
			val p = Prop(propSize++, type, this, hasNoAccessors, writeProtected)
			pps.add(p as Prop<Any>)
			onPropCreated(p)
			return p
		}
		
		open protected fun onPropCreated(p: Prop<*>) = Unit
		open protected fun onObjectProduced(obj: T) = Unit
		
		override fun <D : Any> read(input: D, factory: ReaderFactory<D>): T? = Produce(factory(input), SchemeWriter(this))
		override fun <D : Any> write(input: T, factory: WriterFactory<D>): D? = Produce(SchemeReader(input, this), factory())
		fun <D : Any> write(input: T, factory: WriterFactory<D>, compact: Boolean): D? = Produce(SchemeReader(input, this, compact), factory())
		
		/* internal */
		
		override fun isInstance(v: Any): Boolean {
			return super.isInstance(v) || typeKClass.java.isAssignableFrom(v.javaClass)
		}
		
		override fun instance(): T {
			init()
			return when (hasConstructor) {
				true -> instance(arrayOfNulls<Any?>(propSize).apply { for (n in 0 until propSize) this[n] = Unit })
				false -> typeKClass.java.newInstance()
			}
		}
		
		override fun toString(v: T?, sequenceLimit: Int): String {
			if (v == null) return "{}"
			val text = StringBuilder("{")
			props.forEachIndexed { ix, p ->
				var value = p.toString(p.get(v), sequenceLimit)
				text.append("${p.name}=").append("$value")
				if (ix < propSize - 1) text.append(", ")
			}
			text.append("}")
			return text.toString()
		}
		
		internal fun equalProps(v1: T, v2: T): Boolean = props.all { p ->
			val r = p.equal(p.get(v1), p.get(v2))
			//todo remove
			if (!r) println("NonEQ: ${p.name}; PType= ${p.type.javaClass.simpleName};  V1:${p.get(v1)?.javaClass?.name}= ${p.toString(p.get(v1))};  V2:${p.get(v2)?.javaClass?.name}= ${p.toString(p.get(v2))}")
			r
		}
		
		override fun equal(v1: T?, v2: T?): Boolean = v1 === v2 || (v1 != null && v2 != null && equalProps(v1, v2))
		override fun copy(v: T?, deep: Boolean): T? = if (v == null) null else {
			val newV = instance()
			for (p in props) p.set(newV, if (deep) p.copy(p.get(v), true) else p.get(v))
			onObjectProduced(newV)
			newV
		}
		
		override fun hash(v: T): Int {
			var code = 1
			props.forEach { p ->
				val pv = p.get(v)
				code = code * 31 + if (pv == null) 0 else p.hash(pv)
			}
			return code
		}
		
		internal fun instance(values: Array<Any?>): T {
			val constrVals = arrayOfNulls<Any>(constrProps!!.size)
			constrProps!!.forEach { p -> constrVals[p.constrIndex] = p.asInstance(values[p.index], p.nullable) }
			//		println("CONSTR values [${values.joinToString()}];   constrVals [${constrVals.joinToString()}];  ")
			val obj = constructor!!.call(*constrVals)
			nonConstrProps!!.forEach { it.set(obj, values[it.index]) }
			return obj
		}
		
		override fun objectWriter(): Writer<T>? = SchemeWriter(this)
		override fun sequenceWriter(): Writer<T>? = SchemeWriter(this)
		override fun readEntry(v: T, name: String?, entryBuilder: EntryBuilder, compact: Boolean?): Entry {
			val reader = SchemeReader(v, this)
			return if (compact ?: this.compact) entryBuilder.SequenceEntry(name, reader)
			else entryBuilder.ObjectEntry(name, reader)
		}
		
		
		fun init() {
			if (inited) return
			inited = true
			initProps()
			initConstructor()
		}
		
		@Suppress("UNCHECKED_CAST")
		internal open fun initProps() {
			val propClasName = propClassName + "<"
			fun isProp(kotP: KProperty<*>): Boolean = kotP.returnType.javaType.typeName.startsWith(propClasName)
			//
			javaClass.kotlin.memberProperties.forEach { kotP ->
				if (isProp(kotP)) {
					val prop = kotP.get(this) as Prop<Any>
					if (!prop.inited) initProp(prop, kotP as KProperty<Prop<Any>>)
				}
			}
		}
		
		@Suppress("UNCHECKED_CAST")
		internal fun <T : Any> initProp(prop: Prop<T>, kotP: KProperty<Prop<T>>) {
			prop.inited = true
			prop.name = kotP.name
			val objProps = typeKClass.memberProperties
			val objP = objProps.find { it -> it.name == prop.name }
			if (objP == null) run { if (!prop.stub) println("Property ${javaClass.name}.${kotP.name} is absent in $typeKClass"); return }
			if (prop.stub) println("${javaClass.name}.${kotP.name} is stub prop but it's counterpart is present in $typeKClass")
			prop.nullable = objP.returnType.isMarkedNullable
			if (!prop.sameKType(objP.returnType)) {
				throw Exception("${javaClass.name}.${prop.name}: Prop<${prop.typeName}> type argument does not match to ${typeKClass.jvmName}.${prop.name}: ${objP.returnType}")
			}
			objP.isAccessible = true
			val mutableP = objP as? KMutableProperty1<*, *>
			if (prop.writeProtected == null) prop.writeProtected = mutableP == null
			if (prop.noAccessors) prop.field = objP.javaField?.apply { isAccessible = true }
			else {
				prop.getter = objP.getter as KCallable<T>
				if (prop.writeProtected == false)
					if (mutableP != null) prop.setter = mutableP.setter as KCallable<T>
					else prop.field = objP.javaField?.apply { isAccessible = true }
			}
//			prop.getter = objP.getter as KCallable<T>
//			if (objP is KMutableProperty1) prop.setter = objP.setter as KCallable<T>
//			if (prop.noAccessors) prop.field = objP.javaField?.apply { isAccessible = true }
			//					pl("initProp ${prop.name};  nullable? ${prop.nullable};  ${prop.getter?.name};  ${prop.setter?.name}")
		}
		
		internal fun initConstructor() {
			// todo : issue if constr param has same name but is not val/var. I.e. fakes real prop.
			val constr = typeKClass.primaryConstructor
			val params = constr?.parameters ?: return
			var skip = false
			val constrPps = mutableListOf<Prop<Any>>()
			//		println("${javaClass.simpleName} CONSTR params: ${params.input { "${it.name}: ${it.type.javaType.typeName}" }}")
			for (param in params) {
				val prop = propMap[param.name]
				if (prop == null) throw Exception("${typeKClass.qualifiedName} primary constructor should have only params that correspond to ${javaClass.name} Props. Param '${param.name}' doesn't.")
				if (!prop.sameKType(param.type)) throw Exception("${typeKClass.qualifiedName} primary constructor param '${param.name}: ${param.type}' type doesn't match type of to corresponding '${javaClass.name}.${prop.name}: ${prop.typeName}'.")
				prop.constrIndex = param.index
				constrPps.add(prop)
			}
			if (constr != null && constrPps.isNotEmpty()) {
				hasConstructor = true
				constr.isAccessible = true
				constructor = constr
				constrProps = arrayOf(* constrPps.toTypedArray())
				nonConstrProps = arrayOf(* props.filter { it.constrIndex < 0 }.toTypedArray())
				//			println("${javaClass.name}:: constrProps: ${constrProps!!.input { it.name }};  nonconstrProps: ${nonconstrProps!!.input { it.name }}")
			}
			// else last hope is overridden instance() method
		}
		
		private fun initPropMap(): Map<String, Prop<Any>> = mutableMapOf<String, Prop<Any>>().apply {
			for (p in props) {
				this[p.name] = p
				if (p.alias != null) this[p.alias!!] = p
			}
		}
		
		internal fun addAlias(alias: String, prop: Prop<Any>) {
			if (inited) (propMap as MutableMap<String, Prop<Any>>)[alias] = prop
		}
		
		internal fun removeAlias(alias: String) {
			if (inited) (propMap as MutableMap<String, Prop<Any>>).remove(alias)
		}
	}
}


/* AUTO SCHEMA */
class AUTOSCHEMAof<T : Any>(typeKClass: KClass<T>) : PropType.SCHEMAof<T>(typeKClass) {
	companion object {
		val schemaCache: MutableMap<KClass<*>, SCHEMAof<*>> = mutableMapOf()
		val typeCache: MutableMap<String, PropType<*>> = mutableMapOf()
		private fun cache(type: PropType<*>) = run { typeCache[type.typeName] = type }
		
		init {
			cache(INT); cache(STRING)
		}
		
		@Suppress("UNCHECKED_CAST")
		inline operator fun <reified R : Any> invoke(): SCHEMAof<R> {
			val typeKClass = R::class
			val schema = AUTOSCHEMAof(typeKClass)
			return schemaCache.getOrPut(typeKClass) { schema } as SCHEMAof<R>
		}
	}
	
	init {
		inited = true
		initProps()
		initConstructor()
	}
	
	@Suppress("UNCHECKED_CAST")
	override fun initProps() {
		schemaCache[typeKClass] = this
		// only constructor?, public?, with cached type
		val objProps = typeKClass.memberProperties
		for (objP in objProps) {
			//	if (!objP.isAccessible) continue
			val retType = objP.returnType
			val retTypeName = PropType.pureTypeName(retType)
			val pType = typeCache[retTypeName]
			if (pType == null) {
				println("$typeKClass.${objP.name}: $retTypeName  ignored due to absence of type in cache."); continue
			}
			val prop = Prop(propSize++, pType, this, true, null) as Prop<Any>
			pps.add(prop)
			prop.inited = true
			prop.name = objP.name
			prop.nullable = retType.isMarkedNullable
			prop.getter = objP.getter as KCallable<Any>
			if (objP is KMutableProperty1) prop.setter = objP.setter as KCallable<Any>
			prop.field = objP.javaField?.apply { isAccessible = true }
		}
	}
}


/* LIST  TYPE */
class LISTof<E : Any>(override val elementType: PropType<E>) : COLLECTIONof<List<E>, E>(elementType, List::class as KClass<List<E>>) {
	override fun instance(): List<E> = listOf()
	override fun addElement(e: E, index: Int, seq: List<E>): List<E> = seq + e
}


class MLISTof<E : Any>(override val elementType: PropType<E>) : COLLECTIONof<MutableList<E>, E>(elementType, MutableList::class as KClass<MutableList<E>>) {
	// due to glitch: https://youtrack.jetbrains.com/issue/KT-11754
	override val typeName = "kotlin.collections.MutableList<${elementType.typeName}>"
	
	override fun instance(): MutableList<E> = mutableListOf()
	override fun addElement(e: E, index: Int, seq: MutableList<E>): MutableList<E> {
		seq += e
		return seq
	}
}


/* SET  TYPE */
class SETof<E : Any>(override val elementType: PropType<E>) : COLLECTIONof<Set<E>, E>(elementType, Set::class as KClass<Set<E>>) {
	override fun instance(): Set<E> = setOf()
	override fun addElement(e: E, index: Int, seq: Set<E>): Set<E> = seq + e
	override fun equal(v1: Set<E>?, v2: Set<E>?): Boolean = v1 === v2 || (v1 != null && v2 != null && v1.size == v2.size && run {
		for (e1 in v1) if (v2.none { e2 -> elementType.equal(e1, e2) }) return false
		true
	})
}


class MSETof<E : Any>(override val elementType: PropType<E>) : COLLECTIONof<MutableSet<E>, E>(elementType, MutableSet::class as KClass<MutableSet<E>>) {
	// due to glitch: https://youtrack.jetbrains.com/issue/KT-11754
	override val typeName = "kotlin.collections.MutableSet<${elementType.typeName}>"
	
	override fun instance(): MutableSet<E> = mutableSetOf()
	override fun addElement(e: E, index: Int, seq: MutableSet<E>): MutableSet<E> {
		seq += e
		return seq
	}
	
	override fun equal(v1: MutableSet<E>?, v2: MutableSet<E>?): Boolean = v1 === v2 || (v1 != null && v2 != null && v1.size == v2.size && run {
		for (e1 in v1) if (v2.none { e2 -> elementType.equal(e1, e2) }) return false
		true
	})
}

// todo oter kotlin collections  @ linkedSet

