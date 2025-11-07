package org.example.blogkmp

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.example.blogkmp.models.NewPost
import org.example.blogkmp.models.Post

class BlogApi(
    baseUrl: String = BASE_URL,
    private val client: HttpClient = defaultClient()
) {
    private val postsUrl = "$baseUrl/posts"

    suspend fun listPosts(): List<Post> =
        client.get(postsUrl).body()

    suspend fun getPost(id: Int): Post =
        client.get("$postsUrl/$id").body()

    suspend fun createPost(newPost: NewPost): Post =
        client.post(postsUrl) {
            contentType(ContentType.Application.Json)
            setBody(newPost)
        }.body()

    suspend fun updatePost(id: Int, post: NewPost): Post =
        client.put("$postsUrl/$id") {
            contentType(ContentType.Application.Json)
            setBody(post)
        }.body()

    suspend fun deletePost(id: Int): Boolean =
        client.delete("$postsUrl/$id").status.isSuccess()

    companion object {
        fun defaultClient() = HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = false
                    isLenient = true
                })
            }
        }
    }
}
