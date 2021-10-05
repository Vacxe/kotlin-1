/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.transformer

import org.jetbrains.kotlin.commonizer.cir.CirName
import org.jetbrains.kotlin.commonizer.mergedtree.*
import org.jetbrains.kotlin.commonizer.utils.fastForEach

abstract class AbstractCirNodeTransformer<D : Any> : CirNodeTransformer {
    override fun invoke(root: CirRootNode) {
        val context = newTransformationContext(root)
        root.modules.entries.toTypedArray().fastForEach { (name, module) -> processModule(module, name, context) }
    }

    abstract fun newTransformationContext(root: CirRootNode): D

    private fun processModule(moduleNode: CirModuleNode, moduleName: CirName, context: D) {
        val moduleContext = beforeModule(moduleNode, moduleName, context)
        moduleNode.packages.values.toTypedArray().fastForEach { processPackage(it, moduleContext) }
        afterModule(moduleNode, moduleName, moduleContext)
    }

    private fun processPackage(packageNode: CirPackageNode, context: D) {
        val packageContext = beforePackage(packageNode, context)
        packageNode.properties.values.toTypedArray().fastForEach { processProperty(it, packageContext) }
        packageNode.functions.values.toTypedArray().fastForEach { processFunction(it, packageContext) }
        packageNode.classes.values.toTypedArray().fastForEach { processClass(it, packageContext) }
        packageNode.typeAliases.values.toTypedArray().fastForEach { processTypeAlias(it, packageContext) }
        afterPackage(packageNode, packageContext)
    }

    private fun processClass(classNode: CirClassNode, context: D) {
        val classContext = beforeClass(classNode, context)
        classNode.constructors.values.toTypedArray().fastForEach { processConstructor(it, classContext) }
        classNode.classes.values.toTypedArray().fastForEach { processClass(it, classContext) }
        classNode.functions.values.toTypedArray().fastForEach { processFunction(it, classContext) }
        classNode.properties.values.toTypedArray().fastForEach { processProperty(it, classContext) }
        afterClass(classNode, classContext)
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

    // TODO: review when the dust settles
    open fun beforeModule(moduleNode: CirModuleNode, moduleName: CirName, context: D): D = context
    open fun afterModule(moduleNode: CirModuleNode, moduleName: CirName, context: D) {}
    open fun beforePackage(packageNode: CirPackageNode, context: D): D = context
    open fun afterPackage(packageNode: CirPackageNode, context: D) {}
    open fun beforeClass(classNode: CirClassNode, context: D): D = context
    open fun afterClass(classNode: CirClassNode, context: D) {}
    open fun transformProperty(propertyNode: CirPropertyNode, context: D) {}
    open fun transformFunction(functionNode: CirFunctionNode, context: D) {}
    open fun transformConstructor(constructorNode: CirClassConstructorNode, context: D) {}
    open fun transformTypeAlias(typeAliasNode: CirTypeAliasNode, context: D) {}
}

abstract class AbstractContextlessCirNodeTransformer : AbstractCirNodeTransformer<Unit>() {
    final override fun newTransformationContext(root: CirRootNode) {
    }
}
