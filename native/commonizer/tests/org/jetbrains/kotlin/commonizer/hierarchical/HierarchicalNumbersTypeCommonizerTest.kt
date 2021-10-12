/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.hierarchical

import org.jetbrains.kotlin.commonizer.AbstractInlineSourcesCommonizationTest
import org.jetbrains.kotlin.commonizer.AbstractInlineSourcesCommonizationTest.ParametersBuilder
import org.jetbrains.kotlin.commonizer.assertCommonized
import org.jetbrains.kotlin.commonizer.parseCommonizerTarget
import org.jetbrains.kotlin.commonizer.utils.InlineSourceBuilder
import org.jetbrains.kotlin.commonizer.withAllLeaves

class HierarchicalNumbersTypeCommonizerTest : AbstractInlineSourcesCommonizationTest() {

    fun `test Byte and Byte - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = Byte")
            simpleSingleSourceTarget("b", "typealias X = Byte")
        }

        result.assertCommonized(
            "(a, b)",
            """
                typealias X = Byte
            """.trimIndent()
        )
    }

    fun `test Byte and Short - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = Byte")
            simpleSingleSourceTarget("b", "typealias X = Short")
        }

        result.assertCommonized(
            "(a, b)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : Number, SignedInteger<X>
            """.trimIndent()
            )
        }
    }

    fun `test Byte and Int - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = Byte")
            simpleSingleSourceTarget("b", "typealias X = Int")
        }

        result.assertCommonized(
            "(a, b)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : Number, SignedInteger<X>
            """.trimIndent()
            )
        }
    }

    fun `test Byte and Long - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = Byte")
            simpleSingleSourceTarget("b", "typealias X = Long")
        }

        result.assertCommonized(
            "(a, b)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : Number, SignedInteger<X>
            """.trimIndent()
            )
        }
    }

    fun `test Short and Byte - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = Short")
            simpleSingleSourceTarget("b", "typealias X = Byte")
        }

        result.assertCommonized("(a, b)") {
            generatedPhantoms()
            source(
                """
                expect class X : Number, SignedInteger<X>
            """.trimIndent()
            )
        }
    }

    fun `test Short and Short - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = Short")
            simpleSingleSourceTarget("b", "typealias X = Short")
        }

        result.assertCommonized(
            "(a, b)",
            """
                typealias X = Short
            """.trimIndent()
        )
    }

    fun `test Short and Int - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = Short")
            simpleSingleSourceTarget("b", "typealias X = Int")
        }

        result.assertCommonized("(a, b)") {
            generatedPhantoms()
            source(
                """
                expect class X : Number, SignedInteger<X>
            """.trimIndent()
            )
        }
    }

    fun `test Short and Long - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = Short")
            simpleSingleSourceTarget("b", "typealias X = Long")
        }

        result.assertCommonized(
            "(a, b)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : Number, SignedInteger<X>
            """.trimIndent()
            )
        }
    }

    fun `test Int and Byte - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = Int")
            simpleSingleSourceTarget("b", "typealias X = Byte")
        }

        result.assertCommonized(
            "(a, b)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : Number, SignedInteger<X>
            """.trimIndent()
            )
        }
    }

    fun `test Int and Short - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = Int")
            simpleSingleSourceTarget("b", "typealias X = Short")
        }

        result.assertCommonized(
            "(a, b)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : Number, SignedInteger<X>
            """.trimIndent()
            )
        }
    }

    fun `test Int and Int - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = Int")
            simpleSingleSourceTarget("b", "typealias X = Int")
        }

        result.assertCommonized(
            "(a, b)",
            """
                typealias X = Int
            """.trimIndent()
        )
    }

    fun `test Int and Long - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = Int")
            simpleSingleSourceTarget("b", "typealias X = Long")
        }

        result.assertCommonized(
            "(a, b)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : Number, SignedInteger<X>
            """.trimIndent()
            )
        }
    }

    fun `test Long and Byte - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = Long")
            simpleSingleSourceTarget("b", "typealias X = Byte")
        }

        result.assertCommonized(
            "(a, b)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : Number, SignedInteger<X>
            """.trimIndent()
            )
        }
    }

    fun `test Long and Short - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = Long")
            simpleSingleSourceTarget("b", "typealias X = Short")
        }

        result.assertCommonized(
            "(a, b)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : Number, SignedInteger<X>
            """.trimIndent()
            )
        }
    }

    fun `test Long and Int - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = Long")
            simpleSingleSourceTarget("b", "typealias X = Int")
        }

        result.assertCommonized(
            "(a, b)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : Number, SignedInteger<X>
            """.trimIndent()
            )
        }
    }

    fun `test Long and Long - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = Long")
            simpleSingleSourceTarget("b", "typealias X = Long")
        }

        result.assertCommonized(
            "(a, b)",
            """
                typealias X = Long
            """.trimIndent()
        )
    }

    fun `test UByte and UByte - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = UByte")
            simpleSingleSourceTarget("b", "typealias X = UByte")
        }

        result.assertCommonized(
            "(a, b)",
            """
                typealias X = UByte
            """.trimIndent()
        )
    }

    fun `test UByte and UShort - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = UByte")
            simpleSingleSourceTarget("b", "typealias X = UShort")
        }

        result.assertCommonized(
            "(a, b)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : UnsignedInteger<X>
            """.trimIndent()
            )
        }
    }

    fun `test UByte and UInt - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = UByte")
            simpleSingleSourceTarget("b", "typealias X = UInt")
        }

        result.assertCommonized(
            "(a, b)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : UnsignedInteger<X>
            """.trimIndent()
            )
        }
    }

    fun `test UByte and ULong - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = UByte")
            simpleSingleSourceTarget("b", "typealias X = ULong")
        }

        result.assertCommonized(
            "(a, b)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : UnsignedInteger<X>
            """.trimIndent()
            )
        }
    }

    fun `test UShort and UByte - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = UShort")
            simpleSingleSourceTarget("b", "typealias X = UByte")
        }

        result.assertCommonized(
            "(a, b)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : UnsignedInteger<X>
            """.trimIndent()
            )
        }
    }

    fun `test UShort and UShort - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = UShort")
            simpleSingleSourceTarget("b", "typealias X = UShort")
        }

        result.assertCommonized(
            "(a, b)",
            """
                typealias X = UShort
            """.trimIndent()
        )
    }

    fun `test UShort and UInt - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = UShort")
            simpleSingleSourceTarget("b", "typealias X = UInt")
        }

        result.assertCommonized(
            "(a, b)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : UnsignedInteger<X>
            """.trimIndent()
            )
        }
    }

    fun `test UShort and ULong - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = UShort")
            simpleSingleSourceTarget("b", "typealias X = ULong")
        }

        result.assertCommonized(
            "(a, b)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : UnsignedInteger<X>
            """.trimIndent()
            )
        }
    }

    fun `test UInt and UByte - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = UInt")
            simpleSingleSourceTarget("b", "typealias X = UByte")
        }

        result.assertCommonized(
            "(a, b)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : UnsignedInteger<X>
            """.trimIndent()
            )
        }
    }

    fun `test UInt and UShort - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = UInt")
            simpleSingleSourceTarget("b", "typealias X = UShort")
        }

        result.assertCommonized(
            "(a, b)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : UnsignedInteger<X>
            """.trimIndent()
            )
        }
    }

    fun `test UInt and UInt - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = UInt")
            simpleSingleSourceTarget("b", "typealias X = UInt")
        }

        result.assertCommonized(
            "(a, b)",
            """
                typealias X = UInt
            """.trimIndent()
        )
    }

    fun `test UInt and ULong - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = UInt")
            simpleSingleSourceTarget("b", "typealias X = ULong")
        }

        result.assertCommonized(
            "(a, b)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : UnsignedInteger<X>
            """.trimIndent()
            )
        }
    }

    fun `test ULong and UByte - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = ULong")
            simpleSingleSourceTarget("b", "typealias X = UByte")
        }

        result.assertCommonized(
            "(a, b)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : UnsignedInteger<X>
            """.trimIndent()
            )
        }
    }

    fun `test ULong and UShort - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = ULong")
            simpleSingleSourceTarget("b", "typealias X = UShort")
        }

        result.assertCommonized(
            "(a, b)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : UnsignedInteger<X>
            """.trimIndent()
            )
        }
    }

    fun `test ULong and UInt - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = ULong")
            simpleSingleSourceTarget("b", "typealias X = UInt")
        }

        result.assertCommonized(
            "(a, b)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : UnsignedInteger<X>
            """.trimIndent()
            )
        }
    }

    fun `test ULong and ULong - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = ULong")
            simpleSingleSourceTarget("b", "typealias X = ULong")
        }

        result.assertCommonized("(a, b)", "typealias X = ULong")
    }

    fun `test UInt and Long - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget("a", "typealias X = UInt")
            simpleSingleSourceTarget("b", "typealias X = Long")
        }

        result.assertCommonized("(a, b)", "expect class X")
    }

    fun `test UIntVarOf and ULongVarOf - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget(
                "a", """
                typealias A = UInt
                typealias X = kotlinx.cinterop.UIntVarOf<A>
            """.trimIndent()
            )
            simpleSingleSourceTarget(
                "b", """
                typealias A = ULong
                typealias X = kotlinx.cinterop.ULongVarOf<A>
            """.trimIndent()
            )
        }

        result.assertCommonized(
            "(a, b)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class A : UnsignedInteger<A>
                expect class X : kotlinx.cinterop.UnsignedVarOf<A>
            """.trimIndent()
            )
        }
    }

    fun `test IntVarOf and LongVarOf - typealias`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget(
                "a", """
                typealias A = Int
                typealias X = kotlinx.cinterop.IntVarOf<A>
            """.trimIndent()
            )
            simpleSingleSourceTarget(
                "b", """
                typealias A = Long
                typealias X = kotlinx.cinterop.LongVarOf<A>
            """.trimIndent()
            )
        }

        result.assertCommonized(
            "(a, b)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class A : Number, SignedInteger<A>
                expect class X : kotlinx.cinterop.SignedVarOf<A>
            """.trimIndent()
            )
        }
    }

    fun `test Int and Long - typealias chain`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")

            simpleSingleSourceTarget(
                "a", """
                    typealias A = Int
                    typealias B = A
                    typealias C = B
                    typealias X = C
                """.trimIndent()
            )

            simpleSingleSourceTarget(
                "b", """
                    typealias A = Long
                    typealias B = A
                    typealias X = B
                """.trimIndent()
            )
        }

        result.assertCommonized("(a, b)") {
            generatedPhantoms()
            source(
                """
                expect class A : Number, SignedInteger<A>
                typealias B = A
                typealias X = B
            """.trimIndent()
            )
        }
    }

    fun `test function with pure number types parameter`() {
        val result = commonize {
            outputTarget("(a, b)")
            simpleSingleSourceTarget("a", "fun x(p: Int) {}")
            simpleSingleSourceTarget("b", "fun x(p: Long) {}")
        }

        /*
        Only functions that use a TA in their signature are supposed to be
        commonized using our number's commonization hack.

        This is a hard requirement. It would also be reasonable if we would add
        support for this case, since there would be reasonable code that people
        could write with this!
         */
        result.assertCommonized("(a, b)", "")
    }

    fun `test function with aliased number value parameter`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget(
                "a", """
                    typealias A = Int
                    typealias X = A
                    fun x(p: X) {}
                """.trimIndent()
            )
            simpleSingleSourceTarget(
                "b", """
                    typealias B = Long
                    typealias X = B
                    fun x(p: X) {}
                """.trimIndent()
            )
        }

        result.assertCommonized("(a, b)") {
            generatedPhantoms()
            source(
                """
                expect class X : Number, SignedInteger<X>
                expect fun x(p: X)
            """.trimIndent()
            )
        }
    }

    fun `test property with pure number return type`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerDependency("a", "b", "(a, b)") { unsignedIntegers() }
            simpleSingleSourceTarget("a", "val x: UInt = null!!")
            simpleSingleSourceTarget("b", "val x: ULong = null!!")
        }

        result.assertCommonized("(a, b)") {
            generatedPhantoms()
            source("expect val x: UnsignedInteger<*>")
        }
    }

    fun `test property with aliased number return type`() {
        val result = commonize {
            outputTarget("(a, b)")
            registerFakeStdlibDependency("(a, b)")
            simpleSingleSourceTarget(
                "a", """
                    typealias X = UShort
                    val x: X = null!!
                """.trimIndent()
            )
            simpleSingleSourceTarget(
                "b", """
                    typealias X = ULong
                    val x: X = null!!
                """.trimIndent()
            )
        }

        result.assertCommonized("(a, b)") {
            generatedPhantoms()
            source(
                """
                expect class X : UnsignedInteger<X>
                expect val x: X 
            """.trimIndent()
            )
        }
    }

    fun `test multilevel hierarchy`() {
        val result = commonize {
            outputTarget("(a, b)", "(c, d)", "(e, f)", "(c, d, e, f)", "(a, b, c, d, e, f)")
            registerFakeStdlibDependency("(a, b)", "(c, d)", "(e, f)", "(c, d, e, f)", "(a, b, c, d, e, f)")
            simpleSingleSourceTarget("a", "typealias X = Short")
            simpleSingleSourceTarget("b", "typealias X = Int")
            simpleSingleSourceTarget("c", "typealias X = Int")
            simpleSingleSourceTarget("d", "typealias X = Int")
            simpleSingleSourceTarget("e", "typealias X = Long")
            simpleSingleSourceTarget("f", "typealias X = Int")
        }

        result.assertCommonized(
            "(a, b)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : Number, SignedInteger<X>
            """.trimIndent()
            )
        }

        result.assertCommonized(
            "(c, d)", """
                typealias X = Int
            """.trimIndent()
        )

        result.assertCommonized(
            "(e, f)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : Number, SignedInteger<X>
            """.trimIndent()
            )
        }

        result.assertCommonized(
            "(c, d, e, f)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : Number, SignedInteger<X>
            """.trimIndent()
            )
        }

        result.assertCommonized(
            "(a, b, c, d, e, f)"
        ) {
            generatedPhantoms()
            source(
                """
                expect class X : Number, SignedInteger<X>
            """.trimIndent()
            )
        }
    }
}

private fun ParametersBuilder.registerFakeStdlibDependency(vararg outputTarget: String) {
    val allTargets = outputTarget.map { parseCommonizerTarget(it) }.withAllLeaves()
    registerDependency(*allTargets.toTypedArray()) {
        unsignedIntegers()
        unsignedVarIntegers()
        singedVarIntegers()
    }
}

private fun InlineSourceBuilder.ModuleBuilder.unsignedIntegers() {
    source(
        """
        package kotlin
        class UByte
        class UShort
        class UInt
        class ULong
        """.trimIndent(), "unsigned.kt"
    )
}

private fun InlineSourceBuilder.ModuleBuilder.unsignedVarIntegers() {
    source(
        """
        package kotlinx.cinterop
        class UByteVarOf<T : UByte>
        class UShortVarOf<T : UShort>
        class UIntVarOf<T : UInt>
        class ULongVarOf<T : ULong>
        """.trimIndent(), "UnsignedVarOf.kt"
    )
}

private fun InlineSourceBuilder.ModuleBuilder.singedVarIntegers() {
    source(
        """
        package kotlinx.cinterop
        class ByteVarOf<T : Byte>
        class ShortVarOf<T : Short>
        class IntVarOf<T : Int>
        class LongVarOf<T : Long>
        """.trimIndent(), "SignedVarOf.kt"
    )
}
