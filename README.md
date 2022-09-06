# KordEx Bot Template

This repository contains a basic KordEx example bot for you to use as a template for your own KordEx bots. This
includes the following:

* A basic extension that allows you to slap other people, using both message commands and slash commands
* A basic bot configuration that enables slash commands and shows you how to conditionally provide a different
  message command prefix for different guilds
* A Gradle Kotlin build script that makes use of the Kotlin Discord public maven repo, Detekt for linting (with a 
  fairly strict configuration), and a Git commit hook plugin that runs Detekt when you make a commit - this also makes
  use of Gradle 7's new version catalog feature, for easy configuration of dependencies
* GitHub CI scripts that build the bot and publish its artefacts
* A reasonable `.gitignore` file, including one in the `.idea` folder that ignores files that shouldn't be committed -
  if you're using IDEA yourself, you should install the Ignore plugin to handle changes to this for you
* A Groovy-based Logback config, so you have reasonable logging out of the box
* A Gradle wrapper using Gradle 7.1.1

## Potential Changes

* The `.yml` files in `.github/` are used to configure GitHub apps. If you're not using them, you can remove them.
* The provided `LICENSE` file contains The Unlicense, which makes this repository public domain. You'll probably want
  to change this - we suggest looking at [Choose a License](https://choosealicense.com/) if you're not sure where to 
  start.
* In the `build.gradle.kts`:
    * Set the `group` and `version` properties as appropriate
    * If you're not using this to test KordEx builds, you can remove the `mavenLocal()` from the `repositories` block
    * In the `application` and `tasks.jar` blocks, update the main class path/name as appropriate
    * To target a newer/older Java version, change the options in the `KotlinCompile` configuration and `java` blocks
* In the `settings.gradle.kts`, update the name of the root project as appropriate.
* The bundled Detekt config is pretty strict. You can check over `detekt.yml` if you want to change it, but you need to 
  follow the TODOs in that file regardless.
* The Logback configuration is in `src/main/resources/logback.groovy`. If the logging setup doesn't suit, you can change
  it there.

## Bundled Bot

* `App.kt` includes a basic bot which uses environment variables (or variables in a `.env` file) for the testing guild
  ID (`TEST_SERVER`) and the bot's token (`TOKEN`). You can specify these either directly as environment variables, or
  as `KEY=value` pairs in a file named `.env`. This file also includes some example code that shows one potential way 
  of providing different command prefixes for different servers.
* `TestExtension.kt` includes a simple example extension that creates a `slap` command. This command works as both a
  message command and slash command, and allows you to slap other users with whatever you wish, defaulting to a
  `large, smelly trout`.

To test the bot, we recommend using a `.env` file that looks like the following:

```dotenv
TOKEN=abc...
TEST_SERVER=123...
```

Create this file, fill it out, and run the `run` gradle task for testing in development.
