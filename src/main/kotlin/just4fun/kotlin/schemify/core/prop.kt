package just4fun.kotlin.schemify.core

import com.sun.org.apache.xpath.internal.operations.Bool
import just4fun.kotlin.schemify.hide
import java.lang.reflect.Field
import kotlin.reflect.*
import kotlin.reflect.jvm.javaField


/*PROP*/
// todo alias names
// todo cache all types : not to have several  ListType<Int> instances
class Prop<T : Any> internal constructor(override val index: Int, val type: PropType<T>, internal val schema: PropType.SCHEMAof<*>, internal val noAccessors: Boolean, internal var writeProtected: Boolean?) : iPropType<T> by type, PropInfo {
	
	override var name: String = ""
		internal set(value) = run { field = value }
	override val stub = type === PropType.STUB
	override var extra: Any? = null
	internal var nullable = true
	internal var getter: KCallable<T>? = null
	internal var setter: KCallable<T>? = null
	internal var field: Field? = null// WARN: 2+ times faster but !!! slips past accessors
	internal var inited = false
	internal var constrIndex = -1
	//TODO replace alias with Reader/Writer map/filter
	var alias: String? = null
		get() = field
		set(value) {
			if (field != null) schema.removeAlias(field!!)
			if (value != null) schema.addAlias(value, this as Prop<Any>)
			field = value
		}
	
	operator fun get(o: Any): T? = if (field == null) getter?.call(o) else field!!.get(o) as T?

	operator fun set(o: Any, v: Any?): Unit {
		if(writeProtected == true || v == Unit) return// allow to skip updating for Unit values
		val ev = asInstance(v, nullable)
		if (setter != null) setter!!.call(o, ev) else if (field != null) field!!.set(o, ev) // todo else warn: prop is no inited or stub
//		if (field == null) setter?.call(o, ev) else if (setter != null) field!!.set(o, ev)
	}

	@Suppress("UNCHECKED_CAST")
	operator fun getValue(thisRef: Any?, property: KProperty<*>): Prop<T> {
		if (!inited) schema.initProp(this, property as KProperty<Prop<T>>)
		//		println("Access prop ${ this.name}; ")
		return this
	}
}

interface PropInfo {
	val index: Int
	val name: String
	val stub: Boolean
	var extra: Any?
	operator fun component1() = index
	operator fun component2() = name
	operator fun component3() = stub
	operator fun component4() = extra
}


/*PROP TYPE*/
interface iPropType<T : Any> {
	val typeKClass: KClass<T>
	val typeName: String

	fun instance(): T
	fun default(): T? = null

	fun isInstance(v: Any): Boolean = v.javaClass == typeKClass.java//v.javaClass.kotlin == typeKClass 3x slower: 3000 ns
	fun asInstance(v: Any?, nullable: Boolean): T? {
		val ev = if (v == null || v == Unit) null else asInstance(v)
		return ev ?: if (nullable) default() else instance()
	}

	@Suppress("UNCHECKED_CAST")
	fun asInstance(v: Any): T? = when {
		isInstance(v) -> v as? T
		else -> PropType.evalError(v, this, Exception("${v.javaClass.kotlin.javaObjectType} is not ${typeKClass.javaObjectType}"))
	}

	fun copy(v: T?, deep: Boolean = false): T? = v
	fun equal(v1: T?, v2: T?): Boolean = v1 == v2
	fun differ(v1: T?, v2: T?): Boolean = !equal(v1, v2)
	fun toString(v: T?, sequenceLimit: Int = -1): String = v.toString()
	fun hash(v: T): Int = v.hashCode()
	// todo compare

	@hide fun sameKType(otherKType: KType): Boolean {
		val name = otherKType.toString()
		return name.length <= typeName.length + 1 && name.startsWith(typeName)
	}

	@hide fun readEntry(v: T, name: String?, entryBuilder: EntryBuilder, compact: Boolean?): Entry
	@hide fun objectWriter(): Writer<T>? = null
	@hide fun sequenceWriter(): Writer<T>? = null
}
