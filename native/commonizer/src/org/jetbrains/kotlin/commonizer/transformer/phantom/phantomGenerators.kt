/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.transformer.phantom

import org.jetbrains.kotlin.commonizer.cir.*
import org.jetbrains.kotlin.commonizer.ilt.NumericCirEntityIds
import org.jetbrains.kotlin.commonizer.ilt.asCirEntityId
import org.jetbrains.kotlin.commonizer.mergedtree.*
import org.jetbrains.kotlin.commonizer.utils.CommonizedGroup
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.konan.*
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.utils.SmartList

internal val SIGNED_INTEGER_ID = PHANTOM_SIGNED_INTEGER.asCirEntityId()
internal val UNSIGNED_INTEGER_ID = PHANTOM_UNSIGNED_INTEGER.asCirEntityId()
internal val SIGNED_VAR_OF_ID = PHANTOM_SIGNED_VAR_OF.asCirEntityId()
internal val UNSIGNED_VAR_OF_ID = PHANTOM_UNSIGNED_VAR_OF.asCirEntityId()

internal fun convertExtensionFunction(generationContext: GenerationContext, receiverType: CirEntityId): GeneratedFunction {
    val typeParameter = CirTypeParameter(
        annotations = emptyList(),
        name = CirName.create("R"),
        isReified = true,
        variance = Variance.INVARIANT,
        upperBounds = listOf(
            CirClassType.createInterned(
                classId = CirEntityId.create("kotlin/Any"),
                outerType = null,
                visibility = Visibilities.Public,
                arguments = emptyList(),
                isMarkedNullable = false,
            ),
        ),
    )

    val convertFunction = CirFunction(
        annotations = emptyList(),
        name = CirName.create("convert"),
        typeParameters = listOf(
            typeParameter,
        ),
        visibility = Visibilities.Public,
        modality = Modality.FINAL,
        containingClass = null,
        valueParameters = emptyList(),
        hasStableParameterNames = true,
        extensionReceiver = CirExtensionReceiver(
            annotations = emptyList(),
            type = CirClassType.createInterned(
                classId = receiverType,
                outerType = null,
                visibility = Visibilities.Public,
                arguments = SmartList(CirStarTypeProjection),
                isMarkedNullable = false,
            ),
        ),
        returnType = CirTypeParameterType.createInterned(index = 0, isMarkedNullable = false),
        kind = CallableMemberDescriptor.Kind.DECLARATION,
        modifiers = CirFunctionModifiers.createInterned(
            isOperator = false,
            isInfix = false,
            isInline = true,
            isTailrec = false,
            isSuspend = false,
            isExternal = true,
        )
    )

    return convertFunction.toGeneratedFunction(generationContext)
}

internal fun generalizedIntegerInterface(generationContext: GenerationContext, entityId: CirEntityId): GeneratedInterface {
    val name = entityId.relativeNameSegments.last()

    val selfTypeParameter = CirTypeParameter(
        annotations = emptyList(),
        name = CirName.create("SELF"),
        isReified = false,
        variance = Variance.INVARIANT,
        upperBounds = SmartList(
            CirClassType.createInterned(
                classId = entityId,
                outerType = null,
                visibility = Visibilities.Public,
                arguments = SmartList(
                    CirRegularTypeProjection(
                        projectionKind = Variance.INVARIANT,
                        type = CirTypeParameterType.createInterned(index = 0, isMarkedNullable = false)
                    )
                ),
                isMarkedNullable = false,
            )
        )
    )

    val classProducer = {
        CirClass.create(
            annotations = emptyList(),
            name = name,
            typeParameters = SmartList(selfTypeParameter),
            visibility = Visibilities.Public,
            modality = Modality.OPEN,
            kind = ClassKind.INTERFACE,
            companion = null,
            isCompanion = false,
            isData = false,
            isValue = false,
            isInner = false,
            isExternal = false,
            supertypes = emptyList(),
        )
    }

    val classNode = createClassNode(entityId, classProducer, generationContext)

    val converters = (SIGNED_INTEGERS + UNSIGNED_INTEGERS + FLOATING_POINTS).map { classId ->
        "to${classId.shortClassName.asString()}" to classId.asString()
    }

    for ((conversionName, convertedType) in converters) {
        val generated =
            abstractIntegerConversionMember(generationContext, CirName.create(conversionName), CirEntityId.create(convertedType))
        classNode.functions[generated.key] = generated.value
    }

    for (generated in mathMembers(generationContext, entityId, classProducer())) {
        classNode.functions[generated.key] = generated.value
    }

    return GeneratedInterface(name, classNode)
}

internal fun mathMembers(context: GenerationContext, id: CirEntityId, containingClass: CirClass): List<GeneratedFunction> {
    return when (id) {
        SIGNED_INTEGER_ID -> signedMathOperations(context, containingClass)
        UNSIGNED_INTEGER_ID -> unsignedMathOperations(context, containingClass)
        else -> error("Math operations should not be generated for $id")
    }
}

