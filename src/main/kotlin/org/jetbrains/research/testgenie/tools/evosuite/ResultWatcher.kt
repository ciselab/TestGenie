package org.jetbrains.research.testgenie.tools.evosuite

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt
import org.evosuite.utils.CompactReport
import org.jetbrains.research.testgenie.data.Report
import org.jetbrains.research.testgenie.tools.getImportsCodeFromTestSuiteCode
import org.jetbrains.research.testgenie.tools.getPackageFromTestSuiteCode
import org.jetbrains.research.testgenie.tools.saveData
import java.io.File
import java.io.FileReader

/**
 * Class used in conjunction with Runner to listen for the results of
 * the generation process. The listener logic runs on a separate thread.
 * Whenever the results are found, they're published on TEST_GENERATION_RESULT_TOPIC
 * and the thread exits.
 *
 * @param project Project context variable which is required for message bus passing
 * @param resultName result path on which to watch for results
 * @param fileUrl the file url (for caching)
 */
class ResultWatcher(
    private val project: Project,
    private val resultName: String,
    private val fileUrl: String,
    private val classFQN: String,
) :
    Runnable {
    private val log = Logger.getInstance(ResultWatcher::class.java)

    override fun run() {
        val sleepDurationMillis: Long = 2000
        val maxWatchDurationMillis: Long = 10000

        val sep = File.separatorChar
        val testResultDirectory = "${FileUtilRt.getTempDirectory()}${sep}testGenieResults$sep"

        val tmpDir = File(testResultDirectory)

        log.info("Started result listener thread for $resultName")

        val startTime = System.currentTimeMillis()

        while (true) {
            val currentTime = System.currentTimeMillis()

            if (currentTime - startTime > maxWatchDurationMillis) {
                log.info("Max watch duration exceeded, exiting Watcher thread for $resultName")
                return
            }

            log.info("Searching for $resultName results in $testResultDirectory")
            val list = tmpDir.list()

            if (list == null) {
                log.info("Empty dir")
            } else {
                for (pathname in list) {
                    if (pathname == resultName) {
                        log.info("Found file $pathname")

                        val gson = Gson()
                        val reader = JsonReader(FileReader("$testResultDirectory$pathname"))

                        val testGenerationResult: CompactReport = gson.fromJson(reader, CompactReport::class.java)

                        saveData(project, Report(testGenerationResult), resultName, fileUrl, getPackageFromTestSuiteCode(testGenerationResult.testSuiteCode), getImportsCodeFromTestSuiteCode(testGenerationResult.testSuiteCode, classFQN))

                        return
                    }
                }
            }
            Thread.sleep(sleepDurationMillis)
        }
    }
}
