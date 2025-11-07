package org.example.blogkmp.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.put
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import org.example.blogkmp.AppLogger
import org.example.blogkmp.BASE_URL
import org.example.blogkmp.models.NewPost
import org.example.blogkmp.models.Post

class BlogApi(
    baseUrl: String = BASE_URL,
    private val client: HttpClient = makeHttpClient(AppLogger)
) {
    private val postsUrl = "$baseUrl/posts"

    suspend fun listPosts(): ApiResult<List<Post>> =
        client.safe { get(postsUrl).body() }

    suspend fun getPost(id: Int): ApiResult<Post> =
        client.safe { get("$postsUrl/$id").body() }

    suspend fun createPost(newPost: NewPost): ApiResult<Post> =
        client.safe {
            post(postsUrl) {
                contentType(ContentType.Application.Json)
                setBody(newPost)
            }.body()
        }

    suspend fun updatePost(id: Int, post: NewPost): ApiResult<Post> =
        client.safe {
            put("$postsUrl/$id") {
                contentType(ContentType.Application.Json)
                setBody(post)
            }.body()
        }

    suspend fun deletePost(id: Int): ApiResult<Boolean> =
        client.safe {
            delete("$postsUrl/$id").status.isSuccess()
        }
}