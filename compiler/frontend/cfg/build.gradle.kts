plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    api(project(":compiler:frontend"))
    compileOnly(intellijCore())
    compileOnly(intellijDependency("guava"))
}

sourceSets {
    "main" { projectDefault() }
    "test" {}
}
