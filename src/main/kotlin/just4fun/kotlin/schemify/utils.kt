package just4fun.kotlin.schemify

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


object Utils {
	/**  Asks [subclass] of  [genericClass] for its type argument class defined by [argIndex].
	Identifies a class of generic type argument supplied by implementing subclass.
	@param[genericClass] - class declaring type arguments.
	@param[subclass] - subclass of [genericClass] should implement arguments or be a subclass of class that does it.
	@para[argIndex] - index of required argument in list type args of [genericClass]
	@throws Exception

	@sample {
	open class X
	open class Y
	class YExt : Y()
	class XExt : X()
	open class Base<T0 : Number, T1 : X, T2 : Y>
	open class BaseSub<T1 : X, T2 : Y> : Base<Long, T1, T2>()
	open class BaseExt : BaseSub<XExt, YExt>()
	class Ext : BaseExt()
	interface Bla
	interface Typ<T : Any> : Bla
	interface TypeImpl : Bla, Typ<String>
	interface TypeImplExt : TypeImpl, Bla
	class TypeImplExtClas : TypeImplExt, Bla
	open class LongType : Typ<Long>, Serializable
	class ExtType : LongType(), Bla
	Utils.classOfTypeArgument(Typ::class.java, TypeImpl::class.java, 0).run { println("0:: ${this}") }// String
	Utils.classOfTypeArgument(Typ::class.java, TypeImplExt::class.java, 0).run { println("0:: ${this}") }// String
	Utils.classOfTypeArgument(Typ::class.java, TypeImplExtClas::class.java, 0).run { println("0:: ${this}") }// String
	Utils.classOfTypeArgument(Typ::class.java, ExtType::class.java, 0).run { println("0:: ${this}") }// Long
	Utils.classOfTypeArgument(Base::class.java, BaseSub::class.java, 0).run { println("1:: ${this}") }// Long
	//	Utils.classOfTypeArgument(Base::class.java, BaseSub::class.java, 1).run { println("2:: ${this}") }// failed T1
	Utils.classOfTypeArgument(Base::class.java, BaseExt::class.java, 0).run { println("3:: ${this}") }// Long
	//	Utils.classOfTypeArgument(Base::class.java, BaseExt::class.java, X::class.java).run { println("3:: ${this}") }//failed X
	Utils.classOfTypeArgument(BaseSub::class.java, BaseExt::class.java, 0).run { println("4:: ${this}") }// XExt
	Utils.classOfTypeArgument(BaseSub::class.java, BaseExt::class.java, Y::class.java).run { println("5:: ${this}") }// YExt
	//	Utils.classOfTypeArgument(Base::class.java, Ext::class.java, X::class.java).run { println("6:: ${this}") }// failed
	Utils.classOfTypeArgument(Base::class.java, Ext::class.java, Number::class.java).run { println("6:: ${this}") }// Long
	Utils.classOfTypeArgument(BaseSub::class.java, Ext::class.java, X::class.java).run { println("7:: ${this}") }// XExt
	Utils.classOfTypeArgument(BaseSub::class.java, Ext::class.java, 1).run { println("7:: ${this}") }// YExt
	//	Utils.classOfTypeArgument(BaseExt::class.java, Ext::class.java, 0).run { println("8:: ${this}") }// failed
	}
	 */
	fun <T> classOfTypeArgument(genericClass: Class<T>, subclass: Class<out T>, argIndex: Int): Class<*> {
		if (subclass == genericClass) throw Exception("'subclass' should be subclass of 'genericClass'")
		var args = typeArguments(genericClass, subclass)!!
		return args.let {
			if (it.size <= argIndex) throw Exception("Generic param #$argIndex of ${genericClass.name} is not found.")
			it[argIndex] as? Class<*> ?: throw Exception("Generic param ${it[argIndex].typeName} is abstract and can not be casted to Class.")
		}
	}

	/**  Asks [subclass] of  [genericClass] for its type argument class defined by [argClass].
	 */
	fun <T> classOfTypeArgument(genericClass: Class<T>, subclass: Class<out T>, argClass: Class<*>): Class<*> {
		if (subclass == genericClass) throw Exception("'subclass' should be subclass of 'genericClass'")
		var args = typeArguments(genericClass, subclass)!!
		for (type in args) {
			if (type is Class<*> && argClass.isAssignableFrom(type)) return type
		}
		throw Exception("No type argument of ${argClass.name} found in ${genericClass.name}.")
	}

	private fun typeArguments(genericClass: Class<*>, subclass: Class<*>): Array<Type>? = when {
		genericClass.isInterface -> {
			var args = if (!subclass.isInterface) resolveSuperclass(genericClass, subclass, subclass.superclass) else null
			args ?: run {
				val index = subclass.interfaces.indexOfFirst { it == genericClass }
				if (index >= 0) subclass.genericInterfaces[index].run {
					if (this !is ParameterizedType) throw Exception("Target class ${genericClass.name} is not parametrized.")
					actualTypeArguments
				} else subclass.interfaces.any { args = typeArguments(genericClass, it); args != null }.run { args }
			}
		}
		else -> resolveSuperclass(genericClass, subclass, subclass.superclass)
	}

	private fun resolveSuperclass(genericClass: Class<*>, subclass: Class<*>, superclass: Class<*>): Array<Type>? = when {
		superclass == Object::class.java -> null
		superclass != genericClass -> typeArguments(genericClass, superclass)
		else -> subclass.genericSuperclass.run {
			if (this !is ParameterizedType) throw Exception("Target class ${genericClass.name} is not parametrized.")
			actualTypeArguments
		}
	}

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

}


