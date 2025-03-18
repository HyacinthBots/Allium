pluginManagement {
    plugins {
        val kotlinVersion = "2.1.0"

        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion

        // Update this in libs.version.toml when you change it here
        id("io.gitlab.arturbosch.detekt") version "1.23.5"

        id("com.github.jakemarsden.git-hooks") version "0.0.2"
        id("com.github.johnrengelman.shadow") version "8.1.1"
        id("com.github.gmazzo.buildconfig") version "3.1.0"
        id("dev.kordex.gradle.kordex") version "1.7.0"
        id("net.kyori.blossom") version "2.1.0"
        id("net.kyori.indra.git") version "3.1.3"
    }
	repositories {
		gradlePluginPortal()
		mavenCentral()

		maven("https://releases-repo.kordex.dev")
		maven("https://snapshots-repo.kordex.dev")
	}
}

rootProject.name = "allium"
