package com.jorge.qatasksdemo.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jorge.qatasksdemo.presentation.navigation.screens.tasks.AddTaskScreen
//import androidx.navigation.compose.*
import com.jorge.qatasksdemo.presentation.screens.login.LoginScreen
import com.jorge.qatasksdemo.presentation.screens.tasks.TaskListScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

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
            TaskListScreen(navController = navController)
        }

        composable("add_task") {
            AddTaskScreen(
                onSave = { title, description ->

                    // Aquí luego llamaremos al ViewModel

                    navController.popBackStack()
                }
            )
        }
    }
}