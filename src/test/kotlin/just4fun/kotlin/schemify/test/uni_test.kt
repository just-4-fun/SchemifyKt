package just4fun.kotlin.schemify.test

//import org.jetbrains.spek.api.*
//import just4fun.kotlin.schemify.Utils
import just4fun.kotlin.schemify.Utils
import just4fun.kotlin.schemify.core.*
import java.io.Serializable
import java.lang.reflect.*
import java.util.*
import java.util.concurrent.locks.Condition
import kotlin.jvm.internal.markers.KMutableMap
import kotlin.reflect.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.properties

val N = 1000

///**/
//fun main(args: Array<String>) {
//
//}


/**/
// todo suggest collection fun <T, R> findNotNull(T -> R?): R?


fun main(args: Array<String>) {
}


//\"\\\/\b\f\n\r\t
//json = """ { "p1" : -100 , "p8" : "00\"A\"a" , "p9" : null , "p10" : [ "qwer" , "1234" ] , "p21" : [ { "x" : 123,"y":"ABC" } , { "x":344,"y":"XZY" } ] } """
//json = """{"p1":-100,"p4":0.01,"p6":"ဂ","p7":true,"p8":"00Aa","p9":null,"p10":["qwer","1234"],"p19":[["asd","123"],["xyz","555"]],"p20":{"x":123,"y":"ABC"},"p21":[{"x":123,"y":"ABC"},{"x":344,"y":"XZY"}]}"""
//json = """{"p0":100,"p1":-100,"p2":100,"p3":100,"p4":0.01,"p5":0.01,"p6":"ဂ","p7":true,"p8":"00Aa","p9":null,"p10":["qwer","1234"],"p11":"AQID","p12":[true,false,true],"p13":["a","A","0"],"p14":[0.12,12.0],"p15":[123.01,0.123],"p16":[123.01,0.123],"p17":1234567890,"p18":[1234,5678],"p19":[["asd","123"],["xyz","555"]],"p20":{"x":123,"y":"ABC"},"p21":[{"x":123,"y":"ABC"},{"x":344,"y":"XZY"}]}"""


/* reflect constructor params */
//fun main(args: Array<String>) {
//	reflectClass(X::class)
//	reflectClass(Y::class)
//	reflectClass(Z::class)
//}
//
//fun reflectClass(clas: KClass<*>) {
//	println("Class ${clas.qualifiedName}")
//	val props = clas.memberProperties.input { p -> "${p.name}: ${p.returnType}" }.joinToString()
//	println("Props:  $props")
//	val pmConstr = clas.primaryConstructor
//	println("Primary constr")
//	reflectFun(pmConstr)
//	println("All constrs")
//	val constrs = clas.constructors
//	constrs.forEach { reflectFun(it)}
//	println("")
//}
//
//fun <T : Any> reflectFun(func: KFunction<T>?) {
//	if (func == null) run { println("function is null");return }
////	val pmsStr = func.parameters.input { p -> "[${p.kind.ordinal}-${if (p.isOptional) "?" else "!"}-${p.index}-${p.name}: ${p.type}]" }.joinToString()
//	val pmsStr = func.parameters.input { p -> "[${if (p.isOptional) "?-" else ""}${p.name}: ${p.type}]" }.joinToString()
//	println("${func.name}(${pmsStr})")
//}
//
//data class X(val p0: Int, var p1: String = "empty", var p4: Int = 11) {
//	constructor(a1: String, a0: Int) : this(a0, a1, 12)
//	constructor() : this(1)
//	val p2: List<Int> = listOf(0, 1)
//	var p3 = byteArrayOf(1, 2)
//}
//
//class Y {
//	var p0: Int = 0
//	val p1: String = "ok"
//}
//
//class Z private constructor(var p0: Int = 0, val p1: String = "ok", fk: Int = 0) {
//	var fk: String = "0"
//}


/* as cast*/
//fun main(args: Array<String>) {
//	val a: Any? = "ok"
////	println("${a as Int}")//ClassCastException
////	println("${a as Int?}")//ClassCastException
//	println("${a as? Int}")// null
//}


