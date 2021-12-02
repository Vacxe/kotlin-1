/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.text

import kotlin.math.abs

// From string.kt


/**
 * Converts the characters in the specified array to a string.
 */
@SinceKotlin("1.2")
@Deprecated("Use CharArray.concatToString() instead", ReplaceWith("chars.concatToString()"))
@DeprecatedSinceKotlin(warningSince = "1.4", errorSince = "1.5")
public actual fun String(chars: CharArray): String {
    var result = ""
    for (char in chars) {
        result += char
    }
    return result
}

/**
 * Converts the characters from a portion of the specified array to a string.
 *
 * @throws IndexOutOfBoundsException if either [offset] or [length] are less than zero
 * or `offset + length` is out of [chars] array bounds.
 */
@SinceKotlin("1.2")
@Deprecated("Use CharArray.concatToString(startIndex, endIndex) instead", ReplaceWith("chars.concatToString(offset, offset + length)"))
@DeprecatedSinceKotlin(warningSince = "1.4", errorSince = "1.5")
public actual fun String(chars: CharArray, offset: Int, length: Int): String {
    if (offset < 0 || length < 0 || chars.size - offset < length)
        throw IndexOutOfBoundsException("size: ${chars.size}; offset: $offset; length: $length")
    var result = ""
    for (index in offset until offset + length) {
        result += chars[index]
    }
    return result
}

/**
 * Concatenates characters in this [CharArray] into a String.
 */
@SinceKotlin("1.4")
@WasExperimental(ExperimentalStdlibApi::class)
public actual fun CharArray.concatToString(): String {
    var result = ""
    for (char in this) {
        result += char
    }
    return result
}

/**
 * Concatenates characters in this [CharArray] or its subrange into a String.
 *
 * @param startIndex the beginning (inclusive) of the subrange of characters, 0 by default.
 * @param endIndex the end (exclusive) of the subrange of characters, size of this array by default.
 *
 * @throws IndexOutOfBoundsException if [startIndex] is less than zero or [endIndex] is greater than the size of this array.
 * @throws IllegalArgumentException if [startIndex] is greater than [endIndex].
 */
@SinceKotlin("1.4")
@WasExperimental(ExperimentalStdlibApi::class)
@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
public actual fun CharArray.concatToString(startIndex: Int = 0, endIndex: Int = this.size): String {
    AbstractList.checkBoundsIndexes(startIndex, endIndex, this.size)
    var result = ""
    for (index in startIndex until endIndex) {
        result += this[index]
    }
    return result
}

/**
 * Returns a [CharArray] containing characters of this string.
 */
@SinceKotlin("1.4")
@WasExperimental(ExperimentalStdlibApi::class)
public actual fun String.toCharArray(): CharArray {
    return CharArray(length) { get(it) }
}

/**
 * Returns a [CharArray] containing characters of this string or its substring.
 *
 * @param startIndex the beginning (inclusive) of the substring, 0 by default.
 * @param endIndex the end (exclusive) of the substring, length of this string by default.
 *
 * @throws IndexOutOfBoundsException if [startIndex] is less than zero or [endIndex] is greater than the length of this string.
 * @throws IllegalArgumentException if [startIndex] is greater than [endIndex].
 */
@SinceKotlin("1.4")
@WasExperimental(ExperimentalStdlibApi::class)
@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
public actual fun String.toCharArray(startIndex: Int = 0, endIndex: Int = this.length): CharArray {
    AbstractList.checkBoundsIndexes(startIndex, endIndex, length)
    return CharArray(endIndex - startIndex) { get(startIndex + it) }
}

/**
 * Decodes a string from the bytes in UTF-8 encoding in this array.
 *
 * Malformed byte sequences are replaced by the replacement char `\uFFFD`.
 */
@SinceKotlin("1.4")
@WasExperimental(ExperimentalStdlibApi::class)
public actual fun ByteArray.decodeToString(): String {
    return decodeUtf8(this, 0, size, false)
}

/**
 * Decodes a string from the bytes in UTF-8 encoding in this array or its subrange.
 *
 * @param startIndex the beginning (inclusive) of the subrange to decode, 0 by default.
 * @param endIndex the end (exclusive) of the subrange to decode, size of this array by default.
 * @param throwOnInvalidSequence specifies whether to throw an exception on malformed byte sequence or replace it by the replacement char `\uFFFD`.
 *
 * @throws IndexOutOfBoundsException if [startIndex] is less than zero or [endIndex] is greater than the size of this array.
 * @throws IllegalArgumentException if [startIndex] is greater than [endIndex].
 * @throws CharacterCodingException if the byte array contains malformed UTF-8 byte sequence and [throwOnInvalidSequence] is true.
 */
