package org.example.blogkmp

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.example.blogkmp.dtos.ErrorDto

fun makeHttpClient(appLogger: Logger): HttpClient = HttpClient {
    expectSuccess = false

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
        )
    }
    install(Logging) {
        // Choose one logger implementation:
        // Logger.DEFAULT, Logger.SIMPLE, or provide a custom logger:
        logger = Logger.DEFAULT
        level = LogLevel.INFO
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
