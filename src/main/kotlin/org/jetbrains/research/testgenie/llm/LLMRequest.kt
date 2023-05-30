package org.jetbrains.research.testgenie.llm


class LLMRequest {
    val url = "https://all.egn.llm.stgn.grazie.aws.intellij.net"
//    private val grazieToken = TODO(Read comments in the request())



    fun request(prompt: String) {

        // TODO: We need to send request via Grazie PlayGround: https://play.stgn.grazie.ai/chat.
        //  The temporary token for testing is available in the playground page.
        //  For sending request we should either use
        //  1) Grazie API (https://jetbrains.team/p/grazi/packages/maven/grazie-platform-public/ai.grazie.api/api-gateway-api-jvm?v=0.2.162&tab=overview) and in general grazie platform code (public maven packages: https://jetbrains.team/p/grazi/packages/maven/grazie-platform-public; private packages: https://jetbrains.team/p/grazi/packages/maven/grazie-platform )
        //  2) check how they did it in LLM_for_code project
    }

}