@SinceKotlin("1.4")
@WasExperimental(ExperimentalStdlibApi::class)
@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
public actual fun ByteArray.decodeToString(
    startIndex: Int = 0,
    endIndex: Int = this.size,
    throwOnInvalidSequence: Boolean = false
): String {
    AbstractList.checkBoundsIndexes(startIndex, endIndex, this.size)
    return decodeUtf8(this, startIndex, endIndex, throwOnInvalidSequence)
}

/**
 * Encodes this string to an array of bytes in UTF-8 encoding.
 *
 * Any malformed char sequence is replaced by the replacement byte sequence.
 */
@SinceKotlin("1.4")
@WasExperimental(ExperimentalStdlibApi::class)
public actual fun String.encodeToByteArray(): ByteArray {
    return encodeUtf8(this, 0, length, false)
}

/**
 * Encodes this string or its substring to an array of bytes in UTF-8 encoding.
 *
 * @param startIndex the beginning (inclusive) of the substring to encode, 0 by default.
 * @param endIndex the end (exclusive) of the substring to encode, length of this string by default.
 * @param throwOnInvalidSequence specifies whether to throw an exception on malformed char sequence or replace.
 *
 * @throws IndexOutOfBoundsException if [startIndex] is less than zero or [endIndex] is greater than the length of this string.
 * @throws IllegalArgumentException if [startIndex] is greater than [endIndex].
 * @throws CharacterCodingException if this string contains malformed char sequence and [throwOnInvalidSequence] is true.
 */
@SinceKotlin("1.4")
@WasExperimental(ExperimentalStdlibApi::class)
@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
public actual fun String.encodeToByteArray(
    startIndex: Int = 0,
    endIndex: Int = this.length,
    throwOnInvalidSequence: Boolean = false
): ByteArray {
    AbstractList.checkBoundsIndexes(startIndex, endIndex, length)
    return encodeUtf8(this, startIndex, endIndex, throwOnInvalidSequence)
}

/**
 * Returns a substring of this string that starts at the specified [startIndex] and continues to the end of the string.
 */
public actual fun String.substring(startIndex: Int): String =
    subSequence(startIndex, this.length) as String

/**
 * Returns the substring of this string starting at the [startIndex] and ending right before the [endIndex].
 *
 * @param startIndex the start index (inclusive).
 * @param endIndex the end index (exclusive).
 */
public actual fun String.substring(startIndex: Int, endIndex: Int): String =
    subSequence(startIndex, endIndex) as String

/**
 * Returns a copy of this string converted to upper case using the rules of the default locale.
 */
@Deprecated("Use uppercase() instead.", ReplaceWith("uppercase()"))
@DeprecatedSinceKotlin(warningSince = "1.5")
public actual fun String.toUpperCase(): String = uppercase()

/**
 * Returns a copy of this string converted to upper case using Unicode mapping rules of the invariant locale.
 *
 * This function supports one-to-many and many-to-one character mapping,
 * thus the length of the returned string can be different from the length of the original string.
 *
 * @sample samples.text.Strings.uppercase
 */
@SinceKotlin("1.5")
public actual fun String.uppercase(): String = uppercaseImpl()

/**
 * Returns a copy of this string converted to lower case using the rules of the default locale.
 */
@Deprecated("Use lowercase() instead.", ReplaceWith("lowercase()"))
@DeprecatedSinceKotlin(warningSince = "1.5")
public actual fun String.toLowerCase(): String = lowercase()

/**
 * Returns a copy of this string converted to lower case using Unicode mapping rules of the invariant locale.
 *
 * This function supports one-to-many and many-to-one character mapping,
 * thus the length of the returned string can be different from the length of the original string.
 *
 * @sample samples.text.Strings.lowercase
 */
@SinceKotlin("1.5")
public actual fun String.lowercase(): String = lowercaseImpl()

/**
 * Returns a copy of this string having its first letter titlecased using the rules of the default locale,
 * or the original string if it's empty or already starts with a title case letter.
 *
 * The title case of a character is usually the same as its upper case with several exceptions.
 * The particular list of characters with the special title case form depends on the underlying platform.
 *
 * @sample samples.text.Strings.capitalize
 */
@Deprecated("Use replaceFirstChar instead.", ReplaceWith("replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }"))
@DeprecatedSinceKotlin(warningSince = "1.5")
public actual fun String.capitalize(): String = replaceFirstChar(Char::uppercaseChar)

