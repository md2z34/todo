package com.example.todo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.todo.data.Todo
import com.example.todo.data.TodoRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoViewModel(private val repository: TodoRepository): ViewModel() {
    val todos: StateFlow<List<Todo>> = repository.allTodos.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.Lazily,
        initialValue = emptyList()
    )

    fun addTodos(text: String) {
        viewModelScope.launch {
            val todo = Todo(text = text, isDone = false)
            repository.insertTodo(todo)
        }
    }

    fun toggleTodo(todo: Todo) {
        viewModelScope.launch {
            repository.updateTodo(todo.copy(isDone = !todo.isDone))
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            repository.deleteTodo(todo)
        }
    }
}

class TodoViewModelFactory(private val repository: TodoRepository): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TodoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}