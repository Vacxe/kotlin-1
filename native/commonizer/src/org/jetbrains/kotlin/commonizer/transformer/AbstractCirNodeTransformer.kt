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

    private fun processModule(moduleNode: CirModuleNode, moduleName: CirName, context: D) {
        beforeModule(moduleNode, moduleName, context)
        moduleNode.packages.values.forEach { processPackage(it, context) }
        afterModule(moduleNode, moduleName, context)
    }

    private fun processPackage(packageNode: CirPackageNode, context: D) {
        beforePackage(packageNode, context)
        packageNode.properties.values.forEach { processProperty(it, context) }
        packageNode.functions.values.forEach { processFunction(it, context) }
        packageNode.classes.values.forEach { processClass(it, context) }
        packageNode.typeAliases.values.forEach { processTypeAlias(it, context) }
        afterPackage(packageNode, context)
    }

    private fun processClass(classNode: CirClassNode, context: D) {
        beforeClass(classNode, context)
        classNode.constructors.values.forEach { processConstructor(it, context) }
        classNode.classes.values.forEach { processClass(it, context) }
        classNode.functions.values.forEach { processFunction(it, context) }
        classNode.properties.values.forEach { processProperty(it, context) }
        afterClass(classNode, context)
    }

    private fun processProperty(propertyNode: CirPropertyNode, context: D) {
        transformProperty(propertyNode, context)
    }

    private fun processFunction(functionNode: CirFunctionNode, context: D) {
        transformFunction(functionNode, context)
    }

    private fun processTypeAlias(typeAliasNode: CirTypeAliasNode, context: D) {
        transformTypeAlias(typeAliasNode, context)
    }

    private fun processConstructor(constructorNode: CirClassConstructorNode, context: D) {
        transformConstructor(constructorNode, context)
    }

    open fun beforeModule(moduleNode: CirModuleNode, moduleName: CirName, context: D) {}
    open fun afterModule(moduleNode: CirModuleNode, moduleName: CirName, context: D) {}
    open fun beforePackage(packageNode: CirPackageNode, context: D) {}
    open fun afterPackage(packageNode: CirPackageNode, context: D) {}
    open fun beforeClass(classNode: CirClassNode, context: D) {}
    open fun afterClass(classNode: CirClassNode, context: D) {}
    open fun transformProperty(propertyNode: CirPropertyNode, context: D) {}
    open fun transformFunction(functionNode: CirFunctionNode, context: D) {}
    open fun transformConstructor(constructorNode: CirClassConstructorNode, context: D) {}
    open fun transformTypeAlias(typeAliasNode: CirTypeAliasNode, context: D) {}
}

abstract class AbstractContextlessCirNodeTransformer : AbstractCirNodeTransformer<Unit>() {
    final override fun newTransformationContext() {
    }
}
