package just4fun.kotlin.schemify.test.specs

import just4fun.kotlin.schemify.core.*
import org.jetbrains.spek.api.*
import kotlin.reflect.KClass
import just4fun.kotlin.schemify.core.PropType.*


class TestSchemaObject : Spek() { init {
	given("Schema Hierarchy") {

		open class BaseObj<T : BaseObj<T>> : SchemaObject<T>() {
			var id = 0
		}

		open class Obj() : BaseObj<Obj>() {
			var p0: Int = 10
			var p1: String? = null
		}

		class ObjXt : Obj()

		abstract class BaseSchema<T : BaseObj<T>>(typeKClass: KClass<T>) : SCHEMAof<T>(typeKClass) {
			val id by PROP_of(INT, true)
		}

		class ObjSchema : BaseSchema<Obj>(Obj::class) {
			val p0 by PROP_of(INT, true)
			val p1 by PROP_of(STRING)
			override fun onPropCreated(p: Prop<*>) {
				p.extra = mutableMapOf<String, Any>()
			}
		}

		val schema = ObjSchema()
		val obj = Obj()
		val objx = ObjXt()

		on("Extending SchemaObject") {
			it("Object has schema property available") { shouldEqual(schema, obj.schema) }
			it("Even ObjXt subclass") { shouldEqual(schema, objx.schema) }
		}
		on("Set base class id property") {
			obj.id = 1
			it("prop id is updated") { shouldEqual(1, schema.id.get(obj)) }
			it("and vise versa") {
				schema.id.set(obj, 2)
				shouldEqual(2, obj.id)
			}
		}
		on("Schema of <T> can be used with subclasses of T.") {
			it("access props is ok") {
				schema.id.set(objx, 1)
				shouldEqual(1, objx.id)
				schema.p0.set(objx, 2)
				shouldEqual(2, schema.p0.get(objx))
			}
			it("call schema methods is ok") {
				shouldEqual(schema.hash(objx), objx.hashCode())
			}
		}
	}
}
}

