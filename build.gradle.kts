plugins {
    id("java")
    id("antlr")
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
tasks.register<Exec>("runScript") {
    commandLine("bash", "run.sh")
}
tasks.register<JavaExec>("runMain") {
    group = "application"
    description = "Runs the main class"

    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("net.villagerzock.Main") // <- deine Main Klasse

    //dependsOn("runScript")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "net.villagerzock.Main"
        )
    }
}