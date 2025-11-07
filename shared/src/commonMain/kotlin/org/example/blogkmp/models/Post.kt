package org.example.blogkmp.models

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: Int,
    val title: String,
    val body: String,
    val author: String,
    val createdAtEpochMs: Long
)