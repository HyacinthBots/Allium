import dev.kordex.gradle.plugins.docker.file.*
import dev.kordex.gradle.plugins.kordex.DataCollection
import org.apache.tools.ant.taskdefs.condition.Os
import java.util.*

plugins {
	distribution

	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.serialization)

	alias(libs.plugins.kordex.plugin)
	alias(libs.plugins.kordex.docker)
	alias(libs.plugins.kordex.translation)

	id("net.kyori.blossom") version "2.1.0"
	id("net.kyori.indra.git") version "3.2.0"
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


group = "org.hyacinthbots"

if (Os.isFamily(Os.FAMILY_WINDOWS)) {
	version = "eww-testing-on-windows"
} else {
	version = if ("git branch --show-current"
			.runCommand(workingDir = rootDir)
			.replace("/", ".") == "root") {
		"date +%Y-%m-%d".runCommand()
	} else {
		"date +%Y-%m-%d".runCommand() + "-build.local-" +
			"git rev-parse --short=8 HEAD"
				.runCommand(workingDir = rootDir) +
			"-" +
			"git branch --show-current"
				.runCommand(workingDir = rootDir)
				.replace("/", ".")
	}
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
	implementation(libs.kotlin.stdlib)
	implementation(libs.kx.ser)
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

kordEx {
	// The current LTS Java version
	jvmTarget = 21

	kordVersion = "0.18.0-SNAPSHOT"

	module("func-mappings")
	module("pluralkit")

	ignoreIncompatibleKotlinVersion = true

	bot {
		mainClass = "org.hyacinthbots.allium.AppKt"

		dataCollection(DataCollection.Minimal)
	}

	i18n {
		bundle("allium.strings", "org.hyacinthbots.allium.i18n")
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
		gradleVersion = "9.3.1"
		distributionType = Wrapper.DistributionType.BIN
	}
}

docker {
	// Create the Dockerfile in the root folder.
	file(rootProject.file("Dockerfile"))

	commands {
		// Each function (aside from comment/emptyLine) corresponds to a Dockerfile instruction.
		// See: https://docs.docker.com/reference/dockerfile/

		from("openjdk:21-jdk-slim")

		emptyLine()

		comment("Create required directories")
		runShell("mkdir -p /bot/plugins")
		runShell("mkdir -p /bot/data")
		runShell("mkdir -p /dist/out")

		emptyLine()

		// Add volumes for locations that you need to persist. This is important!
		comment("Declare required volumes")
		volume("/bot/data")  // Storage for data files
		volume("/bot/plugins")  // Plugin ZIP/JAR location

		emptyLine()

		comment("Copy the distribution files into the container")
		copy("build/distributions/${project.name}-${project.version}.tar", "/dist")

		emptyLine()

		comment("Extract the distribution files, and prepare them for use")
		runShell("tar -xf /dist/${project.name}-${project.version}.tar -C /dist/out")

		if (file("src/main/dist/plugins").isDirectory) {
			runShell("mv /dist/out/${project.name}-${project.version}/plugins/* /bot/plugins")
		}

		runShell("chmod +x /dist/out/${project.name}-${project.version}/bin/$name")

		emptyLine()

		comment("Clean up unnecessary files")
		runShell("rm /dist/${project.name}-${project.version}.tar")

		emptyLine()

		comment("Set the correct working directory")
		workdir("/bot")

		emptyLine()

		comment("Run the distribution start script")
		entryPointExec("/dist/out/${project.name}-${project.version}/bin/$name")
	}
}
