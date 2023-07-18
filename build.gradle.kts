import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileOutputStream
import java.net.URL
import java.util.zip.ZipInputStream

fun properties(key: String) = project.findProperty(key).toString()

val thunderdomeVersion = "1.0.5"

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.7.21"
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij") version "1.13.3"
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
    maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
    maven("https://packages.jetbrains.team/maven/p/grazi/grazie-platform-public")
}

dependencies {
    implementation(files("lib/evosuite-$thunderdomeVersion.jar"))
    implementation(files("lib/standalone-runtime.jar"))
    implementation(files("lib/jacocoagent.jar"))
    implementation(files("lib/jacococli.jar"))
    implementation(files("lib/mockito-core-5.0.0.jar"))
    implementation(files("lib/JUnitRunner-1.0.jar"))

    // validation dependencies
    // https://mvnrepository.com/artifact/junit/junit
    implementation("junit:junit:4.13")
    // https://mvnrepository.com/artifact/org.jacoco/org.jacoco.core
    implementation("org.jacoco:org.jacoco.core:0.8.8")
    // https://mvnrepository.com/artifact/com.github.javaparser/javaparser-core
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.24.2")

    // https://gitlab.com/mvysny/konsume-xml
    implementation("com.gitlab.mvysny.konsume-xml:konsume-xml:1.0")

    // From the jetbrains repository
    testImplementation("com.intellij.remoterobot:remote-robot:0.11.13")
    testImplementation("com.intellij.remoterobot:remote-fixtures:0.11.13")

    // https://mvnrepository.com/artifact/com.squareup.okhttp3/logging-interceptor
    testImplementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")

    // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-engine
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")

    // https://mvnrepository.com/artifact/org.assertj/assertj-core
    testImplementation("org.assertj:assertj-core:3.22.0")

    // https://mvnrepository.com/artifact/com.automation-remarks/video-recorder-junit5
    implementation("com.automation-remarks:video-recorder-junit5:2.0")

    // https://mvnrepository.com/artifact/org.mockito/mockito-all
    testImplementation("org.mockito:mockito-all:1.10.19")

    // https://mvnrepository.com/artifact/net.jqwik/jqwik
    testImplementation("net.jqwik:jqwik:1.6.5")

    // https://mvnrepository.com/artifact/com.github.javaparser/javaparser-symbol-solver-core
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.24.2")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-test
    implementation("org.jetbrains.kotlin:kotlin-test:1.8.0")

    // LLM
    // https://packages.jetbrains.team/maven/p/grazi/grazie-platform-public/ai/grazie/api/api-gateway-api-jvm/
    implementation("ai.grazie.api:api-gateway-api-jvm:0.2.162")
    // https://packages.jetbrains.team/maven/p/grazi/grazie-platform-public/ai/grazie/api/api-gateway-client-jvm/
    implementation("ai.grazie.api:api-gateway-client-jvm:0.2.161")
    // https://packages.jetbrains.team/maven/p/grazi/grazie-platform-public/ai/grazie/client/client-ktor-jvm/
    implementation("ai.grazie.client:client-ktor:0.2.161")
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

    test {
        useJUnitPlatform()
        if (System.getProperty("test.profile") != "ui") {
            exclude("**/*uiTest*")
        }
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
            }.joinToString("\n").run { markdownToHTML(this) },
        )

        // Get the latest available change notes from the changelog file
        changeNotes.set(
            provider {
                changelog.run {
                    getOrNull(properties("pluginVersion")) ?: getLatest()
                }.toHTML()
            },
        )
    }

    // Configure UI tests plugin
    // Read more: https://github.com/JetBrains/intellij-ui-test-robot
    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
        systemProperty("idea.trust.all.projects", "true")
        systemProperty("ide.show.tips.on.startup.default.value", "false")
        systemProperty("jb.consents.confirmation.enabled", "false")
        systemProperty("ide.mac.file.chooser.native", "false")
        systemProperty("apple.laf.useScreenMenuBar", "false")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN").trimIndent())
        privateKey.set(System.getenv("PRIVATE_KEY").trimIndent())
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
