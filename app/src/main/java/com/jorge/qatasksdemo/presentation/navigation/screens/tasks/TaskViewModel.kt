package com.jorge.qatasksdemo.presentation.navigation.screens.tasks


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jorge.qatasksdemo.domain.model.Task
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskViewModel : ViewModel() {

    data class RemovedTask(
        val task: Task,
        val index: Int
    )

    private val seedTasks: List<Task> =
        listOf(
            Task(
                id = 1001,
                title = "Seed: Write test cases",
                description = "Create basic test cases for login and tasks flow"
            ),
            Task(
                id = 1002,
                title = "Seed: Automate with Maestro",
                description = "Add a Maestro flow for add/edit/delete tasks"
            ),
            Task(
                id = 1003,
                title = "Seed: Add Room",
                description = "Persist tasks locally and test DAO operations"
            )
        )

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks = _tasks.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    // Deterministic re-ordering for QA automation: refresh toggles sort order.
    private var sortAscending: Boolean = true

    fun refresh() {
        if (_isRefreshing.value) return

        viewModelScope.launch {
            _isRefreshing.value = true

            // Simulate a network call / database sync.
            delay(800)

            val current = _tasks.value

            // Re-insert seed tasks (even if the user deleted them), preserving their current state
            // when present.
            val currentById = current.associateBy { it.id }
            val rebuiltSeeds = seedTasks.map { seed ->
                currentById[seed.id] ?: seed
            }

            val nonSeedTasks = current.filterNot { task ->
                seedTasks.any { seed -> seed.id == task.id }
            }

            val merged = rebuiltSeeds + nonSeedTasks

            // Re-order tasks on each refresh (toggle asc/desc) so the refresh has an observable
            // effect for UI automation.
            _tasks.value =
                if (sortAscending) {
                    merged.sortedBy { it.id }
                } else {
                    merged.sortedByDescending { it.id }
                }

            sortAscending = !sortAscending

            // If tasks were empty, ensure at least the seeds exist.
            if (_tasks.value.isEmpty()) {
                _tasks.value = seedTasks
            }

            _isRefreshing.value = false
        }
    }

    private fun nextId(): Int {
        val maxId = _tasks.value.maxOfOrNull { it.id } ?: 1000
        return maxId + 1
    }

    fun addTask(
        title: String,
        description: String
    ) {

        val newTask = Task(
            id = nextId(),
            title = title,
            description = description
        )

        _tasks.value += newTask
    }

    fun updateTask(
        id: Int,
        title: String,
        description: String
    ) {
        _tasks.value =
            _tasks.value.map { task ->
                if (task.id == id) {
                    task.copy(
                        title = title,
                        description = description
                    )
                } else {
                    task
                }
            }
    }

    fun removeTask(id: Int): RemovedTask? {
        val current = _tasks.value
        val index = current.indexOfFirst { it.id == id }
        if (index == -1) return null

        val removed = current[index]
        _tasks.value = current.toMutableList().also { it.removeAt(index) }

        return RemovedTask(
            task = removed,
            index = index
        )
    }

    fun restoreTask(
        task: Task,
        index: Int
    ) {
        val current = _tasks.value

        // Avoid duplicates if the task already exists.
        if (current.any { it.id == task.id }) return

        val mutable = current.toMutableList()
        val safeIndex = index.coerceIn(0, mutable.size)
        mutable.add(safeIndex, task)
        _tasks.value = mutable
    }

    fun deleteTask(id: Int) {
        removeTask(id)
    }

    fun setCompleted(
        id: Int,
        completed: Boolean
    ) {
        _tasks.value =
            _tasks.value.map { task ->
                if (task.id == id) task.copy(completed = completed) else task
            }
    }

    fun toggleCompleted(id: Int) {
        val current = _tasks.value
        val task = current.firstOrNull { it.id == id } ?: return
        setCompleted(
            id = id,
            completed = !task.completed
        )
    }
}