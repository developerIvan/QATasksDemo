package com.jorge.qatasksdemo.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jorge.qatasksdemo.presentation.navigation.screens.tasks.AddTaskScreen
import com.jorge.qatasksdemo.presentation.navigation.screens.tasks.EditTaskScreen
import com.jorge.qatasksdemo.presentation.navigation.screens.tasks.TaskViewModel
import com.jorge.qatasksdemo.presentation.screens.login.LoginScreen
import com.jorge.qatasksdemo.presentation.screens.tasks.TaskListScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val taskViewModel: TaskViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("tasks")
                }
            )
        }

        composable("tasks") {
            TaskListScreen(
                navController = navController,
                viewModel = taskViewModel
            )
        }

        composable("add_task") {
            AddTaskScreen(
                onSave = { title, description ->
                    taskViewModel.addTask(
                        title = title,
                        description = description
                    )

                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("edit_task/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull()

            if (taskId != null) {
                EditTaskScreen(
                    taskId = taskId,
                    viewModel = taskViewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}