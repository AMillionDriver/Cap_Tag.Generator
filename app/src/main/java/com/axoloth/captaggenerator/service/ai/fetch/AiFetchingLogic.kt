package com.axoloth.captaggenerator.service.ai.fetch

import android.util.Base64
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class AiProviderResult<T>(
    val value: T,
    val providerName: String
)

class AiUnavailableException(
    message: String = AiFetchingLogic.USER_FACING_UNAVAILABLE_MESSAGE,
    cause: Throwable? = null
) : Exception(message, cause)

object AiFetchingLogic {
    const val USER_FACING_UNAVAILABLE_MESSAGE =
        "Maaf, saat ini AI sedang banyak permintaan. Silakan coba lagi beberapa saat lagi."

    private const val TAG = "AiFetchingLogic"
    private const val AI_PROXY_URL_KEY = "AI_PROXY_URL"
    private const val FETCH_INTERVAL_SECONDS = 3600L
    private const val REQUEST_TIMEOUT_MS = 30_000L

    private val providerOrder = listOf(
        AiProvider(
            providerName = "Gemini",
            remoteConfigKey = "Gemini_AI",
            modelRemoteConfigKey = "Gemini_Model",
            defaultModel = "gemini-2.5-flash-lite",
            endpoint = null,
            kind = AiProviderKind.GEMINI
        ),
        AiProvider(
            providerName = "DeepSeek",
            remoteConfigKey = "Deepseek_AI",
            modelRemoteConfigKey = "Deepseek_Model",
            defaultModel = "deepseek-chat",
            endpoint = "https://api.deepseek.com/chat/completions",
            kind = AiProviderKind.OPENAI_COMPATIBLE
        ),
        AiProvider(
            providerName = "GPT",
            remoteConfigKey = "GPT_AI",
            modelRemoteConfigKey = "GPT_Model",
            defaultModel = "gpt-4o-mini",
            endpoint = "https://api.openai.com/v1/chat/completions",
            kind = AiProviderKind.OPENAI_COMPATIBLE
        ),
        AiProvider(
            providerName = "Groq",
            remoteConfigKey = "Groq_AI",
            modelRemoteConfigKey = "Groq_Model",
            defaultModel = "llama-3.1-8b-instant",
            endpoint = "https://api.groq.com/openai/v1/chat/completions",
            kind = AiProviderKind.OPENAI_COMPATIBLE
        )
    )

    /**
     * Preferred path untuk produksi: isi Remote Config `AI_PROXY_URL` dengan endpoint backend
     * yang menyimpan API key di server. Kalau belum ada proxy, app masih bisa memakai
     * API key dari Remote Config sebagai mode transisi, tetapi key client-side tetap bisa diekstrak.
     */
    suspend fun <T> generateWithFallback(
        prompt: String,
        expectJson: Boolean = false,
        transform: (String) -> T?
    ): AiProviderResult<T> {
        val remoteConfig = getRemoteConfig()
        val proxyUrl = remoteConfig.getString(AI_PROXY_URL_KEY).trim()
        val failures = mutableListOf<String>()

        if (proxyUrl.isNotBlank()) {
            runCatching {
                callProxy(proxyUrl, prompt, expectJson)
            }.onSuccess { rawText ->
                transform(rawText)?.let { parsed ->
                    return AiProviderResult(parsed, "AI Proxy")
                }
                failures += "AI Proxy: invalid response"
            }.onFailure { error ->
                failures += "AI Proxy: ${safeReason(error)}"
            }
        }

        for (provider in providerOrder) {
            val apiKey = remoteConfig.getDecodedSecret(provider.remoteConfigKey)
            if (apiKey.isNullOrBlank()) {
                failures += "${provider.providerName}: API key kosong"
                continue
            }

            val modelName = remoteConfig.getString(provider.modelRemoteConfigKey)
                .trim()
                .ifBlank { provider.defaultModel }

            runCatching {
                when (provider.kind) {
                    AiProviderKind.GEMINI -> callGemini(apiKey, modelName, prompt, expectJson)
                    AiProviderKind.OPENAI_COMPATIBLE -> callOpenAiCompatible(
                        endpoint = requireNotNull(provider.endpoint),
                        apiKey = apiKey,
                        modelName = modelName,
                        prompt = prompt
                    )
                }
            }.onSuccess { rawText ->
                if (isUnavailableResponse(rawText)) {
                    failures += "${provider.providerName}: provider overload/blank"
                    return@onSuccess
                }

                transform(rawText)?.let { parsed ->
                    return AiProviderResult(parsed, provider.providerName)
                }
                failures += "${provider.providerName}: invalid response"
            }.onFailure { error ->
                failures += "${provider.providerName}: ${safeReason(error)}"
            }
        }

        Log.w(TAG, "All AI providers failed: ${failures.joinToString(" | ")}")
        throw AiUnavailableException()
    }

