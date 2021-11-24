/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.test.fir

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.calls.KtBoundSymbol
import org.jetbrains.kotlin.analysis.api.calls.KtCallInfo
import org.jetbrains.kotlin.analysis.api.calls.KtDelegatedConstructorCallKind
import org.jetbrains.kotlin.analysis.api.impl.barebone.test.FrontendApiTestConfiguratorService
import org.jetbrains.kotlin.analysis.api.impl.barebone.test.expressionMarkerProvider
import org.jetbrains.kotlin.analysis.api.impl.base.KtMapBackedSubstitutor
import org.jetbrains.kotlin.analysis.api.impl.base.test.test.framework.AbstractHLApiSingleModuleTest
import org.jetbrains.kotlin.analysis.api.symbols.*
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtPossibleMemberSymbol
import org.jetbrains.kotlin.analysis.api.types.KtSubstitutor
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaGetter

abstract class AbstractResolveCallTest(configurator: FrontendApiTestConfiguratorService) : AbstractHLApiSingleModuleTest(configurator) {
    override fun doTestByFileStructure(ktFiles: List<KtFile>, module: TestModule, testServices: TestServices) {
        val ktFile = ktFiles.first()
        val expression = testServices.expressionMarkerProvider.getSelectedElement(ktFile)

        val actual = executeOnPooledThreadInReadAction {
            analyseForTest(expression) {
                resolveCall(expression)?.let { stringRepresentation(it) }
            }
        } ?: "null"
        testServices.assertions.assertEqualsToTestDataFileSibling(actual)
    }

    private fun KtAnalysisSession.resolveCall(element: PsiElement): KtCallInfo? = when (element) {
        is KtValueArgument -> element.getArgumentExpression()?.resolveCall()
        is KtDeclarationModifierList -> {
            val annotationEntry = element.annotationEntries.singleOrNull()
                ?: error("Only single annotation entry is supported for now")
            annotationEntry.resolveCall()
        }
        is KtFileAnnotationList -> {
            val annotationEntry = element.annotationEntries.singleOrNull()
                ?: error("Only single annotation entry is supported for now")
            annotationEntry.resolveCall()
        }
        is KtElement -> element.resolveCall()
        else -> error("Selected element type (${element::class.simpleName}) is not supported for resolveCall()")
    }

}

private fun KtAnalysisSession.stringRepresentation(call: KtCallInfo): String {
    fun Any.stringValue(substitutor: KtSubstitutor): String {
        fun KtType.render() = substitutor.substituteOrSelf(this).asStringForDebugging().replace('/', '.')
        return when (this) {
            is KtFunctionLikeSymbol -> buildString {
                append(
                    when (this@stringValue) {
                        is KtFunctionSymbol -> callableIdIfNonLocal ?: name
                        is KtSamConstructorSymbol -> callableIdIfNonLocal ?: name
                        is KtConstructorSymbol -> "<constructor>"
                        is KtPropertyGetterSymbol -> callableIdIfNonLocal ?: "<getter>"
                        is KtPropertySetterSymbol -> callableIdIfNonLocal ?: "<setter>"
                        else -> error("unexpected symbol kind in KtCall: ${this@stringValue::class.java}")
                    }
                )
                append("(")
                (this@stringValue as? KtFunctionSymbol)?.receiverType?.let { receiver ->
                    append("<extension receiver>: ${receiver.render()}")
                    if (valueParameters.isNotEmpty()) append(", ")
                }

                @Suppress("DEPRECATION")
                (this@stringValue as? KtPossibleMemberSymbol)?.getDispatchReceiverType()?.let { dispatchReceiverType ->
                    append("<dispatch receiver>: ${dispatchReceiverType.render()}")
                    if (valueParameters.isNotEmpty()) append(", ")
                }
                valueParameters.joinTo(this) { it.stringValue(substitutor) }
                append(")")
                append(": ${returnType.render()}")
            }
            is KtValueParameterSymbol -> "${if (isVararg) "vararg " else ""}$name: ${returnType.render()}"
            is KtTypeParameterSymbol -> this.nameOrAnonymous.asString()
            is KtVariableSymbol -> "${if (isVal) "val" else "var"} $name: ${returnType.render()}"
            is Boolean -> toString()
            is Map<*, *> -> entries.joinToString(
                prefix = "{ ",
                postfix = " }"
            ) { (k, v) -> "${k?.stringValue(substitutor)} -> (${v?.stringValue(substitutor)})" }
            is KtExpression -> this.text
            is KtDelegatedConstructorCallKind -> toString()
            is KtSubstitutor.Empty -> "<empty substitutor>"
            is KtMapBackedSubstitutor -> {
                val mappingText = getAsMap().entries
                    .joinToString(prefix = "{", postfix = "}") { (k, v) -> k.stringValue(substitutor) + " = " + v.asStringForDebugging() }
                "<map substitutor: $mappingText>"
            }
            is KtSubstitutor -> "<complex substitutor>"
            else -> buildString {
                val substitutorToUse = if (this@stringValue is KtBoundSymbol<*>) this@stringValue.substitutor else substitutor
                val clazz = this::class
                append(clazz.simpleName!!)
                appendLine(":")
                val propertyByName = clazz.memberProperties.associateBy(KProperty1<*, *>::name)
                clazz.primaryConstructor!!.parameters
                    .filter { it.name != "token" }
                    .joinTo(this, separator = "\n") { parameter ->
                        val name = parameter.name!!.removePrefix("_")
                        val value = propertyByName[name]!!.javaGetter!!(call)?.stringValue(substitutorToUse)?.replace("\n", "\n  ")
                        "$name = $value"
                    }
            }
        }
    }

    return call.stringValue(KtSubstitutor.Empty(token))
}

