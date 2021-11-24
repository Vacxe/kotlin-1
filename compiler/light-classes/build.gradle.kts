
plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    api(project(":compiler:util"))
    api(project(":compiler:backend"))
    api(project(":compiler:frontend"))
    api(project(":compiler:frontend.java"))
    compileOnly(intellijCore())
    compileOnly(intellijDependency("asm-all"))
    compileOnly(intellijDependency("trove4j"))
    compileOnly(intellijDependency("guava"))
}

sourceSets {
    "main" { projectDefault() }
    "test" {}
}

