plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    api(kotlinStdlib())
    api(project(":compiler:compiler.version"))

    compileOnly(intellijCore())
    compileOnly(intellijDependency("log4j"))
    compileOnly(intellijDependency("asm-all"))
    compileOnly(jpsModel())
    compileOnly(jpsModelImpl())
}

sourceSets {
    "main" {
        projectDefault()
        resources.srcDir(File(rootDir, "resources"))
    }
    "test" {}
}
