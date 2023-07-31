package org.jetbrains.research.testgenie.tools.llm.generation

import ai.grazie.api.gateway.client.SuspendableAPIGatewayClient
import ai.grazie.client.common.SuspendableHTTPClient
import ai.grazie.client.ktor.GrazieKtorHTTPClient
import ai.grazie.model.auth.v5.AuthData
import ai.grazie.model.cloud.AuthType
import ai.grazie.model.cloud.exceptions.HTTPStatusException
import ai.grazie.model.llm.chat.LLMChat
import ai.grazie.model.llm.chat.LLMChatMessage
import ai.grazie.model.llm.chat.LLMChatRole
import ai.grazie.model.llm.profile.OpenAIProfileIDs
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import kotlinx.coroutines.runBlocking
import org.jetbrains.research.testgenie.TestGenieBundle
import org.jetbrains.research.testgenie.tools.llm.SettingsArguments
import org.jetbrains.research.testgenie.tools.llm.error.LLMErrorManager
import org.jetbrains.research.testgenie.tools.llm.test.TestSuiteGeneratedByLLM

/**
 * This class represents a manager for making requests to the LLM (Live Learning Model).
 */
class LLMRequestManager {
    private val url = "https://api.app.stgn.grazie.aws.intellij.net"
    private val grazieToken = SettingsArguments.grazieUserToken()

    private val log: Logger = Logger.getInstance(this.javaClass)

    // Prepare Authentication Data
    private val authData = AuthData(
        token = grazieToken,
        originalUserToken = grazieToken,
        originalServiceToken = null,
        grazieAgent = null,
    )

    // Initiate the client
    private val client = SuspendableAPIGatewayClient(
        serverUrl = url,
        authType = AuthType.User,
        httpClient = SuspendableHTTPClient.WithV5(GrazieKtorHTTPClient.Default, authData),
    )

    private val chatHistory = mutableListOf<LLMChatMessage>()

    /**
     * Sends a request to LLM with the given prompt and returns the generated TestSuite.
     *
     * @param prompt the prompt to send to LLM
     * @param indicator the progress indicator to show progress during the request
     * @param packageName the name of the package for the generated TestSuite
     * @param project the project associated with the request
     * @param llmErrorManager the error manager to handle errors during the request
     * @return the generated TestSuite, or null if the response is empty or blank
     */
    fun request(prompt: String, indicator: ProgressIndicator, packageName: String, project: Project, llmErrorManager: LLMErrorManager): TestSuiteGeneratedByLLM? {
        // Prepare the chat
        val llmChat = buildChat(prompt)

        // Prepare the test assembler
        val testsAssembler = TestsAssembler(project, indicator)

        // Send Request to LLM
        log.info("Sending Request ...")
        runBlocking {
            try {
                client.llm().chat(llmChat, OpenAIProfileIDs.GPT4).collect { it: String ->
                    testsAssembler.receiveResponse(it)
                }
            } catch (e: HTTPStatusException) {
                when (e.status) {
                    401 -> llmErrorManager.errorProcess(TestGenieBundle.message("incorrectToken"), project)
                    500 -> llmErrorManager.errorProcess(TestGenieBundle.message("serverProblems"), project)
                    400 -> llmErrorManager.errorProcess(TestGenieBundle.message("tooLongPrompt"), project)
                    else -> llmErrorManager.errorProcess(llmErrorManager.createRequestErrorMessage(e.status), project)
                }
                null
            }
        }
        // save the full response in the chat history
        val response = testsAssembler.rawText
        log.debug("The full response: \n $response")
        chatHistory.add(LLMChatMessage(LLMChatRole.Assistant, response))

        // check if response is empty
        if (response.isEmpty() || response.isBlank()) return null

        return testsAssembler.returnTestSuite(packageName).reformat()
    }

    /**
     * Builds a new LLMChat instance using the given prompt.
     * Adds the prompt to the chat history and then constructs the LLMChat object with the chat history.
     *
     * @param prompt The prompt for the user.
     * @return The newly created LLMChat object.
     */
    private fun buildChat(prompt: String): LLMChat {
        // add new prompt to chat history
        chatHistory.add(LLMChatMessage(LLMChatRole.User, prompt))
        // build and return LLMChat
        return LLMChat.build {
            chatHistory.forEach {
                message(it.role, it.text)
            }
        }
    }
}
