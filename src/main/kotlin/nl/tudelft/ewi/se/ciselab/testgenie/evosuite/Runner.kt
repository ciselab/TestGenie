package nl.tudelft.ewi.se.ciselab.testgenie.evosuite

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.util.concurrency.AppExecutorUtil
import nl.tudelft.ewi.se.ciselab.testgenie.TestGenieBundle
import nl.tudelft.ewi.se.ciselab.testgenie.settings.TestGenieSettingsService
import java.io.File
import java.nio.charset.Charset
import java.util.regex.Pattern

/**
 * A utility class that runs evosuite as a separate process in its various
 * modes of operation.
 *
 * @param projectPath The root of the project we're testing, this sets the working dir of the evosuite process
 * @param projectClassPath Path to the class path containing the compiled classes. This will change according to
 * build system (e.g. Maven target/classes or Gradle build/classes)
 * @param classFQN Fully qualified name of the class under test
 */
class Runner(
    private val project: Project,
    private val projectPath: String,
    private val projectClassPath: String,
    private val classFQN: String
) {
    private val log = Logger.getInstance(this::class.java)

    private val evoSuiteProcessTimeout: Long = 12000000 // TODO: Source from config
    private val javaPath = "java" // TODO: Source from config
    private val evosuiteVersion = "1.0.2" // TODO: Figure out a better way to source this

    private val pluginsPath = System.getProperty("idea.plugins.path")
    private var evoSuitePath = "$pluginsPath/TestGenie/lib/evosuite-$evosuiteVersion.jar"

    private val ts = System.currentTimeMillis()
    private val sep = File.separatorChar
    private val testResultDirectory = "${FileUtilRt.getTempDirectory()}${sep}testGenieResults$sep"
    private val testResultName = "test_gen_result_$ts"
    private val serializeResultPath = "\"$testResultDirectory$testResultName\""

    private val settingsState = TestGenieSettingsService.getInstance().state

    private var command = mutableListOf<String>()

    /**
     * Sets up evosuite to run for a target class. This is the simplest configuration.
     */
    fun forClass(): Runner {
        command = SettingsArguments(projectClassPath, projectPath, serializeResultPath, classFQN).build()
        return this
    }

    /**
     * Sets up evosuite to run for a target method of the target class. This attaches a method prefix argument
     * to the evosuite process.
     */
    fun forMethod(method: String): Runner {
        command =
            SettingsArguments(projectClassPath, projectPath, serializeResultPath, classFQN).forMethodPrefix(method)
                .build()
        return this
    }

    /**
     * Performs final argument preparation and launches the evosuite process on a separate thread.
     *
     * @return the path to which results will be (eventually) saved
     */
    fun runEvoSuite(): String {
        if (!settingsState?.seed.isNullOrBlank()) command.add("-seed=${settingsState?.seed}")
        if (!settingsState?.configurationId.isNullOrBlank()) command.add("-Dconfiguration_id=${settingsState?.configurationId}")

        val cmd = ArrayList<String>()

        cmd.add(javaPath)
        cmd.add("-jar")
        cmd.add(evoSuitePath)
        cmd.addAll(command)
        val cmdString = cmd.fold(String()) { acc, e -> acc.plus(e).plus(" ") }

        log.info("Starting EvoSuite with arguments: $cmdString")
        log.info("Results will be saved to $serializeResultPath")

        ProgressManager.getInstance()
            .run(object : Task.Backgroundable(project, TestGenieBundle.message("evosuiteTestGenerationMessage")) {
                override fun run(indicator: ProgressIndicator) {
                    indicator.isIndeterminate = false
                    indicator.text = TestGenieBundle.message("evosuiteSearchMessage")
                    val evoSuiteProcess = GeneralCommandLine(cmd)
                    evoSuiteProcess.charset = Charset.forName("UTF-8")
                    evoSuiteProcess.setWorkDirectory(projectPath)
                    val handler = OSProcessHandler(evoSuiteProcess)

                    // attach process listener for output
                    handler.addProcessListener(object : ProcessAdapter() {
                        override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {

                            if (indicator.isCanceled) {
                                log.info("Cancelling search")
                                handler.destroyProcess()
                            }

                            val text = event.text

                            val progressMatcher = Pattern.compile("Progress:[>= ]*(\\d+(?:\\.\\d+)?)%").matcher(text)
                            val coverageMatcher = Pattern.compile("Cov:[>= ]*(\\d+(?:\\.\\d+)?)%").matcher(text)

                            log.info(text) // kept for debugging purposes

                            val progress =
                                if (progressMatcher.find()) progressMatcher.group(1)?.toDouble()?.div(100) else null
                            val coverage =
                                if (coverageMatcher.find()) coverageMatcher.group(1)?.toDouble()?.div(100) else null
                            if (progress != null && coverage != null) {
                                indicator.fraction = if (progress >= coverage) progress else coverage
                            } else if (progress != null) {
                                indicator.fraction = progress
                            } else if (coverage != null) {
                                indicator.fraction = coverage
                            }

                            if (indicator.fraction == 1.0 && indicator.text != TestGenieBundle.message("evosuitePostProcessMessage")) {
                                indicator.text = TestGenieBundle.message("evosuitePostProcessMessage")
                            }
                        }
                    })

                    handler.startNotify()

                    // treat this as a join handle
                    if (!handler.waitFor(evoSuiteProcessTimeout)) {
                        log.error("EvoSuite process exceeded timeout - ${evoSuiteProcessTimeout}ms")
                    }

                    if (handler.exitCode == 0) {
                        // if process wasn't cancelled, start result watcher
                        if (!indicator.isCanceled) {
                            AppExecutorUtil.getAppScheduledExecutorService()
                                .execute(ResultWatcher(project, testResultName))
                        }
                    } else {
                        log.error("EvoSuite process exited with non-zero exit code - ${handler.exitCode}")
                    }

                    indicator.fraction = 1.0
                    indicator.stop()
                }
            })
        return testResultName
    }
}
