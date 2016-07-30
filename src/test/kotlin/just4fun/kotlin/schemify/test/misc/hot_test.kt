package just4fun.kotlin.schemify.test.misc


import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonToken
import just4fun.kotlin.schemify.core.*
import java.util.*
import kotlin.reflect.KClass
import just4fun.kotlin.schemify.production.*
import just4fun.kotlin.schemify.test.*
import just4fun.kotlin.schemify.core.PropType.*

fun main(args: Array<String>) {
	val N = 1000
	val objSchema = ObjSchema()

	val obj = Obj(0, "zzz", true, 0, 2.4f, 0.25, -26, 27, '\u0028', byteArrayOf(2, 9), Point("pp", 8.8f), mutableListOf(3, 30), Date(1462630720414), arrayOf("a", "b", "c"), AutoObj(45, "AuO"), intArrayOf(555, 223))

//	val xml1 = Produce(ObjectReader(input, objSchema, false), XmlWriter())
	val xml1 = objSchema.write(obj, XmlFactory, false)
	println("XML: $xml1")
//	val obj1 = Produce(XmlReader(xml1!!.toCharArray()), ObjectWriter(objSchema))
	val obj1 = objSchema.read(xml1!!, XmlFactory)
	println("EQ? ${objSchema.equal(obj, obj1)}")

/*
//	objSchema.writeAsSequence = false
	var json1: String? = Produce(ObjectReader(input, objSchema), JsonWriter())
//	println("JSON1[${json1!!.length}]=  ${json1}")
//	val obj1 = Producer(DefaultReader(json1!!.toCharArray()), ObjectWriter(objSchema))
//	var json2: String? = Producer(ObjectReader(obj1!!, objSchema), DefaultWriter())
//	println("JSON2[${json2!!.length}]=  ${json2}")
//	println("JSON EQ? ${json1 == json2}")
//	println("OBJ EQ? ${objSchema.equal(input, obj1)}")
//	val obj2 = Producer(JsonReader(json1!!), ObjectWriter(objSchema))
//	objSchema.writeAsSequence = false
//	json1 = Producer(ObjectReader(obj2!!, objSchema), JsonWriter())
//	println("JSON2=  ${json1}")
//	objSchema.writeAsSequence = true
//	json1 = Producer(ObjectReader(obj2!!, objSchema, true), JsonWriter())
//	println("JSON3=  ${json1}")
//	println("EQ? ${objSchema.equal(input, obj2)}")

//	fun json1() = measureTime("Dec New", N) {//
//		Producer(ObjectReader(input, objSchema), DefaultWriter())
//	}
//	fun json2() = measureTime("Dec Old", N) {//
//		objSchema.deconstruct(input, DefaultFactory)
//	}
//	fun json3() = measureTime("Dec TooOld", N) {//
//		objSchema.deconstruct(input, DefaultFactoryOld)
//	}
//	var json1 = json1()
//	var json2 = json2()
//	var json3 = json3()
//	println("EQ12? ${json1 == json2};  EQ13? ${json1 == json3} ")
//	json1();json2();json3();
//	json2();json3();json1();

	fun obj1() = measureTime("Rec New", N) {//
		Produce(DefaultReader(json1!!.toCharArray()), ObjectWriter(objSchema))
	}
	fun obj2() = measureTime("Rec Old", N) {//
		objSchema.reconstruct(json1, DefaultFactory)
	}
	fun obj3() = measureTime("Rec TooOld", N) {//
		objSchema.reconstruct(json1, DefaultFactoryOld)
	}
	val obj1 = obj1()
	val obj2 = obj2()
	val obj3 = obj3()
	println("EQ12? ${objSchema.equal(obj1, obj2)};  EQ13? ${objSchema.equal(obj1, obj3)} ")
	obj3();obj2();obj1()
	obj3();obj2();obj1()
	obj3();obj2();obj1()
	obj3();obj2();obj1()
	obj3();obj2();obj1()
	obj3();obj2();obj1()
	obj3();obj2();obj1()
	obj3();obj2();obj1()
*/

/*
	val aero = Aero()
	aero.input = intArrayOf(*(0..100).toList().toIntArray())
	val json = Aero.deconstruct(aero, DefaultFactory)
	val a1 = measureTime("Rec New", N) {//
		Aero.reconstruct(json, DefaultFactory)
	}
	val a2 = measureTime("Rec  Old", N) {//
		Aero.reconstruct(json, DefaultFactoryOld)
	}
*/


/*
//	val json = """[0,10,"zzz",true,103,2.4,0.25,-26,27,40,[2,9],null,{"x":"pp","y":8.8},[3,30],1462630720414,["a","b","c"],[45,"AuO"],[555,223]]"""
//	val jsonm = """{"id":0,"p0":10,"p1":"zzz","p2":true,"p3":103,"p4":2.4,"p5":0.25,"p6":-26,"p7":27,"p8":40,"p9":[2,9],"p10":null,"p11":{"x":"pp","y":8.8},"p12":[3,30],"p13":1462630720414,"p14":["a","b","c"],"p15":{"a0":45,"a1":"AuO"},"p16":[555,223]}"""
//	val json3 = measureTime("DEC input Old", N) {
//		objSchema.deconstruct(input, DefaultFactory)// 180.000
//	}
//	val json2 = measureTime("DEC input New", N) {// 220.000;  + 40.000
//		objSchema.deconstruct(input, DefaultFactory2)
//	}
//	val json2m = measureTime("DEC input New", N) {//190.000;  + 25.000
//		objSchema.deconstruct(input, DefaultFactory2, true)
//	}
//	val json3m = measureTime("DEC input Old", N) {// 165.000
//		objSchema.deconstruct(input, DefaultFactory, true)
//	}
//	val obj2 = measureTime("REC input New", N) {//  390.000 + 40.000
//		objSchema.reconstruct(json, DefaultFactory2)
//	}
//	val obj3 = measureTime("REC input Old", N) {// 350.000
//		objSchema.reconstruct(json, DefaultFactory)
//	}
//	val obj2m = measureTime("REC input New New", N) {// 400.000;  + 40.000
//		objSchema.reconstruct(jsonm, DefaultFactory2)
//	}
//	val obj3m = measureTime("REC input New Old", N) {// 360.000
//		objSchema.reconstruct(jsonm, DefaultFactory)
//	}
//	println("json=?json2 > ${json == json2}")
//	println("json=?json3 > ${json == json3}")
//	println("json=?json2m > ${jsonm == json2m}")
//	println("json=?json3m > ${jsonm == json3m}")
//	println("input =? obj2 > ${input == obj2}")
//	println("input =? obj3 > ${input == obj3};")
//	println("input =? obj2m > ${input == obj2m};")
//	println("input =? obj3m > ${input == obj3m};")
//	println("${json2}")
//	println("${json3}")
//	println("${json2m}")
//	println("${json3m}")
//	println("${obj2m!!.p8}")
*/

/*
	val asMap = false
	val json = objSchema.deconstruct(input,DefaultFactory2, asMap)
	println("DEC1:  ${json}")
	val obj2 = objSchema.reconstruct(json, DefaultFactory2)
	println("DEC2:  ${objSchema.deconstruct(obj2,DefaultFactory2, asMap)}")
	println("${objSchema.equal(input, obj2)}")
//	objSchema.reconstruct(objSchema.deconstruct(input,DefaultFactory, true), DefaultFactory)
*/

/*
	val xmlA = objSchema.deconstruct(input, XmlFactory)
	val xmlM = objSchema.deconstruct(input, XmlFactory, true)
	println("${ objSchema.deconstruct(input,DefaultFactory, true)}")
	println("${xmlA}")
	println("${xmlM}")
*/

/*
	val json = """[0,10,"zzz",true,103,2.4,0.25,-26,27,40,[2,9],null,{"x":"pp","y":8.8},[3,30],1462630720414,["a","b","c"],[45,"AuO"],[555,223]]"""
	val jsonm = """{"id":0,"p0":10,"p1":"zzz","p2":true,"p3":103,"p4":2.4,"p5":0.25,"p6":-26,"p7":27,"p8":40,"p9":[2,9],"p10":null,"p11":{"x":"pp","y":8.8},"p12":[3,30],"p13":1462630720414,"p14":["a","b","c"],"p15":{"a0":45,"a1":"AuO"},"p16":[555,223]}"""
	val json2 = measureTime("input Default WRITE New", N) {
		objSchema.deconstruct(input, DefaultFactory)
	}
	val obj2 = measureTime("input Default READ New", N) {
		objSchema.reconstruct(json, DefaultFactory)
	}
	val json3 = measureTime("2 Json WRITE New", N) {
		objSchema.deconstruct(input, JsonFactory)
	}
	val obj3 = measureTime("2 Json READ New", N) {
		objSchema.reconstruct(json, JsonFactory)
	}
	val json2m = measureTime("input Default WRITE New", N) {
		objSchema.deconstruct(input, DefaultFactory, true)
	}
	val obj2m = measureTime("input Default READ New", N) {
		objSchema.reconstruct(jsonm, DefaultFactory)
	}
	println("input =? obj1 > ${input == obj2}; json1=?json2 > ${json == json2}")
	println("input =? obj3 > ${input == obj3}; json3=?json4 > ${json3 == json}")
	println("input =? obj2m > ${input == obj2m}")
	println("${json2}")
	println("${json3}")
	println("${json2m}")
	println("${obj2m!!.p8}")
	assert(json == json2)
	assert(json == json3)
*/


	/*
		println("${byteArrayOf(1).javaClass == ByteArrayType.typeKClass.java}")
	println("point0 to String: ${Point("has schema", 18.55f)}")// todo include in test
		val obj0 = ObjXt()
		println("ObjSchema props: ${objSchema.props.input { it.name }.joinToString()}")
		objSchema.p1.set(obj0, "direct")
		println("obj0:: $obj0")
		val input = Obj(0, "zzz", true, 0, 2.4f, 0.25, -26, 27, '\u0028', byteArrayOf(2, 9), Point("pp", 8.8f), mutableListOf(3, 30), Date(), arrayOf("a", "b", "c"), AutoObj(45, "AuO"), intArrayOf(555, 223))
		objSchema.props[2].set(input, "via props")
		println("input:: $input")
		//	//
		val jsonMapFactory = JsonMapFactory()
		val jsonArrayFactory = JsonArrayFactory()
		val jarr1 = objSchema.consume(input, jsonArrayFactory)
		println("JSON input > $jarr1")
		val jmap1 = objSchema.consume(input, jsonMapFactory)
		println("JSON input > $jmap1")
		val obj4 = objSchema.produce(jarr1, jsonArrayFactory)
		println("obj4:: $obj4")
		val obj5 = objSchema.produce(jmap1, jsonMapFactory)
		println("obj5:: $obj5")
		var json = """[123,10,"ok",true,23,2.4,0.25,26,27,"(","Agk=",null,["ppZZ",8.8],[3,30],1458923875432,["x","y","z"],[31,"Do12"],[215,775]]"""
		var obj6 = objSchema.produce(json, jsonArrayFactory)
		println("obj6:: $obj6")
		json = """[123,10,"ok",true,23,2.4,0.25,26,27,"(","Agk=",null,["ppPP",8.8,11],[3,30],1458923875432,["x","y","z"],[31,"Do12"],[215,775]]"""
		obj6 = objSchema.produce(json, jsonArrayFactory)
		println("obj6:: $obj6")
		// test extra
		objSchema.p0.description = "'Object id'"
		println("Id description= ${objSchema.p0.description}")
		//
		val obj7 = objSchema.produce(json, jsonArrayFactory)
		val obj8 = objSchema.copy(obj7, true)
		obj7!!.p9[0] = 21
		println("obj7:: $obj7")
		println("obj8:: $obj8")
		println("Equals 7, 8 ?  ${objSchema.equal(obj7, obj8)}")
		val obj9 = objSchema.copy(obj7, false)
		obj7!!.p9[0] = 22
		println("obj9:: $obj9")
		println("Equals 7, 9 ?  ${objSchema.equal(obj7, obj9)}")
		obj9!!.p9 = byteArrayOf(100, 101, 102, 103, 104, 105, 106, 107, 108, 108, 110, 111)
		println("Obj9:: ${objSchema.toString(obj9, 1)}")
		//
		val point0 = Point("point", 0.88f)
		println("point0 to String: $point0")
		val point0clone = point0.clone(true)
		println("point0 clone:: $point0clone")
		println("point0 isEqual::  ${point0 == point0clone}")
		MutableListType(IntType).asInstance(listOf(1, 2, 3))
		val json10 = """[123,10,"ok",true,103,2.4,0.25,26,27,"(",[100,101,102,103,104,105,106,107,108,108,110,111],null,{"x":"ppPP","y":8.8},[3,30],1458923875432,["x","y","z"],[31,"Do12"],[215,775]]"""
		val obj10 = objSchema.fromString(json10)
		println("obj10:: $obj10")
		println("${obj10 == objSchema.fromString(json10)}")
	*/
}