/* measure delegate vs getter */
//fun main(args: Array<String>) {
//	val N  =1//000000
//	val s = Schema()
//	fun get0()  = measureTime("P0", N, 3) {
//		val v = s.p0
//		v.v
//	}
//	fun get1()  = measureTime("P1", N, 3) {
//		val v = s.p1
//		v.v
//	}
//	println(get1())// 1000 / 7
//	println(get0())// 1300 / 8
//}
//class Schema {
//	val p0 by Prop(1)
//	val p1 = Prop(2)
//}
//class Prop<T : Any>(val v: T) {
//	var inited = false
//	internal  operator fun getValue(thisRef: Any?, property: KProperty<*>): Prop<T> = if(inited) this else {inited = true; this}
//}


/*access uninited lateinit */
//fun main(args: Array<String>) {
//	val o = X()
//	println("${o.x == null}")// throws kotlin.UninitializedPropertyAccessException: lateinit property x has not been initialized
//}
//class X{
//	lateinit var x: String
//}


/* accessors performance coparison */
//fun main(args: Array<String>) {
//	val N = 1//000000
//	val input = Obj()
//	val p = ObjSchema.props[5]
//	val prop = ObjSchema.p5
//	println("${p === prop}")
//	val kprop = Obj::class.declaredMemberProperties.find { it.name == "p5" }!! as KMutableProperty1<Any, Any>
//	val setter = KMutableProperty1<Any, Any>::set
//	val getter = KMutableProperty1<Any, Any>::get
//	val field = kprop.javaField!!.apply { isAccessible = true }
//	fun g1() = measureTime("DIRECT <<", N) {
//		input.p5
//	}
//	fun g2() = measureTime("PROP <<", N) {
//		prop.get(input)
//	}
//	fun g3() = measureTime("SETTER <<", N) {
//		getter(kprop, input)
//	}
//	fun g4() = measureTime("FIELD <<", N) {
//		field.get(input)
//	}
//	fun s1() = measureTime("DIRECT >>", N) {
//		input.p5 = 100.0
//	}
//	fun s2() = measureTime("PROP >>", N) {
//		prop.set(input, 101)
//	}
//	fun s3() = measureTime("SETTER >>", N) {
//		setter(kprop, input, 102.0)
//	}
//	fun s4() = measureTime("FIELD >>", N) {
//		field.set(input, 103.0)
//	}
////	s1();// 766 / 7
////	s2();// 13000 / 180
////	s3();// 13000 / 200
//	s4();//  2300 / 15 !!! doesn't touch accessors
////	g1();// 1000 / 8
////	g2();// 10000 / 140
////	g3();// 10000 / 160
////	g4();// 1800 / 14 !!! doesn't touch accessors
//	println("${input.p5}")
//}
//class Obj() {
//	constructor(a0: Int, a1: String, a2: Boolean, a3: Long, a4: Float, a5: Double, a6: Short, a7: Byte, a8: Char, a9: ByteArray, a11: Point, a12: MutableList<Int>) : this() {
//		p1 = a1; p2 = a2;/*p3 = a3;*/p4 = a4;p5 = a5;p6 = a6;p7 = a7;p8 = a8;p9 = a9;p11 = a11;p12 = a12;
//	}
//
//	val p0: Int = 10
//	var p1: String = "oops"
//		private set(value) = run { field = value }
//	var p2: Boolean? = false
//	val p3: Long = 103L
//	var p4: Float = 14f
//	var p5: Double = 1.5
//	var p6: Short = 106
//	var p7: Byte = 0b00000000
//	var p8: Char = '\u0108'
//	var p9 = byteArrayOf(0, 1, 9)
//	//	val p10 = "STUB"
//	var p11: Point? = Point("x", 0.7f)
//	var p12: MutableList<Int>? = mutableListOf(12, 13, 14)
//}
//
//object ObjSchema : SchemaType<Obj>() {
//	override fun instance(): Obj = Obj()
//	override val typeClass = Obj::class
//	val p0 = PROP(IntType)
//	val p1 = PROP(StringType)
//	val p2 = PROP(BooleanType)
//	val p3 = PROP(LongType)
//	val p4 = PROP(FloatType)
//	val p5 = PROP(DoubleType)
//	val p6 = PROP(ShortType)
//	val p7 = PROP(ByteType)
//	val p8 = PROP(CharType)
//	val p9 = PROP(BytesType)
//	val p10 = STUB()
//	val p11 = PROP(Point)
//	val p12 = PROP(ListType(IntType))
//}
//
//class Point(var x: String, var y: Float) {
//	companion object : SchemaType<Point>() {
//		override val typeClass = Point::class
//		override fun instance(): Point = Point("", 0f)
//		val x = PROP(StringType)
//		val y = PROP(FloatType)
//	}
//}


