package nl.tudelft.ewi.se.ciselab.testgenie.evosuite

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.util.concurrency.AppExecutorUtil
import nl.tudelft.ewi.se.ciselab.testgenie.TestGenieBundle
import nl.tudelft.ewi.se.ciselab.testgenie.Util
import nl.tudelft.ewi.se.ciselab.testgenie.editor.Workspace
import nl.tudelft.ewi.se.ciselab.testgenie.services.TestGenieSettingsService
import java.io.File
import java.nio.charset.Charset
import java.util.UUID
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
    private val classFQN: String,
    private val fileUrl: String,
    private val modTs: Long
) {
    private val log = Logger.getInstance(this::class.java)

    private val evoSuiteProcessTimeout: Long = 12000000 // TODO: Source from config
    private val evosuiteVersion = "1.0.2" // TODO: Figure out a better way to source this

    private val pluginsPath = System.getProperty("idea.plugins.path")
    private var evoSuitePath = "$pluginsPath/TestGenie/lib/evosuite-$evosuiteVersion.jar"

    private val id = UUID.randomUUID().toString()
    private val sep = File.separatorChar
    private val testResultDirectory = "${FileUtilRt.getTempDirectory()}${sep}testGenieResults$sep"
    private val testResultName = "test_gen_result_$id"

    private var key = Workspace.TestJobInfo(fileUrl, classFQN, modTs, testResultName)

    private val serializeResultPath = "\"$testResultDirectory$testResultName\""

    private val settingsState = TestGenieSettingsService.getInstance().state

    private var command = mutableListOf<String>()

    init {
        Util.makeTmp()
    }

    /**
     * Sets up evosuite to run for a target class. This is the simplest configuration.
     */
    fun forClass(): Runner {
        command = SettingsArguments(projectClassPath, projectPath, serializeResultPath, classFQN).build()
        return this
    }

    /**
     * Sets up evosuite to run for a target method of the target class. This attaches a method descriptor argument
     * to the evosuite process.
     *
     * @param methodDescriptor The method descriptor of the method under test
     */
    fun forMethod(methodDescriptor: String): Runner {
        command =
            SettingsArguments(projectClassPath, projectPath, serializeResultPath, classFQN).forMethod(methodDescriptor)
                .build()

        // attach method desc. to target unit key
        key = Workspace.TestJobInfo(fileUrl, "$classFQN#$methodDescriptor", modTs, testResultName)

        return this
    }

    /**
     * Sets up evosuite to run for a target line of the target class. This attaches the selected line argument
     * to the evosuite process.
     */
    fun forLine(selectedLine: Int): Runner {
        command = SettingsArguments(projectClassPath, projectPath, serializeResultPath, classFQN).forLine(selectedLine)
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

        cmd.add(settingsState?.javaPath!!)
        cmd.add("-jar")
        cmd.add(evoSuitePath)
        cmd.addAll(command)
        val cmdString = cmd.fold(String()) { acc, e -> acc.plus(e).plus(" ") }

        log.info("Starting EvoSuite with arguments: $cmdString")
        log.info("Results will be saved to $serializeResultPath")

        ProgressManager.getInstance()
            .run(object : Task.Backgroundable(project, TestGenieBundle.message("evosuiteTestGenerationMessage")) {
                override fun run(indicator: ProgressIndicator) {
                    try {
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

                                    val workspace = project.service<Workspace>()
                                    workspace.cancelPendingResult(testResultName)

                                    handler.destroyProcess()
                                }

                                val text = event.text

                                val progressMatcher =
                                    Pattern.compile("Progress:[>= ]*(\\d+(?:\\.\\d+)?)%").matcher(text)
                                val coverageMatcher = Pattern.compile("Cov:[>= ]*(\\d+(?:\\.\\d+)?)%").matcher(text)

                                log.info(text) // kept for debugging purposes

                                val progress =
                                    if (progressMatcher.find()) progressMatcher.group(1)?.toDouble()?.div(100)
                                    else null
                                val coverage =
                                    if (coverageMatcher.find()) coverageMatcher.group(1)?.toDouble()?.div(100)
                                    else null
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
                            evosuiteError("EvoSuite process exceeded timeout - ${evoSuiteProcessTimeout}ms")
                        }

                        if (!indicator.isCanceled) {
                            if (handler.exitCode == 0) {
                                // if process wasn't cancelled, start result watcher
                                AppExecutorUtil.getAppScheduledExecutorService()
                                    .execute(ResultWatcher(project, testResultName))
                            } else {
                                evosuiteError("EvoSuite process exited with non-zero exit code - ${handler.exitCode}")
                            }
                        }

                        indicator.fraction = 1.0
                        indicator.stop()
                    } catch (e: Exception) {
                        evosuiteError(TestGenieBundle.message("evosuiteErrorMessage").format(e.message))
                        e.printStackTrace()
                    }
                }
            })

        val workspace = project.service<Workspace>()
        workspace.addPendingResult(testResultName, key)

        return testResultName
    }

    /**
     * Show an EvoSuite execution error balloon.
     *
     * @param msg the balloon content to display
     */
    private fun evosuiteError(msg: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("EvoSuite Execution Error")
            .createNotification(
                TestGenieBundle.message("evosuiteErrorTitle"),
                msg,
                NotificationType.ERROR
            )
            .notify(project)
    }
}