@Suppress("UNCHECKED_CAST")
var PropInfo.description: String?
	get() = (extra as? MutableMap<String, Any?>)?.get("description") as? String
	set(value) = run { (extra as? MutableMap<String, Any?>)?.set("description", value) }


/*DEFs*/
open class BaseObj<T : BaseObj<T>> : SchemaObject<T>() {
	var id = 0
}

open class Obj() : BaseObj<Obj>() {

	constructor(a0: Int, a1: String, a2: Boolean, a3: Long, a4: Float, a5: Double, a6: Short, a7: Byte, a8: Char, a9: ByteArray, a11: Point, a12: MutableList<Int>, a13: Date, a14: Array<String>, a15: AutoObj, a16: IntArray) : this() {
		p1 = a1; p2 = a2;/*p3 = a3;*/p4 = a4;p5 = a5;p6 = a6;p7 = a7;p8 = a8;p9 = a9;p11 = a11;p12 = a12;p13 = a13; p14 = a14; p15 = a15; p16 = a16
	}

	val p0: Int = 10
	var p1: String = "oops"
		private set(value) = run { field = value }
	var p2: Boolean? = false
	val p3: Long = 103L
	var p4: Float = 14f
	var p5: Double = 1.5
	var p6: Short = 106
	var p7: Byte = 0b00000000
	var p8: Char = '\u0108'
	var p9 = byteArrayOf(0, 1, 9)
	//	val p10 = Unit
	var p11: Point? = Point("x", 0.7f)
	var p12: List<Int>? = mutableListOf(12, 13, 14)
	var p13: Date? = null
	var p14: Array<String>? = null
	var p15: AutoObj = AutoObj(12, "au")
	var p16: IntArray? = null
}

