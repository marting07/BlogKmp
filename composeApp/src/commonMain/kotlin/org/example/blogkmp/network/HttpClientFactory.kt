package org.example.blogkmp.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.http.isSuccess
import org.example.blogkmp.dtos.ErrorDto

fun makeHttpClient(appLogger: Logger = Logger.DEFAULT): HttpClient = HttpClient {
    expectSuccess = false

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
    install(Logging) {
        // Choose one logger implementation:
        // Logger.DEFAULT, Logger.SIMPLE, or provide a custom logger:
        this.logger = Logger.Companion.DEFAULT
        this.level = LogLevel.INFO
        // Optional filters:
        // filter { request -> request.url.host.contains("your-host") }
        // sanitizeHeader { header -> header == HttpHeaders.Authorization }
    }

    HttpResponseValidator {
        handleResponseExceptionWithRequest { cause, request ->
            appLogger.log("HTTP exception on $request.method.value $request.url $cause.message $cause")
        }
        validateResponse { response ->
            if (!response.status.isSuccess()) {
                val raw = runCatching { response.bodyAsText() }.getOrNull()
                val msg = runCatching { Json.decodeFromString(ErrorDto.serializer(), raw ?: "") }
                    .getOrNull()
                    ?.message
                    ?: raw
                    ?: "HTTP ${response.status.value}"
                appLogger.log("Non-success HTTP response: status=$response.status.value, msg=$msg")
                throw ResponseException(response, msg)
            }
        }
    }
}

suspend inline fun <reified T> HttpClient.safe(
    crossinline block: suspend HttpClient.() -> T
): ApiResult<T> {
    return try {
        val value = block(this)
        ApiResult.Ok(value)
    } catch (e: Throwable) {
        val status = (e as? ResponseException)?.response?.status
        val raw = (e as? ResponseException)?.let { rex ->
            runCatching { rex.response.bodyAsText() }.getOrNull()
        }
        ApiResult.Err(status = status, message = e.message ?: "Network error", rawBody = raw)
    }
}