/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.text.regex

// Access to the decomposition tables. =========================================================================
/** Gets canonical class for given codepoint from decomposition mappings table. */
internal fun getCanonicalClassInternal(ch: Int): Int = TODO()

/** Check if the given character is in table of single decompositions. */
internal fun hasSingleCodepointDecompositionInternal(ch: Int): Boolean = TODO()

/** Returns a decomposition for a given codepoint. */
internal fun getDecompositionInternal(ch: Int): IntArray? = TODO()

/**
 * Decomposes the given string represented as an array of codepoints. Saves the decomposition into [outputCodepoints] array.
 * Returns the length of the decomposition.
 */
internal fun decomposeString(inputCodePoints: IntArray, inputLength: Int, outputCodePoints: IntArray): Int = TODO()

/**
 * Decomposes the given codepoint. Saves the decomposition into [outputCodepoints] array starting with [fromIndex].
 * Returns the length of the decomposition.
 */
internal fun decomposeCodePoint(codePoint: Int, outputCodePoints: IntArray, fromIndex: Int): Int = TODO()
// =============================================================================================================