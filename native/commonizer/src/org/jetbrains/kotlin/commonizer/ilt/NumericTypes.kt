/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.ilt

import org.jetbrains.kotlin.commonizer.cir.CirEntityId
import org.jetbrains.kotlin.descriptors.konan.*
import org.jetbrains.kotlin.name.ClassId

object NumericCirEntityIds {
    val SIGNED_INTEGER_IDS: Set<CirEntityId> = SIGNED_INTEGERS.toCirEntityIds()
    val UNSIGNED_INTEGER_IDS: Set<CirEntityId> = UNSIGNED_INTEGERS.toCirEntityIds()
    val FLOATING_POINT_IDS: Set<CirEntityId> = FLOATING_POINTS.toCirEntityIds()
    val SIGNED_VAR_IDS: Set<CirEntityId> = SIGNED_VARS.toCirEntityIds()
    val UNSIGNED_VAR_IDS: Set<CirEntityId> = UNSIGNED_VARS.toCirEntityIds()
    val FLOATING_POINT_VAR_IDS: Set<CirEntityId> = FLOATING_POINT_VARS.toCirEntityIds()
    val PHANTOM_INTEGER_IDS = PHANTOM_INTEGERS.toCirEntityIds()
    val PHANTOM_VARIABLE_IDS = PHANTOM_VARIABLES.toCirEntityIds()

    val INTEGER_IDS = SIGNED_INTEGER_IDS + UNSIGNED_INTEGER_IDS
    val INTEGER_VAR_IDS = SIGNED_VAR_IDS + UNSIGNED_VAR_IDS

    val PHANTOM_TYPE_IDS = PHANTOM_INTEGER_IDS + PHANTOM_VARIABLE_IDS

    private fun Collection<ClassId>.toCirEntityIds(): Set<CirEntityId> =
        map { it.asCirEntityId() }.toSet()
}

fun ClassId.asCirEntityId(): CirEntityId =
    CirEntityId.create(this)
