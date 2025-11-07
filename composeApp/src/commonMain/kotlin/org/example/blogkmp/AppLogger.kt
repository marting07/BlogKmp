package org.example.blogkmp

import io.ktor.client.plugins.logging.Logger

object AppLogger : Logger {
    override fun log(message: String) {
        println("[HTTP] $message")
    }
}