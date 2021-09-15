/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.transformer.phantom

import org.jetbrains.kotlin.commonizer.cir.*
import org.jetbrains.kotlin.commonizer.mergedtree.*
import org.jetbrains.kotlin.commonizer.transformer.CirNodeTransformer
import org.jetbrains.kotlin.commonizer.utils.CommonizedGroup
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.storage.StorageManager

internal class PhantomGenerationTransformer(
    private val storageManager: StorageManager,
) : CirNodeTransformer {

    override fun invoke(root: CirRootNode) {
        // TODO: For prototyping purposes only generate stuff for posix to not solve the issue with duplicated classifiers
        root.modules[POSIX_MODULE_NAME]?.let { moduleNode ->
            addMissingPackages(moduleNode)

            generateUniqueDeclarations(moduleNode)
        }
    }

    private fun addMissingPackages(moduleNode: CirModuleNode) {
        for ((expectedPackage, _) in UNIQUE_DECLARATIONS) {
            moduleNode.packages.computeIfAbsent(expectedPackage) {
                val newPackage = CirPackage.create(expectedPackage)
                CirPackageNode(
                    CommonizedGroup(moduleNode.targetDeclarations.size) { newPackage },
                    storageManager.createNullableLazyValue { newPackage },
                )
            }
        }
    }

    private fun generateUniqueDeclarations(moduleNode: CirModuleNode) {
        UNIQUE_DECLARATIONS.forEach { (packageName, generationFunction) -> // todo: wrap into a decent class, if the approach is to remain
            val context = GenerationContext(storageManager, moduleNode, packageName)
            generate(context) { generationFunction(context) }
        }
    }

    private fun generate(context: GenerationContext, generator: () -> Generated) {
        val generatedResult = generator()
        val knownPackage = context.moduleNode.packages[context.packageName]
            ?: throw AssertionError("Expected package ${context.packageName} wasn't created for module ${context.moduleNode}")
        generatedResult.store(knownPackage)
    }

    companion object {
        private val CINTEROP_PACKAGE_NAME = CirPackageName.create("kotlinx.cinterop")
        private val KOTLIN_PACKAGE_NAME = CirPackageName.create("kotlin")
        private val POSIX_MODULE_NAME = CirName.create(Name.special("<org.jetbrains.kotlin.native.platform.posix>"))

        private val UNIQUE_DECLARATIONS = listOf<Pair<CirPackageName, (GenerationContext) -> Generated>>(
            KOTLIN_PACKAGE_NAME to { context -> generalizedIntegerInterface(context, SIGNED_INTEGER_ID) },
            KOTLIN_PACKAGE_NAME to { context -> generalizedIntegerInterface(context, UNSIGNED_INTEGER_ID) },
            CINTEROP_PACKAGE_NAME to { context -> varOfClass(context, SIGNED_VAR_OF_ID, SIGNED_INTEGER_ID) },
            CINTEROP_PACKAGE_NAME to { context -> varOfClass(context, UNSIGNED_VAR_OF_ID, UNSIGNED_INTEGER_ID) },
            CINTEROP_PACKAGE_NAME to { context -> convertExtensionFunction(context, SIGNED_INTEGER_ID) },
            CINTEROP_PACKAGE_NAME to { context -> convertExtensionFunction(context, UNSIGNED_INTEGER_ID) },
            CINTEROP_PACKAGE_NAME to { context -> valueExtensionProperty(context, UNSIGNED_INTEGER_ID, UNSIGNED_VAR_OF_ID) },
            CINTEROP_PACKAGE_NAME to { context -> valueExtensionProperty(context, SIGNED_INTEGER_ID, SIGNED_VAR_OF_ID) },
        )
    }
}
