# Todo App (Jetpack Compose + Room)

A learning project for Android development using:
- **Jetpack Compose** for UI
- **Room** for local SQLite persistence
- **ViewModel + StateFlow** for UI state and lifecycle-safe logic

This README is based on the implementation and debugging process from the chat.

---

## What this app does

- Top 25% of screen:
  - Text field for new todo text
  - **Add Todo** button
- Bottom 75%:
  - List of todos from Room database
  - Checkbox to mark done/undone
  - Strike-through text when done
  - Delete icon to remove todo
- Data is persisted in Room (`todo_database`) and survives app restarts.

---

## Tech stack

- Kotlin
- Android Gradle Plugin (AGP) 9.1.0
- Jetpack Compose (BOM `2026.02.01`)
- Room `2.8.3`
- KSP `2.2.10-2.0.2`
- Lifecycle/ViewModel Compose `2.10.0`

Key dependency config:
- `app/build.gradle.kts`
- `gradle/libs.versions.toml`

---

## Project architecture (simple MVVM-ish)

### UI Layer
- `app/src/main/java/com/example/todo/MainActivity.kt`
  - `MainActivity`
  - `TodoScreen(...)` composable

### ViewModel Layer
- `app/src/main/java/com/example/todo/viewmodel/TodoViewModel.kt`
  - `TodoViewModel`
  - `TodoViewModelFactory`

### Data Layer
- `app/src/main/java/com/example/todo/data/Todo.kt`
  Room entity (`todos` table)
- `app/src/main/java/com/example/todo/data/TodoDao.kt`
  CRUD DAO methods (`Flow<List<Todo>>`, insert/update/delete)
- `app/src/main/java/com/example/todo/data/TodoDatabase.kt`
  Room database singleton (`todo_database`)
- `app/src/main/java/com/example/todo/data/TodoRepository.kt`
  Repository over DAO

Data flow:
1. UI triggers ViewModel actions (`add`, `toggle`, `delete`)
2. ViewModel launches coroutines (`viewModelScope.launch`)
3. Repository -> DAO -> Room DB
4. DAO emits updates via `Flow`
5. ViewModel exposes `StateFlow`
6. Compose collects state and recomposes automatically

---

## Important Kotlin/Compose concepts used

### `suspend fun`
DAO write methods are `suspend` so DB work is async/non-blocking and called from coroutines.

### `StateFlow` + `stateIn(...)`
`TodoViewModel` converts DAO `Flow` to `StateFlow`:

- `scope = viewModelScope` -> lifecycle-aware
- `started = SharingStarted.Lazily`
- `initialValue = emptyList()`

Compose uses `collectAsState()` to observe and recompose.

### Kotlin `by` delegation
```kotlin
private val viewModel: TodoViewModel by viewModels { ... }
```
`viewModels()` provides a delegate that creates/caches lifecycle-aware ViewModel instances.

---

## Build & run (Windows / PowerShell)

From project root:

```powershell
.\gradlew.bat --stop
.\gradlew.bat -version
.\gradlew.bat :app:assembleDebug
```

---

## Environment and toolchain notes from this project

This project hit two common setup issues and applied these fixes:

### 1) KSP + built-in Kotlin source set restriction
Error included:
- `Using kotlin.sourceSets DSL to add Kotlin sources is not allowed with built-in Kotlin`

Fix used in `gradle.properties`:
```properties
android.disallowKotlinSourceSets=false
```

### 2) JDK toolchain download / mismatch (Foojay)
Errors included:
- Could not download toolchain from Foojay
- Cannot find Java installation matching languageVersion=21

Current setup:
- `gradle/gradle-daemon-jvm.properties` has `toolchainVersion=25`
- `gradle.properties` points Gradle to Android Studio JBR:
  - `org.gradle.java.home=C:\\Program Files\\Android\\Android Studio\\jbr`
  - `org.gradle.java.installations.auto-download=false`

If builds still fail, verify that `jbr` version matches required `toolchainVersion`.

---

## Database location on device/emulator

Room DB name: `todo_database`
Location (internal app storage):
- `/data/data/com.example.todo/databases/todo_database`
- also typically:
  - `todo_database-wal`
  - `todo_database-shm`

Use Android Studio **Device Explorer** to inspect.

---

## Current implementation notes

- `MainActivity.kt` currently wires UI to `TodoViewModel` and Room-backed state.
- `TodoViewModel` method name is `addTodos(...)` (plural). It works, but `addTodo(...)` might be a cleaner name.
- There may be harmless IDE warnings for unused imports depending on intermediate edits.

---

## Next improvements (optional)

1. **Input validation UX**
   - Disable Add button when input blank
2. **Better DI**
   - Replace manual factory wiring with Hilt/Koin
3. **Testing**
   - DAO tests with in-memory Room
   - ViewModel tests for add/toggle/delete flows
4. **UI polish**
   - Empty-state illustration
   - Swipe-to-delete
   - Filter tabs (All/Active/Done)

---

## Quick recap of the learning path

1. Set up Compose + Room + ViewModel dependencies
2. Built UI with top input area and bottom todo list
3. Added Room entity/DAO/database/repository
4. Wired ViewModel + Room to Compose (`collectAsState`)
5. Resolved icons import issue
6. Resolved toolchain and KSP environment issues

---

Happy coding!

