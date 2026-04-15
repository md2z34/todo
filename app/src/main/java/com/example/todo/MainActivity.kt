package com.example.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.todo.ui.theme.TodoTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete


data class TodoUiItem(
    val id: Long,
    val text: String,
    val isDone: Boolean
    )

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TodoScreen(modifier = Modifier.padding(innerPadding))                }
            }
        }
    }
}

@Composable
fun TodoScreen(modifier: Modifier = Modifier) {
    val todos = remember { mutableStateListOf<TodoUiItem>() }
    var inputText by rememberSaveable { mutableStateOf("") }
    var nextId by rememberSaveable { mutableStateOf(0L) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top 25%: new todo input area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.25f),
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("New todo") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val trimmed = inputText.trim()
                    if (trimmed.isNotEmpty()) {
                        todos.add(
                            TodoUiItem(
                                id = nextId,
                                text = trimmed,
                                isDone = false
                            )
                        )
                        nextId += 1
                        inputText = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Todo")
            }
        }

        HorizontalDivider()

        // Bottom 75%: todo list
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.75f)
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (todos.isEmpty()) {
                item {
                    Text(
                        text = "No todos yet. Add one above.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(todos, key = { it.id }) { todo ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Checkbox(
                        checked = todo.isDone,
                        onCheckedChange = { checked ->
                            val index = todos.indexOfFirst { it.id == todo.id }
                            if (index != -1) {
                                todos[index] = todo.copy(isDone = checked)
                            }
                        }
                    )

                    Text(
                        text = todo.text,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 12.dp),
                        textDecoration = if (todo.isDone) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        }
                    )

                    IconButton(
                        onClick = { todos.removeAll { it.id == todo.id } }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete todo",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TodoScreenPreview() {
    TodoTheme {
        TodoScreen()
    }
}