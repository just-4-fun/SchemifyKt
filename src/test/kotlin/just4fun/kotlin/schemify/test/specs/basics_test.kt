package just4fun.kotlin.schemify.test.specs


import just4fun.kotlin.schemify.core.*
import org.jetbrains.spek.api.*
import java.util.*
import kotlin.reflect.KClass
import org.junit.Assert.*
import just4fun.kotlin.schemify.core.PropType.*
import just4fun.kotlin.schemify.production.DefaultFactory


class TestBasics : Spek() { init {
	given("Schema") {
		class Obj {
			var p0: Int = 0
			var p1: Int = 0
			var p2: Int? = null
			val p3: Int = 0
			val p3_1: Int = 0
			var p3_2: Int = 0
			var p4: Int = 0
				get() = run { field + 100 }
			var p5: Int = 0
				get() = run { field + 100 }
			private var p6: Int = 0
			val pStub: Unit = Unit
		}

		class ObjSchema : SCHEMAof<Obj>(Obj::class) {
			val p0 = PROP_of(INT)
			val p1 by PROP_of(INT)
			val p2 by PROP_of(INT)
			val p3 by PROP_of(INT)
			val p3_1 by PROP_of(INT, writeProtected = false)
			val p3_2 by PROP_of(INT, writeProtected = true)
			val p4 by PROP_of(INT)
			val p5 by PROP_of(INT, true)
			val p6 by PROP_of(INT)
			val pStub by PROP_STUB()
			val pLost by PROP_of(INT)
		}

		val schema = ObjSchema()
		val obj = Obj()

		/*basic rules*/

		on("Incompatible schema prop and matching object property types") {
			it("Should throw Exception") {
				shouldThrow(Exception::class.java) {
					class Obj {
						var p0: Int = 0
					}

					class ObjSchema : SCHEMAof<Obj>(Obj::class) {
						val p0 = PROP_of(LONG)
					}
					ObjSchema().init()
				}
			}
		}
		on("Schema prop VS object property: sequence nullable VS non-nullable element types") {
			it("Should throw Exception") {
				shouldThrow(Exception::class.java) {
					class Obj {
						var p0: MutableList<Int?> = mutableListOf()
					}

					class ObjSchema : SCHEMAof<Obj>(Obj::class) {
						val p0 = PROP_of(MLISTof(INT))
					}
					ObjSchema().init()
				}
			}
		}
		on("Empty schema") {
			it("Should be ok") {
				class Obj
				class ObjSchema : SCHEMAof<Obj>(Obj::class)
				ObjSchema().init()
			}
		}
		on("SchemaObject instance created before schema") {
			class Obj : SchemaObject<Obj>() {
				val p0: Int = 10
				var p1: String? = null
			}

			class ObjSchema : SCHEMAof<Obj>(Obj::class) {
				val p0 by PROP_of(INT, true)
				val p1 by PROP_of(STRING)
			}
			it("fails") {
				shouldThrow(Exception::class.java) {
					// schema instantiation should go before there !!!
					val obj = Obj()
				}
			}
		}
		on("Position of property in object does not affect position of schema prop") {
			class Obj {
				// swapping doesn't matter
				var p1: Int = 10
				var p0: Int = 1
			}

			class ObjSchema : SCHEMAof<Obj>(Obj::class) {
				// swapping matters
				val p0 by PROP_of(INT)
				val p1 by PROP_of(INT)
			}

			val schema = ObjSchema()
			val obj = schema.instance()
			it("Shoul have initial values") {
				shouldEqual(1, schema.p0.get(obj))
				shouldEqual(10, schema.p1.get(obj))
			}
		}
		on("Set prop p0 assigned with '='") {
			schema.p0.set(obj, 1)
			it("Got null. As prop is not yet inited") { shouldEqual(null, schema.p0.get(obj)) }
		}
		on("Set prop p1 assigned with 'by'") {
			schema.p1.set(obj, 1)
			it("Got 1. As prop is inited when accessed via delegate") { shouldEqual(1, schema.p1.get(obj)) }
		}
		on("Nullable prop p2 initially holdS null") {
			it("Returns null") { shouldEqual(null, obj.p2) }
		}
		// TODO writeProtected property
		on("Prop p3 corresponds to val can't be modified") {
			schema.p3[obj] = 1
			it("It's not mutable") { shouldEqual(0, schema.p3.get(obj)) }
		}
		on("Prop p3_1 corresponds to val but with writeProtected = false can be modified") {
			schema.p3_1[obj] = 1
			it("It's not mutable") { shouldEqual(1, schema.p3_1.get(obj)) }
		}
		on("Prop p3_2 corresponds to var but with writeProtected = true can't be modified") {
			schema.p3_2[obj] = 1
			it("It's not mutable") { shouldEqual(0, schema.p3_2.get(obj)) }
		}
		on("Prop p4 has getter and  'hasNoAccessors=false'") {
			it("Getter works") { shouldEqual(100, schema.p4.get(obj)) }
		}
		on("Prop p5 has getter and  'hasNoAccessors=true'") {
			// So modified via Java field. (! 2x faster).
			it("Getter does NOT work") { shouldEqual(0, schema.p5.get(obj)) }
		}
		on("Private property p6 is modifiable") {
			//	input.p7 = 2 // compile time error. But:
			schema.p6.set(obj, 1)
			it("Should be 1") { shouldEqual(1, schema.p6.get(obj)) }
		}
		// todo is it correct ? shouldn't be default
		on("Access prop that is absent in object") {
			schema.pLost.set(obj, 1)
			it("Should produce null") { shouldEqual(null, schema.pLost.get(obj)) }
		}
		on("Access stub prop") {
			schema.pStub.set(obj, 1)
			it("Should produce Unit") { shouldEqual(Unit, schema.pStub.get(obj)) }
		}

		/*constructors*/

		on("Object with valid primary constructor containing only prop properties") {
			// order in constructor doesn't matter.
			// default values are ignored
			class Obj(var p1: Int, var p0: Int = 1) {
				val p2 = 1
			}

			class ObjSchema : SCHEMAof<Obj>(Obj::class) {
				val p0 by PROP_of(INT)
				val p1 by PROP_of(INT)
				val p2 by PROP_of(INT)
			}

			val schema = ObjSchema()
			val obj = schema.instance()
			it("Default value is ignored") { shouldEqual(0, schema.p0.get(obj)) }
		}
		on("Constructor doesn't distinguish prop properties and temp arguments: (which non val or var)") {
			class Obj(var p1: Int, p0: Int) {
				var p0: Int = 1
			}

			class ObjSchema : SCHEMAof<Obj>(Obj::class) {
				val p0 by PROP_of(INT)
				val p1 by PROP_of(INT)
			}

			val schema = ObjSchema()
			val obj = schema.instance()
			it("p0 contains initial but non default value") { shouldEqual(1, schema.p0.get(obj)) }
		}
		on("Inaccessible Constructor") {
			class Obj private constructor(var p1: Int, var p0: Int)

			class ObjSchema : SCHEMAof<Obj>(Obj::class) {
				val p0 by PROP_of(INT)
				val p1 by PROP_of(INT)
			}

			val schema = ObjSchema()
			val obj = schema.instance()
			it("Object should exist") { shouldNotBeNull(obj) }
			it("Should be ok") { shouldEqual(0, schema.p0.get(obj)) }
		}
		on("Data class Constructor") {
			data class Obj(var p1: Int, val p0: Int)

			class ObjSchema : SCHEMAof<Obj>(Obj::class) {
				val p0 by PROP_of(INT)
				val p1 by PROP_of(INT)
			}

			val schema = ObjSchema()
			val obj = schema.instance()
			schema.p1.set(obj, 1)
			it("Should be ok") { shouldEqual(1, schema.p1.get(obj)) }
		}
		on("Primary constructor that has params other than prop properties") {
			it("Should fail") {
				shouldThrow(Exception::class.java) {
					class Obj(var p1: Int, var p0: Int, val nonProp: Int = 0)

					class ObjSchema : SCHEMAof<Obj>(Obj::class) {
						val p0 by PROP_of(INT)
						val p1 by PROP_of(INT)
					}

					val schema = ObjSchema()
					schema.init()
				}
			}
		}
		on("Usage of prop extra") {
			class ObjSchema : SCHEMAof<Obj>(Obj::class) {
				val p0 by PROP_of(INT)
				val p1 by PROP_of(INT)
				override fun onPropCreated(p: Prop<*>) {
					p.extra = mutableMapOf<String, Any>()
				}
			}

			val schema = ObjSchema()
			schema.p0.description = "Object identifier"
			it("should be accessible") { shouldEqual("Object identifier", schema.p0.description) }
		}
		on("Usage of prop alias") {
			class Obj(var p1: Int, var p0: Int)

			class ObjSchema : SCHEMAof<Obj>(Obj::class) {
				val p0 by PROP_of(INT)
				val p1 by PROP_of(INT).apply { alias = "alias1" }
			}

			val schema = ObjSchema()
			val obj = schema.instance()
			val prod1 = schema.write(obj, DefaultFactory, false)
			val prod2 = schema.write(obj, DefaultFactory, true)
//			println("Prod= $prod1")
			val objMod1 = schema.read("""{"p0":0,"alias1":42}""", DefaultFactory)
			schema.p1.alias = "alias2"
			val objMod2 = schema.read("""{"p0":0,"alias2":11,"alias1":42}""", DefaultFactory)
			val objMod3 = schema.read("""{"p0":0,"nonProp":22,"p1":33}""", DefaultFactory)

			it("map representation inserts alias but sequence doe not") {
				shouldEqual("""{"p0":0,"p1":0,"alias1":0}""", prod1)
				shouldEqual("""[0,0]""", prod2)
			}
			it("initial alias assignment") {
				shouldEqual(objMod1!!.p1, 42)
			}
			it("change alias") {
				shouldEqual(objMod2!!.p1, 11)
			}
		}
		// TODO strange alias usage
		on("Usage of prop alias in constructor") {
			class Obj(var p1: Int, nonProp: Int) {
				var p0: Int = 0
			}

			class ObjSchema : SCHEMAof<Obj>(Obj::class) {
				val p0 by PROP_of(INT).apply { alias = "nonProp" }
				val p1 by PROP_of(INT)
			}

			val schema = ObjSchema()
			val obj = schema.instance()
//			println("Prod= $prod1")
			val objMod1 = schema.read("""{"p1":0,"nonProp":42}""", DefaultFactory)

			it("setting alias in constructor will not affect prop") {
				shouldEqual(objMod1!!.p0, 0)
			}
		}


		// todo test skip updating Unit values

	}
}
}

// implicit Prop.extra usage via extension
@Suppress("UNCHECKED_CAST")
var PropInfo.description: String?
	get() = (extra as? MutableMap<String, Any?>)?.get("description") as? String
	set(value) = run { (extra as? MutableMap<String, Any?>)?.set("description", value) }



