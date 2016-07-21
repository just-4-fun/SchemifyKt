/*
package just4fun.kotlin.schemify.test.specs

import just4fun.kotlin.schemify.core.Map2String
import just4fun.kotlin.schemify.core.String2Map
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.shouldEqual

class TestMap2string : Spek() { init {
	given("Json string"){
		on("Parse Number"){
			it("should be ok"){
				shouldEqual(0, String2Map("0 ").result())
				shouldEqual(1, String2Map("1 ").result())
				shouldEqual(-1, String2Map("-1 ").result())
				shouldEqual(1.1, String2Map("1.1 ").result())
				shouldEqual(-1.1, String2Map("-1.1 ").result())
				shouldEqual(1.0, String2Map("1. ").result())
				shouldEqual(-1.0, String2Map("-1. ").result())
				shouldEqual(999999999, String2Map("999999999 ").result())
				shouldEqual(-999999999, String2Map("-999999999 ").result())
				shouldEqual(9999999991, String2Map("9999999991 ").result())
				shouldEqual(-9999999991, String2Map("-9999999991 ").result())
				shouldEqual(999999999999999999, String2Map("999999999999999999 ").result())
				shouldEqual(-999999999999999999, String2Map("-999999999999999999 ").result())
				shouldEqual("9999999999999999999", String2Map("9999999999999999999 ").result())
				shouldEqual("-9999999999999999999", String2Map("-9999999999999999999 ").result())
				shouldEqual(999999999999999999.0, String2Map("999999999999999999.0 ").result())
				shouldEqual(-999999999999999999.0, String2Map("-999999999999999999.0 ").result())
				shouldEqual(0.010203040506070809012345, String2Map("0.010203040506070809012345 ").result())
				shouldEqual(-0.010203040506070809012345, String2Map("-0.010203040506070809012345 ").result())
			}
		}
		on("Parse String") {
			it("should be ok") {
				shouldEqual("", String2Map("\"\" ").result())
				shouldEqual("\\", String2Map("\"\\\\\" ").result())
				shouldEqual("\"", String2Map("\"\\\"\" ").result())
				shouldEqual("""""", String2Map(""" "" """).result())
				shouldEqual("""<\>""", String2Map(""" "<\\>" """).result())
				shouldEqual("""<">""", String2Map(""" "<\">" """).result())
				shouldEqual("""<Ы>""", String2Map(""" "<Ы>" """).result())
				shouldEqual("""<\u1002>""", String2Map(""" "<\u1002>" """).result())
				shouldEqual("""</>""", String2Map(""" "</>" """).result())
				shouldEqual("""<\n>""", String2Map(""" "<\n>" """).result())
				shouldEqual("""<\b>""", String2Map(""" "<\b>" """).result())
				shouldEqual("""<\f>""", String2Map(""" "<\f>" """).result())
				shouldEqual("""<\r>""", String2Map(""" "<\r>" """).result())
				shouldEqual("""<\t>""", String2Map(""" "<\t>" """).result())
				shouldEqual(Map2String("").result(), String2Map("\"\" ").result().let{""""$it""""})
				shouldEqual(Map2String("a").result(), String2Map("\"a\" ").result().let{""""$it""""})
				shouldEqual(Map2String("\\").result(), String2Map("\"\\\\\" ").result().let{""""$it""""})
				shouldEqual(Map2String("Ы").result(), String2Map("\"Ы\" ").result().let{""""$it""""})
				shouldEqual(Map2String("\uA012").result(), String2Map("\"\uA012\" ").result().let{""""$it""""})
				shouldEqual(Map2String("=\n=").result(), String2Map("\"=\n=\" ").result().let{""""$it""""})
				println(">> ${String2Map("\"[ \\\" ]\" ").result()}")
				println(">> ${String2Map("\"[ \\ ]\" ").result()}")
				println(">> ${String2Map("\"[ \u1111 ]\" ").result()}")
				println(">> ${String2Map("\"[ a\na ]\" ").result()}")
				println("""<\n>""")
				val v = Map2String("a")

			}
		}
	}

}}
*/
