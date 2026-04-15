package com.example.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.runtime.collectAsState
import com.example.todo.data.Todo
import com.example.todo.data.TodoDatabase
import com.example.todo.data.TodoRepository
import com.example.todo.viewmodel.TodoViewModel
import com.example.todo.viewmodel.TodoViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: TodoViewModel by viewModels {
        val database = TodoDatabase.getInstance(this)
        val repository = TodoRepository(database.todoDao())
        TodoViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodoTheme {
                val todos by viewModel.todos.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TodoScreen(
                        todos = todos,
                        onAddTodo = {text -> viewModel.addTodos(text) },
                        onToggleTodo = { todo -> viewModel.toggleTodo(todo) },
                        onDeleteTodo = { todo -> viewModel.deleteTodo(todo) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun TodoScreen(
    todos: List<Todo>,
    onAddTodo: (String) -> Unit,
    onToggleTodo: (Todo) -> Unit,
    onDeleteTodo: (Todo) -> Unit,
    modifier: Modifier = Modifier
) {
    // Only local UI state remains here — the text the user is currently typing
    var inputText by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top 25%: input area
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
                        onAddTodo(trimmed)   // → ViewModel → Room → StateFlow → recompose
                        inputText = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Todo")
            }
        }

        HorizontalDivider()

        // Bottom 75%: todo list driven by Room via StateFlow
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
                        onCheckedChange = { onToggleTodo(todo) }
                    )

                    Text(
                        text = todo.text,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 12.dp),
                        textDecoration = if (todo.isDone) TextDecoration.LineThrough
                        else TextDecoration.None
                    )

                    IconButton(onClick = { onDeleteTodo(todo) }) {
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
        TodoScreen(
            todos = listOf(
                Todo(id = 1, text = "Buy groceries", isDone = false),
                Todo(id = 2, text = "Walk the dog", isDone = true)
            ),
            onAddTodo = {},
            onToggleTodo = {},
            onDeleteTodo = {}
        )
    }
}