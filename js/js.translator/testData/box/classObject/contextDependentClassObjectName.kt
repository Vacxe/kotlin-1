package foo.bar.baz

class MyClass {
    companion object {
        fun ok(): String {
            val okMaker = object {
                fun getOk(): String = "OK"
            }
            return okMaker.getOk()
        }
    }
}

// CHECK_FUNCTION_EXISTS: foo_bar_baz_MyClass$Companion$ok$okMaker$1 TARGET_BACKENDS=JS_IR
fun box(): String {
    return MyClass.ok()
}
