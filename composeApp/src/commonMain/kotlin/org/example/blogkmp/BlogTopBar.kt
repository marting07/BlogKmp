package org.example.blogkmp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogTopBar(onRefresh: () -> Unit, enabled: Boolean) {
    CenterAlignedTopAppBar(
        title = { Text("Simple Blog") },
        actions = {
            IconButton(onClick = onRefresh, enabled = enabled) {
                Icon(Icons.Default.Refresh, contentDescription = "Reload")
            }
        }
    )
}