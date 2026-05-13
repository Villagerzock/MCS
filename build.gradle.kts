plugins {
    id("java")
    id("antlr")
    id("com.gradleup.shadow") version "9.0.0"
}

group = "net.villagerzock"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:4.13.1")
    implementation("org.antlr:antlr4-runtime:4.13.1")
    implementation("com.google.code.gson:gson:2.14.0")
    implementation("info.picocli:picocli:4.7.7")
}

tasks.generateGrammarSource {
    outputDirectory = file("$buildDir/generated-src/antlr/main/net/villagerzock/compiler/parser")

    arguments = arguments + listOf(
        "-visitor",
        "-package", "net.villagerzock.compiler.parser"
    )
}
tasks.shadowJar {
    archiveClassifier.set("")
}
tasks.register<Exec>("runScript") {
    commandLine("bash", "run.sh")
}
tasks.configureEach {
    if (name != "runScript" && org.gradle.internal.os.OperatingSystem.current().isLinux) {
        dependsOn("runScript")
    }
}
tasks.register<JavaExec>("runMain") {
    group = "application"
    description = "Runs the main class"

    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("net.villagerzock.Main")
    args = listOf("./testScripts/","--ast","-v"/*,"-cp","./classpath/"*/)
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "net.villagerzock.Main"
        )
    }
}