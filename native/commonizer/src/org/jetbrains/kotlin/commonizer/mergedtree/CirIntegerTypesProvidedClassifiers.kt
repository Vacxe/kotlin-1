/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.mergedtree

import org.jetbrains.kotlin.commonizer.cir.CirEntityId
import org.jetbrains.kotlin.commonizer.ilt.NumericCirEntityIds
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.types.Variance

object CirIntegerTypesProvidedClassifiers : CirProvidedClassifiers {
    override fun hasClassifier(classifierId: CirEntityId): Boolean =
        classifierId in NumericCirEntityIds.PHANTOM_TYPE_IDS

    override fun classifier(classifierId: CirEntityId): CirProvided.Classifier? =
        when (classifierId) {
            in NumericCirEntityIds.PHANTOM_TYPE_IDS -> fakeClassifier
            else -> null
        }

    private val fakeClassifier = CirProvided.RegularClass(
        listOf(CirProvided.TypeParameter(0, Variance.INVARIANT)),
        emptyList(),
        Visibilities.Public,
        ClassKind.INTERFACE, // TODO: split
    )
}
