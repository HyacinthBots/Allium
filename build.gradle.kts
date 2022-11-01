import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application

    kotlin("jvm")
    kotlin("plugin.serialization")

    id("com.github.gmazzo.buildconfig") version "3.1.0"
    id("com.github.jakemarsden.git-hooks")
    id("com.github.johnrengelman.shadow")
    id("io.gitlab.arturbosch.detekt")
    id("net.kyori.blossom") version "1.3.1"
}

fun String.runCommand(
    workingDir: File = File("."),
    timeoutAmount: Long = 60,
    timeoutUnit: TimeUnit = TimeUnit.SECONDS
): String = ProcessBuilder(split("\\s(?=(?:[^'\"`]*(['\"`])[^'\"`]*\\1)*[^'\"`]*$)".toRegex()))
    .directory(workingDir)
    .redirectOutput(ProcessBuilder.Redirect.PIPE)
    .redirectError(ProcessBuilder.Redirect.PIPE)
    .start()
    .apply { waitFor(timeoutAmount, timeoutUnit) }
    .run {
        val error = errorStream.bufferedReader().readText().trim()
        if (error.isNotEmpty()) {
            throw Exception(error)
        }
        inputStream.bufferedReader().readText().trim()
    }


group = "de.notjansel.sbbot"
version = "0.1.3-build.local-" + "git rev-parse --short=8 HEAD".runCommand(workingDir = rootDir)

blossom {
    replaceToken("@version@", version)
}

repositories {
    google()
    mavenCentral()

    maven {
        name = "Sonatype Snapshots"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }

    maven {
        name = "Kotlin Discord"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }
}

dependencies {
    detektPlugins(libs.detekt)

    implementation(libs.kord.extensions)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kx.ser)
    implementation(libs.gson)


    // Logging dependencies
    implementation(libs.jansi)
    implementation(libs.logback)
    implementation(libs.logging)
}

application {
    // This is deprecated, but the Shadow plugin requires it
    mainClassName = "de.notjansel.sbbot.AppKt"
}

gitHooks {
    setHooks(
        mapOf("pre-commit" to "detekt")
    )
}

tasks.processResources {
    inputs.property("version", version)
}

tasks.withType<KotlinCompile> {
    // Current LTS version of Java
    kotlinOptions.jvmTarget = "17"

    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "de.notjansel.sbbot.AppKt"
        )
    }
}

java {
    // Current LTS version of Java
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

detekt {
    buildUponDefaultConfig = true
    autoCorrect = true
    config = rootProject.files("detekt.yml")
}
