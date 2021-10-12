/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.transformer.phantom

import org.jetbrains.kotlin.commonizer.cir.*
import org.jetbrains.kotlin.commonizer.ilt.NumericCirEntityIds
import org.jetbrains.kotlin.commonizer.ilt.asCirEntityId
import org.jetbrains.kotlin.commonizer.mergedtree.*
import org.jetbrains.kotlin.commonizer.transformer.*
import org.jetbrains.kotlin.descriptors.konan.PHANTOM_SIGNED_INTEGER
import org.jetbrains.kotlin.descriptors.konan.PHANTOM_SIGNED_VAR_OF
import org.jetbrains.kotlin.descriptors.konan.PHANTOM_UNSIGNED_INTEGER
import org.jetbrains.kotlin.descriptors.konan.PHANTOM_UNSIGNED_VAR_OF
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.utils.SmartList
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

internal class PhantomIntegerSupertypeCirNodeTransformer(
    private val storageManager: StorageManager,
    private val classifiers: CirKnownClassifiers,
) : AbstractCirNodeTransformer<TypeAliasTransformationContext>() {
    override fun newTransformationContext(root: CirRootNode) = TypeAliasTransformationContext.Empty

    override fun beforeModule(
        moduleNode: CirModuleNode,
        moduleName: CirName,
        context: TypeAliasTransformationContext,
    ): TypeAliasTransformationContext = context.copy(classNodeIndex = ClassNodeIndex(moduleNode))

    override fun beforePackage(packageNode: CirPackageNode, context: TypeAliasTransformationContext): TypeAliasTransformationContext =
        context.copy(packageNode = packageNode)

    override fun transformTypeAlias(
        typeAliasNode: CirTypeAliasNode,
        context: TypeAliasTransformationContext
    ) {
        require(context.packageNode != null) { "Package node is empty during type alias transformation" }
        val typeAliasClassNode = context.classNodeIndex[typeAliasNode.id]
            ?: context.packageNode.createArtificialClassNode(typeAliasNode, storageManager, classifiers)
        fillArtificialClassNode(typeAliasNode, typeAliasClassNode)
    }

    private fun fillArtificialClassNode(
        typeAliasNode: CirTypeAliasNode,
        artificialClassNode: CirClassNode,
    ) {
        typeAliasNode.targetDeclarations.forEachIndexed { targetIndex, typeAlias ->
            when {
                typeAlias == null || artificialClassNode.targetDeclarations[targetIndex] != null -> return@forEachIndexed
                typeAlias.expandedType.classifierId in NumericCirEntityIds.INTEGER_IDS ->
                    addPhantomIntegerSupertype(typeAlias, artificialClassNode, typeAliasNode, targetIndex)
                typeAlias.expandedType.classifierId in NumericCirEntityIds.INTEGER_VAR_IDS ->
                    addPhantomIntegerVarSupertype(typeAlias, artificialClassNode, targetIndex)
            }
        }
    }

    private fun addPhantomIntegerSupertype(
        typeAlias: CirTypeAlias,
        artificialClassNode: CirClassNode,
        typeAliasNode: CirTypeAliasNode,
        targetIndex: Int
    ) {
        if (artificialClassNode.targetDeclarations[targetIndex] != null) return

        artificialClassNode.targetDeclarations[targetIndex] = typeAlias.toArtificialCirClass(
            SupertypeResolutionMode.AddPhantomIntegerSupertype(classifiers, targetIndex, typeAliasNode)
        )
    }

    private fun addPhantomIntegerVarSupertype(
        typeAlias: CirTypeAlias, artificialClassNode: CirClassNode, targetIndex: Int
    ) {
        if (artificialClassNode.targetDeclarations[targetIndex] != null) return

        val integerParameterType = typeAlias.underlyingType.arguments.singleOrNull()?.safeAs<CirRegularTypeProjection>() ?: return
        val typeAliasArgumentType = integerParameterType.type.safeAs<CirTypeAliasType>() ?: return

        artificialClassNode.targetDeclarations[targetIndex] = typeAlias.toArtificialCirClass(
            SupertypeResolutionMode.AddPhantomIntegerVariableSupertype(classifiers, targetIndex, typeAliasArgumentType)
        )
    }
}

