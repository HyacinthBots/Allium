import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Date

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


group = "org.hyacinthbots.allium"
//version = "0.4-build.local-" + "git rev-parse --short=8 HEAD".runCommand(workingDir = rootDir)
var buildTime = Date().time / 1000
version = "0.3.3"
// The current LTS Java version
val javaVersion = 17

blossom {
    replaceToken("@version@", version)
    replaceToken("@buildTime@", buildTime)
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

    maven {
        name = "FabricMC"
        url = uri("https://maven.fabricmc.net/")
    }

    maven {
        name = "JitPack"
        url = uri("https://jitpack.io")
    }

    maven {
        name = "QuiltMC (Releases)"
        url = uri("https://maven.quiltmc.org/repository/release/")
    }

    maven {
        name = "QuiltMC (Snapshots)"
        url = uri("https://maven.quiltmc.org/repository/snapshot/")
    }
}

dependencies {
    detektPlugins(libs.detekt)

    implementation(libs.kord.extensions.core)
    implementation(libs.kord.extensions.mappings)
    implementation(libs.kord.extensions.pluralkit)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kx.ser)
    implementation(libs.gson)
    implementation(libs.doc.gen)

    // Logging dependencies
    implementation(libs.jansi)
    implementation(libs.logback)
    implementation(libs.logging)
    implementation(libs.groovy)
}

application {
    mainClass.set("org.hyacinthbots.allium.AppKt")
}

gitHooks {
    setHooks(
        mapOf("pre-commit" to "detekt")
    )
}

tasks {
    processResources {
        inputs.property("version", version)
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = javaVersion.toString()
            languageVersion = "1.7" // The current major revision of Kotlin
            incremental = true
            freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
        }
    }

    jar {
        manifest {
            attributes(
                "Main-Class" to "org.hyacinthbots.allium.AppKt"
            )
        }
    }

    wrapper {
        /*
        Update gradle by changing `gradleVersion` below to the new version,
        then run `./gradlew wrapper` twice to update the scripts properly.
         */
        gradleVersion = "8.0-rc-1"
        distributionType = Wrapper.DistributionType.BIN
    }
}

java {
    // Current LTS version of Java
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
}

detekt {
    buildUponDefaultConfig = true
    autoCorrect = true
    config = rootProject.files("detekt.yml")
}
