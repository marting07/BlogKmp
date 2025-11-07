package org.example.blogkmp.models

import kotlinx.serialization.Serializable

@Serializable
data class NewPost(
    val title: String,
    val body: String,
    val author: String
)