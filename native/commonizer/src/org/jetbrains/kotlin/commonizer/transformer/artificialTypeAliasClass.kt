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
import org.jetbrains.kotlin.commonizer.transformer.phantom.withPhantomIntegerSupertypes
import org.jetbrains.kotlin.commonizer.transformer.phantom.withPhantomVarOfSupertypes
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.storage.StorageManager

internal typealias ClassNodeIndex = Map<CirEntityId, CirClassNode>

internal fun ClassNodeIndex(module: CirModuleNode): ClassNodeIndex = module.packages.values
    .flatMap { pkg -> pkg.classes.values }
    .associateBy { clazz -> clazz.id }

sealed class TypeAliasToClassConversion(
    val classifiers: CirKnownClassifiers,
    val targetIndex: Int,
) {
    class Inline(
        classifiers: CirKnownClassifiers,
        targetIndex: Int,
    ) : TypeAliasToClassConversion(classifiers, targetIndex)

    class PhantomInteger(
        classifiers: CirKnownClassifiers,
        targetIndex: Int,
        val typeAliasNode: CirTypeAliasNode,
    ) : TypeAliasToClassConversion(classifiers, targetIndex)

    class PhantomVarOf(
        classifiers: CirKnownClassifiers,
        targetIndex: Int,
        val originalTypeArgument: CirTypeAliasType,
    ) : TypeAliasToClassConversion(classifiers, targetIndex)
}

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
    typeAliasConversion: TypeAliasToClassConversion
): CirClass =
    CirClass.create(
        annotations = emptyList(), name = name, typeParameters = typeParameters,
        supertypes = resolveSupertypes(typeAliasConversion),
        visibility = this.visibility, modality = Modality.FINAL, kind = ClassKind.CLASS,
        companion = null, isCompanion = false, isData = false, isValue = false, isInner = false, isExternal = false
    )

private fun CirTypeAlias.resolveSupertypes(conversion: TypeAliasToClassConversion): List<CirType> = with(conversion) {
    if (expandedType.isMarkedNullable) return emptyList()
    val resolver = SimpleCirSupertypesResolver(
        classifiers = classifiers.classifierIndices[targetIndex],
        dependencies = CirProvidedClassifiers.of(
            classifiers.commonDependencies, classifiers.targetDependencies[targetIndex]
        )
    ).let { simpleSupertypeResolver ->
        when (conversion) {
            is TypeAliasToClassConversion.Inline -> simpleSupertypeResolver
            is TypeAliasToClassConversion.PhantomInteger ->
                simpleSupertypeResolver.withPhantomIntegerSupertypes(conversion.typeAliasNode)
            is TypeAliasToClassConversion.PhantomVarOf ->
                simpleSupertypeResolver.withPhantomVarOfSupertypes(conversion.originalTypeArgument)
        }
    }

    return resolver.supertypes(expandedType).toList()
}
