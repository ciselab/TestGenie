package org.jetbrains.research.testgenie.tools.llm.generation

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import org.jetbrains.research.testgenie.tools.llm.test.TestCaseGeneratedByLLM
import org.jetbrains.research.testgenie.tools.llm.test.TestLine
import org.jetbrains.research.testgenie.tools.llm.test.TestLineType
import org.jetbrains.research.testgenie.tools.llm.test.TestSuiteGeneratedByLLM


var rawText = ""

val importPattern = Regex(
    pattern = "^import\\s+(static\\s)?((?:[a-zA-Z_]\\w*\\.)*[a-zA-Z_](?:\\w*\\.?)*)(?:\\.\\*)?;",
    options = setOf(RegexOption.MULTILINE),
)

var lastTestCount = 0

class TestsAssembler(
    val indicator: ProgressIndicator,
) {
    private val log: Logger = Logger.getInstance(this.javaClass)
    fun receiveResponse(text: String) {
        // Collect the response and update the progress bar
        rawText = rawText.plus(text)
        val generatedTestsCount = rawText.split("@Test").size - 1

        if (lastTestCount != generatedTestsCount) {
            indicator.text = "Generating test #$generatedTestsCount"
            lastTestCount = generatedTestsCount
        }

        log.debug(rawText)
    }

    fun getLLMResponse(): String{
        return rawText
    }

    companion object {
        fun returnTestSuite(packageName: String): TestSuiteGeneratedByLLM {
            val testSuite = TestSuiteGeneratedByLLM()
            rawText = rawText.split("```")[1]

            testSuite.packageString = packageName

            // save imports
            testSuite.imports = importPattern.findAll(rawText, 0).map {
                it.groupValues[0]
            }.toSet()

            val testSet: MutableList<String> = rawText.split("@Test").toMutableList()
            testSet.removeAt(0)

            testSet.forEach ca@{
                val rawTest = "@Test$it"
                val currentTest = TestCaseGeneratedByLLM()

                // Get expected Exception
                if (rawTest.startsWith("@Test(expected =")) {
                    currentTest.expectedException = rawTest
                        .split(")")[0]
                        .trim()
                }

                // Get unexpected exceptions
                if (!rawTest.contains("public void")) {
                    println("WARNING: The raw Test does not contain public void:\n $rawTest")
                    return@ca
                }
                val interestingPartOfSignature = rawTest
                    .split("public void")[1]
                    .split("{")[0]
                    .split("()")[1]
                    .trim()
                if (interestingPartOfSignature.contains("throws")) {
                    currentTest.throwsException = interestingPartOfSignature
                        .split("throws")[1]
                        .trim()
                }

                // Get test's name
                currentTest.name = rawTest
                    .split("public void ")[1]
                    .split("()")[0]
                    .trim()

                // Get test's body
                // remove opening bracket
                var testBody: String = rawTest.split("{")[1].trim()

                // remove closing bracket

                val tempList = testBody.split("}").toMutableList()
                tempList.removeLast()

                if (testSuite.testCases.size == testSet.size - 1) {
                    // it is the last test. So we should remove another closing bracket
                    if (tempList.isNotEmpty()) {
                        tempList.removeLast()
                    } else {
                        println("WARNING: the final test does not have to brackets:\n $testBody")
                    }
                }

                testBody = tempList.joinToString("}")

                // Save each line
                val lines = testBody.split("\n").toMutableList()
                lines.forEach { rawLine ->
                    val line = rawLine.trim()

                    val type: TestLineType = when {
                        line.startsWith("//") -> TestLineType.COMMENT
                        line.isBlank() -> TestLineType.BREAK
                        line.lowercase().startsWith("assert") -> TestLineType.ASSERTION
                        else -> TestLineType.CODE
                    }

                    currentTest.lines.add(TestLine(type, line))
                }

                testSuite.testCases.add(currentTest)
            }
            return testSuite
        }
    }
}
