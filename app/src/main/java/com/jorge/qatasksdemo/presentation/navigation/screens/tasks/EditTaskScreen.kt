package com.jorge.qatasksdemo.presentation.navigation.screens.tasks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun EditTaskScreen(
    taskId: Int,
    viewModel: TaskViewModel,
    onDone: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val task = tasks.firstOrNull { it.id == taskId }

    var title by rememberSaveable(taskId) { mutableStateOf("") }
    var description by rememberSaveable(taskId) { mutableStateOf("") }
    var initialized by rememberSaveable(taskId) { mutableStateOf(false) }

    LaunchedEffect(task) {
        if (!initialized && task != null) {
            title = task.title
            description = task.description
            initialized = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(text = "Edit Task")

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.testTag("task_title_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.testTag("task_description_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.updateTask(
                    id = taskId,
                    title = title,
                    description = description
                )
                onDone()
            },
            modifier = Modifier.testTag("save_task_button")
        ) {
            Text("Save")
        }
    }
}