/**
 * Returns a copy of this string having its first letter lowercased using the rules of the default locale,
 * or the original string if it's empty or already starts with a lower case letter.
 *
 * @sample samples.text.Strings.decapitalize
 */
@Deprecated("Use replaceFirstChar instead.", ReplaceWith("replaceFirstChar { it.lowercase() }"))
@DeprecatedSinceKotlin(warningSince = "1.5")
public actual fun String.decapitalize(): String = replaceFirstChar(Char::lowercaseChar)

/**
 * Returns a string containing this char sequence repeated [n] times.
 * @throws [IllegalArgumentException] when n < 0.
 * @sample samples.text.Strings.repeat
 */
public actual fun CharSequence.repeat(n: Int): String {
    require(n >= 0) { "Count 'n' must be non-negative, but was $n." }
    return when (n) {
        0 -> ""
        1 -> this.toString()
        else -> {
            var result = ""
            if (!isEmpty()) {
                var s = this.toString()
                var count = n
                while (true) {
                    if ((count and 1) == 1) {
                        result += s
                    }
                    count = count ushr 1
                    if (count == 0) {
                        break
                    }
                    s += s
                }
            }
            return result
        }
    }
}

/**
 * Returns a new string with all occurrences of [oldChar] replaced with [newChar].
 */
@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
public actual fun String.replace(oldChar: Char, newChar: Char, ignoreCase: Boolean = false): String {
    return buildString(length) {
        this@replace.forEach { c ->
            append(if (c.equals(oldChar, ignoreCase)) newChar else c)
        }
    }
}

/**
 * Returns a new string obtained by replacing all occurrences of the [oldValue] substring in this string
 * with the specified [newValue] string.
 */
@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
public actual fun String.replace(oldValue: String, newValue: String, ignoreCase: Boolean = false): String {
    run {
        var occurrenceIndex: Int = indexOf(oldValue, 0, ignoreCase)
        // FAST PATH: no match
        if (occurrenceIndex < 0) return this

        val oldValueLength = oldValue.length
        val searchStep = oldValueLength.coerceAtLeast(1)
        val newLengthHint = length - oldValueLength + newValue.length
        if (newLengthHint < 0) throw OutOfMemoryError()
        val stringBuilder = StringBuilder(newLengthHint)

        var i = 0
        do {
            stringBuilder.append(this, i, occurrenceIndex).append(newValue)
            i = occurrenceIndex + oldValueLength
            if (occurrenceIndex >= length) break
            occurrenceIndex = indexOf(oldValue, occurrenceIndex + searchStep, ignoreCase)
        } while (occurrenceIndex > 0)
        return stringBuilder.append(this, i, length).toString()
    }
}

/**
 * Returns a new string with the first occurrence of [oldChar] replaced with [newChar].
 */
@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
public actual fun String.replaceFirst(oldChar: Char, newChar: Char, ignoreCase: Boolean = false): String {
    val index = indexOf(oldChar, ignoreCase = ignoreCase)
    return if (index < 0) this else this.replaceRange(index, index + 1, newChar.toString())
}

/**
 * Returns a new string obtained by replacing the first occurrence of the [oldValue] substring in this string
 * with the specified [newValue] string.
 */
@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
public actual fun String.replaceFirst(oldValue: String, newValue: String, ignoreCase: Boolean = false): String {
    val index = indexOf(oldValue, ignoreCase = ignoreCase)
    return if (index < 0) this else this.replaceRange(index, index + oldValue.length, newValue)
}

/**
 * Returns `true` if this string is equal to [other], optionally ignoring character case.
 *
 * Two strings are considered to be equal if they have the same length and the same character at the same index.
 * If [ignoreCase] is true, the result of `Char.uppercaseChar().lowercaseChar()` on each character is compared.
 *
 * @param ignoreCase `true` to ignore character case when comparing strings. By default `false`.
 */
@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
public actual fun String?.equals(other: String?, ignoreCase: Boolean = false): Boolean {
    if (this == null) return other == null
    if (other == null) return false
    if (!ignoreCase) return this == other

    if (this.length != other.length) return false

    for (index in 0 until this.length) {
        val thisChar = this[index]
        val otherChar = other[index]
        if (!thisChar.equals(otherChar, ignoreCase)) {
            return false
        }
    }

    return true
}

/**
 * Compares two strings lexicographically, optionally ignoring case differences.
 *
 * If [ignoreCase] is true, the result of `Char.uppercaseChar().lowercaseChar()` on each character is compared.
 */
