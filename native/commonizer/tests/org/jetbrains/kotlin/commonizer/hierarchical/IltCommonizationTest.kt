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

                    expect class ilt_t : SignedInteger<ilt_t>
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
                    public class IntVarOf<T : Int>
                    public class LongVarOf<T : Long>
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
                expect class Ilt : SignedInteger<Ilt>
                expect class IltVar : kotlinx.cinterop.SignedVarOf<Ilt>
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

            public open expect interface UnsignedInteger<SELF : UnsignedInteger<SELF>> {
                public final expect inline infix fun and(other: SELF): SELF 

                public final expect inline operator fun compareTo(other: UShort): Int 

                public final expect inline operator fun compareTo(other: UInt): Int 

                public final expect inline operator fun compareTo(other: UnsignedInteger<*>): Int 

                public final expect inline operator fun compareTo(other: ULong): Int 

                public final expect inline operator fun compareTo(other: UByte): Int 

                public final expect inline operator fun dec(): SELF 

                public final expect inline operator fun div(other: UShort): UnsignedInteger<*> 

                public final expect inline operator fun div(other: UInt): UnsignedInteger<*> 

                public final expect inline operator fun div(other: ULong): UnsignedInteger<*> 

                public final expect inline operator fun div(other: UnsignedInteger<*>): UnsignedInteger<*> 

                public final expect inline operator fun div(other: UByte): UnsignedInteger<*> 

                public final expect inline operator fun inc(): SELF 

                public final expect inline fun inv(): SELF 

                public final expect inline operator fun minus(other: UnsignedInteger<*>): UnsignedInteger<*> 

                public final expect inline operator fun minus(other: ULong): UnsignedInteger<*> 

                public final expect inline operator fun minus(other: UByte): UnsignedInteger<*> 

                public final expect inline operator fun minus(other: UShort): UnsignedInteger<*> 

                public final expect inline operator fun minus(other: UInt): UnsignedInteger<*> 

                public final expect inline infix fun or(other: SELF): SELF 

                public final expect inline operator fun plus(other: UByte): UnsignedInteger<*> 

                public final expect inline operator fun plus(other: UShort): UnsignedInteger<*> 

                public final expect inline operator fun plus(other: UInt): UnsignedInteger<*> 

                public final expect inline operator fun plus(other: ULong): UnsignedInteger<*> 

                public final expect inline operator fun plus(other: UnsignedInteger<*>): UnsignedInteger<*> 

                public final expect inline operator fun rem(other: UInt): UnsignedInteger<*> 

                public final expect inline operator fun rem(other: UnsignedInteger<*>): UnsignedInteger<*> 

                public final expect inline operator fun rem(other: UShort): UnsignedInteger<*> 

                public final expect inline operator fun rem(other: ULong): UnsignedInteger<*> 

                public final expect inline operator fun rem(other: UByte): UnsignedInteger<*> 

                public final expect inline infix fun shl(bitCount: Int): SELF 

                public final expect inline infix fun shr(bitCount: Int): SELF 

                public final expect inline operator fun times(other: UInt): UnsignedInteger<*> 

                public final expect inline operator fun times(other: UnsignedInteger<*>): UnsignedInteger<*> 

                public final expect inline operator fun times(other: ULong): UnsignedInteger<*> 

                public final expect inline operator fun times(other: UByte): UnsignedInteger<*> 

                public final expect inline operator fun times(other: UShort): UnsignedInteger<*> 

                public final expect inline fun toByte(): Byte 

                public final expect inline fun toDouble(): Double 

                public final expect inline fun toFloat(): Float 

                public final expect inline fun toInt(): Int 

                public final expect inline fun toLong(): Long 

                public final expect inline fun toShort(): Short 

                public final expect inline fun toUByte(): UByte 

                public final expect inline fun toUInt(): UInt 

                public final expect inline fun toULong(): ULong 

                public final expect inline fun toUShort(): UShort 

                public final expect inline infix fun xor(other: SELF): SELF 
            }

            public open expect interface SignedInteger<SELF : SignedInteger<SELF>> {
                public final external expect infix fun and(other: SELF): SELF 

                public final expect inline operator fun compareTo(other: SignedInteger<*>): Int 

                public final expect inline operator fun compareTo(other: Byte): Int 

                public final expect inline operator fun compareTo(other: Long): Int 

                public final expect inline operator fun compareTo(other: Short): Int 

                public final expect inline operator fun compareTo(other: Int): Int 

                public final external expect operator fun dec(): SELF 

                public final expect inline operator fun div(other: Int): SignedInteger<*> 

                public final expect inline operator fun div(other: Short): SignedInteger<*> 

                public final expect inline operator fun div(other: Byte): SignedInteger<*> 

                public final expect inline operator fun div(other: SignedInteger<*>): SignedInteger<*> 

                public final expect inline operator fun div(other: Long): SignedInteger<*> 

                public final external expect operator fun inc(): SELF 

                public final external expect fun inv(): SELF 

                public final expect inline operator fun minus(other: SignedInteger<*>): SignedInteger<*> 

                public final expect inline operator fun minus(other: Byte): SignedInteger<*> 

                public final expect inline operator fun minus(other: Short): SignedInteger<*> 

                public final expect inline operator fun minus(other: Long): SignedInteger<*> 

                public final expect inline operator fun minus(other: Int): SignedInteger<*> 

                public final external expect infix fun or(other: SELF): SELF 

                public final expect inline operator fun plus(other: SignedInteger<*>): SignedInteger<*> 

                public final expect inline operator fun plus(other: Long): SignedInteger<*> 

                public final expect inline operator fun plus(other: Short): SignedInteger<*> 

                public final expect inline operator fun plus(other: Byte): SignedInteger<*> 

                public final expect inline operator fun plus(other: Int): SignedInteger<*> 

                public final expect inline operator fun rem(other: SignedInteger<*>): SignedInteger<*> 

                public final expect inline operator fun rem(other: Int): SignedInteger<*> 

                public final expect inline operator fun rem(other: Long): SignedInteger<*> 

                public final expect inline operator fun rem(other: Byte): SignedInteger<*> 

                public final expect inline operator fun rem(other: Short): SignedInteger<*> 

                public final external expect infix fun shl(bitCount: Int): SELF 

                public final external expect infix fun shr(bitCount: Int): SELF 

                public final expect inline operator fun times(other: Short): SignedInteger<*> 

                public final expect inline operator fun times(other: SignedInteger<*>): SignedInteger<*> 

                public final expect inline operator fun times(other: Byte): SignedInteger<*> 

                public final expect inline operator fun times(other: Int): SignedInteger<*> 

                public final expect inline operator fun times(other: Long): SignedInteger<*> 

                public final expect inline fun toByte(): Byte 

                public final expect inline fun toDouble(): Double 

                public final expect inline fun toFloat(): Float 

                public final expect inline fun toInt(): Int 

                public final expect inline fun toLong(): Long 

                public final expect inline fun toShort(): Short 

                public final expect inline fun toUByte(): UByte 

                public final expect inline fun toUInt(): UInt 

                public final expect inline fun toULong(): ULong 

                public final expect inline fun toUShort(): UShort 

                public final external expect infix fun xor(other: SELF): SELF 
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