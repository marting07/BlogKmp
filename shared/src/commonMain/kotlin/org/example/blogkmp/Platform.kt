package org.example.blogkmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform