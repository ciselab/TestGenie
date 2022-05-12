import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileOutputStream
import java.net.URL
import java.util.zip.ZipInputStream

fun properties(key: String) = project.findProperty(key).toString()

val thunderdomeVersion = "1.0.2"

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.6.10"
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij") version "1.4.0"
    // Gradle Changelog Plugin
    id("org.jetbrains.changelog") version "1.3.1"
    // Gradle Qodana Plugin
    id("org.jetbrains.qodana") version "0.1.13"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

// Configure project's dependencies
repositories {
    mavenCentral()
}

// include evo suite jar
dependencies {
    implementation(files("lib/evosuite-$thunderdomeVersion.jar"))
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(properties("pluginVersion"))
    groups.set(emptyList())
}

// Configure Gradle Qodana Plugin - read more: https://github.com/JetBrains/gradle-qodana-plugin
qodana {
    cachePath.set(projectDir.resolve(".qodana").canonicalPath)
    reportPath.set(projectDir.resolve("build/reports/inspections").canonicalPath)
    saveReport.set(true)
    showReport.set(System.getenv("QODANA_SHOW_REPORT")?.toBoolean() ?: false)
}

tasks {
    compileKotlin {
        dependsOn("updateEvosuite")
    }
    // Set the JVM compatibility versions
    properties("javaVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = it
        }
    }

    wrapper {
        gradleVersion = properties("gradleVersion")
    }

    patchPluginXml {
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription.set(
            projectDir.resolve("README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run { markdownToHTML(this) }
        )

        // Get the latest available change notes from the changelog file
        changeNotes.set(
            provider {
                changelog.run {
                    getOrNull(properties("pluginVersion")) ?: getLatest()
                }.toHTML()
            }
        )
    }

    // Configure UI tests plugin
    // Read more: https://github.com/JetBrains/intellij-ui-test-robot
    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels.set(listOf(properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
    }
}

/**
 * Custom gradle task used to source the custom evosuite binary
 * required for the build process. It functions as follows:
 * 1. Read the version specified inside build.gradle
 * 2. If the specified jar version is present for the build process, the
 * task finishes successfully, otherwise:
 * 3. Attempt to fetch the corresponding release from the supplied
 * download url.
 * 4. Unzips the release and places the raw jar inside the directory used by the build process
 */
abstract class UpdateEvoSuite : DefaultTask() {
    @Input
    var version: String = ""

    @TaskAction
    fun execute() {
        val libDir = File("lib")
        if (!libDir.exists()) {
            libDir.mkdirs()
        }

        val jarName = "evosuite-$version.jar"

        if (libDir.listFiles()?.any { it.name.matches(Regex(jarName)) } == true) {
            logger.info("Specified evosuite jar found, skipping update")
            return
        }

        logger.info("Specified evosuite jar not found, downloading release $jarName")

        val downloadUrl =
            "https://github.com/ciselab/evosuite/releases/download/thunderdome/release/$version/release.zip"
        val stream = try {
            URL(downloadUrl).openStream()
        } catch (e: Exception) {
            logger.error("Error fetching latest evosuite custom release - $e")
            return
        }

        ZipInputStream(stream).use { zipInputStream ->
            while (zipInputStream.nextEntry != null) {
                val file = File("lib", jarName)
                val outputStream = FileOutputStream(file)
                outputStream.write(zipInputStream.readAllBytes())
                outputStream.close()
            }
        }

        logger.info("Latest evosuite jar successfully downloaded, cleaning up lib directory")
        libDir.listFiles()?.filter { !it.name.matches(Regex(jarName)) }?.map {
            if (it.delete()) {
                logger.info("Deleted outdated release ${it.name}")
            }
        }
    }
}

tasks.register<UpdateEvoSuite>("updateEvosuite") {
    version = thunderdomeVersion
}
