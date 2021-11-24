plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    api(project(":core:descriptors"))
    api(project(":core:descriptors.jvm"))
    api(project(":compiler:util"))
    api(project(":compiler:config.jvm"))
    api("javax.annotation:jsr250-api:1.0")
    api(project(":compiler:frontend"))
    api(project(":compiler:resolution.common.jvm"))
    compileOnly(intellijCore())
    compileOnly(intellijDependency("asm-all"))
    compileOnly(intellijDependency("trove4j"))
    compileOnly(intellijDependency("guava"))
}

sourceSets {
    "main" { projectDefault() }
    "test" {}
}