/* val = function */
//fun main(args: Array<String>) {
//	val f = X::test
//	val x = X()
//	f(x, "ok")
//}
//class X {
//	fun test(v: String): Int {
//		println("test > ${v}")
//		return 1
//	}
//}


/* for-in vs while-get */
//fun main(args: Array<String>) {// 7 ms
//	val list = arrayOfNulls<Int>(1000000)
//	measureTime ("GET",1, false){
//		var sum = 0
//		var count = list.size
//		while (count-- > 0)  {
//			val item = list[count]
//			sum += item ?: 1
//		}
//		println("${sum}")
//	}
//	measureTime ("FOR",1, false){// 6 ms
//		var sum = 0
//		for (item in list) {
//			sum += item ?: 1
//		}
//		println("${sum}")
//	}
//	measureTime ("RANGE",1, false){// 28 ms
//		var sum = 0
//		(0..1000000).forEachIndexed { i, item -> sum += 1}
//		println("${sum}")
//	}
//
//}

/* overloading clash */
//class X {
//	@JvmName("a1")fun a(v: List<Int>) {}
//	@JvmName("a2")fun a(v: List<String>) {}
//}
//interface X {
//	@JvmName("a1")fun a(v: List<Int>) {}
//	@JvmName("a2")fun a(v: List<String>) {}
//}


/* reflect null in  non-null list*/
//fun main(args: Array<String>) {
//	val input = A(1, "oops")
//	val xProp = A::class.declaredMemberProperties.find { it.name == "list" }!!
//	val xSetter = if (xProp is KMutableProperty1<*, *>)  xProp as KMutableProperty1<A, Iterable<Int?>>  else null
//	xSetter?.isAccessible = true
//	xSetter?.set(input, mutableListOf(null, 4))
//	println("setter= $xSetter;  input.x= ${input.list}")
//	input.test()
//}
//open class Z
//class A(var x: Int, var y: String) : Z() {
//	var list: List<Int> = listOf(1, 2)
//	fun test() {
//		list.forEach { pl("${it == null}") }
//	}
//}


/* null type */
//fun main(args: Array<String>) {
//	val list1 = mutableListOf<String?>("", null)
//	val list2 = mutableListOf<String>("", null)
//	val s: String? = ""
//	val v = s?.
//	fun <T: X?>test1(v: T): Unit = run{v.x  = ""; v = null}
//	fun <T: X>test2(v: T): Unit = run{v.x  = ""}
//	fun <T: X>test3(v: T?): Unit = run{v?.x  = ""}
//}
//val clas = 	kotlin.jvm.internal.Reflection.createKotlinClass(0.javaClass)// todo ?
//fun <E> Array<out E>.firstNotNull1(): E = this.filterNotNull().first()
//fun <E : Any> Array<out E?>.firstNotNull2(): E = this.filterNotNull2().first()
//public fun <T> Array<out T>.filterNotNull1(): List<T>  {
//	return filterNotNullTo(ArrayList<T>())
//}
//public fun <T : Any> Array<out T?>.filterNotNull2(): List<T> {
//	return filterNotNullTo(ArrayList<T>())
//}
//open class X(var x: String)
//class Gen2(var v: Any?) {
//	val clas = v?.javaClass
//	init { v = null }
//}
//class Gen3<T>(var v: T?) {
//	val clas = v?.javaClass
//	init {v = null }
//}
//class Gen4Impl : Gen4<String?>(null)
//open class Gen4<T>(var v: T) {
//	val clas = v?.javaClass
//	init { v.hashCode() ; v = null }
//}
//class Gen5<T: Any>(var v: T) {
//	val clas = v.javaClass
//	init { v = null }
//}
//class Gen6<T: Any>(var v: T?) {
//	val clas = v?.javaClass
//	init { v = null }
//}