internal fun signedMathOperations(generationContext: GenerationContext, containingClass: CirClass): List<GeneratedFunction> {
    return mathOperations(
        generationContext, containingClass,
        isSingletonMembersInline = false,
        isSingletonMembersExternal = true,
        realIntegerIds = NumericCirEntityIds.SIGNED_INTEGER_IDS,
        phantomIntegerId = SIGNED_INTEGER_ID,
    )
}

internal fun unsignedMathOperations(generationContext: GenerationContext, containingClass: CirClass): List<GeneratedFunction> {
    return mathOperations(
        generationContext, containingClass,
        isSingletonMembersInline = true,
        isSingletonMembersExternal = false,
        realIntegerIds = NumericCirEntityIds.UNSIGNED_INTEGER_IDS,
        phantomIntegerId = UNSIGNED_INTEGER_ID,
    )
}

@OptIn(ExperimentalStdlibApi::class)
internal fun mathOperations(
    generationContext: GenerationContext,
    containingClass: CirClass,
    isSingletonMembersInline: Boolean,
    isSingletonMembersExternal: Boolean,
    realIntegerIds: Collection<CirEntityId>,
    phantomIntegerId: CirEntityId,
): List<GeneratedFunction> {
    val selfType = CirTypeParameterType.createInterned(index = 0, isMarkedNullable = false)
    val intType = KOTLIN_INT.asCirEntityId().toCirType()
    val unspecifiedIntegerType = phantomIntegerId.toCirType(arguments = SmartList(CirStarTypeProjection))
    val parameterTypesForDuplicatedMembers = realIntegerIds.map { it.toCirType() } +
            phantomIntegerId.toCirType(arguments = SmartList(CirStarTypeProjection))

    val singletonMembers = buildList {
        listOf("inc", "dec").map { name ->
            MathMemberFunctionContext(
                name = name,
                isInline = isSingletonMembersInline,
                isExternal = isSingletonMembersExternal,
                isOperator = true,
                valueParameters = emptyList(),
                returnType = selfType,
                containingClass = containingClass,
            )
        }.forEach(::add)

        add(
            MathMemberFunctionContext(
                name = "inv",
                isInline = isSingletonMembersInline,
                isExternal = isSingletonMembersExternal,
                valueParameters = emptyList(),
                returnType = selfType,
                containingClass = containingClass,
            )
        )

        listOf("and", "or", "xor").map { name ->
            MathMemberFunctionContext(
                name = name,
                isInline = isSingletonMembersInline,
                isExternal = isSingletonMembersExternal,
                isInfix = true,
                valueParameters = SmartList(simpleValueParameter(name = "other", type = selfType)),
                returnType = selfType,
                containingClass = containingClass,
            )
        }.forEach(::add)

        listOf("shl", "shr").map { name ->
            MathMemberFunctionContext(
                name = name,
                isInline = isSingletonMembersInline,
                isExternal = isSingletonMembersExternal,
                isInfix = true,
                valueParameters = SmartList(simpleValueParameter(name = "bitCount", type = intType)),
                returnType = selfType,
                containingClass = containingClass,
            )
        }.forEach(::add)
    }

    val duplicatedMembers = buildList {
        for (parameterType in parameterTypesForDuplicatedMembers) {
            listOf("plus", "minus", "div", "rem", "times").map { name ->
                MathMemberFunctionContext(
                    name = name, isInline = true, isOperator = true,
                    valueParameters = SmartList(simpleValueParameter(name = "other", type = parameterType)),
                    returnType = unspecifiedIntegerType,
                    containingClass = containingClass,
                )
            }.forEach(::add)

            add(
                MathMemberFunctionContext(
                    name = "compareTo", isInline = true, isOperator = true,
                    valueParameters = SmartList(simpleValueParameter(name = "other", type = parameterType)),
                    returnType = intType,
                    containingClass = containingClass,
                )
            )
        }
    }

    return (singletonMembers + duplicatedMembers).map { memberContext ->
        mathFunction(generationContext, memberContext)
    }
}

private data class MathMemberFunctionContext(
    val name: String,
    val containingClass: CirClass,
    val valueParameters: List<CirValueParameter>,
    val returnType: CirType,
    val isOperator: Boolean = false,
    val isInfix: Boolean = false,
    val isInline: Boolean = false,
    val isExternal: Boolean = false,
)

internal fun simpleValueParameter(name: String, type: CirType): CirValueParameter {
    return CirValueParameter.createInterned(
        annotations = emptyList(),
        name = CirName.create(name),
        returnType = type,
        varargElementType = null,
        declaresDefaultValue = false,
        isCrossinline = false,
        isNoinline = false,
    )
}

private fun mathFunction(generationContext: GenerationContext, functionContext: MathMemberFunctionContext): GeneratedFunction {
    val cirFunction = CirFunction(
        annotations = emptyList(),
        name = CirName.create(functionContext.name),
        typeParameters = emptyList(),
        visibility = Visibilities.Public,
        modality = Modality.FINAL,
        containingClass = null,
        valueParameters = functionContext.valueParameters,
        hasStableParameterNames = true,
        extensionReceiver = null,
        returnType = functionContext.returnType,
        kind = CallableMemberDescriptor.Kind.DECLARATION,
        modifiers = CirFunctionModifiers.createInterned(
            isOperator = functionContext.isOperator,
            isInfix = functionContext.isInfix,
            isInline = functionContext.isInline,
            isTailrec = false,
            isSuspend = false,
            isExternal = functionContext.isExternal,
        )
    )

    return cirFunction.toGeneratedFunction(generationContext, CirMemberContext.empty.withContextOf(functionContext.containingClass))
}

