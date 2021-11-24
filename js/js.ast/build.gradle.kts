plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    api(project(":core:descriptors"))
    compileOnly(intellijCore())
    compileOnly(intellijDependency("trove4j"))
}

sourceSets {
    "main" { projectDefault() }
    "test" {}
}
