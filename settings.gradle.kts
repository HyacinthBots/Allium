pluginManagement {
    plugins {
        val kotlinVersion = "1.8.10"
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion

        // Update this in libs.version.toml when you change it here
        id("io.gitlab.arturbosch.detekt") version "1.22.0"

        id("com.github.jakemarsden.git-hooks") version "0.0.2"
        id("com.github.johnrengelman.shadow") version "7.1.2"
    }
}

rootProject.name = "Allium"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}
