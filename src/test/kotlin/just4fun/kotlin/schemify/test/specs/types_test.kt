package just4fun.kotlin.schemify.test.specs


import just4fun.kotlin.schemify.core.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.shouldEqual
import org.jetbrains.spek.api.shouldBeTrue
import org.jetbrains.spek.api.shouldThrow
import java.util.*
import just4fun.kotlin.schemify.core.PropType
import just4fun.kotlin.schemify.core.PropType.*


class TestTypeCast : Spek() { init {
	given("Types") {
		// todo stub type
		on("String to Number converter"){
			it("should be ok"){
				shouldEqual(-0.0, "-0".convert())
				shouldEqual(100.0, "100".convert())
				shouldEqual(-100.0, "-100".convert())
				shouldEqual(-100.0, "-100.0".convert())
				shouldEqual(-100.001, "-100.001".convert())
				shouldEqual(100.0, " 100".convert())
				shouldEqual(100.0, "100 ".convert())
				shouldEqual(100.0, " 100 ".convert())
				shouldEqual(100.0, "bla100bla".convert())
				shouldEqual(-100.0, "-bla-100bla123".convert())
				shouldEqual(100.0, "bla- 100 .01bla ".convert())
				shouldEqual(100.0, " +100 ".convert())
				shouldEqual(100.0, " 100. 01 ".convert())
				shouldEqual(100.1, " 100.100 ".convert())
				shouldEqual(100.1, " 100,1 ".convert())
			}
		}
		on("eval types"){
			it("Any to Long") {
				val type = LONG
				shouldEqual(-100.001.toLong(), type.asInstance("bla-100.001bla"))
				shouldEqual(1000000000000000000L.toLong(), type.asInstance(1000000000000000000L))
				shouldEqual(1000000000.toLong(), type.asInstance(1000000000))
				shouldEqual(10000000000.000000000001.toLong(), type.asInstance(10000000000.000000000001))
				shouldEqual(.00000001f.toLong(), type.asInstance(.00000001f))
				shouldEqual(10000.toLong(), type.asInstance(10000.toShort()))
				shouldEqual(100.toLong(), type.asInstance(100.toByte()))
				shouldEqual('\u1234'.toLong(), type.asInstance('\u1234'))
				shouldEqual(0, type.asInstance(false))
				shouldEqual(1, type.asInstance(true))
				shouldEqual(0, type.asInstance("bla"))
				shouldEqual(0, type.asInstance(Object()))
			}
			it("Any to int") {
				val type = INT
				shouldEqual(-100.001.toInt(), type.asInstance("bla-100.001bla"))
				shouldEqual(1000000000000000000L.toInt(), type.asInstance(1000000000000000000L))
				shouldEqual(1000000000.toInt(), type.asInstance(1000000000))
				shouldEqual(10000000000.000000000001.toInt(), type.asInstance(10000000000.000000000001))
				shouldEqual(.00000001f.toInt(), type.asInstance(.00000001f))
				shouldEqual(10000.toInt(), type.asInstance(10000.toShort()))
				shouldEqual(100.toInt(), type.asInstance(100.toByte()))
				shouldEqual('\u1234'.toInt(), type.asInstance('\u1234'))
				shouldEqual(0, type.asInstance(false))
				shouldEqual(1, type.asInstance(true))
				shouldEqual(0, type.asInstance("bla"))
				shouldEqual(0, type.asInstance(Object()))
			}
			it("Any to Double") {
				val type = DOUBLE
				shouldEqual(-100.001.toDouble(), type.asInstance("bla-100.001bla"))
				shouldEqual(1000000000000000000L.toDouble(), type.asInstance(1000000000000000000L))
				shouldEqual(1000000000.toDouble(), type.asInstance(1000000000))
				shouldEqual(10000000000.000000000001.toDouble(), type.asInstance(10000000000.000000000001))
				shouldEqual(.00000001f.toDouble(), type.asInstance(.00000001f))
				shouldEqual(10000.toDouble(), type.asInstance(10000.toShort()))
				shouldEqual(100.toDouble(), type.asInstance(100.toByte()))
				shouldEqual('\u1234'.toDouble(), type.asInstance('\u1234'))
				shouldEqual(0.0, type.asInstance(false))
				shouldEqual(1.0, type.asInstance(true))
				shouldEqual(0.0, type.asInstance("bla"))
				shouldEqual(0.0, type.asInstance(Object()))
			}
			it("Any to Float") {
				val type = FLOAT
				shouldEqual(-100.001.toFloat(), type.asInstance("bla-100.001bla"))
				shouldEqual(1000000000000000000L.toFloat(), type.asInstance(1000000000000000000L))
				shouldEqual(1000000000.toFloat(), type.asInstance(1000000000))
				shouldEqual(10000000000.000000000001.toFloat(), type.asInstance(10000000000.000000000001))
				shouldEqual(.00000001f.toFloat(), type.asInstance(.00000001f))
				shouldEqual(10000.toFloat(), type.asInstance(10000.toShort()))
				shouldEqual(100.toFloat(), type.asInstance(100.toByte()))
				shouldEqual('\u1234'.toFloat(), type.asInstance('\u1234'))
				shouldEqual(0.0f, type.asInstance(false))
				shouldEqual(1.0f, type.asInstance(true))
				shouldEqual(0.0f, type.asInstance("bla"))
				shouldEqual(0.0f, type.asInstance(Object()))
			}
			it("Any to Short") {
				val type = SHORT
				//				shouldThrow(Throwable::class.java){-100.001.toShort() == type.eval("bla-100.001bla")} // glitch
				shouldEqual(-100, type.asInstance("bla-100.001bla"))
				shouldEqual(100.001.toShort(), type.asInstance("bla100.001bla"))
				shouldEqual(1000000000000000000L.toShort(), type.asInstance(1000000000000000000L))
				shouldEqual(1000000000.toShort(), type.asInstance(1000000000))
				shouldEqual(10000000000.000000000001.toShort(), type.asInstance(10000000000.000000000001))
				shouldEqual(.00000001f.toShort(), type.asInstance(.00000001f))
				shouldEqual(10000.toShort(), type.asInstance(10000.toShort()))
				shouldEqual(100.toShort(), type.asInstance(100.toByte()))
				shouldEqual('\u1234'.toShort(), type.asInstance('\u1234'))
				shouldEqual(0, type.asInstance(false))
				shouldEqual(1, type.asInstance(true))
				shouldEqual(0, type.asInstance("bla"))
				shouldEqual(0, type.asInstance(Object()))
			}
			it("Any to Byte") {
				val type = BYTE
				//				shouldThrow(Throwable::class.java){-100.001.toByte() == type.eval("bla-100.001bla")} // glitch
				shouldEqual(100.001.toByte(), type.asInstance("bla100.001bla"))
				shouldEqual(1000000000000000000L.toByte(), type.asInstance(1000000000000000000L))
				shouldEqual(1000000000.toByte(), type.asInstance(1000000000))
				shouldEqual(10000000000.000000000001.toByte(), type.asInstance(10000000000.000000000001))
				shouldEqual(.00000001f.toByte(), type.asInstance(.00000001f))
				shouldEqual(10000.toByte(), type.asInstance(10000.toShort()))
				shouldEqual(100.toByte(), type.asInstance(100.toByte()))
				shouldEqual('\u1234'.toByte(), type.asInstance('\u1234'))
				shouldEqual(0, type.asInstance(false))
				shouldEqual(1, type.asInstance(true))
				shouldEqual(0, type.asInstance("bla"))
				shouldEqual(0, type.asInstance(Object()))
			}
			it("Any to Char") {
				val type = CHAR
				shouldEqual(1000000000000000000L.toChar(), type.asInstance(1000000000000000000L))
				shouldEqual(1000000000.toChar(), type.asInstance(1000000000))
				shouldEqual(10000000000.000000000001.toChar(), type.asInstance(10000000000.000000000001))
				shouldEqual(.00000001f.toChar(), type.asInstance(.00000001f))
				shouldEqual(10000.toChar(), type.asInstance(10000.toShort()))
				shouldEqual(100.toChar(), type.asInstance(100.toByte()))
				shouldEqual('\u1234'.toChar(), type.asInstance('\u1234'))
				shouldEqual('0', type.asInstance(false))
				shouldEqual('1', type.asInstance(true))
				shouldEqual('b', type.asInstance("bla"))
				shouldEqual('\u0000', type.asInstance(Object()))
			}
			it("Any to Boolean") {
				val type = BOOLEAN
				shouldEqual(true, type.asInstance(10L))
				shouldEqual(false, type.asInstance(0.0))
				shouldEqual(false, type.asInstance("false"))
				shouldEqual(true, type.asInstance("true"))
				shouldEqual(true, type.asInstance('a'))
				shouldEqual(false, type.asInstance('0'))
				shouldEqual(false, type.asInstance(""))
				shouldEqual(false, type.asInstance("0"))
				shouldEqual(false, type.asInstance("0.0"))
				shouldEqual(false, type.asInstance("0,0"))
				shouldEqual(false, type.asInstance("null"))

				shouldEqual(true, type.asInstance("Nop"))
				BOOLEAN.falseLike =  listOf("", "0", "null", "0.0", "0,0", "Nop")
				shouldEqual(false, type.asInstance("Nop"))
			}
			it("Any to ByteArray") {
				val type = BYTES
				shouldEqual(null, type.asInstance(100))
				shouldEqual(null, type.asInstance("100"))
				shouldEqual(null, type.asInstance(true))
				shouldBeTrue(type.asInstance(byteArrayOf(1, 2, 3))!!.toList().containsAll(byteArrayOf(1, 2, 3).toList()))
				shouldBeTrue(type.asInstance(arrayOf(1, 2, 3))!!.toList().containsAll(byteArrayOf(1, 2, 3).toList()))
				shouldBeTrue(type.asInstance(listOf(1, 2, 3))!!.toList().containsAll(byteArrayOf(1, 2, 3).toList()))
				shouldBeTrue(type.asInstance(listOf(1.1f, 2.2, 3L))!!.toList().containsAll(byteArrayOf(1, 2, 3).toList()))
				shouldBeTrue(type.asInstance(listOf("1", "2", "3"))!!.toList().containsAll(byteArrayOf(1, 2, 3).toList()))
			}
			it("Any to Long Value type") {
				class DateType : LONGof<Date>(Date::class) {
					override fun instance(): Date = Date()
					override fun fromValue(v: Long): Date? = Date().apply { time = v }
					override fun toValue(v: Date?): Long? = v?.time
				}
				val type = DateType()
				shouldEqual(Date(1458923875432), type.asInstance(1458923875432))
				shouldEqual(Date(1458923875432), type.asInstance("1458923875432"))
				shouldEqual(Date(1), type.asInstance(true))
			}
			// todo
			it("Any to raw ARRAY") {
				val type = ARRAY
			}
			// todo
			it("Any to raw COLLECTION") {
				val type = COLLECTION
			}
			// todo
			it("Any to raw MAP") {
				val type = MAP
			}
			// todo
			it("Any to ARRAY of") {
				val type = ARRAYof(INT)
			}
			it("Any to Collection type") {// todo more cases
				val type = MLISTof(INT)
				shouldEqual(mutableListOf(1, 2, 3), type.asInstance(mutableListOf(1, 2, 3)))
				shouldEqual(mutableListOf(1, 2, 3), type.asInstance(mutableListOf("1", "2", "3")))
				shouldEqual(mutableListOf(1, 2, 3), type.asInstance(listOf(1, 2, 3)))
				shouldEqual(mutableListOf(1, 2, 3), type.asInstance(arrayOf(1, 2, 3)))
				shouldEqual(mutableListOf(1, 2, 3, 0), type.asInstance(arrayOf(1, 2, 3, null)))
				shouldEqual(mutableListOf(1, 2, 3), type.asInstance(setOf("1", "2", "3")))
			}
			it("Any to Schema type") {// todo more cases
				open class Obj {
					var p1: Int = 10
					var p0: Int = 1
				}

				class ObjX : Obj()

				class ObjSchema : SCHEMAof<Obj>(Obj::class) {
					val p0 by PROP_of(INT)
					val p1 by PROP_of(INT)
				}

				val type = ObjSchema()
				val obj = type.instance()
				val objx = ObjX()
				shouldEqual(obj, type.asInstance(obj))
				shouldEqual(objx, type.asInstance(objx))
			}
		}
	}
}
}


fun String.convert(): Double = PropType.string2number(this, String::toDouble, Double::toDouble)
