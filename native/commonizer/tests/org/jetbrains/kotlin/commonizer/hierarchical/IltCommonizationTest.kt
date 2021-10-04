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

                    typealias ilt_t = Int
                    typealias ilt_t_alias = ilt_t
                """.trimIndent()
            )

            simpleSingleSourceTarget(
                "b", """
                    package test
                    
                    typealias ilt_t = Long
                    typealias ilt_t_alias = ilt_t
                """.trimIndent()
            )
        }

        result.assertCommonized("(a, b)") {
            generatedPhantoms()
            source(
                """
                    package test                    

                    expect class ilt_t : Number(), SignedInteger<ilt_t>
                    typealias ilt_t_alias = ilt_t
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
                expect class Ilt : Number(), SignedInteger<Ilt>
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
                
                expect class Integer : Number(), SignedInteger<Integer>
            """.trimIndent()
            )
        }

        result.assertCommonized("(a, b, c)") {
            generatedPhantoms()
            source(
                """
                package test
                
                expect class Integer : Number(), SignedInteger<Integer>
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
                
                expect class Integer : Number(), SignedInteger<Integer>
            """.trimIndent()
            )
        }

        result.assertCommonized("(c, d)") {
            generatedPhantoms()
            source(
                """
                package test
                
                expect class Integer : Number(), SignedInteger<Integer>
            """.trimIndent()
            )
        }

        result.assertCommonized("(a, b, c, d)") {
            generatedPhantoms()
            source(
                """
                package test
                
                expect class Integer : Number(), SignedInteger<Integer>
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
                expect class Inner : Number(), SignedInteger<Inner>
                typealias Outer = Inner
            """.trimIndent()
            )
        }

        result.assertCommonized("(a, b, c)") {
            generatedPhantoms()
            source(
                """
                expect class Outer : Number(), SignedInteger<Outer>
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
                expect class Inner : Number(), SignedInteger<Inner>
                typealias Outer = Inner
            """.trimIndent()
            )
        }

        result.assertCommonized("(c, d)") {
            generatedPhantoms()
            source(
                """
                expect class Different : Number(), SignedInteger<Different>
                typealias Outer = Different
            """.trimIndent()
            )
        }

        result.assertCommonized("(a, b, c, d)") {
            generatedPhantoms()
            source(
                """
                expect class Outer : Number(), SignedInteger<Outer>
            """.trimIndent()
            )
        }
    }
}

private fun InlineSourceBuilder.ModuleBuilder.generatedPhantoms() {
    dependency {
        source(
            name = "phantomIntegers.kt", content = """
            package kotlin

            class UByte
            class UShort
            class UInt
            class ULong            

            expect interface UnsignedInteger<SELF : UnsignedInteger<SELF>> {
                expect inline infix fun and(other: SELF): SELF 

                expect inline operator fun compareTo(other: UShort): Int 

                expect inline operator fun compareTo(other: UInt): Int 

                expect inline operator fun compareTo(other: UnsignedInteger<*>): Int 

                expect inline operator fun compareTo(other: ULong): Int 

                expect inline operator fun compareTo(other: UByte): Int 

                expect inline operator fun dec(): SELF 

                expect inline operator fun div(other: UShort): UnsignedInteger<*> 

                expect inline operator fun div(other: UInt): UnsignedInteger<*> 

                expect inline operator fun div(other: ULong): UnsignedInteger<*> 

                expect inline operator fun div(other: UnsignedInteger<*>): UnsignedInteger<*> 

                expect inline operator fun div(other: UByte): UnsignedInteger<*> 

                expect inline operator fun inc(): SELF 

                expect inline fun inv(): SELF 

                expect inline operator fun minus(other: UnsignedInteger<*>): UnsignedInteger<*> 

                expect inline operator fun minus(other: ULong): UnsignedInteger<*> 

                expect inline operator fun minus(other: UByte): UnsignedInteger<*> 

                expect inline operator fun minus(other: UShort): UnsignedInteger<*> 

                expect inline operator fun minus(other: UInt): UnsignedInteger<*> 

                expect inline infix fun or(other: SELF): SELF 

                expect inline operator fun plus(other: UByte): UnsignedInteger<*> 

                expect inline operator fun plus(other: UShort): UnsignedInteger<*> 

                expect inline operator fun plus(other: UInt): UnsignedInteger<*> 

                expect inline operator fun plus(other: ULong): UnsignedInteger<*> 

                expect inline operator fun plus(other: UnsignedInteger<*>): UnsignedInteger<*> 

                expect inline operator fun rem(other: UInt): UnsignedInteger<*> 

                expect inline operator fun rem(other: UnsignedInteger<*>): UnsignedInteger<*> 

                expect inline operator fun rem(other: UShort): UnsignedInteger<*> 

                expect inline operator fun rem(other: ULong): UnsignedInteger<*> 

                expect inline operator fun rem(other: UByte): UnsignedInteger<*> 

                expect inline infix fun shl(bitCount: Int): SELF 

                expect inline infix fun shr(bitCount: Int): SELF 

                expect inline operator fun times(other: UInt): UnsignedInteger<*> 

                expect inline operator fun times(other: UnsignedInteger<*>): UnsignedInteger<*> 

                expect inline operator fun times(other: ULong): UnsignedInteger<*> 

                expect inline operator fun times(other: UByte): UnsignedInteger<*> 

                expect inline operator fun times(other: UShort): UnsignedInteger<*> 

                expect inline fun toByte(): Byte 

                expect inline fun toDouble(): Double 

                expect inline fun toFloat(): Float 

                expect inline fun toInt(): Int 

                expect inline fun toLong(): Long 

                expect inline fun toShort(): Short 

                expect inline fun toUByte(): UByte 

                expect inline fun toUInt(): UInt 

                expect inline fun toULong(): ULong 

                expect inline fun toUShort(): UShort 

                expect inline infix fun xor(other: SELF): SELF 
            }

            expect interface SignedInteger<SELF : SignedInteger<SELF>> {
                expect external infix fun and(other: SELF): SELF 

                expect inline operator fun compareTo(other: SignedInteger<*>): Int 

                expect inline operator fun compareTo(other: Byte): Int 

                expect inline operator fun compareTo(other: Long): Int 

                expect inline operator fun compareTo(other: Short): Int 

                expect inline operator fun compareTo(other: Int): Int 

                expect external operator fun dec(): SELF 

                expect inline operator fun div(other: Int): SignedInteger<*> 

                expect inline operator fun div(other: Short): SignedInteger<*> 

                expect inline operator fun div(other: Byte): SignedInteger<*> 

                expect inline operator fun div(other: SignedInteger<*>): SignedInteger<*> 

                expect inline operator fun div(other: Long): SignedInteger<*> 

                expect external operator fun inc(): SELF 

                expect external fun inv(): SELF 

                expect inline operator fun minus(other: SignedInteger<*>): SignedInteger<*> 

                expect inline operator fun minus(other: Byte): SignedInteger<*> 

                expect inline operator fun minus(other: Short): SignedInteger<*> 

                expect inline operator fun minus(other: Long): SignedInteger<*> 

                expect inline operator fun minus(other: Int): SignedInteger<*> 

                expect external infix fun or(other: SELF): SELF 

                expect inline operator fun plus(other: SignedInteger<*>): SignedInteger<*> 

                expect inline operator fun plus(other: Long): SignedInteger<*> 

                expect inline operator fun plus(other: Short): SignedInteger<*> 

                expect inline operator fun plus(other: Byte): SignedInteger<*> 

                expect inline operator fun plus(other: Int): SignedInteger<*> 

                expect inline operator fun rem(other: SignedInteger<*>): SignedInteger<*> 

                expect inline operator fun rem(other: Int): SignedInteger<*> 

                expect inline operator fun rem(other: Long): SignedInteger<*> 

                expect inline operator fun rem(other: Byte): SignedInteger<*> 

                expect inline operator fun rem(other: Short): SignedInteger<*> 

                expect external infix fun shl(bitCount: Int): SELF 

                expect external infix fun shr(bitCount: Int): SELF 

                expect inline operator fun times(other: Short): SignedInteger<*> 

                expect inline operator fun times(other: SignedInteger<*>): SignedInteger<*> 

                expect inline operator fun times(other: Byte): SignedInteger<*> 

                expect inline operator fun times(other: Int): SignedInteger<*> 

                expect inline operator fun times(other: Long): SignedInteger<*> 

                expect inline fun toByte(): Byte 

                expect inline fun toDouble(): Double 

                expect inline fun toFloat(): Float 

                expect inline fun toInt(): Int 

                expect inline fun toLong(): Long 

                expect inline fun toShort(): Short 

                expect inline fun toUByte(): UByte 

                expect inline fun toUInt(): UInt 

                expect inline fun toULong(): ULong 

                expect inline fun toUShort(): UShort 

                expect external infix fun xor(other: SELF): SELF 
            }

        """.trimIndent()
        )
    }

    dependency {
        source(
            name = "phantomVariables.kt", content = """
                package kotlinx.cinterop               

                expect open class SignedVarOf<T : kotlin.SignedInteger<T>> : kotlinx.cinterop.CVariable
                expect open class UnsignedVarOf<T : kotlin.UnsignedInteger<T>> : kotlinx.cinterop.CVariable
            """.trimIndent()
        )
    }
}
