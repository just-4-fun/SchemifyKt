package just4fun.kotlin.schemify.test.specs

import just4fun.kotlin.schemify.core.AUTOSCHEMAof
import just4fun.kotlin.schemify.core.Prop
import just4fun.kotlin.schemify.core.PropType.LONGof
import just4fun.kotlin.schemify.core.PropType.SCHEMAof
import just4fun.kotlin.schemify.core.ReaderFactory
import just4fun.kotlin.schemify.core.WriterFactory
import just4fun.kotlin.schemify.production.DefaultFactory
import just4fun.kotlin.schemify.production.JsonFactory
import just4fun.kotlin.schemify.production.XmlFactory
import org.jetbrains.spek.api.On
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.shouldBeTrue
import org.jetbrains.spek.api.shouldEqual
import java.util.*
import kotlin.reflect.KClass


class TestProduction : Spek() { init {

	fun <O : Any, T : Any> test(on: On, obj: O, schema: SCHEMAof<O>, readerFactory: ReaderFactory<T>, writerFactory: WriterFactory<T>, info: String) {
		val prod1 = schema.write(obj, writerFactory, schema.writeAsSequence)
		val prod2 = schema.write(obj, writerFactory, !schema.writeAsSequence)
		println("Prod1\n$prod1")
		println("Prod2\n$prod2")
		val obj1 = schema.read(prod1!!, readerFactory)
		val obj2 = schema.read(prod2!!, readerFactory)
		on.it(info) {
			shouldBeTrue(schema.equal(obj, obj1!!))
			shouldBeTrue(schema.equal(obj, obj2))
			shouldBeTrue(schema.equal(obj1, obj2))
		}
	}

	given("Schema normal") {
		class Simple(val x: Int, var y: String)

		class DateType : LONGof<Date>(Date::class) {
			override fun instance(): Date = Date()
			override fun fromValue(v: Long): Date? = Date().apply { time = v }
			override fun toValue(v: Date?): Long? = v?.time
		}

		open class ObjBase {
			open var p0: Long = 100L
			open var p1: Int = 100
			open var p2: Short = 100
			open var p3: Byte = 100
			open var p4: Double? = 0.01
			open var p5: Float = 0.01f
			open var p6: Char = '\u1002'
			open var p7: Boolean = true
			open var p8: String? = "00Aa"
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
			var p24: MutableMap<String, Any?> = mutableMapOf("k0" to 22, "k1" to listOf("ok", "oops"), "k2" to arrayOf("x", "8"), "k3" to mapOf("a" to 12, 12 to "a"))
		}

		abstract class ObjSchema<O : ObjBase>(typeKClass: KClass<O>) : SCHEMAof<O>(typeKClass) {
			val p0 by PROP_Long()
			val p1 by PROP_Int()
			val p2 by PROP_Short()
			val p3 by PROP_Byte()
			val p4 by PROP_Double()
			val p5 by PROP_Float()
			val p6 by PROP_Char()
			val p7 by PROP_Boolean()
			val p8 by PROP_String().apply { alias = "p8_alias" }
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
			override fun onPropCreated(p: Prop<*>) {

			}
		}

		class ObjN : ObjBase()

		class ObjC(override var p0: Long, override var p1: Int, override var p2: Short, override var p3: Byte, override var p4: Double?, override var p5: Float, override var p6: Char, override var p7: Boolean, override var p8: String?) : ObjBase()

		class SchemaN : ObjSchema<ObjN>(ObjN::class)

		class SchemaC : ObjSchema<ObjC>(ObjC::class)

		val schN = SchemaN()
		val schC = SchemaC()
		val objN = ObjN()
		val objC = ObjC(201L, 202, 203, 104, 20.5, 20.6f, 'S', true, "manual")

		on("Default Factoy") {
			val factory = DefaultFactory
			test(this, objN, schN, factory, factory, "without constructor")
			test(this, objC, schC, factory, factory, "with constructor")
		}
		on("Json Factoy") {
			val factory = JsonFactory
			test(this, objN, schN, factory, factory, "without constructor")
			test(this, objC, schC, factory, factory, "with constructor")
		}
		on("XML Factoy") {
			val factory = XmlFactory
			test(this, objN, schN, factory, factory, "without constructor")
			test(this, objC, schC, factory, factory, "with constructor")
		}
	}

	given("Schema empty") {
		class Obj

		val sch = object : SCHEMAof<Obj>(Obj::class)  {}
		val obj = Obj()

		on("Default Factoy") {
			val factory = DefaultFactory
			test(this, obj, sch, factory, factory, "")
		}
		on("Json Factoy") {
			val factory = JsonFactory
			test(this, obj, sch, factory, factory, "")
		}
		on("XML Factoy") {
			val factory = XmlFactory
			test(this, obj, sch, factory, factory, "")
		}
	}
}
	// todo test extreme cases (empty schema/seq etc)
	// todo test Sequence read/write
}
