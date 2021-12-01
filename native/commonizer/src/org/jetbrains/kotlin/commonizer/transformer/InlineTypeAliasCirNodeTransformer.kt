/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.transformer

import org.jetbrains.kotlin.commonizer.cir.*
import org.jetbrains.kotlin.commonizer.mergedtree.*
import org.jetbrains.kotlin.commonizer.mergedtree.CirNodeRelationship.ParentNode
import org.jetbrains.kotlin.storage.StorageManager

internal data class TypeAliasTransformationContext(
    val classNodeIndex: ClassNodeIndex,
    val packageNode: CirPackageNode?,
) {
    companion object {
        val Empty = TypeAliasTransformationContext(
            classNodeIndex = emptyMap(),
            packageNode = null,
        )
    }
}

internal class InlineTypeAliasCirNodeTransformer(
    private val storageManager: StorageManager,
    private val classifiers: CirKnownClassifiers,
) : AbstractCirNodeTransformer<TypeAliasTransformationContext>() {
    override fun newTransformationContext(root: CirRootNode) = TypeAliasTransformationContext.Empty

    override fun beforeModule(
        moduleNode: CirModuleNode,
        moduleName: CirName,
        context: TypeAliasTransformationContext,
    ): TypeAliasTransformationContext =
        context.copy(classNodeIndex = ClassNodeIndex(moduleNode))

    override fun beforePackage(packageNode: CirPackageNode, context: TypeAliasTransformationContext): TypeAliasTransformationContext =
        context.copy(packageNode = packageNode)

    override fun transformTypeAlias(typeAliasNode: CirTypeAliasNode, context: TypeAliasTransformationContext) = with(context) {
        require(packageNode != null) { "Package node is empty during type alias transformation" }

        val targetClassNode = classNodeIndex[typeAliasNode.id]
            ?: packageNode.createArtificialClassNode(typeAliasNode, storageManager, classifiers)
        inlineTypeAliasIfPossible(classNodeIndex, typeAliasNode, targetClassNode)
    }

    private fun inlineTypeAliasIfPossible(classes: ClassNodeIndex, fromTypeAliasNode: CirTypeAliasNode, intoClassNode: CirClassNode) {
        fromTypeAliasNode.targetDeclarations.forEachIndexed { targetIndex, typeAlias ->
            if (typeAlias != null) {
                inlineTypeAliasIfPossible(classes, typeAlias, intoClassNode, targetIndex)
            }
        }
    }

    private fun inlineTypeAliasIfPossible(
        classes: ClassNodeIndex, fromTypeAlias: CirTypeAlias, intoClassNode: CirClassNode, targetIndex: Int
    ) {
        if (fromTypeAlias.typeParameters.isNotEmpty()) {
            // Inlining parameterized TAs is not supported yet
            return
        }

        if (fromTypeAlias.underlyingType.arguments.isNotEmpty() ||
            fromTypeAlias.underlyingType.run { this as? CirClassType }?.outerType?.arguments?.isNotEmpty() == true
        ) {
            // Inlining TAs with parameterized underlying types is not supported yet
            return
        }

        if (intoClassNode.targetDeclarations[targetIndex] != null) {
            // No empty spot to inline the type-alias into
            return
        }

        val fromAliasedClassNode = classes[fromTypeAlias.expandedType.classifierId]

        val intoArtificialClass = ArtificialAliasedCirClass(
            pointingTypeAlias = fromTypeAlias,
            pointedClass = fromAliasedClassNode?.targetDeclarations?.get(targetIndex)
                ?: fromTypeAlias.toArtificialCirClass(classifiers, targetIndex)
        )

        intoClassNode.targetDeclarations[targetIndex] = intoArtificialClass

        if (fromAliasedClassNode != null && !fromTypeAlias.expandedType.isMarkedNullable) {
            inlineArtificialMembers(fromAliasedClassNode, intoClassNode, intoArtificialClass, targetIndex)
        }
    }

    private fun inlineArtificialMembers(
        fromAliasedClassNode: CirClassNode,
        intoClassNode: CirClassNode,
        intoClass: CirClass,
        targetIndex: Int
    ) {
        val targetSize = intoClassNode.targetDeclarations.size

        fromAliasedClassNode.constructors.forEach { (key, aliasedConstructorNode) ->
            val aliasedConstructor = aliasedConstructorNode.targetDeclarations[targetIndex] ?: return@forEach
            intoClassNode.constructors.getOrPut(key) {
                buildClassConstructorNode(storageManager, targetSize, classifiers, ParentNode(intoClassNode))
            }.targetDeclarations[targetIndex] = aliasedConstructor.withContainingClass(intoClass)
        }

        fromAliasedClassNode.functions.forEach { (key, aliasedFunctionNode) ->
            val aliasedFunction = aliasedFunctionNode.targetDeclarations[targetIndex] ?: return@forEach
            intoClassNode.functions.getOrPut(key) {
                buildFunctionNode(storageManager, targetSize, classifiers, ParentNode(intoClassNode))
            }.targetDeclarations[targetIndex] = aliasedFunction.withContainingClass(intoClass)
        }

        fromAliasedClassNode.properties.forEach { (key, aliasedPropertyNode) ->
            val aliasedProperty = aliasedPropertyNode.targetDeclarations[targetIndex] ?: return@forEach
            intoClassNode.properties.getOrPut(key) {
                buildPropertyNode(storageManager, targetSize, classifiers, ParentNode(intoClassNode))
            }.targetDeclarations[targetIndex] = aliasedProperty.withContainingClass(intoClass)
        }
    }
}

private data class ArtificialAliasedCirClass(
    val pointingTypeAlias: CirTypeAlias,
    val pointedClass: CirClass
) : CirClass by pointedClass {
    override val name: CirName = pointingTypeAlias.name
    override var companion: CirName?
        get() = null
        set(_) = throw UnsupportedOperationException("Can't set companion on artificial class (pointed by $pointingTypeAlias)")
}
