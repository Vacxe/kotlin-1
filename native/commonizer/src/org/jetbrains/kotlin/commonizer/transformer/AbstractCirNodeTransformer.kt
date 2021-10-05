/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.transformer

import org.jetbrains.kotlin.commonizer.cir.CirName
import org.jetbrains.kotlin.commonizer.mergedtree.*

abstract class AbstractCirNodeTransformer<D : Any> : CirNodeTransformer {
    override fun invoke(root: CirRootNode) {
        val context = newTransformationContext()
        root.modules.forEach { (name, module) -> processModule(module, name, context) }
    }

    abstract fun newTransformationContext(): D

    private fun processModule(moduleNode: CirModuleNode, moduleName: CirName, context: D): D {
        var ctxt = context
        ctxt = beforeModule(moduleNode, moduleName, ctxt)
        moduleNode.packages.values.forEach { ctxt = processPackage(it, ctxt) }
        ctxt = afterModule(moduleNode, moduleName, ctxt)

        return ctxt
    }

    private fun processPackage(packageNode: CirPackageNode, context: D): D {
        var ctxt = context
        ctxt = beforePackage(packageNode, ctxt)
        packageNode.properties.values.forEach { ctxt = processProperty(it, ctxt) }
        packageNode.functions.values.forEach { ctxt = processFunction(it, ctxt) }
        packageNode.classes.values.forEach { ctxt = processClass(it, ctxt) }
        packageNode.typeAliases.values.forEach { ctxt = processTypeAlias(it, ctxt) }
        afterPackage(packageNode, ctxt)

        return ctxt
    }

    private fun processClass(classNode: CirClassNode, context: D): D {
        var ctxt = context
        ctxt = beforeClass(classNode, ctxt)
        classNode.constructors.values.forEach { ctxt = processConstructor(it, ctxt) }
        classNode.classes.values.forEach { ctxt = processClass(it, ctxt) }
        classNode.functions.values.forEach { ctxt = processFunction(it, ctxt) }
        classNode.properties.values.forEach { ctxt = processProperty(it, ctxt) }
        ctxt = afterClass(classNode, ctxt)

        return ctxt
    }

    private fun processProperty(propertyNode: CirPropertyNode, context: D): D = transformProperty(propertyNode, context)
    private fun processFunction(functionNode: CirFunctionNode, context: D): D = transformFunction(functionNode, context)
    private fun processTypeAlias(typeAliasNode: CirTypeAliasNode, context: D): D = transformTypeAlias(typeAliasNode, context)
    private fun processConstructor(constructorNode: CirClassConstructorNode, context: D): D =
        transformConstructor(constructorNode, context)

    // TODO: review when the dust settles
    open fun beforeModule(moduleNode: CirModuleNode, moduleName: CirName, context: D): D = context
    open fun afterModule(moduleNode: CirModuleNode, moduleName: CirName, context: D): D = context
    open fun beforePackage(packageNode: CirPackageNode, context: D): D = context
    open fun afterPackage(packageNode: CirPackageNode, context: D): D = context
    open fun beforeClass(classNode: CirClassNode, context: D): D = context
    open fun afterClass(classNode: CirClassNode, context: D): D = context
    open fun transformProperty(propertyNode: CirPropertyNode, context: D): D = context
    open fun transformFunction(functionNode: CirFunctionNode, context: D): D = context
    open fun transformConstructor(constructorNode: CirClassConstructorNode, context: D): D = context
    open fun transformTypeAlias(typeAliasNode: CirTypeAliasNode, context: D): D = context
}

abstract class AbstractContextlessCirNodeTransformer : AbstractCirNodeTransformer<Unit>() {
    final override fun newTransformationContext() {
    }
}
