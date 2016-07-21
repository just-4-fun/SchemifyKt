package just4fun.kotlin.schemify.test.specs

import just4fun.kotlin.schemify.core.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.shouldBeFalse
import org.jetbrains.spek.api.shouldBeTrue
import java.util.*
import kotlin.reflect.KClass
import just4fun.kotlin.schemify.core.PropType.*


class TestEqualty : Spek() { init {
	given("Test types equality") {
		class DateType : LONGof<Date>(Date::class) {
			override fun instance(): Date = Date()
			override fun fromValue(v: Long): Date? = Date().apply { time = v }
			override fun toValue(v: Date?): Long? = v?.time
		}
		on("LongType") {
			val type = LONG
			it("should be ok") {
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal(100L, 100L))
				shouldBeFalse(type.equal(100L, null))
				shouldBeFalse(type.equal(null, 100L))
				shouldBeFalse(type.equal(-100L, 100L))
				shouldBeFalse(type.equal(-100L, 100L))
			}
		}
		on("IntType") {
			val type = INT
			it("should be ok") {
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal(100, 100))
				shouldBeFalse(type.equal(100, null))
				shouldBeFalse(type.equal(null, 100))
				shouldBeFalse(type.equal(-100, 100))
			}
		}
		on("ShortType") {
			val type = SHORT
			it("should be ok") {
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal(100, 100))
				shouldBeFalse(type.equal(100, null))
				shouldBeFalse(type.equal(null, 100))
				shouldBeFalse(type.equal(-100, 100))
			}
		}
		on("ByteType") {
			val type = BYTE
			it("should be ok") {
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal(100, 100))
				shouldBeFalse(type.equal(100, null))
				shouldBeFalse(type.equal(null, 100))
				shouldBeFalse(type.equal(-100, 100))
			}
		}
		on("DoubleType") {
			val type = DOUBLE
			it("should be ok") {
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal(100.0, 100.0))
				shouldBeFalse(type.equal(100.0, null))
				shouldBeFalse(type.equal(null, 100.0))
				shouldBeFalse(type.equal(-100.0, 100.0))
			}
		}
		on("FloatType") {
			val type = FLOAT
			it("should be ok") {
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal(100.0f, 100.0f))
				shouldBeFalse(type.equal(100.0f, null))
				shouldBeFalse(type.equal(null, 100.0f))
				shouldBeFalse(type.equal(-100.0f, 100.0f))
			}
		}
		on("CharType") {
			val type = CHAR
			it("should be ok") {
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal('a', 'a'))
				shouldBeFalse(type.equal('a', null))
				shouldBeFalse(type.equal(null, 'a'))
				shouldBeFalse(type.equal('a', 'A'))
			}
		}
		on("BooleanType") {
			val type = BOOLEAN
			it("should be ok") {
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal(true, true))
				shouldBeTrue(type.equal(false, false))
				shouldBeFalse(type.equal(true, null))
				shouldBeFalse(type.equal(null, false))
				shouldBeFalse(type.equal(true, false))
			}
		}
		on("StringType") {
			val type = STRING
			it("should be ok") {
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal("true", "true"))
				shouldBeTrue(type.equal("", ""))
				shouldBeFalse(type.equal("true", null))
				shouldBeFalse(type.equal(null, "false"))
				shouldBeFalse(type.equal("true", "false"))
			}
		}
		on("ArrayType") {
			val type = ARRAYof(STRING)
			it("should be ok") {
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal(arrayOf("true", "false"), arrayOf("true", "false")))
				shouldBeTrue(type.equal(arrayOf(), arrayOf()))
				shouldBeFalse(type.equal(arrayOf("true", "false"), null))
				shouldBeFalse(type.equal(null, arrayOf("true", "false")))
				shouldBeFalse(type.equal(arrayOf("true", "false"), arrayOf("false", "true")))
			}
		}
		on("LongArrayType") {
			val type = LONGS
			it("should be ok") {
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal(longArrayOf(100, -100), longArrayOf(100, -100)))
				shouldBeTrue(type.equal(longArrayOf(), longArrayOf()))
				shouldBeFalse(type.equal(longArrayOf(100, -100), null))
				shouldBeFalse(type.equal(null, longArrayOf(100, -100)))
				shouldBeFalse(type.equal(longArrayOf(100, -100), longArrayOf(-100, 100)))
			}
		}
		on("IntArrayType") {
			val type = INTS
			it("should be ok") {
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal(intArrayOf(100, -100), intArrayOf(100, -100)))
				shouldBeTrue(type.equal(intArrayOf(), intArrayOf()))
				shouldBeFalse(type.equal(intArrayOf(100, -100), null))
				shouldBeFalse(type.equal(null, intArrayOf(100, -100)))
				shouldBeFalse(type.equal(intArrayOf(100, -100), intArrayOf(-100, 100)))
			}
		}
		on("ShortArrayType") {
			val type = SHORTS
			it("should be ok") {
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal(shortArrayOf(100, -100), shortArrayOf(100, -100)))
				shouldBeTrue(type.equal(shortArrayOf(), shortArrayOf()))
				shouldBeFalse(type.equal(shortArrayOf(100, -100), null))
				shouldBeFalse(type.equal(null, shortArrayOf(100, -100)))
				shouldBeFalse(type.equal(shortArrayOf(100, -100), shortArrayOf(-100, 100)))
			}
		}
		on("ByteArrayType") {
			val type = BYTES
			it("should be ok") {
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal(byteArrayOf(100, -100), byteArrayOf(100, -100)))
				shouldBeTrue(type.equal(byteArrayOf(), byteArrayOf()))
				shouldBeFalse(type.equal(byteArrayOf(100, -100), null))
				shouldBeFalse(type.equal(null, byteArrayOf(100, -100)))
				shouldBeFalse(type.equal(byteArrayOf(100, -100), byteArrayOf(-100, 100)))
			}
		}
		on("DoubleArrayType") {
			val type = DOUBLES
			it("should be ok") {
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal(doubleArrayOf(100.0, -100.0), doubleArrayOf(100.0, -100.0)))
				shouldBeTrue(type.equal(doubleArrayOf(), doubleArrayOf()))
				shouldBeFalse(type.equal(doubleArrayOf(100.0, -100.0), null))
				shouldBeFalse(type.equal(null, doubleArrayOf(100.0, -100.0)))
				shouldBeFalse(type.equal(doubleArrayOf(100.0, -100.0), doubleArrayOf(-100.0, 100.0)))
			}
		}
		on("FloatArrayType") {
			val type = FLOATS
			it("should be ok") {
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal(floatArrayOf(100f, -100f), floatArrayOf(100f, -100f)))
				shouldBeTrue(type.equal(floatArrayOf(), floatArrayOf()))
				shouldBeFalse(type.equal(floatArrayOf(100f, -100f), null))
				shouldBeFalse(type.equal(null, floatArrayOf(100f, -100f)))
				shouldBeFalse(type.equal(floatArrayOf(100f, -100f), floatArrayOf(-100f, 100f)))
			}
		}
		on("CharArrayType") {
			val type = CHARS
			it("should be ok") {
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal(charArrayOf('a', 'A'), charArrayOf('a', 'A')))
				shouldBeTrue(type.equal(charArrayOf(), charArrayOf()))
				shouldBeFalse(type.equal(charArrayOf('a', 'A'), null))
				shouldBeFalse(type.equal(null, charArrayOf('a', 'A')))
				shouldBeFalse(type.equal(charArrayOf('a', 'A'), charArrayOf('A', 'a')))
			}
		}
		on("BooleanArrayType") {
			val type = BOOLEANS
			it("should be ok") {
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal(booleanArrayOf(true, false), booleanArrayOf(true, false)))
				shouldBeTrue(type.equal(booleanArrayOf(), booleanArrayOf()))
				shouldBeFalse(type.equal(booleanArrayOf(true, false), null))
				shouldBeFalse(type.equal(null, booleanArrayOf(true, false)))
				shouldBeFalse(type.equal(booleanArrayOf(true, false), booleanArrayOf(false, true)))
			}
		}
		on("ListType") {
			val type = LISTof(STRING)
			it("should be ok") {
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal(listOf("true", "false"), listOf("true", "false")))
				shouldBeTrue(type.equal(listOf(), listOf()))
				shouldBeFalse(type.equal(listOf("true", "false"), null))
				shouldBeFalse(type.equal(null, listOf("true", "false")))
				shouldBeFalse(type.equal(listOf("true", "false"), listOf("false", "true")))
			}
		}
		on("MutableListType") {
			val type = MLISTof(STRING)
			it("should be ok") {
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal(mutableListOf("true", "false"), mutableListOf("true", "false")))
				shouldBeTrue(type.equal(mutableListOf(), mutableListOf()))
				shouldBeFalse(type.equal(mutableListOf("true", "false"), null))
				shouldBeFalse(type.equal(null, mutableListOf("true", "false")))
				shouldBeFalse(type.equal(mutableListOf("true", "false"), mutableListOf("false", "true")))
			}
		}
		on("SetType") {
			val type = SETof(STRING)
			it("should be ok") {
				shouldBeTrue(setOf("true", "true", "false").equals(setOf("false", "false", "true")))
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal(setOf("true", "true", "false"), setOf("false", "false", "true")))
				shouldBeTrue(type.equal(setOf(), setOf()))
				shouldBeFalse(type.equal(setOf("true", "false"), null))
				shouldBeFalse(type.equal(null, setOf("true", "false")))
				shouldBeFalse(type.equal(setOf("true", "true"), setOf("false", "true")))
			}
		}
		on("MutableSetType") {
			val type = MSETof(STRING)
			it("should be ok") {
				shouldBeTrue(mutableSetOf("true", "true", "false").equals(mutableSetOf("false", "false", "true")))
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal(mutableSetOf("true", "true", "false"), mutableSetOf("false", "false", "true")))
				shouldBeTrue(type.equal(mutableSetOf(), mutableSetOf()))
				shouldBeFalse(type.equal(mutableSetOf("true", "false"), null))
				shouldBeFalse(type.equal(null, mutableSetOf("true", "false")))
				shouldBeFalse(type.equal(mutableSetOf("true", "true"), mutableSetOf("false", "true")))
			}
		}
		on("DateType as ValueType") {
			val type = DateType()
			it("should be ok") {
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal(Date(1000000000L), Date(1000000000L)))
				shouldBeTrue(type.equal(Date(), Date()))
				shouldBeFalse(type.equal(Date(1000000000L), null))
				shouldBeFalse(type.equal(null, Date(1000000000L)))
				shouldBeFalse(type.equal(Date(1111111111L), Date(2222222222L)))
			}
		}
		on("RawCollectionType") {
			val type = COLLECTION
			it("should be ok") {
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal(listOf("v0", "v1"), mutableListOf("v0", "v1")))
				shouldBeTrue(type.equal(listOf("v0", "v1"), type.asInstance(arrayOf("v0", "v1"))))
				shouldBeTrue(type.equal(listOf<Any?>(), listOf<Any?>()))
				shouldBeFalse(type.equal(listOf("v0", "v1"), null))
				shouldBeFalse(type.equal(null, listOf("v0", "v1")))
				shouldBeFalse(type.equal(listOf("k0", "v0"), listOf("k0", "v1")))
				shouldBeFalse(type.equal(listOf("k0", "v0"), listOf("k0", "v0", "k1", 12)))
			}
		}
		on("RawArrayType") {
			val type = ARRAY
			it("should be ok") {
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal(arrayOf("v0", "v1"), arrayOf("v0", "v1")))
				shouldBeTrue(type.equal(arrayOf("v0", "v1"), type.asInstance(listOf("v0", "v1"))))
				shouldBeTrue(type.equal(arrayOf<Any?>(), arrayOf<Any?>()))
				shouldBeFalse(type.equal(arrayOf("v0", "v1"), null))
				shouldBeFalse(type.equal(null, arrayOf("v0", "v1")))
				shouldBeFalse(type.equal(arrayOf("k0", "v0"), arrayOf("k0", "v1")))
				shouldBeFalse(type.equal(arrayOf("k0", "v0"), arrayOf("k0", "v0", "k1", 12)))
			}
		}
		on("RawMapType") {
			val type = MAP
			it("should be ok") {
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal(mutableMapOf("key" to "value"), mutableMapOf("key" to "value")))
				shouldBeTrue(type.equal(mutableMapOf(), mutableMapOf()))
				shouldBeFalse(type.equal(mutableMapOf("key" to "value"), null))
				shouldBeFalse(type.equal(null, mutableMapOf("key" to "value")))
				shouldBeFalse(type.equal(mutableMapOf("k0" to "v0"), mutableMapOf("k0" to "v1")))
				shouldBeFalse(type.equal(mutableMapOf("k0" to "v0"), mutableMapOf("k0" to "v0", "k1" to 12)))
			}
		}
		on("SchemaType") {
			class Simple(val x: Int, var y: String)
			
			class Obj {
				var p0: Long = 100L
				var p1: Int = 100
				var p2: Short = 100
				var p3: Byte = 100
				var p4: Double? = 0.01
				var p5: Float = 0.01f
				var p6: Char = '\u1002'
				var p7: Boolean = true
				var p8: String? = "00Aa"
				//	var p9: STUB
				var p10: Array<String> = arrayOf("qwer", "1234")
				var p11: ByteArray = byteArrayOf(1, 2, 3)
				var p12: BooleanArray = booleanArrayOf(true, false, true)
				var p13: List<Char>? = listOf('a', 'A', '0')
				var p14: MutableList<Float> = mutableListOf(0.12f, 12.0f)
				var p15: Set<Double>? = setOf(123.01, 0.123)
				var p16: MutableSet<Double> = mutableSetOf(123.01, 0.123)
				var p17: Date = Date(1234567890)
				var p18: List<Date>? = listOf(Date(1234), Date(5678))
				var p19: Array<Array<String>> = arrayOf(arrayOf("asd", "123"), arrayOf("xyz", "555"))
				var p20: Simple = Simple(123, "ABC")
				var p21: MutableSet<Simple>? = mutableSetOf(Simple(123, "ABC"), Simple(344, "XZY"))
				var p22: Collection<Any?> = mutableListOf(123, "abc", listOf("ok", "oops"), arrayOf("x", "8"), mapOf("a" to 12, 12 to "a"))
				var p23: Array<Any?>? = arrayOf(123, "abc", listOf("ok", "oops"), arrayOf("x", "8"), mapOf("a" to 12, 12 to "a"))
				var p24: MutableMap<String, Any?> = mutableMapOf("k0" to  22, "k1" to listOf("ok", "oops"), "k2" to arrayOf("x", "8"), "k3" to mapOf("a" to 12, 12 to "a"))
			}

			class ObjSchema : SCHEMAof<Obj>(Obj::class) {
				val p0 by PROP_Long()
				val p1 by PROP_Int()
				val p2 by PROP_Short()
				val p3 by PROP_Byte()
				val p4 by PROP_Double()
				val p5 by PROP_Float()
				val p6 by PROP_Char()
				val p7 by PROP_Boolean()
				val p8 by PROP_String()
				val p9 by PROP_STUB()
				val p10 by PROP_ArrayOf(STRING)
				val p11 by PROP_Bytes()
				val p12 by PROP_Booleans()
				val p13 by PROP_ListOf(CHAR)
				val p14 by PROP_MutableListOf(FLOAT)
				val p15 by PROP_SetOf(DOUBLE)
				val p16 by PROP_MutableSetOf(DOUBLE)
				val p17 by PROP_of(DateType())
				val p18 by PROP_ListOf(DateType())
				val p19 by PROP_ArrayOf(ARRAYof(STRING))
				val p20 by PROP_AutoSchemaOf<Simple>()
				val p21 by PROP_MutableSetOf(AUTOSCHEMAof<Simple>())
				val p22 by PROP_Collection()
				val p23 by PROP_Array()
				val p24 by PROP_Map()
			}

			val type = ObjSchema()
			val obj = Obj()
			val obj0 = Obj().apply { p0 = 0 }
			val obj1 = Obj().apply { p1 = 0 }
			val obj2 = Obj().apply { p2 = 0 }
			val obj3 = Obj().apply { p3 = 0 }
			val obj4 = Obj().apply { p4 = 0.0 }
			val obj5 = Obj().apply { p5 = 0f }
			val obj6 = Obj().apply { p6 = '0' }
			val obj7 = Obj().apply { p7 = false }
			val obj8 = Obj().apply { p8 = "" }
			//			val obj9 = Obj().apply { p9 =  }
			val obj10 = Obj().apply { p10[1] = "WWW" }
			val obj11 = Obj().apply { p11[1] = 22 }
			val obj12 = Obj().apply { p12[1] = true }
			val obj13 = Obj().apply { p13 = listOf('Q', 'W', 'E') }
			val obj14 = Obj().apply { p14[1] = 100.1f }
			val obj15 = Obj().apply { p15 = setOf(1.0, 2.2, 3.3) }
			val obj16 = Obj().apply { p16.add(0.0001) }
			val obj17 = Obj().apply { p17.time = 888888888L }
			val obj18 = Obj().apply { p18 = listOf(Date(), Date()) }
			val obj19 = Obj().apply { p19!![0][0] = "oops" }
			val obj20 = Obj().apply { p20.y = "blabla" }
			val obj21 = Obj().apply { p21!!.add(Simple(21, "XC5")) }
			val obj22 = Obj().apply { p22 = p22.plus(Simple(21, "XC5")) }
			val obj23 = Obj().apply { p23!![0] = Simple(21, "XC5") }
			val obj24 = Obj().apply { p24.put("k2", "v22") }

			it("should be ok") {
				shouldBeTrue(type.equal(null, null))
				shouldBeTrue(type.equal(Obj(), Obj()))
				shouldBeFalse(type.equal(Obj(), null))
				shouldBeFalse(type.equal(null, Obj()))
				shouldBeFalse(type.equal(obj, obj0))
				shouldBeFalse(type.equal(obj, obj1))
				shouldBeFalse(type.equal(obj, obj2))
				shouldBeFalse(type.equal(obj, obj3))
				shouldBeFalse(type.equal(obj, obj4))
				shouldBeFalse(type.equal(obj, obj5))
				shouldBeFalse(type.equal(obj, obj6))
				shouldBeFalse(type.equal(obj, obj7))
				shouldBeFalse(type.equal(obj, obj8))
				//				shouldBeFalse(type.equal(input, obj9))
				shouldBeFalse(type.equal(obj, obj10))
				shouldBeFalse(type.equal(obj, obj11))
				shouldBeFalse(type.equal(obj, obj12))
				shouldBeFalse(type.equal(obj, obj13))
				shouldBeFalse(type.equal(obj, obj14))
				shouldBeFalse(type.equal(obj, obj15))
				shouldBeFalse(type.equal(obj, obj16))
				shouldBeFalse(type.equal(obj, obj17))
				shouldBeFalse(type.equal(obj, obj18))
				shouldBeFalse(type.equal(obj, obj19))
				shouldBeFalse(type.equal(obj, obj20))
				shouldBeFalse(type.equal(obj, obj21))
				shouldBeFalse(type.equal(obj, obj22))
				shouldBeFalse(type.equal(obj, obj23))
				shouldBeFalse(type.equal(obj, obj24))
			}
			it("should be ok if cloning") {
				shouldBeTrue(type.equal(Obj(), type.copy(Obj())))
			}
		}
	}
}
}