    suspend fun generateTextWithFallback(prompt: String): AiProviderResult<String> {
        return generateWithFallback(prompt = prompt) { rawText ->
            rawText.takeIf { !isUnavailableResponse(it) }
        }
    }

    internal fun isUnavailableResponse(text: String): Boolean {
        return text.isBlank() || looksLikeCapacityMessage(text)
    }

    private suspend fun getRemoteConfig(): FirebaseRemoteConfig {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(FETCH_INTERVAL_SECONDS)
            .build()

        runCatching { remoteConfig.setConfigSettingsAsync(configSettings).await() }
        runCatching { remoteConfig.fetchAndActivate().await() }
            .onFailure { Log.w(TAG, "Remote Config fetch failed, using activated/default values: ${it.message}") }

        return remoteConfig
    }

    private suspend fun callProxy(
        proxyUrl: String,
        prompt: String,
        expectJson: Boolean
    ): String = postJson(
        endpoint = proxyUrl,
        headers = emptyMap(),
        payload = JSONObject()
            .put("prompt", prompt)
            .put("responseMimeType", if (expectJson) "application/json" else "text/plain")
    ).extractProxyText()

    private suspend fun callGemini(
        apiKey: String,
        modelName: String,
        prompt: String,
        expectJson: Boolean
    ): String = withTimeout(REQUEST_TIMEOUT_MS) {
        val generativeModel = if (expectJson) {
            GenerativeModel(
                modelName = modelName,
                apiKey = apiKey,
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                }
            )
        } else {
            GenerativeModel(
                modelName = modelName,
                apiKey = apiKey
            )
        }

        generativeModel.generateContent(prompt).text.orEmpty()
    }

    private suspend fun callOpenAiCompatible(
        endpoint: String,
        apiKey: String,
        modelName: String,
        prompt: String
    ): String {
        val payload = JSONObject()
            .put("model", modelName)
            .put(
                "messages",
                JSONArray().put(
                    JSONObject()
                        .put("role", "user")
                        .put("content", prompt)
                )
            )
            .put("temperature", 0.7)

        val response = postJson(
            endpoint = endpoint,
            headers = mapOf("Authorization" to "Bearer $apiKey"),
            payload = payload
        )

        return response
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }

    private suspend fun postJson(
        endpoint: String,
        headers: Map<String, String>,
        payload: JSONObject
    ): JSONObject = withContext(Dispatchers.IO) {
        withTimeout(REQUEST_TIMEOUT_MS) {
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = REQUEST_TIMEOUT_MS.toInt()
                readTimeout = REQUEST_TIMEOUT_MS.toInt()
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                headers.forEach { (key, value) -> setRequestProperty(key, value) }
            }

            try {
                connection.outputStream.use { output ->
                    output.write(payload.toString().toByteArray(Charsets.UTF_8))
                }

                val responseCode = connection.responseCode
                val stream = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream ?: connection.inputStream
                }
                val responseText = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }

                if (responseCode !in 200..299) {
                    throw AiHttpException(responseCode, responseText)
                }

                JSONObject(responseText)
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun JSONObject.extractProxyText(): String {
        return optString("text")
            .ifBlank { optString("content") }
            .ifBlank { optString("response") }
            .ifBlank { optString("result") }
    }

    private fun FirebaseRemoteConfig.getDecodedSecret(key: String): String? {
        val rawValue = getString(key).trim()
        if (rawValue.isBlank()) return null

        val decoded = runCatching {
            String(Base64.decode(rawValue, Base64.DEFAULT), Charsets.UTF_8).trim()
        }.getOrNull()

        return decoded
            ?.takeIf { it.looksLikePrintableSecret() }
            ?: rawValue
    }

    private fun String.looksLikePrintableSecret(): Boolean {
        return length >= 16 && all { it.code in 33..126 }
    }

    private fun looksLikeCapacityMessage(text: String): Boolean {
        val lower = text.lowercase()
        return listOf(
            "high demand",
            "overloaded",
            "over capacity",
            "too many requests",
            "rate limit",
            "resource exhausted",
            "quota exceeded",
            "try again later",
            "temporarily unavailable"
        ).any { it in lower }
    }

    private fun safeReason(error: Throwable): String {
        return when (error) {
            is AiHttpException -> "HTTP ${error.code}"
            is TimeoutCancellationException -> "timeout"
            is IOException -> "network"
            else -> error.message ?: error::class.java.simpleName
        }
    }

    private data class AiProvider(
        val providerName: String,
        val remoteConfigKey: String,
        val modelRemoteConfigKey: String,
        val defaultModel: String,
        val endpoint: String?,
        val kind: AiProviderKind
    )

    private enum class AiProviderKind {
        GEMINI,
        OPENAI_COMPATIBLE
    }

    private class AiHttpException(
        val code: Int,
        responseBody: String
    ) : IOException(responseBody.take(500))
}