class ObjXt : Obj()


abstract class BaseSchema<T : BaseObj<T>>(typeKClass: KClass<T>) : PropType.SCHEMAof<T>(typeKClass) {
	val id by PROP_of(INT, true)
}

class ObjSchema : BaseSchema<Obj>(Obj::class) {
	val p0 by PROP_Int(true)
	val p1 by PROP_String()
	val p2 by PROP_Boolean(true)
	val p3 by PROP_Long(true)
	val p4 by PROP_Float(true)
	val p5 by PROP_Double(true)
	val p6 by PROP_Short(true)
	val p7 by PROP_Byte(true)
	val p8 by PROP_Char(true)
	val p9 by PROP_Bytes(true)
	val p10 by PROP_STUB()
	val p11 by PROP_of(Point, true)
	val p12 by PROP_ListOf(INT, true)
	val p13 by PROP_of(DateType, true)
	val p14 by PROP_ArrayOf(STRING, true)
	val p15 by PROP_of(AUTOSCHEMAof<AutoObj>())
	val p16 by PROP_Ints(true)

	override fun onPropCreated(p: Prop<*>) {
		p.extra = mutableMapOf<String, Any>()
	}
}

class Point(var x: String, var y: Float) : SchemaObject<Point>() {
	//	override val schema: SchemaType<Point> = Point

	companion object : PropType.SCHEMAof<Point>(Point::class) {
		init {compact = false}
		override fun instance(): Point = Point("", 0f)
		val x by PROP_of(STRING)
		val y by PROP_of(FLOAT)
	}
}

object DateType : LONGof<Date>(Date::class) {
	override fun instance(): Date = Date()
	override fun fromValue(v: Long): Date? = Date().apply { time = v }
	override fun toValue(v: Date?): Long? = v?.time
}


class AutoObj(var a0: Int, var a1: String?)


class Aero {
	companion object : PropType.SCHEMAof<Aero>(Aero::class) {
		val seq = PROP_Ints()
	}

	var seq: IntArray? = null
}
