package org.example.blogkmp

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
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
