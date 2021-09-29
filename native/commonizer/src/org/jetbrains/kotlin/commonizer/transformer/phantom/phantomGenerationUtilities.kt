/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.transformer.phantom

import org.jetbrains.kotlin.commonizer.cir.*
import org.jetbrains.kotlin.commonizer.mergedtree.*
import org.jetbrains.kotlin.commonizer.utils.CommonizedGroup
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.storage.StorageManager


internal data class GenerationContext(
    val storageManager: StorageManager,
    val moduleNode: CirModuleNode,
    val packageName: CirPackageName,
) {
    val groupSize: Int
        get() = moduleNode.targetDeclarations.size
}

internal typealias Generated = GeneratedBase<*, *, *>

internal abstract class GeneratedBase<K : Any, D : CirDeclaration, V : CirNode<D, D>>(
    val key: K,
    val value: V,
) {
    abstract val CirPackageNode.storageMap: MutableMap<K, V>

    // TODO: API is counter-intuitive: why is it reversed? package field is a function/interface property
    fun store(cirPackage: CirPackageNode) {
        cirPackage.storageMap.putIfAbsent(key, value)
    }
}

internal class GeneratedFunction(
    approximationKey: FunctionApproximationKey,
    functionNode: CirFunctionNode
) : GeneratedBase<FunctionApproximationKey, CirFunction, CirFunctionNode>(approximationKey, functionNode) {
    override val CirPackageNode.storageMap: MutableMap<FunctionApproximationKey, CirFunctionNode>
        get() = functions
}

internal class GeneratedInterface(
    name: CirName,
    classifierNode: CirClassNode
) : GeneratedBase<CirName, CirClass, CirClassNode>(name, classifierNode) {
    override val CirPackageNode.storageMap: MutableMap<CirName, CirClassNode>
        get() = classes
}

internal class GeneratedProperty(
    approximationKey: PropertyApproximationKey,
    propertyNode: CirPropertyNode,
) : GeneratedBase<PropertyApproximationKey, CirProperty, CirPropertyNode>(approximationKey, propertyNode) {
    override val CirPackageNode.storageMap: MutableMap<PropertyApproximationKey, CirPropertyNode>
        get() = properties
}

internal fun createClassNode(
    id: CirEntityId,
    creatingFn: () -> CirClass,
    context: GenerationContext,
//    supertypes: List<CirType> = emptyList(),
): CirClassNode {
    val commonWithoutSupertypes = creatingFn()
    val platformWithSupertypes = creatingFn()/*.also { it.supertypes = supertypes }*/
    return CirClassNode(
        id,
        targetDeclarations = CommonizedGroup(context.groupSize) { platformWithSupertypes },
        commonDeclaration = context.storageManager.createNullableLazyValue {
            commonWithoutSupertypes
        }
    )
}

internal fun CirFunction.toGeneratedFunction(
    context: GenerationContext,
    memberContext: CirMemberContext = CirMemberContext.empty
): GeneratedFunction {
    val functionNode = CirFunctionNode(
        CommonizedGroup(context.groupSize) { this },
        context.storageManager.createNullableLazyValue { this },
    )

    return GeneratedFunction(
        FunctionApproximationKey.create(this, SignatureBuildingContext(memberContext, this)),
        functionNode
    )
}

internal fun CirEntityId.toCirType(
    outerType: CirClassType? = null,
    arguments: List<CirTypeProjection> = emptyList(),
    isMarkedNullable: Boolean = false,
): CirType = CirClassType.createInterned(
    this,
    outerType = outerType,
    arguments = arguments,
    isMarkedNullable = isMarkedNullable,
)
