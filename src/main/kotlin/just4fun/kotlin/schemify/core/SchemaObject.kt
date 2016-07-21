package just4fun.kotlin.schemify.core
import just4fun.kotlin.schemify.core.PropType.*



/* SCHEMA OBJECT */
abstract class SchemaObject<ThisType : Any> : Cloneable {
	@Suppress("CAST_NEVER_SUCCEEDS")
// todo  should be silent or throw exception  if schema = null
	open val schema = PropType.schemaFor(javaClass as Class<ThisType>) ?: throw Exception("Schema of ${javaClass.name} should be created before it's instances.")

	@Suppress("UNCHECKED_CAST")
	override fun toString(): String = schema.toString(this as ThisType, -1)

	@Suppress("UNCHECKED_CAST")
	fun clone(deep: Boolean): ThisType = schema.copy(this as ThisType, deep)!!
	override fun clone(): ThisType = clone(true)

	@Suppress("UNCHECKED_CAST")
	override fun equals(other: Any?): Boolean = when {
		null == other -> false
		this === other -> true
		schema.isInstance(other) -> schema.equalProps(this as ThisType, other as ThisType)
		else -> false
	}

	@Suppress("UNCHECKED_CAST")
	override fun hashCode(): Int = schema.hash(this as ThisType)
}


