package org.example.blogkmp

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.statuspages.*
import org.example.blogkmp.dtos.ErrorDto
import org.example.blogkmp.models.NewPost
import org.example.blogkmp.models.Post

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = SERVER_HOST_URL, module = Application::module)
        .start(wait = true)
}

private object InMemory {
    private val idSeq = AtomicInteger(0)
    private val posts = ConcurrentHashMap<Int, Post>()

    init {
        // Seed a couple of posts
        add(NewPost("Welcome", "This is our first post!", "Admin"))
        add(NewPost("Compose + Ktor", "All shared UI talking to Ktor in-memory store.", "You"))
    }

    fun all(): List<Post> = posts.values.sortedBy { it.id }

    fun get(id: Int): Post? = posts[id]

    fun add(newPost: NewPost): Post {
        val id = idSeq.incrementAndGet()
        val post = Post(
            id = id,
            title = newPost.title,
            body = newPost.body,
            author = newPost.author,
            createdAtEpochMs = System.currentTimeMillis()
        )
        posts[id] = post
        return post
    }

    fun update(id: Int, updated: NewPost): Post? {
        val existing = posts[id] ?: return null
        val post = existing.copy(
            title = updated.title,
            body = updated.body,
            author = updated.author
        )
        posts[id] = post
        return post
    }

    fun delete(id: Int): Boolean = posts.remove(id) != null
}

fun Application.module() {
    install(CallLogging)
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
    }
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            // log full stacktrace to stdout
            cause.printStackTrace()
            // return a small JSON error (so client doesn't try to decode as List<Post>)
            call.respond(HttpStatusCode.InternalServerError, ErrorDto(cause.message ?: "Server error"))
        }
    }

    routing {
        get("/") {
            call.respondText("Ktor Blog API up on port $SERVER_PORT")
        }

        route("/posts") {
            get {
                call.respond(InMemory.all())
            }
            post {
                val payload = call.receive<NewPost>()
                call.respond(HttpStatusCode.Created, InMemory.add(payload))
            }
            get("{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest)
                val found = InMemory.get(id) ?: return@get call.respond(HttpStatusCode.NotFound)
                call.respond(found)
            }
            put("{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest)
                val payload = call.receive<NewPost>()
                val updated = InMemory.update(id, payload)
                    ?: return@put call.respond(HttpStatusCode.NotFound)
                call.respond(updated)
            }
            delete("{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest)
                if (InMemory.delete(id)) call.respond(HttpStatusCode.NoContent)
                else call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
