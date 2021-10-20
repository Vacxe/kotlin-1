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
                typealias X = SignedInteger
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
                typealias X = SignedInteger
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
                typealias X = SignedInteger
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
                typealias X = SignedInteger
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
                typealias X = SignedInteger
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
                typealias X = SignedInteger
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
                typealias X = SignedInteger
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
                typealias X = SignedInteger
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
                typealias X = SignedInteger
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
                typealias X = SignedInteger
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
                typealias X = SignedInteger
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
                typealias X = SignedInteger
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
                typealias X = UnsignedInteger
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
                typealias X = UnsignedInteger
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
                typealias X = UnsignedInteger
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
                typealias X = UnsignedInteger
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
                typealias X = UnsignedInteger
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
                typealias X = UnsignedInteger
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
                typealias X = UnsignedInteger
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
                typealias X = UnsignedInteger
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
                typealias X = UnsignedInteger
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
                typealias X = UnsignedInteger
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
                typealias X = UnsignedInteger
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
                typealias X = UnsignedInteger
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
                typealias A = UnsignedInteger
                typealias X = kotlinx.cinterop.UnsignedVarOf<A>
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
                typealias A = SignedInteger
                typealias X = kotlinx.cinterop.SignedVarOf<A>
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
                typealias A = SignedInteger
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
                typealias X = SignedInteger
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
            source("expect val x: UnsignedInteger")
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
                typealias X = UnsignedInteger
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
                typealias X = SignedInteger
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
                typealias X = SignedInteger
            """.trimIndent()
            )
        }

        result.assertCommonized(
            "(c, d, e, f)"
        ) {
            generatedPhantoms()
            source(
                """
                typealias X = SignedInteger
            """.trimIndent()
            )
        }

        result.assertCommonized(
            "(a, b, c, d, e, f)"
        ) {
            generatedPhantoms()
            source(
                """
                typealias X = SignedInteger
            """.trimIndent()
            )
        }
    }

    fun `test type aliases to different integer literal types`() {
        val result = commonize {
            outputTarget("(a, b)")

            simpleSingleSourceTarget(
                "a", """
                    package test                    

                    typealias IntegerType = Int
                    typealias IntegerTypeAlias = IntegerType
                """.trimIndent()
            )

            simpleSingleSourceTarget(
                "b", """
                    package test
                    
                    typealias IntegerType = Long
                    typealias IntegerTypeAlias = IntegerType
                """.trimIndent()
            )
        }

        result.assertCommonized("(a, b)") {
            generatedPhantoms()
            source(
                """
                    package test                    

                    typealias IntegerType = SignedInteger
                    typealias IntegerTypeAlias = IntegerType
                """.trimIndent(),
                name = "test.kt"
            )


        }
    }

    fun `test variable allocation`() {
        val result = commonize {
            outputTarget("(a, b)")

            registerDependency("a", "b", "(a, b)") {
                source(
                    """
                    package kotlinx.cinterop
                    class IntVarOf<T : Int>
                    class LongVarOf<T : Long>
                """.trimIndent()
                )
            }

            simpleSingleSourceTarget(
                "a", """
                package test
                typealias Integer = Int
                typealias IntegerVar = kotlinx.cinterop.IntVarOf<Integer>
            """.trimIndent()
            )

            simpleSingleSourceTarget(
                "b", """
                package test
                
                typealias Integer = Long
                typealias IntegerVar = kotlinx.cinterop.LongVarOf<Integer>
            """.trimIndent()
            )
        }

        result.assertCommonized("(a, b)") {
            generatedPhantoms()
            source(
                name = "test.kt", content = """
                package test
                
                typealias Integer = SignedInteger
                typealias IntegerVar = kotlinx.cinterop.SignedVarOf<Integer>
            """.trimIndent()
            )
        }
    }

    // Int + Long -> SingedInteger; SignedInteger + Long -> SignedInteger
    fun `test two step commonization emits correct phantom supertypes`() {
        val result = commonize {
            outputTarget("(a, b)", "(a, b, c)")

            "a" withSource """
                package test
                
                typealias Integer = Int
            """.trimIndent()

            "b" withSource """
                package test
                
                typealias Integer = Long
            """.trimIndent()

            "c" withSource """
                package test
                
                typealias Integer = Long
            """.trimIndent()
        }

        result.assertCommonized("(a, b)") {
            generatedPhantoms()
            source(
                """
                package test
                
                typealias Integer = SignedInteger
            """.trimIndent()
            )
        }

        result.assertCommonized("(a, b, c)") {
            generatedPhantoms()
            source(
                """
                package test
                
                typealias Integer = SignedInteger
            """.trimIndent()
            )
        }
    }

    // Int + Long -> SingedInteger; SignedInteger + Long -> SignedInteger
    fun `test two step commonization with two intermediate targets and different aliases`() {
        val result = commonize {
            outputTarget("(a, b)", "(c, d)", "(a, b, c, d)")

            "a" withSource """
                package test
                
                typealias A = Int
                typealias Integer = A
            """.trimIndent()

            "b" withSource """
                package test
                
                typealias B = Long
                typealias Integer = B
            """.trimIndent()

            "c" withSource """
                package test
                
                typealias A = Short
                typealias Integer = A
            """.trimIndent()

            "d" withSource """
                package test
                
                typealias C = Int
                typealias Integer = C
            """.trimIndent()
        }

        result.assertCommonized("(a, b)") {
            generatedPhantoms()
            source(
                """
                package test
                
                typealias Integer = SignedInteger
            """.trimIndent()
            )
        }

        result.assertCommonized("(c, d)") {
            generatedPhantoms()
            source(
                """
                package test
                
                typealias Integer = SignedInteger
            """.trimIndent()
            )
        }

        result.assertCommonized("(a, b, c, d)") {
            generatedPhantoms()
            source(
                """
                package test
                
                typealias Integer = SignedInteger
            """.trimIndent()
            )
        }
    }

    fun `test two levels of type aliases in phantom supertypes`() {
        val result = commonize {
            outputTarget("(a, b)", "(a, b, c)")

            "a" withSource """
                typealias Inner = Int
                typealias Outer = Inner
            """.trimIndent()

            "b" withSource """
                typealias Inner = Long
                typealias Outer = Inner
            """.trimIndent()

            "c" withSource """
                typealias Different = Long
                typealias Outer = Different
            """.trimIndent()
        }

        result.assertCommonized("(a, b)") {
            generatedPhantoms()
            source(
                """
                typealias Inner = SignedInteger
                typealias Outer = Inner
            """.trimIndent()
            )
        }

        result.assertCommonized("(a, b, c)") {
            generatedPhantoms()
            source(
                """
                typealias Outer = SignedInteger
            """.trimIndent()
            )
        }
    }

    fun `test no leaf numbers after first commonization for type alias chain`() {
        val result = commonize {
            outputTarget("(a, b)", "(c, d)", "(a, b, c, d)")

            "a" withSource """
                typealias Inner = Int
                typealias Outer = Inner
            """.trimIndent()

            "b" withSource """
                typealias Inner = Long
                typealias Outer = Inner
            """.trimIndent()

            "c" withSource """
                typealias Different = Byte
                typealias Outer = Different
            """.trimIndent()

            "d" withSource """
                typealias Different = Short
                typealias Outer = Different
            """.trimIndent()
        }

        result.assertCommonized("(a, b)") {
            generatedPhantoms()
            source(
                """
                typealias Inner = SignedInteger
                typealias Outer = Inner
            """.trimIndent()
            )
        }

        result.assertCommonized("(c, d)") {
            generatedPhantoms()
            source(
                """
                typealias Different = SignedInteger
                typealias Outer = Different
            """.trimIndent()
            )
        }

        result.assertCommonized("(a, b, c, d)") {
            generatedPhantoms()
            source(
                """
                typealias Outer = SignedInteger
            """.trimIndent()
            )
        }
    }

    // ((LongVar, IntVar), ShortVar), (ByteVar, IntVar))
    fun `test multi-level commonization of chained integer variable type aliases`() {
        val result = commonize {
            outputTarget("(a, b)", "(a, b, c)", "(d, e)", "(a, b, c, d, e)")

            registerFakeStdlibDependency("(a, b)", "(a, b, c)", "(d, e)", "(a, b, c, d, e)")

            "a" withSource """
                import kotlinx.cinterop.*
                
                typealias Ab = Long
                typealias Abc = Ab
                typealias Abcde = Abc
                
                typealias AbVar = LongVarOf<Ab>
                typealias AbcVar = AbVar
                typealias AbcdeVar = AbcVar
            """.trimIndent()

            "b" withSource """
                import kotlinx.cinterop.*
                
                typealias Ab = Int
                typealias Abc = Ab
                typealias Abcde = Abc
                
                typealias AbVar = IntVarOf<Ab>
                typealias AbcVar = AbVar
                typealias AbcdeVar = AbcVar
            """.trimIndent()

            "c" withSource """
                import kotlinx.cinterop.*
                
                typealias Abc = Short
                typealias Abcde = Abc
                
                typealias AbcVar = ShortVarOf<Abc>
                typealias AbcdeVar = AbcVar
            """.trimIndent()

            "d" withSource """
                import kotlinx.cinterop.*
                
                typealias De = Byte
                typealias Abcde = De
                
                typealias DeVar = ByteVarOf<De>
                typealias AbcdeVar = DeVar
            """.trimIndent()

            "e" withSource """
                import kotlinx.cinterop.*
                
                typealias De = Int
                typealias Abcde = De
                
                typealias DeVar = IntVarOf<De>
                typealias AbcdeVar = DeVar
            """.trimIndent()
        }

        result.assertCommonized("(a, b)") {
            generatedPhantoms()
            source(
                """
                typealias Ab = SignedInteger
                typealias Abc = Ab
                typealias Abcde = Abc
                
                typealias AbVar = kotlinx.cinterop.SignedVarOf<Ab>
                typealias AbcVar = AbVar
                typealias AbcdeVar = AbcVar                
            """.trimIndent()
            )
        }

        result.assertCommonized("(a, b, c)") {
            generatedPhantoms()
            source(
                """
                typealias Abc = SignedInteger
                typealias Abcde = Abc
                
                expect class AbcVar : kotlinx.cinterop.SignedVarOf<Abc>
                typealias AbcdeVar = AbcVar                
            """.trimIndent()
            )
        }

        result.assertCommonized("(d, e)") {
            generatedPhantoms()
            source(
                """
                typealias De = SignedInteger
                typealias Abcde = De
                
                expect class DeVar : kotlinx.cinterop.SignedVarOf<De>
                typealias AbcdeVar = DeVar                    
                """.trimIndent()
            )
        }

        result.assertCommonized("(a, b, c, d, e)") {
            generatedPhantoms()
            source(
                """
                typealias Abcde = SignedInteger

                expect class AbcdeVar : kotlinx.cinterop.SignedVarOf<Abcde>
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

internal fun InlineSourceBuilder.ModuleBuilder.generatedPhantoms() {
    dependency {
        source(
            name = "phantomIntegers.kt", content = """
            package kotlin        

            expect interface UnsignedInteger
            expect interface SignedInteger
        """.trimIndent()
        )

        source(
            name = "phantomVariables.kt", content = """
                package kotlinx.cinterop

                open class CVariable
                expect open class SignedVarOf<T : kotlin.SignedInteger> : CVariable
                expect open class UnsignedVarOf<T : kotlin.UnsignedInteger> : CVariable
            """.trimIndent()
        )
    }
}
