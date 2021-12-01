/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.core

import org.jetbrains.kotlin.commonizer.cir.*
import org.jetbrains.kotlin.commonizer.ilt.NumericCirEntityIds
import org.jetbrains.kotlin.commonizer.ilt.asCirEntityId
import org.jetbrains.kotlin.descriptors.konan.PHANTOM_SIGNED_INTEGER
import org.jetbrains.kotlin.descriptors.konan.PHANTOM_SIGNED_VAR_OF
import org.jetbrains.kotlin.descriptors.konan.PHANTOM_UNSIGNED_INTEGER
import org.jetbrains.kotlin.descriptors.konan.PHANTOM_UNSIGNED_VAR_OF
import org.jetbrains.kotlin.utils.SmartList

class NumericTypeCommonizer(
    typeCommonizer: TypeCommonizer
) : AssociativeCommonizer<CirClassOrTypeAliasType> {

    private val typeArgumentListCommonizer = TypeArgumentListCommonizer(typeCommonizer)

    override fun commonize(first: CirClassOrTypeAliasType, second: CirClassOrTypeAliasType): CirClassOrTypeAliasType? {
        val twoTypes = listOf(first, second)
        return when {
            twoTypes.all { it.classifierId in commonizableSignedIntegerIds } -> phantomSignedInteger
            twoTypes.all { it.classifierId in commonizableUnsignedIntegerIds } -> phantomUnsignedInteger
            twoTypes.all { it.classifierId in commonizableSignedVarIds } -> commonizeSignedVarOf(first, second, isSigned = true)
            twoTypes.all { it.classifierId in commonizableUnsignedVarIds } -> commonizeSignedVarOf(first, second, isSigned = false)
            else -> null
        }
    }

    private fun commonizeSignedVarOf(
        first: CirClassOrTypeAliasType,
        second: CirClassOrTypeAliasType,
        isSigned: Boolean
    ): CirClassOrTypeAliasType? {
        if (first !is CirClassType || second !is CirClassType) return null

        val argument = typeArgumentListCommonizer.commonize(listOf(first.arguments, second.arguments))?.singleOrNull()
            ?: return null

        return createCommonVarOfType(isSigned = isSigned, argument = argument)
    }

    private fun createCommonVarOfType(isSigned: Boolean, argument: CirTypeProjection): CirClassType {
        return CirClassType.createInterned(
            classId = if (isSigned) SIGNED_VAR_OF_ID else UNSIGNED_VAR_OF_ID,
            outerType = null,
            arguments = SmartList(argument),
            isMarkedNullable = false,
        )
    }

    private val commonizableSignedIntegerIds: Set<CirEntityId> =
        NumericCirEntityIds.SIGNED_INTEGER_IDS + SIGNED_INTEGER_ID
    private val commonizableUnsignedIntegerIds: Set<CirEntityId> =
        NumericCirEntityIds.UNSIGNED_INTEGER_IDS + UNSIGNED_INTEGER_ID
    private val commonizableSignedVarIds: Set<CirEntityId> =
        NumericCirEntityIds.SIGNED_VAR_IDS + SIGNED_VAR_OF_ID
    private val commonizableUnsignedVarIds: Set<CirEntityId> =
        NumericCirEntityIds.UNSIGNED_VAR_IDS + UNSIGNED_VAR_OF_ID

    private val phantomSignedInteger: CirClassType = createPhantomNumberType(SIGNED_INTEGER_ID)
    private val phantomUnsignedInteger: CirClassType = createPhantomNumberType(UNSIGNED_INTEGER_ID)

    private fun createPhantomNumberType(id: CirEntityId): CirClassType =
        CirClassType.createInterned(
            classId = id,
            outerType = null,
            arguments = emptyList(),
            isMarkedNullable = false,
        )
}

private val SIGNED_INTEGER_ID: CirEntityId get() = PHANTOM_SIGNED_INTEGER.asCirEntityId()
private val SIGNED_VAR_OF_ID: CirEntityId get() = PHANTOM_SIGNED_VAR_OF.asCirEntityId()
private val UNSIGNED_INTEGER_ID: CirEntityId get() = PHANTOM_UNSIGNED_INTEGER.asCirEntityId()
private val UNSIGNED_VAR_OF_ID: CirEntityId get() = PHANTOM_UNSIGNED_VAR_OF.asCirEntityId()
