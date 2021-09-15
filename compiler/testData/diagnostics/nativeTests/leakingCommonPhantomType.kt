// !SKIP_TXT
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -CAST_NEVER_SUCCEEDS
// WITH_RUNTIME

// FILE: commonizer_types.kt

package kotlin

public interface SignedInteger

// FILE: phantom_type_producer.kt

import kotlin.SignedInteger

fun <<!LEAKING_PHANTOM_TYPE_IN_TYPE_PARAMETERS!>T : SignedInteger<!>> topLevelFn1(
    arg: <!LEAKING_PHANTOM_TYPE!>SignedInteger<!>
): <!LEAKING_PHANTOM_TYPE!>SignedInteger<!> {
    null!!
}

fun <!LEAKING_PHANTOM_TYPE!>topLevelFn2<!>() =
    null as SignedInteger

fun <<!LEAKING_PHANTOM_TYPE_IN_TYPE_PARAMETERS!>T<!>> topLevelFn3(): Any where T : Number, T : SignedInteger {
    null!!
}

val topLevelProp1: <!LEAKING_PHANTOM_TYPE!>SignedInteger<!> =
    null!!

val <!LEAKING_PHANTOM_TYPE!>topLevelProp2<!> =
    null as SignedInteger

interface Generic<T>
interface Stub

class Class<<!LEAKING_PHANTOM_TYPE_IN_TYPE_PARAMETERS!>T : SignedInteger<!>>(
    val prop1: <!LEAKING_PHANTOM_TYPE!>SignedInteger<!>,
    val prop2: <!LEAKING_PHANTOM_TYPE!>List<Set<Collection<SignedInteger>>><!>,
    <!VALUE_PARAMETER_WITH_NO_TYPE_ANNOTATION!>val propNoTypeReference<!SYNTAX!><!> = null as SignedInteger<!>,
) : <!LEAKING_PHANTOM_TYPE_IN_SUPERTYPES!>Stub, Generic<SignedInteger><!> {
    class NestedClass<<!LEAKING_PHANTOM_TYPE_IN_TYPE_PARAMETERS!>T: SignedInteger<!>>(
        val ncProp: <!LEAKING_PHANTOM_TYPE!>SignedInteger<!>
    ) {
        fun <!LEAKING_PHANTOM_TYPE!>ncFun<!>() = null as SignedInteger
    }

    inner class InnerClass<<!LEAKING_PHANTOM_TYPE_IN_TYPE_PARAMETERS!>T: SignedInteger<!>>(
        val icProp: <!LEAKING_PHANTOM_TYPE!>SignedInteger<!>
    ) {
        fun <!LEAKING_PHANTOM_TYPE!>icFun<!>() = null as SignedInteger
    }

    fun <!LEAKING_PHANTOM_TYPE!>escapeFromLocalClass<!>() = {
        class Local<T : SignedInteger>(
            private val prop: T,
            val lcProp: SignedInteger,
        ) {
            fun lcFun() = null as SignedInteger
            fun escape(): T = prop
        }

        Local(null as SignedInteger, null as SignedInteger).escape()
    }()

    val <!LEAKING_PHANTOM_TYPE!>prop3<!> =
        null as SignedInteger
    val prop4: <!LEAKING_PHANTOM_TYPE!>SignedInteger<!>
        get() = null!!
    var prop5: <!LEAKING_PHANTOM_TYPE!>SignedInteger<!> =
        null!!
    var prop6: <!LEAKING_PHANTOM_TYPE!>SignedInteger<!>
        get() = null!!
        set(value) { null!! }

    fun <<!LEAKING_PHANTOM_TYPE_IN_TYPE_PARAMETERS!>T: SignedInteger<!>> member1(
        arg1: <!LEAKING_PHANTOM_TYPE!>SignedInteger<!>,
        arg2: T
    ): <!LEAKING_PHANTOM_TYPE!>SignedInteger<!> {
        null!!
    }

    fun <!LEAKING_PHANTOM_TYPE!>implicitLambdaType<!>() = {
        null as Map<Any, List<SignedInteger>>
    }


    /////////////// Shouldn't be reported for local usages/instances ///////////////

    fun hidden1(): Any {
        val localProp = null as SignedInteger
        val fromPrivateFun = hidden3()
        return localProp
    }

    fun hidden2(): Any =
        null as SignedInteger

    private fun hidden3() = null as SignedInteger

}