/* let apply run with */
////fun <T, R> T.let  (                       f:     (T) -> R)  : R = f(this)
////fun <T>      T.apply(                    f: T.() -> Unit): T { f(); return this }
////fun <T, R> T.run  (                      f: T.() -> R)     : R = f()
////fun <R>        run(                          f:    () -> R)     : R = f()
////fun <T, R>    with (receiver: T, f: T.() -> R)    : R = receiver.f()
//fun main(args: Array<String>) {
//	val a: Actor = Actor().apply { use() }
//	val used0: Boolean = a.isUsed()
//	val used1: Boolean = Actor().let { actor -> actor.use(); actor.isUsed() }
//	val used2: Boolean = Actor().run { use(); isUsed() }
//	val used3: Boolean = with(Actor()) { use(); isUsed() }
//	//
//	val actor: Actor? = null
//	if (actor != null) {
//		a.use()
//	}
//	actor?.let { a -> a.use() }
//	actor?.run { use() }
//}
//class Actor {
//	private var flag = false
//	val lazzy: Boolean = run { flag = false; isUsed() }
//	fun use(): Unit = run { flag = true }
//	fun isUsed(): Boolean = flag
//}


/*setter*/
//fun main(args: Array<String>) {
//	val input = A(1, "oops")
//	val xProp = A::class.declaredMemberProperties.find { it.name == "x" }!!
//	val xSetter = if (xProp is KMutableProperty1<*, *>)  xProp as KMutableProperty1<A, Int>  else null
//	xSetter?.isAccessible = true
//	xSetter?.set(input, 2)
//	println("setter= $xSetter;  input.x= ${input.x}")
//	val yProp = A::class.declaredMemberProperties.find { it.name == "y" }!!
//	val ySetter = if (yProp is KMutableProperty1<*, *>)  yProp as KMutableProperty1<A, String>  else null
//	ySetter?.isAccessible = true
//	ySetter?.set(input, "ok")
//	println("setter= $ySetter;  input.y= ${input.y}")
//}
//open class Z
//class A(var x: Int, var y: String) : Z()

/*test KClass equality*/
//fun main(args: Array<String>) {
//	val c = Int::class
//	fun test(v0: Any, v1: Any) {
//		val c0 = v0.javaClass.kotlin
//		val c1 = v1.javaClass.kotlin
//		pl("c= $c;  c0= $c0;  c1= $c1")
//		pl("c= ${c.java};  c0= ${c0.java};  c1= ${c1.java}")
//		pl("c = c0? ${c == c0};  c0 = c1? ${c1 == c0}")
//		pl("c? ${c.javaObjectType};  c? ${c1.java.typeName};  ")
//		pl("c = c1? ${c.javaObjectType == c1.javaObjectType};  ")
//	}
//	test(11, 22)
//}


/*Map VS List search*/
//fun main(args: Array<String>) {
//	val N = 10
//	var count = 0
//	fun next(): Prop = Prop(count++.toString())
//	val list = mutableListOf<Prop>()
//	for(n in 0..N) list.add(next())
//	val input = mutableMapOf<String, Prop>(* list.input { it.name to it }.toTypedArray())
//	fun findInList(name: String) {
//		list.find{it.name == name}
//	}
//	fun findInMap(name: String) {
//		input[name]
//	}
//	fun testSearchList() {
//		for(n in 0..N) findInList(n.toString())
//	}
//	fun testSearchMap() {
//		for(n in 0..N) findInMap(n.toString())
//	}
//	measureTime("MAP", 1000, true) { testSearchMap() }// 12 > 5 times faster
//	measureTime("LIST", 1000, true) { testSearchList() }// 60
//}
//class Prop(val name: String)


