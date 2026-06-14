package com.jorge.qatasksdemo.presentation.screens.tasks

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jorge.qatasksdemo.presentation.navigation.components.TaskCard
import com.jorge.qatasksdemo.presentation.navigation.screens.tasks.TaskViewModel

@Composable
fun TaskListScreen(
    navController: NavController,
    viewModel: TaskViewModel = viewModel()
) {

    val tasks by viewModel.tasks.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("add_task")
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task"
                )
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            items(
                items = tasks,
                key = { it.id }
            ) { task ->
                TaskCard(
                    task = task,
                    onDelete = {
                        viewModel.deleteTask(task.id)
                    },
                    onToggleCompleted = {
                        viewModel.toggleCompleted(task.id)
                    },
                    onEdit = {
                        navController.navigate("edit_task/${task.id}")
                    },
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }
    }
}