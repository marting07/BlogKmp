package org.example.blogkmp.network

import io.ktor.http.HttpStatusCode

sealed interface ApiResult<out T> {
    data class Ok<T>(val value: T) : ApiResult<T>
    data class Err(
        val status: HttpStatusCode? = null,
        val message: String = "Unknown error",
        val rawBody: String? = null
    ) : ApiResult<Nothing>
}

inline fun <T> ApiResult<T>.fold(
    ok: (T) -> Unit,
    err: (ApiResult.Err) -> Unit
) {
    when (this) {
        is ApiResult.Ok  -> ok(value)
        is ApiResult.Err -> err(this)
    }
}
