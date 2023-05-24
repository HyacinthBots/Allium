pluginManagement {
    plugins {
        val kotlinVersion = "1.8.21"
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion

        // Update this in libs.version.toml when you change it here
        id("io.gitlab.arturbosch.detekt") version "1.23.0"

        id("com.github.jakemarsden.git-hooks") version "0.0.2"
        id("com.github.johnrengelman.shadow") version "8.1.1"
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
