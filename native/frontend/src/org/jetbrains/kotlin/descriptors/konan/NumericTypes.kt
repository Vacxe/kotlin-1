/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.descriptors.konan

import org.jetbrains.kotlin.name.ClassId

val KOTLIN_BYTE = ClassId.fromString("kotlin/Byte")
val KOTLIN_SHORT = ClassId.fromString("kotlin/Short")
val KOTLIN_INT = ClassId.fromString("kotlin/Int")
val KOTLIN_LONG = ClassId.fromString("kotlin/Long")

val KOTLIN_UBYTE = ClassId.fromString("kotlin/UByte")
val KOTLIN_USHORT = ClassId.fromString("kotlin/UShort")
val KOTLIN_UINT = ClassId.fromString("kotlin/UInt")
val KOTLIN_ULONG = ClassId.fromString("kotlin/ULong")

val KOTLIN_DOUBLE = ClassId.fromString("kotlin/Double")
val KOTLIN_FLOAT = ClassId.fromString("kotlin/Float")

val KOTLIN_BOOLEAN = ClassId.fromString("kotlin/Boolean")

val BYTE_VAR = ClassId.fromString("kotlinx/cinterop/ByteVarOf")
val SHORT_VAR = ClassId.fromString("kotlinx/cinterop/ShortVarOf")
val INT_VAR = ClassId.fromString("kotlinx/cinterop/IntVarOf")
val LONG_VAR = ClassId.fromString("kotlinx/cinterop/LongVarOf")

val U_BYTE_VAR = ClassId.fromString("kotlinx/cinterop/UByteVarOf")
val U_SHORT_VAR = ClassId.fromString("kotlinx/cinterop/UShortVarOf")
val U_INT_VAR = ClassId.fromString("kotlinx/cinterop/UIntVarOf")
val U_LONG_VAR = ClassId.fromString("kotlinx/cinterop/ULongVarOf")

val DOUBLE_VAR = ClassId.fromString("kotlinx/cinterop/DoubleVarOf")
val FLOAT_VAR = ClassId.fromString("kotlinx/cinterop/FloatVarOf")

val BOOLEAN_VAR = ClassId.fromString("kotlinx/cinterop/BooleanVarOf")

val SIGNED_INTEGERS: Set<ClassId> = setOf(
    KOTLIN_BYTE,
    KOTLIN_SHORT,
    KOTLIN_INT,
    KOTLIN_LONG,
)

val UNSIGNED_INTEGERS: Set<ClassId> = setOf(
    KOTLIN_UBYTE,
    KOTLIN_USHORT,
    KOTLIN_UINT,
    KOTLIN_ULONG,
)

val FLOATING_POINTS: Set<ClassId> = setOf(
    KOTLIN_FLOAT,
    KOTLIN_DOUBLE,
)

val SIGNED_VARS: Set<ClassId> = setOf(
    BYTE_VAR,
    SHORT_VAR,
    INT_VAR,
    LONG_VAR,
)

val UNSIGNED_VARS: Set<ClassId> = setOf(
    U_BYTE_VAR,
    U_SHORT_VAR,
    U_INT_VAR,
    U_LONG_VAR,
)

val FLOATING_POINT_VARS: Set<ClassId> = setOf(
    FLOAT_VAR,
    DOUBLE_VAR,
)

val INTEGERS: Set<ClassId> = SIGNED_INTEGERS + UNSIGNED_INTEGERS
val INTEGER_VARS: Set<ClassId> = SIGNED_VARS + UNSIGNED_VARS

val CVARIABLE_ID = ClassId.fromString("kotlinx/cinterop/CVariable")

val PHANTOM_SIGNED_INTEGER = ClassId.fromString("kotlin/PlatformInt")
val PHANTOM_UNSIGNED_INTEGER = ClassId.fromString("kotlin/PlatformUInt")

val PHANTOM_SIGNED_VAR_OF = ClassId.fromString("kotlinx/cinterop/PlatformIntVarOf")
val PHANTOM_UNSIGNED_VAR_OF = ClassId.fromString("kotlinx/cinterop/PlatformUIntVarOf")

val PHANTOM_INTEGERS: Set<ClassId> = setOf(
    PHANTOM_SIGNED_INTEGER, PHANTOM_UNSIGNED_INTEGER
)

val PHANTOM_VARIABLES: Set<ClassId> = setOf(
    PHANTOM_SIGNED_VAR_OF, PHANTOM_UNSIGNED_VAR_OF
)

val PHANTOM_TYPES: Set<ClassId> =
    PHANTOM_INTEGERS + PHANTOM_VARIABLES
