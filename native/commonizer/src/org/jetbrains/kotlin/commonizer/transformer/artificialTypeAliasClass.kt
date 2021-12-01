/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.transformer

import org.jetbrains.kotlin.commonizer.cir.*
import org.jetbrains.kotlin.commonizer.cir.SimpleCirSupertypesResolver
import org.jetbrains.kotlin.commonizer.mergedtree.*
import org.jetbrains.kotlin.commonizer.mergedtree.CirNodeRelationship
import org.jetbrains.kotlin.commonizer.mergedtree.CirNodeRelationship.Composite.Companion.plus
import org.jetbrains.kotlin.commonizer.mergedtree.buildClassNode
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.storage.StorageManager

internal typealias ClassNodeIndex = Map<CirEntityId, CirClassNode>

internal fun ClassNodeIndex(module: CirModuleNode): ClassNodeIndex = module.packages.values
    .flatMap { pkg -> pkg.classes.values }
    .associateBy { clazz -> clazz.id }

fun CirPackageNode.createArtificialClassNode(
    typeAliasNode: CirTypeAliasNode,
    storageManager: StorageManager,
    classifiers: CirKnownClassifiers,
): CirClassNode {
    val classNode = buildClassNode(
        storageManager = storageManager,
        size = typeAliasNode.targetDeclarations.size,
        classifiers = classifiers,
        // This artificial class node should only try to commonize if the package node is commonized
        //  and if the original typeAliasNode cannot be commonized.
        //  Therefore, this artificial class node acts as a fallback with the original type-alias being still the preferred
        //  option for commonization
        nodeRelationship = CirNodeRelationship.ParentNode(this) + CirNodeRelationship.PreferredNode(typeAliasNode),
        classId = typeAliasNode.id
    )
    this.classes[typeAliasNode.classifierName] = classNode
    return classNode
}

internal fun CirTypeAlias.toArtificialCirClass(
    classifiers: CirKnownClassifiers,
    targetIndex: Int,
): CirClass =
    CirClass.create(
        annotations = emptyList(), name = name, typeParameters = typeParameters,
        supertypes = resolveSupertypes(classifiers, targetIndex),
        visibility = this.visibility, modality = Modality.FINAL, kind = ClassKind.CLASS,
        companion = null, isCompanion = false, isData = false, isValue = false, isInner = false, isExternal = false
    )

private fun CirTypeAlias.resolveSupertypes(
    classifiers: CirKnownClassifiers,
    targetIndex: Int,
): List<CirType> {
    if (expandedType.isMarkedNullable) return emptyList()

    val resolver = SimpleCirSupertypesResolver(
        classifiers = classifiers.classifierIndices[targetIndex],
        dependencies = CirProvidedClassifiers.of(
            classifiers.commonDependencies, classifiers.targetDependencies[targetIndex]
        )
    )

    return resolver.supertypes(expandedType).toList()
}
