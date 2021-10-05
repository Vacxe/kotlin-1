/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.transformer

import org.jetbrains.kotlin.commonizer.cir.CirDeclaration
import org.jetbrains.kotlin.commonizer.cir.CirHasTypeParameters
import org.jetbrains.kotlin.commonizer.mergedtree.*
import org.jetbrains.kotlin.commonizer.mergedtree.CirNodeRelationship.ParentNode
import org.jetbrains.kotlin.commonizer.mergedtree.ClassifierSignatureBuildingContext.TypeAliasInvariant
import org.jetbrains.kotlin.storage.StorageManager

internal class ReApproximationCirNodeTransformer(
    private val storageManager: StorageManager,
    private val classifiers: CirKnownClassifiers,
    private val signatureBuildingContextProvider: SignatureBuildingContextProvider
) : AbstractCirNodeTransformer<ReApproximationCirNodeTransformer.TransformationContext>() {

    internal class SignatureBuildingContextProvider(
        private val classifiers: CirKnownClassifiers,
        private val typeAliasInvariant: Boolean,
        private val skipArguments: Boolean
    ) {
        operator fun invoke(member: CirMemberContext, functionOrPropertyOrConstructor: CirHasTypeParameters): SignatureBuildingContext {
            return SignatureBuildingContext(
                memberContext = member, functionOrPropertyOrConstructor = functionOrPropertyOrConstructor,
                classifierSignatureBuildingContext = if (typeAliasInvariant) TypeAliasInvariant(classifiers.associatedIdsResolver)
                else ClassifierSignatureBuildingContext.Default,
                argumentsSignatureBuildingContext = if (skipArguments) ArgumentsSignatureBuildingContext.SkipArguments
                else ArgumentsSignatureBuildingContext.Default
            )
        }
    }

    internal data class TransformationContext(
        val parent: CirNodeWithMembers<*, *>?,
        val memberContextClasses: ArrayDeque<CirClassNode>,
    )

    override fun newTransformationContext(root: CirRootNode): TransformationContext =
        TransformationContext(parent = null, memberContextClasses = ArrayDeque())

    override fun beforePackage(packageNode: CirPackageNode, context: TransformationContext): TransformationContext {
        require(context.memberContextClasses.isEmpty()) { "Non-empty member context classes before package" }
        return context.copy(parent = packageNode)
    }

    override fun afterPackage(packageNode: CirPackageNode, context: TransformationContext) {
        require(context.memberContextClasses.isEmpty()) { "Non-empty member context classes after package" }
    }

    override fun beforeClass(classNode: CirClassNode, context: TransformationContext): TransformationContext {
        context.memberContextClasses.addFirst(classNode)
        return context.copy(parent = classNode)
    }

    override fun afterClass(classNode: CirClassNode, context: TransformationContext) {
        context.memberContextClasses.removeFirst()
    }

    override fun transformFunction(functionNode: CirFunctionNode, context: TransformationContext) {
        reApproximateNode(
            functionNode, context,
            approximationKeyBuilder = { cirFunction, memberContext ->
                FunctionApproximationKey.create(cirFunction, signatureBuildingContextProvider(memberContext, cirFunction))
            },
            freshNodeBuilder = ::buildFunctionNode,
            parentNodeMembers = { functions }
        )
    }

    override fun transformProperty(propertyNode: CirPropertyNode, context: TransformationContext) {
        reApproximateNode(
            propertyNode, context,
            approximationKeyBuilder = { cirProperty, memberContext ->
                PropertyApproximationKey.create(cirProperty, signatureBuildingContextProvider(memberContext, cirProperty))
            },
            freshNodeBuilder = ::buildPropertyNode,
            parentNodeMembers = { properties }
        )
    }

    override fun transformConstructor(constructorNode: CirClassConstructorNode, context: TransformationContext) {
        reApproximateNode(
            constructorNode, context,
            approximationKeyBuilder = { cirConstructor, memberContext ->
                ConstructorApproximationKey.create(cirConstructor, signatureBuildingContextProvider(memberContext, cirConstructor))
            },
            freshNodeBuilder = ::buildClassConstructorNode,
            parentNodeMembers = {
                require(this is CirClassNode) { "Transform constructor called outside of class node" }
                constructors
            }
        )
    }

    private fun <K, D : CirDeclaration, N : CirNode<D, D>> reApproximateNode(
        oldNode: N,
        transformationContext: TransformationContext,
        approximationKeyBuilder: (D, CirMemberContext) -> K,
        freshNodeBuilder: (StorageManager, size: Int, CirKnownClassifiers, CirNodeRelationship?) -> N,
        parentNodeMembers: CirNodeWithMembers<*, *>.() -> MutableMap<K, N>,
    ) {
        require(transformationContext.parent != null) { "Member owner is absent" }
        /* Only perform re-approximation to nodes that are not 'complete' */
        if (oldNode.targetDeclarations.none { it == null }) return

        oldNode.targetDeclarations.indices.forEach { index ->
            val memberAtIndex = oldNode.targetDeclarations[index] ?: return@forEach

            val memberContext = transformationContext.memberContextClasses.fold(CirMemberContext.empty) { outerContext, cirClassNode ->
                outerContext.withContextOf(cirClassNode.targetDeclarations[index] ?: return@forEach)
            }

            val approximationKey = approximationKeyBuilder(memberAtIndex, memberContext)
            val newNode = transformationContext.parent.parentNodeMembers().getOrPut(approximationKey) {
                freshNodeBuilder(
                    storageManager,
                    transformationContext.parent.targetDeclarations.size,
                    classifiers,
                    ParentNode(transformationContext.parent)
                )
            }

            // Move declaration
            if (newNode.targetDeclarations[index] == null) {
                oldNode.targetDeclarations[index] = null
                newNode.targetDeclarations[index] = memberAtIndex
            }
        }
    }
}
