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

    // UI state
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Create form state
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("You") }

    // Edit/Delete dialogs
    var editing by remember { mutableStateOf<Post?>(null) }
    var deleting by remember { mutableStateOf<Post?>(null) }
    var editTitle by remember { mutableStateOf("") }
    var editBody by remember { mutableStateOf("") }
    var editAuthor by remember { mutableStateOf("") }

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

    fun openEditDialog(p: Post) {
        editing = p
        editTitle = p.title
        editBody = p.body
        editAuthor = p.author
    }

    LaunchedEffect(Unit) { refresh() }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            BlogTopBar(onRefresh = { refresh() }, enabled = !loading)
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (error != null) {
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { refresh() }) { Text("Retry") }
                Spacer(Modifier.height(12.dp))
            }

            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            }

            // New post form
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Body") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = author,
                onValueChange = { author = it },
                label = { Text("Author") },
                modifier = Modifier.fillMaxWidth()
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
                enabled = !loading && title.isNotBlank() && body.isNotBlank()
            ) {
                Text("Publish")
            }

            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(8.dp))

            // Posts list with actions
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 0.dp,
                    start = 0.dp, end = 0.dp,
                    bottom = 1.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(posts, key = { it.id }) { p ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(p.title, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(p.body, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(6.dp))
                            Text("— ${p.author}", style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(onClick = { openEditDialog(p) }) { Text("Edit") }
                                OutlinedButton(
                                    onClick = { deleting = p },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) { Text("Delete") }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit dialog
    if (editing != null) {
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("Edit Post #${editing!!.id}") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editTitle, onValueChange = { editTitle = it },
                        label = { Text("Title") }, modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editBody, onValueChange = { editBody = it },
                        label = { Text("Body") }, modifier = Modifier.fillMaxWidth(), minLines = 3
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editAuthor, onValueChange = { editAuthor = it },
                        label = { Text("Author") }, modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            loading = true; error = null
                            api.updatePost(
                                id = editing!!.id,
                                post = NewPost(editTitle.trim(), editBody.trim(), editAuthor.trim())
                            ).fold(
                                ok = {
                                    editing = null
                                    refresh()
                                },
                                err = { e -> error = e.message }
                            )
                            loading = false
                        }
                    },
                    enabled = editTitle.isNotBlank() && editBody.isNotBlank()
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editing = null }) { Text("Cancel") }
            }
        )
    }

    // Delete confirm dialog
    if (deleting != null) {
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete Post #${deleting!!.id}?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            loading = true; error = null
                            api.deletePost(deleting!!.id).fold(
                                ok = {
                                    deleting = null
                                    refresh()
                                },
                                err = { e -> error = e.message }
                            )
                            loading = false
                        }
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { Text("Cancel") }
            }
        )
    }
}
