package com.jorge.qatasksdemo.presentation.navigation.screens.tasks


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jorge.qatasksdemo.domain.model.Task
import com.jorge.qatasksdemo.domain.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    data class RemovedTask(
        val task: Task,
        val index: Int
    )

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks = _tasks.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        // Observe DB as source of truth for UI
        viewModelScope.launch {
            repository.observeTasks().collectLatest { list ->
                _tasks.value = list
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

        fun createTask(
        title: String,
        description: String
    ) {
        // Keep synchronous path for online mode if desired
        viewModelScope.launch {
            _errorMessage.value = null
            try {
                repository.createTask(title, description)
            } catch (_: Throwable) {
                // Fall back to optimistic offline flow if direct create fails
                createTaskOptimistic(title, description)
            }
        }
    }

    fun createTaskOptimistic(
        title: String,
        description: String
    ) {
        viewModelScope.launch {
            _errorMessage.value = null
            // Generate a negative temp id based on time to minimize collision
            val tempId = (-System.currentTimeMillis()).toInt()
            val temp = Task(
                id = tempId,
                title = title,
                description = description,
                completed = false,
                priority = 0
            )
            // Optimistic local insert
            repository.upsertLocal(temp)
            // Enqueue create for background sync
            try {
                repository.enqueueCreate(tempId, title, description)
            } catch (_: Throwable) {
                // Keep it local; worker can be triggered later via Reset demo or refresh logic
            }
        }
    }


    fun commitUpdate(
        updated: Task,
        previous: Task
    ) {
        viewModelScope.launch {
            _errorMessage.value = null

            try {
                repository.updateTask(updated)
                // DB observer will update UI
                        } catch (t: Throwable) {
                            // Keep optimistic local change and queue for retry
                            try { repository.enqueueUpdate(updated) } catch (_: Throwable) {}
                            _errorMessage.value = "Update queued. Will retry when online."
                        }
        }
    }



        fun commitDelete(removed: RemovedTask) {
        viewModelScope.launch {
            _errorMessage.value = null

            try {
                repository.deleteTask(removed.task.id)
            } catch (t: Throwable) {
                // Offline-first: keep it deleted locally and queue remote delete for later
                try { repository.enqueueDelete(removed.task.id) } catch (_: Throwable) {}
                // Do NOT restore the task
                _errorMessage.value = "Delete queued. Will retry when online."
            }
        }
    }



    fun refresh() {
        if (_isRefreshing.value) return

        viewModelScope.launch {
            _isRefreshing.value = true
            _errorMessage.value = null

            try {
                repository.fetchTasks()
                // DB observer will update UI
            } catch (t: Throwable) {
                // Surface the error; UI shows whatever local DB has
                _errorMessage.value = "Could not refresh tasks. Check your connection and try again."
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun updateTask(
        id: Int,
        title: String,
        description: String
    ) {
        val current = _tasks.value
        val previous = current.firstOrNull { it.id == id } ?: return
        val updated = previous.copy(title = title, description = description)
        // Optimistic local update via DB
        viewModelScope.launch { repository.upsertLocal(updated) }
    }

    fun removeTask(id: Int): RemovedTask? {
        val current = _tasks.value
        val index = current.indexOfFirst { it.id == id }
        if (index == -1) return null

        val removed = current[index]
        // Optimistic local delete via DB
        viewModelScope.launch { repository.deleteLocal(id) }

        return RemovedTask(
            task = removed,
            index = index
        )
    }

    fun restoreTask(
        task: Task,
        index: Int
    ) {
        // Optimistic local restore via DB (ordering may differ)
        viewModelScope.launch { repository.upsertLocal(task) }
    }

    fun deleteTask(id: Int) {
        removeTask(id)
    }

    fun setCompleted(
        id: Int,
        completed: Boolean
    ) {
        val current = _tasks.value
        val task = current.firstOrNull { it.id == id } ?: return
        val updated = task.copy(completed = completed)
        // Optimistic local update via DB
        viewModelScope.launch { repository.upsertLocal(updated) }
    }

        fun toggleCompleted(id: Int) {
        val current = _tasks.value
        val task = current.firstOrNull { it.id == id } ?: return
        setCompleted(
            id = id,
            completed = !task.completed
        )
    }

        suspend fun resetDemo(): Boolean {
        _errorMessage.value = null
        return try {
            repository.resetDemo()
            true
        } catch (t: Throwable) {
            _errorMessage.value = "Could not reset demo. Please try again."
            false
        }
    }
}