internal class PhantomIntegerSupertypesResolver(
    private val baseResolver: CirSupertypesResolver,
    private val originalTypeAlias: CirTypeAliasNode,
) : CirSupertypesResolver {
    override fun supertypes(type: CirClassType): Set<CirClassType> {
        return baseResolver.supertypes(type) + phantomIntegerSupertypesIfAny(type, originalTypeAlias.id)
    }

    private fun phantomIntegerSupertypesIfAny(typeAliasType: CirClassType, integerSupertypeArgumentId: CirEntityId): List<CirClassType> {
        return when (typeAliasType.classifierId) {
            in NumericCirEntityIds.SIGNED_INTEGER_IDS -> SmartList(
                phantomIntegerSupertype(
                    PHANTOM_SIGNED_INTEGER.asCirEntityId(),
                    integerSupertypeArgumentId,
                )
            )
            in NumericCirEntityIds.UNSIGNED_INTEGER_IDS -> SmartList(
                phantomIntegerSupertype(
                    PHANTOM_UNSIGNED_INTEGER.asCirEntityId(),
                    integerSupertypeArgumentId,
                )
            )
            else -> emptyList()
        }
    }

    private fun phantomIntegerSupertype(phantomIntegerId: CirEntityId, argumentId: CirEntityId): CirClassType =
        CirClassType.createInterned(
            classId = phantomIntegerId,
            outerType = null,
            arguments = SmartList(
                CirRegularTypeProjection(
                    projectionKind = Variance.INVARIANT,
                    type = CirClassType.createInterned(
                        classId = argumentId,
                        outerType = null,
                        arguments = emptyList(),
                        isMarkedNullable = false,
                    )
                )
            ),
            isMarkedNullable = false,
        )
}

internal class PhantomVarOfSupertypeResolver(
    private val baseResolver: CirSupertypesResolver,
    private val typeAliasArgumentType: CirTypeAliasType,
) : CirSupertypesResolver {
    override fun supertypes(type: CirClassType): Set<CirClassType> =
        baseResolver.supertypes(type) + phantomIntegerVariableSupertypesIfAny(type)

    private fun phantomIntegerVariableSupertypesIfAny(
        typeAliasType: CirClassType
    ): List<CirClassType> = when (typeAliasType.classifierId) {
        in NumericCirEntityIds.UNSIGNED_VAR_IDS -> SmartList(
            phantomVarOfSupertype(
                PHANTOM_UNSIGNED_VAR_OF.asCirEntityId(),
                typeAliasArgumentType.classifierId,
            )
        )
        in NumericCirEntityIds.SIGNED_VAR_IDS -> SmartList(
            phantomVarOfSupertype(
                PHANTOM_SIGNED_VAR_OF.asCirEntityId(),
                typeAliasArgumentType.classifierId,
            )
        )
        else -> emptyList()
    }

    private fun phantomVarOfSupertype(varOfId: CirEntityId, integerSupertypeArgumentId: CirEntityId): CirClassType =
        CirClassType.createInterned(
            classId = varOfId,
            outerType = null,
            arguments = SmartList(
                CirRegularTypeProjection(
                    projectionKind = Variance.INVARIANT,
                    type = CirClassType.createInterned(
                        classId = integerSupertypeArgumentId,
                        outerType = null,
                        arguments = emptyList(),
                        isMarkedNullable = false,
                    )
                )
            ),
            isMarkedNullable = false,
        )
}

internal fun CirSupertypesResolver.withPhantomIntegerSupertypes(typeAliasNode: CirTypeAliasNode): CirSupertypesResolver =
    PhantomIntegerSupertypesResolver(this, typeAliasNode)

internal fun CirSupertypesResolver.withPhantomVarOfSupertypes(typeAliasArgumentType: CirTypeAliasType): CirSupertypesResolver =
    PhantomVarOfSupertypeResolver(this, typeAliasArgumentType)
