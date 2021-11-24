// IGNORE_BACKEND: JS, JS_IR, JS_IR_ES6, NATIVE, WASM

// expected: rv: 42

class C {
    fun foo() = B().bar()
}

class A {
    val x = 42
}

class B {
    fun bar() = A().x
}

val rv = C().foo()