internal fun abstractIntegerConversionMember(context: GenerationContext, name: CirName, returnTypeId: CirEntityId): GeneratedFunction {
    val cirFunction = CirFunction(
        annotations = emptyList(),
        name = name,
        typeParameters = emptyList(),
        visibility = Visibilities.Public,
        modality = Modality.FINAL,
        containingClass = null,
        valueParameters = emptyList(),
        hasStableParameterNames = true,
        extensionReceiver = null,
        returnType = CirClassType.createInterned(
            returnTypeId,
            outerType = null,
            visibility = Visibilities.Public,
            arguments = emptyList(),
            isMarkedNullable = false,
        ),
        kind = CallableMemberDescriptor.Kind.DECLARATION,
        modifiers = CirFunctionModifiers.createInterned(
            isOperator = false,
            isInfix = false,
            isInline = true,
            isTailrec = false,
            isSuspend = false,
            isExternal = false,
        )
    )

    return cirFunction.toGeneratedFunction(context)
}

internal fun cirTypeParameterWithSelfBound(boundId: CirEntityId): CirTypeParameter =
    CirTypeParameter(
        annotations = emptyList(),
        name = CirName.create("T"),
        isReified = false,
        variance = Variance.INVARIANT,
        upperBounds = SmartList(
            CirClassType.createInterned(
                classId = boundId,
                outerType = null,
                visibility = Visibilities.Public,
                arguments = SmartList(
                    CirRegularTypeProjection(
                        projectionKind = Variance.INVARIANT,
                        type = CirTypeParameterType.createInterned(index = 0, isMarkedNullable = false)
                    )
                ),
                isMarkedNullable = false,
            ),
        )
    )

internal fun varOfClass(context: GenerationContext, id: CirEntityId, boundId: CirEntityId): GeneratedInterface {
    val name = id.relativeNameSegments.last()

    val supertypes = SmartList(
        CirClassType.createInterned(
            classId = CVARIABLE_ID.asCirEntityId(),
            outerType = null,
            visibility = Visibilities.Public,
            arguments = emptyList(),
            isMarkedNullable = false,
        )
    )

    val creatingFn = {
        CirClass.create(
            supertypes = supertypes,
            annotations = emptyList(),
            name = name,
            typeParameters = SmartList(cirTypeParameterWithSelfBound(boundId)),
            visibility = Visibilities.Public,
            modality = Modality.OPEN,
            kind = ClassKind.CLASS,
            companion = null,
            isCompanion = false,
            isData = false,
            isValue = false,
            isInner = false,
            isExternal = false,
        )
    }

    return GeneratedInterface(
        name,
        createClassNode(id, creatingFn, context/*, supertypes*/),
    )
}

internal fun valueExtensionProperty(context: GenerationContext, upperBoundId: CirEntityId, receiverId: CirEntityId): GeneratedProperty {
    val receiver = CirExtensionReceiver(
        annotations = emptyList(),
        type = CirClassType.createInterned(
            classId = receiverId,
            outerType = null,
            visibility = Visibilities.Public,
            arguments = SmartList(
                CirRegularTypeProjection(
                    projectionKind = Variance.INVARIANT,
                    type = CirTypeParameterType.createInterned(
                        index = 0,
                        isMarkedNullable = false,
                    )
                )
            ),
            isMarkedNullable = false,
        )
    )

    val valueProperty = CirProperty(
        annotations = emptyList(),
        name = CirName.create("value"),
        typeParameters = SmartList(cirTypeParameterWithSelfBound(upperBoundId)),
        visibility = Visibilities.Public,
        modality = Modality.FINAL,
        containingClass = null,
        isExternal = false,
        extensionReceiver = receiver,
        kind = CallableMemberDescriptor.Kind.DECLARATION,
        isVar = true,
        isLateInit = false,
        isConst = false,
        isDelegate = false,
        getter = CirPropertyGetter.DEFAULT_NO_ANNOTATIONS,
        setter = CirPropertySetter.createDefaultNoAnnotations(Visibilities.Public),
        backingFieldAnnotations = emptyList(),
        delegateFieldAnnotations = emptyList(),
        compileTimeInitializer = CirConstantValue.NullValue,
        returnType = CirTypeParameterType.createInterned(
            index = 0,
            isMarkedNullable = false,
        )
    )

    return GeneratedProperty(
        approximationKey = PropertyApproximationKey.create(CirMemberContext.empty, valueProperty),
        propertyNode = CirPropertyNode(
            targetDeclarations = CommonizedGroup(context.groupSize) { valueProperty },
            commonDeclaration = context.storageManager.createNullableLazyValue { valueProperty },
        )
    )
}
