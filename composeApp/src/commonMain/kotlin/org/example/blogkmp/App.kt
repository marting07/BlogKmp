package org.example.blogkmp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.blogkmp.models.NewPost
import org.example.blogkmp.models.Post
import org.example.blogkmp.network.BlogApi
import org.example.blogkmp.network.fold
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        BlogScreen()
    }
}

@Composable
private fun BlogScreen() {
    val api = remember { BlogApi() }
    val scope = rememberCoroutineScope()

    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("You") }

    fun refresh() {
        scope.launch {
            loading = true; error = null
            api.listPosts().fold(
                ok = { list -> posts = list },
                err = { e -> error = e.message }
            )
            loading = false
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Simple Blog", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))

        if (error != null) {
            Text("Error: $error", color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Spacer(Modifier.height(12.dp))

        // New post form
        OutlinedTextField(
            value = title, onValueChange = { title = it },
            label = { Text("Title") }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = body, onValueChange = { body = it },
            label = { Text("Body") }, modifier = Modifier.fillMaxWidth(), minLines = 3
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = author, onValueChange = { author = it },
            label = { Text("Author") }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                scope.launch {
                    loading = true; error = null
                    api.createPost(NewPost(title.trim(), body.trim(), author.trim()))
                        .fold(
                            ok = {
                                title = ""; body = ""
                                refresh()
                            },
                            err = { e -> error = e.message }
                        )
                    loading = false
                }
            },
            enabled = title.isNotBlank() && body.isNotBlank()
        ) {
            Text("Publish")
        }

        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(posts) { p ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(p.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(p.body, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(6.dp))
                        Text("— ${p.author}", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
