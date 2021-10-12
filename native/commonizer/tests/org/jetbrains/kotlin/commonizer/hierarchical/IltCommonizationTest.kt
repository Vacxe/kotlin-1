/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.hierarchical

import org.jetbrains.kotlin.commonizer.AbstractInlineSourcesCommonizationTest
import org.jetbrains.kotlin.commonizer.assertCommonized
import org.jetbrains.kotlin.commonizer.utils.InlineSourceBuilder

class IltCommonizationTest : AbstractInlineSourcesCommonizationTest() {
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

                    expect class IntegerType : Number, SignedInteger<IntegerType>
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
                typealias Ilt = Int
                typealias IltVar = kotlinx.cinterop.IntVarOf<Ilt>
            """.trimIndent()
            )

            simpleSingleSourceTarget(
                "b", """
                package test
                
                typealias Ilt = Long
                typealias IltVar = kotlinx.cinterop.LongVarOf<Ilt>
            """.trimIndent()
            )
        }

        result.assertCommonized("(a, b)") {
            generatedPhantoms()
            source(
                name = "test.kt", content = """
                package test
                
                expect class Ilt : Number, SignedInteger<Ilt>
                expect class IltVar : kotlinx.cinterop.SignedVarOf<Ilt>
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
                
                expect class Integer : Number, SignedInteger<Integer>
            """.trimIndent()
            )
        }

        result.assertCommonized("(a, b, c)") {
            generatedPhantoms()
            source(
                """
                package test
                
                expect class Integer : Number, SignedInteger<Integer>
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
                
                expect class Integer : Number, SignedInteger<Integer>
            """.trimIndent()
            )
        }

        result.assertCommonized("(c, d)") {
            generatedPhantoms()
            source(
                """
                package test
                
                expect class Integer : Number, SignedInteger<Integer>
            """.trimIndent()
            )
        }

        result.assertCommonized("(a, b, c, d)") {
            generatedPhantoms()
            source(
                """
                package test
                
                expect class Integer : Number, SignedInteger<Integer>
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
                expect class Inner : Number, SignedInteger<Inner>
                typealias Outer = Inner
            """.trimIndent()
            )
        }

        result.assertCommonized("(a, b, c)") {
            generatedPhantoms()
            source(
                """
                expect class Outer : Number, SignedInteger<Outer>
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
                expect class Inner : Number, SignedInteger<Inner>
                typealias Outer = Inner
            """.trimIndent()
            )
        }

        result.assertCommonized("(c, d)") {
            generatedPhantoms()
            source(
                """
                expect class Different : Number, SignedInteger<Different>
                typealias Outer = Different
            """.trimIndent()
            )
        }

        result.assertCommonized("(a, b, c, d)") {
            generatedPhantoms()
            source(
                """
                expect class Outer : Number, SignedInteger<Outer>
            """.trimIndent()
            )
        }
    }
}

internal fun InlineSourceBuilder.ModuleBuilder.generatedPhantoms() {
    dependency {
        source(
            name = "phantomIntegers.kt", content = """
            package kotlin

            class UByte
            class UShort
            class UInt
            class ULong            

            expect interface UnsignedInteger<SELF : UnsignedInteger<SELF>>
            expect interface SignedInteger<SELF : SignedInteger<SELF>>
        """.trimIndent()
        )

        source(
            name = "phantomVariables.kt", content = """
                package kotlinx.cinterop

                open class CVariable
                expect open class SignedVarOf<T : kotlin.SignedInteger<T>> : CVariable
                expect open class UnsignedVarOf<T : kotlin.UnsignedInteger<T>> : CVariable
            """.trimIndent()
        )
    }
}
