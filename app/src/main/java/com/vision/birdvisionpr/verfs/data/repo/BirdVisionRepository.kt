package com.vision.birdvisionpr.verfs.data.repo

import android.util.Log
import com.vision.birdvisionpr.verfs.domain.model.BirdVisionEntity
import com.vision.birdvisionpr.verfs.domain.model.BirdVisionParam
import com.vision.birdvisionpr.verfs.presentation.app.BirdVisionApplication.Companion.BIRD_VISION_MAIN_TAG
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.plugin
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer



private const val BIRD_VISION_MAIN = "https://birdvisiion.com/config.php"

class BirdVisionRepository {


    private val birdVisionKtorClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 30000
            socketTimeoutMillis = 30000
            requestTimeoutMillis = 30000
        }

    }

    suspend fun birdVisionGetClient(
        birdVisionParam: BirdVisionParam,
        birdVisionConversion: MutableMap<String, Any>?
    ): BirdVisionEntity? =
        withContext(Dispatchers.IO) {
            birdVisionKtorClient.plugin(HttpSend).intercept { request ->
                Log.d(BIRD_VISION_MAIN_TAG, "Ktor: Intercept body ${request.body}")
                execute(request)
            }
            val birdVisionJson = Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            }
            Log.d(
                BIRD_VISION_MAIN_TAG,
                "Ktor: conversation json: ${birdVisionConversion.toString()}"
            )
            val birdVisionBody = birdVisionMergeToFlatJson(
                json = birdVisionJson,
                param = birdVisionParam,
                conversation = birdVisionConversion
            )
            Log.d(
                BIRD_VISION_MAIN_TAG,
                "Ktor: request json: $birdVisionBody"
            )
            return@withContext try {
                val response = birdVisionKtorClient.post(BIRD_VISION_MAIN) {
                    contentType(ContentType.Application.Json) // обязательно JSON
                    accept(ContentType.Application.Json)
                    setBody(birdVisionBody) // JsonObject
                }
                val code = response.status.value
                Log.d(BIRD_VISION_MAIN_TAG, "Ktor: Request status code: $code")
                if (code == 200) {
                    val rawBody = response.bodyAsText() // читаем ответ как текст
                    val birdVisionEntity = Json { ignoreUnknownKeys = true }
                        .decodeFromString(BirdVisionEntity.serializer(), rawBody)
                    Log.d(BIRD_VISION_MAIN_TAG, "Ktor: Get request success")
                    Log.d(BIRD_VISION_MAIN_TAG, "Ktor: $birdVisionEntity")
                    birdVisionEntity
                } else {
                    Log.d(BIRD_VISION_MAIN_TAG, "Ktor: Status code invalid, return null")
                    Log.d(BIRD_VISION_MAIN_TAG, "Ktor: ${response.body<String>()}")
                    null
                }

            } catch (e: Exception) {
                Log.d(BIRD_VISION_MAIN_TAG, "Ktor: Get request failed")
                Log.d(BIRD_VISION_MAIN_TAG, "Ktor: ${e.message}")
                null
            }
        }

    private inline fun <reified T> Json.birdVisionEncodeToJsonObject(value: T): JsonObject =
        encodeToJsonElement(serializer(), value).jsonObject

    private inline fun <reified T> birdVisionMergeToFlatJson(
        json: Json,
        param: T,
        conversation: Map<String, Any>?
    ): JsonObject {

        val paramJson = json.birdVisionEncodeToJsonObject(param)

        return buildJsonObject {
            // поля из param
            paramJson.forEach { (key, value) ->
                put(key, value)
            }

            // динамические поля
            conversation?.forEach { (key, value) ->
                put(key, birdVisionAnyToJsonElement(value))
            }
        }
    }

    private fun birdVisionAnyToJsonElement(value: Any?): JsonElement {
        return when (value) {
            null -> JsonNull
            is String -> JsonPrimitive(value)
            is Number -> JsonPrimitive(value)
            is Boolean -> JsonPrimitive(value)
            is Map<*, *> -> buildJsonObject {
                value.forEach { (k, v) ->
                    if (k is String) {
                        put(k, birdVisionAnyToJsonElement(v))
                    }
                }
            }
            is List<*> -> buildJsonArray {
                value.forEach {
                    add(birdVisionAnyToJsonElement(it))
                }
            }
            else -> JsonPrimitive(value.toString())
        }
    }


}