@SinceKotlin("1.2")
@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
public actual fun String.compareTo(other: String, ignoreCase: Boolean = false): Int {
    if (ignoreCase) {
        val n1 = this.length
        val n2 = other.length
        val min = minOf(n1, n2)
        if (min == 0) return n1 - n2
        for (index in 0 until min) {
            var thisChar = this[index]
            var otherChar = other[index]

            if (thisChar != otherChar) {
                thisChar = thisChar.uppercaseChar()
                otherChar = otherChar.uppercaseChar()

                if (thisChar != otherChar) {
                    thisChar = thisChar.lowercaseChar()
                    otherChar = otherChar.lowercaseChar()

                    if (thisChar != otherChar) {
                        return thisChar.compareTo(otherChar)
                    }
                }
            }
        }
        return n1 - n2
    } else {
        return compareTo(other)
    }
}

/**
 * Returns `true` if the contents of this char sequence are equal to the contents of the specified [other],
 * i.e. both char sequences contain the same number of the same characters in the same order.
 *
 * @sample samples.text.Strings.contentEquals
 */
@SinceKotlin("1.5")
public actual infix fun CharSequence?.contentEquals(other: CharSequence?): Boolean = contentEqualsImpl(other)

/**
 * Returns `true` if the contents of this char sequence are equal to the contents of the specified [other], optionally ignoring case difference.
 *
 * @param ignoreCase `true` to ignore character case when comparing contents.
 *
 * @sample samples.text.Strings.contentEquals
 */
@SinceKotlin("1.5")
public actual fun CharSequence?.contentEquals(other: CharSequence?, ignoreCase: Boolean): Boolean {
    return if (ignoreCase)
        this.contentEqualsIgnoreCaseImpl(other)
    else
        this.contentEqualsImpl(other)
}

/**
 * Returns `true` if this string starts with the specified prefix.
 */
@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
public actual fun String.startsWith(prefix: String, ignoreCase: Boolean = false): Boolean =
    regionMatches(0, prefix, 0, prefix.length, ignoreCase)

/**
 * Returns `true` if a substring of this string starting at the specified offset [startIndex] starts with the specified prefix.
 */
@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
public actual fun String.startsWith(prefix: String, startIndex: Int, ignoreCase: Boolean = false): Boolean =
    regionMatches(startIndex, prefix, 0, prefix.length, ignoreCase)

/**
 * Returns `true` if this string ends with the specified suffix.
 */
@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
public actual fun String.endsWith(suffix: String, ignoreCase: Boolean = false): Boolean =
    regionMatches(length - suffix.length, suffix, 0, suffix.length, ignoreCase)

// From stringsCode.kt

/**
 * Returns `true` if this string is empty or consists solely of whitespace characters.
 *
 * @sample samples.text.Strings.stringIsBlank
 */
public actual fun CharSequence.isBlank(): Boolean = length == 0 || indices.all { this[it].isWhitespace() }

/**
 * Returns `true` if the specified range in this char sequence is equal to the specified range in another char sequence.
 * @param thisOffset the start offset in this char sequence of the substring to compare.
 * @param other the string against a substring of which the comparison is performed.
 * @param otherOffset the start offset in the other char sequence of the substring to compare.
 * @param length the length of the substring to compare.
 */
actual fun CharSequence.regionMatches(
    thisOffset: Int,
    other: CharSequence,
    otherOffset: Int,
    length: Int,
    ignoreCase: Boolean
): Boolean {
    if ((otherOffset < 0) || (thisOffset < 0) || (thisOffset > this.length - length) || (otherOffset > other.length - length)) {
        return false
    }

    for (index in 0 until length) {
        if (!this[thisOffset + index].equals(other[otherOffset + index], ignoreCase))
            return false
    }
    return true
}

private val STRING_CASE_INSENSITIVE_ORDER = Comparator<String> { a, b -> a.compareTo(b, ignoreCase = true) }

/**
 * A Comparator that orders strings ignoring character case.
 *
 * Note that this Comparator does not take locale into account,
 * and will result in an unsatisfactory ordering for certain locales.
 */
@SinceKotlin("1.2")
public actual val String.Companion.CASE_INSENSITIVE_ORDER: Comparator<String>
    get() = STRING_CASE_INSENSITIVE_ORDER

/**
 * Returns `true` if the content of this string is equal to the word "true", ignoring case, and `false` otherwise.
 */
@Deprecated("Use Kotlin compiler 1.4 to avoid deprecation warning.")
@DeprecatedSinceKotlin(hiddenSince = "1.4")
@kotlin.internal.InlineOnly
actual fun String.toBoolean(): Boolean = this.toBoolean()

