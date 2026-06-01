package com.cleanwin.app.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DeepSeekApi {
    @POST("v1/chat/completions")
    suspend fun analyzeText(
        @Header("Authorization") authHeader: String,
        @Body request: DeepSeekRequest
    ): DeepSeekResponse
}

data class DeepSeekRequest(
    val model: String = "deepseek-v4-flash", // 更新为 deepseek-v4-flash 模型
    val messages: List<MessageRole>
)

data class MessageRole(val role: String, val content: String)
data class DeepSeekResponse(val choices: List<Choice>)
data class Choice(val message: MessageRole)

class DeepSeekRepository(private val apiKeyProvider: () -> String) {
    
    private val api: DeepSeekApi

    init {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.deepseek.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(DeepSeekApi::class.java)
    }

    suspend fun extractTaskFromNotification(title: String, content: String): String? {
        val prompt = """
            请从以下手机通知中提取出关键任务或待办事项。
            返回格式必须为 JSON: {"time": "时间", "task": "任务内容"}
            - 时间：有具体的写具体的（格式如"今天 15:00"、"5月31日"），如果没有具体时间，必须写"未知"。
            - 任务内容：简明扼要。注意：如果原始消息是问句，请在任务内容中保留问句形式，不要将其改为陈述句。
            如果没有任务，请返回 "无"。

            通知标题：$title
            通知内容：$content
        """.trimIndent()
        val request = DeepSeekRequest(
            messages = listOf(
                MessageRole("system", "你是一个高效的任务提取助手，只返回 JSON 或 '无'。"),
                MessageRole("user", prompt)
            )
        )

        return try {
            val response = api.analyzeText("Bearer ${apiKeyProvider()}", request)
            response.choices.firstOrNull()?.message?.content
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

