/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package kotlin.native

/**
 * A vector of bits growing if necessary and allowing one to set/clear/read bits from it by a bit index.
 *
 * @constructor creates an empty bit set with the specified [size]
 * @param size the size of one element in the array used to store bits.
 */
public class BitSet(size: Int = ELEMENT_SIZE) : BitSetImpl(size) {
    /**
     * Creates a bit set of given [length] filling elements using [initializer]
     */
    constructor(length: Int, initializer: (Int) -> Boolean): this(length) {
        for (i in 0 until length) {
            set(i, initializer(i))
        }
    }
}