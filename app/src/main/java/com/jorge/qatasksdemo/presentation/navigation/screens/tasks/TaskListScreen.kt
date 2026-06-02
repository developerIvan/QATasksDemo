package com.jorge.qatasksdemo.presentation.screens.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import com.jorge.qatasksdemo.presentation.navigation.screens.tasks.TaskViewModel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add


import androidx.navigation.NavController
@Composable
fun TaskListScreen( navController:NavController) {

    val viewModel: TaskViewModel = viewModel()
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

            items(tasks) { task ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {

                    Text(
                        text = "${task.title} - ${task.description}",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}