/*delegate set reflectively*/
//fun main(args: Array<String>) {
//	val o = Example()
//	pl("before assign")
//	o.assignP("ok")
//}
//class Example {
//	init{ pl("created Example") }
//	var p0 by Delegate<String>("oops")
//	init{ pl("inside Example init p0") }
//	var p1 by Delegate<String>("oops")
//	val pp by lazy{ "ok"}
//	init{ pl("inside Example init p1= ${javaClass.kotlin.declaredMemberProperties.find { it.name == "p1" }?.get(this)}") }
//	fun assignP(v: String) {
////		p = v
////		val s = javaClass.declaredMethods.input { it.name + ": " + it.parameterTypes.input { it.name }.joinToString() }.joinToString ("\n")
////		pl("methods: $s")
//		val f = javaClass.getDeclaredMethod("setP0", String::class.java)
//		f.isAccessible = true
//		f.invoke(this, "reflect ok")
//	}
//}
//class Delegate<T : Any>(v: T) {
//	init{ pl("created Delegate") }
//	operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
//		return "delegated get ${thisRef?.javaClass?.simpleName}.${property.name}"
//	}
//	operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
//		println("delegated set  ${thisRef?.javaClass?.simpleName}.${property.name}= $value")
//	}
//}


/* accessors bench*/
//fun main(args: Array<String>) {
//    val x = X()
//    //    pl("${X::class.java.declaredMethods.input { m-> "${m.name};  ${m.parameterTypes}" }.joinToString()}")
//    val meth1 = X::class.java.getDeclaredMethod("setA", Int::class.java)
//    val prop1 = X::class.declaredMemberProperties.find { it.name == "a" } as KMutableProperty1<X, Int>
//    val meth2 = X::class.java.getDeclaredMethod("setB", String::class.java)
//    val prop2 = X::class.declaredMemberProperties.find { it.name == "b" } as KMutableProperty1<X, String>
//    fun kotlinSet1() = prop1.set(x, 1)
//    fun javaSet1() = meth1.invoke(x, 2)
//    fun kotlinSet2() = prop2.set(x, "ok")
//    fun javaSet2() = meth2.invoke(x, "ok")
//    measureTime("JAVA1", 1000000000, true, ::javaSet1)
//    measureTime("KOTLIN1", 1000000000, true, ::kotlinSet1)
//    measureTime("JAVA2", 1000000000, true, ::javaSet2)
//    measureTime("KOTLIN2", 1000000000, true, ::kotlinSet2)
//    pl("${x.a};  ${x.b}")
//}
//class X {
//    var a = 0
//    var b = "oops"
//}

/*reflection performance*/
//fun main(args: Array<String>) {
//    var res: Any? = null
//    res = measureTime ("JAVA", 1, false, ::reflectJava)//6
//    res = measureTime ("KOTLIN", 1, false, ::reflectKotlin)// 400
//    pl("result= $res")
//}
//fun reflectJava(): Unit {
//    X::class.java.declaredFields
////    Y::class.java.declaredFields
////    Z::class.java.declaredFields
//}
//fun reflectKotlin(): Unit {
//    X::class.properties
////    Y::class.properties
////    Z::class.properties
//}
//class X(val n: Int)
//class Y(val n: Int)
//class Z(val n: Int)


/*Null*/
//fun main(args: Array<String>) {
//    fun eq(a: Any?, b: Any?): Boolean {
//        return a?.equals(b) ?: (b === null)
//    }
//    pl(eq("ok", "ok"))
//    pl(eq("ok", null))
//    pl(eq(null, "ok"))
//    pl(eq(null, null))
//}

/*smart cast*/
//fun main(args: Array<String>) {
//    val y: Any? = null
////    val x0: String = y as String// throws TypeCastException
//    val x1: String? = y as String? //Unsafe
//    val x2: String? = y as? String // Safe
//}

