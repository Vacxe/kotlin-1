plugins {
    kotlin("jvm")
    id("jps-compatible")
}

repositories {
    maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
}

val jflexPath by configurations.creating

dependencies {
    api(project(":core:compiler.common"))
    api(project(":compiler:util"))
    api(project(":compiler:frontend.common"))
    api(project(":kotlin-script-runtime"))

    compileOnly(project(":dependencies:intellij-core"))
    compileOnly(intellijDependency("guava"))
    compileOnly(intellijDependency("trove4j"))

    jflexPath(commonDep("org.jetbrains.intellij.deps.jflex", "jflex"))
}

sourceSets {
    "main" { projectDefault() }
    "test" {}
}



ant.importBuild("buildLexer.xml")

ant.properties["builddir"] = buildDir.absolutePath

tasks.findByName("lexer")!!.apply {
    doFirst {
        ant.properties["flex.classpath"] = jflexPath.asPath
    }
}
