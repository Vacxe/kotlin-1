plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    api(project(":compiler:resolution.common"))
    api(project(":core:compiler.common.jvm"))
    api(project(":compiler:psi"))
    implementation(project(":compiler:util"))
    implementation(commonDep("io.javaslang","javaslang"))
    compileOnly(intellijCore())
    compileOnly(intellijDependency("asm-all"))
    compileOnly(intellijDependency("trove4j"))
    compileOnly(intellijDependency("guava"))
}

sourceSets {
    "main" { projectDefault() }
    "test" {}
}
