package org.jetbrains.research.testgenie.tools.llm.generation

import ai.grazie.api.gateway.client.SuspendableAPIGatewayClient
import ai.grazie.client.common.SuspendableHTTPClient
import ai.grazie.client.ktor.GrazieKtorHTTPClient
import ai.grazie.model.auth.v5.AuthData
import ai.grazie.model.cloud.AuthType
import ai.grazie.model.llm.chat.LLMChat
import ai.grazie.model.llm.chat.LLMChatMessage
import ai.grazie.model.llm.chat.LLMChatRole
import ai.grazie.model.llm.profile.OpenAIProfileIDs
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.jetbrains.research.testgenie.tools.llm.SettingsArguments
import org.jetbrains.research.testgenie.tools.llm.test.TestSuiteGeneratedByLLM

class LLMRequestManager {
    private val url = "https://api.app.stgn.grazie.aws.intellij.net"
    private val grazieToken = SettingsArguments.grazieUserToken()

    private val messagesHistory: MutableList<LLMChatMessage> = mutableListOf()

    private val logger: Logger = Logger.getInstance(this.javaClass)

    fun request(prompt: String, indicator: ProgressIndicator, packageName: String): TestSuiteGeneratedByLLM {
        // Prepare Authentication Data
        val authData = AuthData(
            token = grazieToken,
            originalUserToken = grazieToken,
            originalServiceToken = null,
            grazieAgent = null,
        )

        // Initiate the client
        val client = SuspendableAPIGatewayClient(
            serverUrl = url,
            authType = AuthType.User,
            httpClient = SuspendableHTTPClient.WithV5(GrazieKtorHTTPClient.Default, authData),
        )

        // Prepare the chat
        val llmChat = generateChat(prompt)

        // Prepare the test assembler
        val testsAssembler = TestsAssembler(indicator = indicator)

        // Send Request to LLM
        logger.info("Sending Request ...")
        runBlocking {
            client.llm().chat(llmChat, OpenAIProfileIDs.GPT4).collect { it: String ->
                testsAssembler.receiveResponse(it)
            }
        }

        messagesHistory.add(LLMChatMessage(LLMChatRole.Assistant,testsAssembler.getLLMResponse()))

        return TestsAssembler.returnTestSuite(packageName)
    }

    private fun generateChat(prompt: String): LLMChat {
        // add the new prompt to mutableMessages
        messagesHistory.add(LLMChatMessage(LLMChatRole.User,prompt))

        return LLMChat.build {
            messagesHistory.forEach {message ->
                message(message.role,message.text)
            }
        }

    }
}
