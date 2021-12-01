/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.lower

import org.jetbrains.kotlin.backend.common.lower.InventNamesForLocalClasses
import org.jetbrains.kotlin.ir.backend.js.JsIrBackendContext
import org.jetbrains.kotlin.ir.backend.js.utils.sanitizeName
import org.jetbrains.kotlin.ir.declarations.IrAttributeContainer
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.util.render

class JsInventNamesForLocalClasses(private val context: JsIrBackendContext) : InventNamesForLocalClasses(allowTopLevelCallables = true) {
    override fun computeTopLevelClassName(clazz: IrClass): String {
        val file = clazz.parent as? IrFile ?: error("Top-level class expected: ${clazz.render()}")
        return file.fqName.child(clazz.name).toString()
    }

    override fun sanitizeNameIfNeeded(name: String): String = sanitizeName(name, withHash = false)

    override fun putLocalClassName(declaration: IrAttributeContainer, localClassName: String) {
        if (declaration is IrClass) {
            context.localClassNames[declaration] = localClassName
        }
    }
}
