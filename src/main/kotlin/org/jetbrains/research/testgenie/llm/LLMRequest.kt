package org.jetbrains.research.testgenie.llm

import ai.grazie.api.gateway.client.SuspendableAPIGatewayClient
import ai.grazie.client.common.SuspendableHTTPClient
import ai.grazie.client.ktor.GrazieKtorHTTPClient
import ai.grazie.model.auth.v5.AuthData
import ai.grazie.model.cloud.AuthType
import ai.grazie.model.llm.chat.LLMChat
import ai.grazie.model.llm.profile.OpenAIProfileIDs
import com.intellij.openapi.diagnostic.Logger
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking


class LLMRequest {
    private val url = "https://api.app.stgn.grazie.aws.intellij.net"
    private val grazieToken = SettingsArguments.grazieUserToken()

    private val logger: Logger = Logger.getInstance(this.javaClass)

    fun request(prompt: String): String {
        // Prepare Authentication Data
        val authData = AuthData(
            token = grazieToken,
            originalUserToken = grazieToken,
            originalServiceToken = null,
            grazieAgent = null
        )

        // Initiate the client
        val client = SuspendableAPIGatewayClient(
            serverUrl = url,
            authType = AuthType.User,
            httpClient = SuspendableHTTPClient.WithV5(GrazieKtorHTTPClient.Default, authData)
        )

        // Prepare the chat
        val llmChat = LLMChat.Builder(prompt).build()

        // Send Request to LLM
        logger.info("Sending Request ...")
        val response = runBlocking {
            // ToDo we need to find a way to monitor the progress of test generation
            client.llm().chat(llmChat, OpenAIProfileIDs.GPT4).toList().joinToString("")
        }
        logger.info("The generated tests are: \n $response")

        return response
    }

}