import dev.kordex.gradle.plugins.kordex.DataCollection
import java.util.*

plugins {
	application

	kotlin("jvm")
	kotlin("plugin.serialization")

	id("com.github.gmazzo.buildconfig")
	id("com.github.jakemarsden.git-hooks")
	id("com.github.johnrengelman.shadow")
	id("dev.kordex.gradle.kordex")
	id("io.gitlab.arturbosch.detekt")
	id("net.kyori.blossom")
	id("net.kyori.indra.git")
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

version = if ("git branch --show-current"
		.runCommand(workingDir = rootDir)
		.replace("/", ".") == "root") {
	"1.0.1a"
		} else {
	"1.0.2-build.local-" +
		"git rev-parse --short=8 HEAD"
			.runCommand(workingDir = rootDir) +
		"-" +
		"git branch --show-current"
			.runCommand(workingDir = rootDir)
			.replace("/", ".")
}

var buildTime = Date().time / 1000

sourceSets {
	main {
		blossom {
			kotlinSources {
				property("version", project.version.toString())
				property("buildtime", buildTime.toString())
			}
		}
	}
}

dependencies {
	detektPlugins(libs.detekt)

	implementation(libs.kotlin.stdlib)
	implementation(libs.kx.ser)
	implementation(libs.kx.ser.json)
	implementation(libs.gson)
	implementation(libs.doc.gen)
	implementation(libs.kmongo)

	// Logging dependencies
	implementation(libs.jansi)
	implementation(libs.logging)
	implementation(libs.groovy)
	implementation(libs.logback)
	implementation(libs.logback.groovy)
}

gitHooks {
	setHooks(
		mapOf("pre-commit" to "detekt")
	)
}

kordEx {
	// The current LTS Java version
	jvmTarget = 21

	module("func-mappings")
	module("pluralkit")

	bot {
		mainClass = "org.hyacinthbots.allium.AppKt"

		dataCollection(DataCollection.Minimal)
	}

	i18n {
		classPackage = "org.hyacinthbots.allium.i18n"
		translationBundle = "allium.strings"
	}
}

tasks {
	processResources {
		inputs.property("version", project.version)
	}

	wrapper {
		/*
		 * Update gradle by changing `gradleVersion` below to the new version,
		 * then run `./gradlew wrapper` twice to update the scripts properly.
		 */
		gradleVersion = "8.13"
		distributionType = Wrapper.DistributionType.BIN
	}
}

detekt {
	buildUponDefaultConfig = true
	autoCorrect = true
	config.setFrom(rootProject.files("detekt.yml"))
}
