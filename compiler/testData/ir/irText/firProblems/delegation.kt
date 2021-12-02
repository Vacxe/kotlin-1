// MODULE: lib
// FILE: Delegation.java

public class Delegation {
    public static class ReturnNull {
        public String foo() {
            return null;
        }
    }
}

// MODULE: main(lib)
// FILE: delegation.kt

interface Tr {
    fun foo(): String
}

class DelegateTo : Delegation.ReturnNull(), Tr {
    override fun foo() = super<Delegation.ReturnNull>.foo()
}

class DelegateFrom : Tr by DelegateTo()
