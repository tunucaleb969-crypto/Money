package com.kwame.money

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * CONFIRMED against Google's official Gemini API docs (ai.google.dev/api):
 * endpoint shape, x-goog-api-key header, and generateContent request/response
 * structure. Model chosen: gemini-3.1-flash-lite — GA (not preview), has a
 * free tier, and is priced for cheap/fast tasks like keyboard actions rather
 * than a Pro-tier model, which would be overkill and far more expensive for
 * grammar/rewrite/translate/reply-style work.
 */
class GeminiProvider(private val apiKey: String) : AiProvider {

    private val model = "gemini-3.1-flash-lite"
    private val endpoint =
        "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    override suspend fun generate(systemPrompt: String, userText: String): Result<String> =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("No Gemini API key set. Add one in Money Keyboard settings.")
                )
            }
            try {
                val requestJson = JSONObject().apply {
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
                    })
                    put("contents", JSONArray().put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().put(JSONObject().put("text", userText)))
                    }))
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.4)
                        put("maxOutputTokens", 512)
                    })
                }

                val body = requestJson.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(endpoint)
                    .addHeader("x-goog-api-key", apiKey)
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    if (!response.isSuccessful || responseBody == null) {
                        return@withContext Result.failure(
                            IOException("Gemini API error: HTTP ${response.code} ${response.message}")
                        )
                    }
                    val text = JSONObject(responseBody)
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                    Result.success(text.trim())
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
