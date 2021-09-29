/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.transformer

import org.jetbrains.kotlin.commonizer.cir.*
import org.jetbrains.kotlin.commonizer.cir.CirClassType.Companion.copyInterned
import org.jetbrains.kotlin.commonizer.ilt.*
import org.jetbrains.kotlin.commonizer.mergedtree.*
import org.jetbrains.kotlin.commonizer.mergedtree.CirNodeRelationship.Composite.Companion.plus
import org.jetbrains.kotlin.commonizer.mergedtree.CirNodeRelationship.ParentNode
import org.jetbrains.kotlin.commonizer.mergedtree.CirNodeRelationship.PreferredNode
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.konan.PHANTOM_SIGNED_INTEGER
import org.jetbrains.kotlin.descriptors.konan.PHANTOM_SIGNED_VAR_OF
import org.jetbrains.kotlin.descriptors.konan.PHANTOM_UNSIGNED_INTEGER
import org.jetbrains.kotlin.descriptors.konan.PHANTOM_UNSIGNED_VAR_OF
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.utils.SmartList
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

internal class InlineTypeAliasCirNodeTransformer(
    private val storageManager: StorageManager,
    private val classifiers: CirKnownClassifiers,
) : CirNodeTransformer {
    override fun invoke(root: CirRootNode) {
        root.modules.values.forEach(::prepareArtificialClassesForIntegers)
        root.modules.values.forEach(::invoke)
        root.modules.values.forEach(::processNumericVariables)
    }

    private operator fun invoke(module: CirModuleNode) {
        val classNodeIndex = ClassNodeIndex(module)

        module.packages.values.forEach { packageNode ->
            packageNode.typeAliases.values.forEach { typeAliasNode ->
                val targetClassNode = classNodeIndex[typeAliasNode.id] ?: packageNode.createArtificialClassNode(typeAliasNode)
                inlineTypeAliasIfPossible(classNodeIndex, typeAliasNode, targetClassNode)
            }
        }
    }

    private fun processNumericVariables(module: CirModuleNode) {
        val classNodeIndex = ClassNodeIndex(module) // TODO: optimize
        module.packages.values.forEach { packageNode ->
            // TODO: make sure all integer aliases have been created and can be used
            packageNode.typeAliases.values.forEach { typeAliasNode ->
                val targetClassNode = classNodeIndex[typeAliasNode.id] ?: error("Should exist")
                extractNumericVariable(classNodeIndex, typeAliasNode, targetClassNode)
            }
        }
    }

    // here assume that inlining isn't necessary for numeric types and corresponding type variables
    private fun prepareArtificialClassesForIntegers(module: CirModuleNode) {
        val classNodeIndex = ClassNodeIndex(module)
        module.packages.values.forEach { packageNode ->
            for (typeAliasNode in packageNode.typeAliases.values) {
                if (typeAliasNode.id in classNodeIndex)
                    continue

                val newArtificialNode = packageNode.createArtificialClassNode(typeAliasNode)
                fillArtificialClassNode(typeAliasNode, newArtificialNode, classNodeIndex)
            }
        }
    }

    // fill direct integer supertype for plain type alias or check supertypes of the aliased class, in case of repetitive commonization
    private fun fillArtificialClassNode(
        typeAliasNode: CirTypeAliasNode,
        artificialClassNode: CirClassNode,
        classNodeIndex: ClassNodeIndex,
    ) {
        typeAliasNode.targetDeclarations.forEachIndexed { targetIndex, typeAlias ->
            if (typeAlias != null) {
                fillIntegerSupertypes(classNodeIndex, typeAlias, artificialClassNode, targetIndex)
            }
        }
    }

    private fun fillIntegerSupertypes(
        classNodeIndex: ClassNodeIndex,
        typeAlias: CirTypeAlias,
        artificialClassNode: CirClassNode,
        targetIndex: Int
    ) {
        if (artificialClassNode.targetDeclarations[targetIndex] != null)
            throw AssertionError("Target declaration in newly created artificial class is not empty")

        if (typeAlias.expandedType.classifierId in NumericCirEntityIds.INTEGER_IDS) {
            artificialClassNode.targetDeclarations[targetIndex] =
                typeAlias.toArtificialCirClass(
                    targetIndex,
                    additionalSupertypes = phantomIntegerSupertypesIfAny(typeAlias, artificialClassNode.id)
                )
            return
        }

        val classNode = classNodeIndex[typeAlias.expandedType.classifierId]
            ?: return // here only care about existing classes in common

        val maybeArtificialClass = classNode.targetDeclarations[targetIndex]
            ?: return // looking for a commonized class with single phantom supertype

        // supertypes should exist for a deserialized target declaration
        maybeArtificialClass.supertypes.singleOrNull()?.let { supertype ->
            if (supertype !is CirClassType)
                return

            if (supertype.classifierId in NumericCirEntityIds.PHANTOM_INTEGER_IDS) {
                val supertypeWithCorrectArgument = supertype.copyInterned(
                    arguments = SmartList(
                        CirRegularTypeProjection(
                            Variance.INVARIANT,
                            CirClassType.createInterned(
                                classId = artificialClassNode.id,
                                outerType = null,
                                arguments = emptyList(),
                                isMarkedNullable = false,
                            )
                        )
                    )
                )

                artificialClassNode.targetDeclarations[targetIndex] = typeAlias.toArtificialCirClass(
                    targetIndex,
                    additionalSupertypes = SmartList(supertypeWithCorrectArgument)
                )
            }
        }
    }

    private fun inlineTypeAliasIfPossible(classes: ClassNodeIndex, fromTypeAliasNode: CirTypeAliasNode, intoClassNode: CirClassNode) {
        fromTypeAliasNode.targetDeclarations.forEachIndexed { targetIndex, typeAlias ->
            if (typeAlias != null) {
                inlineTypeAliasIfPossible(classes, typeAlias, intoClassNode, targetIndex)
            }
        }
    }

    private fun extractNumericVariable(classes: ClassNodeIndex, fromTypeAliasNode: CirTypeAliasNode, intoClassNode: CirClassNode) {
        fromTypeAliasNode.targetDeclarations.forEachIndexed { targetIndex, typeAlias ->
            if (typeAlias != null && typeAlias.expandedType.classifierId in NumericCirEntityIds.INTEGER_VAR_IDS) {
                prepareArtificialClassForIntegerVar(classes, typeAlias, intoClassNode, targetIndex)
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
            pointedClass = fromAliasedClassNode?.targetDeclarations?.get(targetIndex) ?: fromTypeAlias.toArtificialCirClass(targetIndex)
            /*pointedClass = fromAliasedClassNode?.targetDeclarations?.get(targetIndex)
                ?: fromTypeAlias.toArtificialCirClass(supertypes = phantomIntegerSupertypesIfAny(fromTypeAlias, intoClassNode.id))*/
        )

        intoClassNode.targetDeclarations[targetIndex] = intoArtificialClass

        if (fromAliasedClassNode != null && !fromTypeAlias.expandedType.isMarkedNullable) {
            inlineArtificialMembers(fromAliasedClassNode, intoClassNode, intoArtificialClass, targetIndex)
        }
    }

    private fun prepareArtificialClassForIntegerVar(
        classNodeIndex: ClassNodeIndex, fromTypeAlias: CirTypeAlias, intoClassNode: CirClassNode, targetIndex: Int
    ) {
        if (intoClassNode.targetDeclarations[targetIndex] != null) {
            return
        }

        val integerParameterType = fromTypeAlias.underlyingType.arguments.singleOrNull()?.safeAs<CirRegularTypeProjection>()
            ?: return
        val expectTypeAliasArtificialClassId = integerParameterType.type.safeAs<CirTypeAliasType>()?.classifierId

        val artificialClass = classNodeIndex[expectTypeAliasArtificialClassId]
            ?: return

        intoClassNode.targetDeclarations[targetIndex] = fromTypeAlias.toArtificialCirClass(
            targetIndex,
            additionalSupertypes = phantomIntegerVariableSupertypesIfAny(
                fromTypeAlias.expandedType.classifierId,
                artificialClass.id
            )
        )
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

    private fun CirPackageNode.createArtificialClassNode(typeAliasNode: CirTypeAliasNode): CirClassNode {
        val classNode = buildClassNode(
            storageManager = storageManager,
            size = typeAliasNode.targetDeclarations.size,
            classifiers = classifiers,
            // This artificial class node should only try to commonize if the package node is commonized
            //  and if the original typeAliasNode cannot be commonized.
            //  Therefore, this artificial class node acts as a fallback with the original type-alias being still the preferred
            //  option for commonization
            nodeRelationship = ParentNode(this) + PreferredNode(typeAliasNode),
            classId = typeAliasNode.id
        )
        this.classes[typeAliasNode.classifierName] = classNode
        return classNode
    }

    private fun CirTypeAlias.toArtificialCirClass(targetIndex: Int, additionalSupertypes: List<CirType> = emptyList()): CirClass =
        CirClass.create(
            annotations = emptyList(), name = name, typeParameters = typeParameters,
            supertypes = resolveSupertypes(targetIndex) + additionalSupertypes,
            visibility = this.visibility, modality = Modality.FINAL, kind = ClassKind.CLASS,
            companion = null, isCompanion = false, isData = false, isValue = false, isInner = false, isExternal = false
        )

    private fun CirTypeAlias.resolveSupertypes(targetIndex: Int): List<CirType> {
        if (expandedType.isMarkedNullable) return emptyList()
        val resolver = SimpleCirSupertypesResolver(
            classifiers = classifiers.classifierIndices[targetIndex],
            dependencies = CirProvidedClassifiers.of(
                classifiers.commonDependencies, classifiers.targetDependencies[targetIndex]
            )
        )
        return resolver.supertypes(expandedType).toList()
    }
}

private typealias ClassNodeIndex = Map<CirEntityId, CirClassNode>

private fun ClassNodeIndex(module: CirModuleNode): ClassNodeIndex = module.packages.values
    .flatMap { pkg -> pkg.classes.values }
    .associateBy { clazz -> clazz.id }

private data class ArtificialAliasedCirClass(
    val pointingTypeAlias: CirTypeAlias,
    val pointedClass: CirClass
) : CirClass by pointedClass {
    override val name: CirName = pointingTypeAlias.name
    override var companion: CirName?
        get() = null
        set(_) = throw UnsupportedOperationException("Can't set companion on artificial class (pointed by $pointingTypeAlias)")
}



/*private fun CirTypeAlias.toArtificialCirClass(
    supertypes: List<CirType> = emptyList()
): CirClass = CirClass.create(
    annotations = emptyList(), name = name, typeParameters = typeParameters,
    visibility = this.visibility, modality = Modality.FINAL, kind = ClassKind.CLASS,
    companion = null, isCompanion = false, isData = false, isValue = false, isInner = false, isExternal = false
).also {
    it.supertypes = supertypes
}*/

private fun phantomIntegerSupertypesIfAny(typeAlias: CirTypeAlias, integerSupertypeArgumentId: CirEntityId): List<CirType> {
    return when (typeAlias.expandedType.classifierId) {
        in NumericCirEntityIds.SIGNED_INTEGER_IDS -> SmartList(
            CirClassType.createInterned(
                classId = PHANTOM_SIGNED_INTEGER.asCirEntityId(),
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
        )
        in NumericCirEntityIds.UNSIGNED_INTEGER_IDS -> SmartList(
            CirClassType.createInterned(
                classId = PHANTOM_UNSIGNED_INTEGER.asCirEntityId(),
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
        )
        else -> emptyList()
    }
}

private fun phantomIntegerVariableSupertypesIfAny(classifierId: CirEntityId, integerSupertypeArgumentId: CirEntityId): List<CirType> {
    return when (classifierId) {
        in NumericCirEntityIds.UNSIGNED_VAR_IDS -> SmartList(
            CirClassType.createInterned(
                classId = PHANTOM_UNSIGNED_VAR_OF.asCirEntityId(),
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
        )
        in NumericCirEntityIds.SIGNED_VAR_IDS -> SmartList(
            CirClassType.createInterned(
                classId = PHANTOM_SIGNED_VAR_OF.asCirEntityId(),
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
        )
        else -> emptyList()
    }
}
