package com.jorge.qatasksdemo.presentation.navigation.screens.tasks


import androidx.lifecycle.ViewModel
import com.jorge.qatasksdemo.domain.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TaskViewModel : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks = _tasks.asStateFlow()

    fun addTask(
        title: String,
        description: String
    ) {

        val newTask = Task(
            id = (_tasks.value.size + 1),
            title = title,
            description = description
        )

        _tasks.value += newTask
    }

    fun deleteTask(id: Int) {

        _tasks.value =
            _tasks.value.filterNot {
                it.id == id
            }
    }

    fun toggleCompleted(id: Int) {

        _tasks.value =
            _tasks.value.map {

                if (it.id == id)
                    it.copy(completed = !it.completed)
                else
                    it
            }
    }
}