/*Function Literals with Receiver*/
//fun main(args: Array<String>) {
//    fun Int.div(other: Int): Int = this / other
//    val sum = fun Int.(other: Int): Int = this + other
//    1.sum(2)
//    1.div(2)
//    html {       // lambda with receiver begins here
//        body()   // calling a method on the receiver object
//    }
//}
//class HTML {
//    fun body() { }
//}
//fun html(init: HTML.() -> Unit): HTML {
//    val html = HTML()  // create the receiver object
//    html.init()        // pass the receiver object to the lambda
//    return html
//}


//fun main(args: Array<String>) {
//    fun <T> lock(lock: Lock, body: () -> T): T {
//        lock.lock()
//        try { return body()} finally { lock.unlock() }
//    }
////    lock(lock, { sharedResource.operation() })
////    lock (lock) { sharedResource.operation() } //if the last parameter to a function is a function
//}


/*default can use other argument*/
//fun main(args: Array<String>) {
//read(X(22))
//}
//fun read(b: X, off: Int = 0, len: Int = b.x()) {
//    println("$len")
//}
//class X(val v:Int) {
//    fun x(): Int = 44
//}

/*input delegate*/
//fun main(args: Array<String>) {
//    println(user.name) // Prints "John Doe"
//    println(user.age)  // Prints 25
//}
//class User(val input: Map<String, Any?>) {
//    val name: String by input
//    val age: Int by input
//}
//class MutableUser(val input: MutableMap<String, Any?>) {
//    var name: String by input
//    var age: Int by input
//}
//val user = User(mapOf(
//        "name" to "John Doe",
//        "age"  to 25
//))


/*observer*/
//fun main(args: Array<String>) {
//    val user = User()
//    user.name = "first"
//    user.name = "second"
//}
//class User {
//    var name: String by Delegates.observable/*vetoable*/("<no name>") {
//        prop, old, new ->
//        println("$old -> $new")
//    }
//}

/*lazy*/
//fun main(args: Array<String>) {
//    println(lazyValue)
//    println(lazyValue)
//}
//val lazyValue: String by lazy {
//    println("computed!")
//    "Hello"
//}

/*delegation*/
//fun main(args: Array<String>) {
//    val e = Example()
//    e.p = "ok"
//    println(e.p)
//    println(e.p.length)
//}
//class Example {
//    var p: String by Delegate()
//}
//class Delegate {
//    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
//        return "$thisRef, thank you for delegating '${property.name}' to me!"
//    }
//    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
//        println("$value has been assigned to '${property.name} in $thisRef.'")
//    }
//}

/*delegation*/
//fun main(args: Array<String>) {
//println("")
//    val b = BaseImpl(10)
//    Derived(b).print() // prints 10
//}
//interface Base {
//    fun print()
//}
//class BaseImpl(val x: Int) : Base {
//    override fun print() { print(x) }
//}
//class Derived(b: Base) : Base by b

/*enum*/
//enum class Color(val rgb: Int) {
//    RED(0xFF0000),
//    GREEN(0x00FF00),
//    BLUE(0x0000FF);
//    fun use(): Int { return 0 }
//}

/*companion*/
//class A {
//    companion object
//    val i = this.javaClass.kotlin.companionObjectInstance
//}


/*access type*/
//fun main(args: Array<String>) {
//    val g1 = Generic<String>()
//    val g2 = Generic<Int>()
//    println(g1.theClass())
//    println(g2.theClass())
//}
//
//class Generic<T : Any>(val c: Class<T>) {
//    companion object {
//        inline operator fun <reified T : Any>invoke() = Generic(T::class.java)
//    }
//    fun theClass(): Class<T> = c
//}


//fun pl(v: Any) = println(v)

fun <T> measureTime(tag: String = "", times: Int = 1, warmup: Int = 3, code: () -> T?): T? {
	repeat(warmup) { n -> code() }
	var result: T?
	var count = times
	val t0 = System.nanoTime()
	if (times <= 1) result = code()
	else do {
		result = code(); count--
	} while (count > 0)
	val t = System.nanoTime() - t0
	println("$tag ::  $times times;  ${t / 1000000} ms;  $t ns;  ${t / times} ns/call")
	return result
}





