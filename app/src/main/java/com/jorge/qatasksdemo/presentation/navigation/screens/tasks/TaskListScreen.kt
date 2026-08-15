package com.jorge.qatasksdemo.presentation.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button

import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton


import androidx.compose.material3.TopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.pulltorefresh.PullToRefreshBox

import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jorge.qatasksdemo.presentation.navigation.components.TaskCard
import com.jorge.qatasksdemo.presentation.navigation.screens.tasks.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    navController: NavController,
    viewModel: TaskViewModel = viewModel()
) {

    val tasks by viewModel.tasks.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(Unit) {
        // Initial load
        viewModel.refresh()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun deleteWithUndo(taskId: Int) {
        val removed = viewModel.removeTask(taskId) ?: return

        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "Task deleted",
                actionLabel = "Undo",
                withDismissAction = true
            )

            if (result == SnackbarResult.ActionPerformed) {
                viewModel.restoreTask(
                    task = removed.task,
                    index = removed.index
                )
            } else {
                // Commit remote deletion only if user didn't undo.
                viewModel.commitDelete(removed)
            }
        }
    }

    fun toggleCompletedWithUndo(taskId: Int) {
        val task = tasks.firstOrNull { it.id == taskId } ?: return
        val previousCompleted = task.completed
        val newCompleted = !previousCompleted

        val updatedTask = task.copy(completed = newCompleted)

        // Optimistic UI update
        viewModel.setCompleted(
            id = taskId,
            completed = newCompleted
        )

        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = if (newCompleted) "Marked as completed" else "Marked as pending",
                actionLabel = "Undo",
                withDismissAction = true
            )

            if (result == SnackbarResult.ActionPerformed) {
                viewModel.setCompleted(
                    id = taskId,
                    completed = previousCompleted
                )
            } else {
                // Commit remote update only if user didn't undo.
                viewModel.commitUpdate(
                    updated = updatedTask,
                    previous = task
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Tasks",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "${tasks.size} items",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                                actions = {
                    IconButton(
                        onClick = { viewModel.refresh() },
                        modifier = Modifier.testTag("refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }

                    var menuExpanded by remember { mutableStateOf(false) }
                    var showResetConfirm by remember { mutableStateOf(false) }

                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.testTag("overflow_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More"
                        )
                    }













                                        DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Reset demo (clear + seed)") },
                            onClick = {
                                menuExpanded = false
                                showResetConfirm = true
                            },
                            modifier = Modifier.testTag("reset_demo_button")
                        )
                    }

                    if (showResetConfirm) {
                        AlertDialog(
                            onDismissRequest = { showResetConfirm = false },
                            title = { Text("Reset demo?") },
                            text = { Text("This will clear all local tasks and insert sample seeds.") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showResetConfirm = false
                                        scope.launch {
                                            val ok = viewModel.resetDemo()
                                            if (ok) snackbarHostState.showSnackbar("Demo reset")
                                        }
                                    },
                                    modifier = Modifier.testTag("confirm_reset_button")
                                ) { Text("Reset") }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { showResetConfirm = false },
                                    modifier = Modifier.testTag("cancel_reset_button")
                                ) { Text("Cancel") }
                            }
                        )
                    }

                }

            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.testTag("snackbar_host")
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("add_task")
                },
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task"
                )
            }
        }
    ) { padding ->

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                viewModel.refresh()
            },
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("tasks_pull_to_refresh"),
            indicator = {
                // Custom indicator so we can keep a stable testTag for Maestro.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("pull_to_refresh_indicator"),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val progress = pullToRefreshState.distanceFraction.coerceIn(0f, 1f)

                    if (isRefreshing || progress > 0f) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (errorMessage != null) {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag("tasks_error_banner"),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Something went wrong",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.refresh() },
                                    modifier = Modifier.testTag("retry_refresh_button")
                                ) {
                                    Text("Retry")
                                }

                                TextButton(
                                    onClick = { viewModel.clearError() },
                                    modifier = Modifier.testTag("dismiss_error_button")
                                ) {
                                    Text("Dismiss")
                                }
                            }
                        }
                    }
                }

                if (tasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                                .testTag("tasks_empty_state"),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No tasks yet",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Pull to refresh to load sample tasks or tap + to create one.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            IconButton(
                                onClick = { viewModel.refresh() },
                                modifier = Modifier.testTag("load_sample_tasks_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Load sample tasks"
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp)
                            .testTag("tasks_list")
                    ) {
                        items(
                            items = tasks,
                            key = { it.id }
                        ) { task ->
                            val dismissState = rememberSwipeToDismissBoxState()

                            SwipeToDismissBox(
                                state = dismissState,
                                modifier = Modifier
                                    .padding(bottom = 12.dp)
                                    .testTag("swipe_delete_${task.id}"),
                                onDismiss = {
                                    deleteWithUndo(task.id)
                                },
                                backgroundContent = {
                                    // Background shown while swiping
                                    val alignment =
                                        if (dismissState.dismissDirection == androidx.compose.material3.SwipeToDismissBoxValue.StartToEnd)
                                            Alignment.CenterStart
                                        else
                                            Alignment.CenterEnd

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.errorContainer)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = alignment
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            ) {
                                TaskCard(
                                    task = task,
                                    onDelete = {
                                        deleteWithUndo(task.id)
                                    },
                                    onToggleCompleted = {
                                        toggleCompletedWithUndo(task.id)
                                    },
                                    onEdit = {
                                        navController.navigate("edit_task/${task.id}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}