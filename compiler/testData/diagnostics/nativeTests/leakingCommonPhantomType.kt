// !SKIP_TXT
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -CAST_NEVER_SUCCEEDS
// WITH_RUNTIME

// FILE: commonizer_types.kt

package kotlin

public interface SignedInteger

// FILE: phantom_type_producer.kt

import kotlin.<!PHANTOM_CLASSIFIER!>SignedInteger<!>

fun <<!LEAKING_PHANTOM_TYPE_IN_TYPE_PARAMETERS!>T : <!PHANTOM_CLASSIFIER!>SignedInteger<!><!>> topLevelFn1(
    arg: <!LEAKING_PHANTOM_TYPE, PHANTOM_CLASSIFIER!>SignedInteger<!>
): <!LEAKING_PHANTOM_TYPE, PHANTOM_CLASSIFIER!>SignedInteger<!> {
    null!!
}

fun <!LEAKING_PHANTOM_TYPE!>topLevelFn2<!>() =
    null as <!PHANTOM_CLASSIFIER!>SignedInteger<!>

fun <<!LEAKING_PHANTOM_TYPE_IN_TYPE_PARAMETERS!>T<!>> topLevelFn3(): Any where T : Number, T : <!PHANTOM_CLASSIFIER!>SignedInteger<!> {
    null!!
}

val topLevelProp1: <!LEAKING_PHANTOM_TYPE, PHANTOM_CLASSIFIER!>SignedInteger<!> =
    null!!

val <!LEAKING_PHANTOM_TYPE!>topLevelProp2<!> =
    null as <!PHANTOM_CLASSIFIER!>SignedInteger<!>

interface Generic<T>
interface Stub

class Class<<!LEAKING_PHANTOM_TYPE_IN_TYPE_PARAMETERS!>T : <!PHANTOM_CLASSIFIER!>SignedInteger<!><!>>(
    val prop1: <!LEAKING_PHANTOM_TYPE, PHANTOM_CLASSIFIER!>SignedInteger<!>,
    val prop2: <!LEAKING_PHANTOM_TYPE!>List<Set<Collection<<!PHANTOM_CLASSIFIER!>SignedInteger<!>>>><!>,
    <!VALUE_PARAMETER_WITH_NO_TYPE_ANNOTATION!>val propNoTypeReference<!SYNTAX!><!> = null as <!PHANTOM_CLASSIFIER!>SignedInteger<!><!>,
) : <!LEAKING_PHANTOM_TYPE_IN_SUPERTYPES!>Stub, Generic<<!PHANTOM_CLASSIFIER!>SignedInteger<!>><!> {
    class NestedClass<<!LEAKING_PHANTOM_TYPE_IN_TYPE_PARAMETERS!>T: <!PHANTOM_CLASSIFIER!>SignedInteger<!><!>>(
        val ncProp: <!LEAKING_PHANTOM_TYPE, PHANTOM_CLASSIFIER!>SignedInteger<!>
    ) {
        fun <!LEAKING_PHANTOM_TYPE!>ncFun<!>() = null as <!PHANTOM_CLASSIFIER!>SignedInteger<!>
    }

    inner class InnerClass<<!LEAKING_PHANTOM_TYPE_IN_TYPE_PARAMETERS!>T: <!PHANTOM_CLASSIFIER!>SignedInteger<!><!>>(
        val icProp: <!LEAKING_PHANTOM_TYPE, PHANTOM_CLASSIFIER!>SignedInteger<!>
    ) {
        fun <!LEAKING_PHANTOM_TYPE!>icFun<!>() = null as <!PHANTOM_CLASSIFIER!>SignedInteger<!>
    }

    fun <!LEAKING_PHANTOM_TYPE!>escapeFromLocalClass<!>() = {
        class Local<T : <!PHANTOM_CLASSIFIER!>SignedInteger<!>>(
            private val prop: T,
            val lcProp: <!PHANTOM_CLASSIFIER!>SignedInteger<!>,
        ) {
            fun lcFun() = null as <!PHANTOM_CLASSIFIER!>SignedInteger<!>
            fun escape(): T = prop
        }

        Local(null as <!PHANTOM_CLASSIFIER!>SignedInteger<!>, null as <!PHANTOM_CLASSIFIER!>SignedInteger<!>).escape()
    }()

    val <!LEAKING_PHANTOM_TYPE!>prop3<!> =
        null as <!PHANTOM_CLASSIFIER!>SignedInteger<!>
    val prop4: <!LEAKING_PHANTOM_TYPE, PHANTOM_CLASSIFIER!>SignedInteger<!>
        get() = null!!
    var prop5: <!LEAKING_PHANTOM_TYPE, PHANTOM_CLASSIFIER!>SignedInteger<!> =
        null!!
    var prop6: <!LEAKING_PHANTOM_TYPE, PHANTOM_CLASSIFIER!>SignedInteger<!>
        get() = null!!
        set(value) { null!! }

    fun <<!LEAKING_PHANTOM_TYPE_IN_TYPE_PARAMETERS!>T: <!PHANTOM_CLASSIFIER!>SignedInteger<!><!>> member1(
        arg1: <!LEAKING_PHANTOM_TYPE, PHANTOM_CLASSIFIER!>SignedInteger<!>,
        arg2: T
    ): <!LEAKING_PHANTOM_TYPE, PHANTOM_CLASSIFIER!>SignedInteger<!> {
        null!!
    }

    fun <!LEAKING_PHANTOM_TYPE!>implicitLambdaType<!>() = {
        null as Map<Any, List<<!PHANTOM_CLASSIFIER!>SignedInteger<!>>>
    }


    /////////////// Shouldn't be reported for local usages/instances ///////////////

    fun hidden1(): Any {
        val localProp = null as <!PHANTOM_CLASSIFIER!>SignedInteger<!>
        val fromPrivateFun = hidden3()
        return localProp
    }

    fun hidden2(): Any =
        null as <!PHANTOM_CLASSIFIER!>SignedInteger<!>

    private fun hidden3() = null as <!PHANTOM_CLASSIFIER!>SignedInteger<!>

}
