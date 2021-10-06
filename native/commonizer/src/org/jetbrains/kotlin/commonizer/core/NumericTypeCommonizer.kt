/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.core

import org.jetbrains.kotlin.commonizer.cir.CirClassType
import org.jetbrains.kotlin.commonizer.cir.CirEntityId
import org.jetbrains.kotlin.commonizer.cir.CirStarTypeProjection
import org.jetbrains.kotlin.commonizer.ilt.NumericCirEntityIds
import org.jetbrains.kotlin.commonizer.ilt.asCirEntityId
import org.jetbrains.kotlin.descriptors.konan.PHANTOM_SIGNED_INTEGER
import org.jetbrains.kotlin.descriptors.konan.PHANTOM_UNSIGNED_INTEGER
import org.jetbrains.kotlin.utils.SmartList

object NumericTypeCommonizer : AssociativeCommonizer<CirClassType> {
    override fun commonize(first: CirClassType, second: CirClassType): CirClassType? = when {
        listOf(first, second).all { it.classifierId in commonizableSignedIntegerIds } -> phantomSignedInteger
        listOf(first, second).all { it.classifierId in commonizableUnsignedIntegerIds } -> phantomUnsignedInteger
        else -> null
    }

    private val commonizableSignedIntegerIds: Set<CirEntityId> =
        NumericCirEntityIds.SIGNED_INTEGER_IDS + PHANTOM_SIGNED_INTEGER.asCirEntityId()
    private val commonizableUnsignedIntegerIds: Set<CirEntityId> =
        NumericCirEntityIds.UNSIGNED_INTEGER_IDS + PHANTOM_UNSIGNED_INTEGER.asCirEntityId()

    private val phantomSignedInteger: CirClassType = createPhantomNumberType(PHANTOM_SIGNED_INTEGER.asCirEntityId())
    private val phantomUnsignedInteger: CirClassType = createPhantomNumberType(PHANTOM_UNSIGNED_INTEGER.asCirEntityId())

    private fun createPhantomNumberType(id: CirEntityId): CirClassType =
        CirClassType.createInterned(
            classId = id,
            outerType = null,
            arguments = SmartList(CirStarTypeProjection),
            isMarkedNullable = false,
        )
}