/**
 * Returns `true` if the contents of this string is equal to the word "true", ignoring case, and `false` otherwise.
 *
 * There are also strict versions of the function available on non-nullable String, [toBooleanStrict] and [toBooleanStrictOrNull].
 */
actual fun String?.toBoolean(): Boolean = this != null && this.lowercase() == "true"

/**
 * Parses the string as a signed [Byte] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 */
actual fun String.toByte(): Byte = toByteOrNull() ?: numberFormatError(this)

/**
 * Parses the string as a signed [Byte] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 * @throws IllegalArgumentException when [radix] is not a valid radix for string to number conversion.
 */
public actual fun String.toByte(radix: Int): Byte = toByteOrNull(radix) ?: numberFormatError(this)

/**
 * Parses the string as a [Short] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 */
public actual fun String.toShort(): Short = toShortOrNull() ?: numberFormatError(this)

/**
 * Parses the string as a [Short] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 * @throws IllegalArgumentException when [radix] is not a valid radix for string to number conversion.
 */
public actual fun String.toShort(radix: Int): Short = toShortOrNull(radix) ?: numberFormatError(this)

/**
 * Parses the string as an [Int] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 */
public actual fun String.toInt(): Int = toIntOrNull() ?: numberFormatError(this)

/**
 * Parses the string as an [Int] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 * @throws IllegalArgumentException when [radix] is not a valid radix for string to number conversion.
 */
public actual fun String.toInt(radix: Int): Int = toIntOrNull(radix) ?: numberFormatError(this)

/**
 * Parses the string as a [Long] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 */
public actual fun String.toLong(): Long = toLongOrNull() ?: numberFormatError(this)

/**
 * Parses the string as a [Long] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 * @throws IllegalArgumentException when [radix] is not a valid radix for string to number conversion.
 */
public actual fun String.toLong(radix: Int): Long = toLongOrNull(radix) ?: numberFormatError(this)

/**
 * Parses the string as a [Double] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 */
public actual fun String.toDouble(): Double = kotlin.text.parseDouble(this)

/**
 * Parses the string as a [Float] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 */
public actual fun String.toFloat(): Float = toDouble() as Float

/**
 * Parses the string as a [Float] number and returns the result
 * or `null` if the string is not a valid representation of a number.
 */
public actual fun String.toFloatOrNull(): Float? = toDoubleOrNull() as Float?

/**
 * Parses the string as a [Double] number and returns the result
 * or `null` if the string is not a valid representation of a number.
 */
public actual fun String.toDoubleOrNull(): Double? {
    try {
        return toDouble()
    } catch (e: NumberFormatException) {
        return null
    }
}

/**
 * Returns a string representation of this [Byte] value in the specified [radix].
 *
 * @throws IllegalArgumentException when [radix] is not a valid radix for number to string conversion.
 */
@SinceKotlin("1.2")
public actual fun Byte.toString(radix: Int): String = this.toLong().toString(radix)

/**
 * Returns a string representation of this [Short] value in the specified [radix].
 *
 * @throws IllegalArgumentException when [radix] is not a valid radix for number to string conversion.
 */
@SinceKotlin("1.2")
public actual fun Short.toString(radix: Int): String = this.toLong().toString(radix)

/**
 * Returns a string representation of this [Int] value in the specified [radix].
 *
 * @throws IllegalArgumentException when [radix] is not a valid radix for number to string conversion.
 */
@SinceKotlin("1.2")
actual fun Int.toString(radix: Int): String = toLong().toString(radix)

/**
 * Returns a string representation of this [Long] value in the specified [radix].
 *
 * @throws IllegalArgumentException when [radix] is not a valid radix for number to string conversion.
 */
@SinceKotlin("1.2")
actual fun Long.toString(radix: Int): String {
    checkRadix(radix)

    fun Long.getChar() = toInt().let { if (it < 10) '0' + it else 'a' + (it - 10) }

    if (radix == 10) return toString()
    if (this in 0 until radix) return getChar().toString()

    val isNegative = this < 0
    val buffer = CharArray(Long.SIZE_BITS + 1)

    var currentBufferIndex= buffer.lastIndex
    var current: Long = this
    while(current != 0L) {
        buffer[currentBufferIndex] = abs(current % radix).getChar()
        current /= radix
        currentBufferIndex--
    }

    if (isNegative) {
        buffer[currentBufferIndex] = '-'
        currentBufferIndex--
    }

    return buffer.concatToString(currentBufferIndex + 1)
}