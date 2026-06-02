package com.jorge.qatasksdemo.presentation.navigation.components

import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.jorge.qatasksdemo.domain.model.Task

@Composable
fun TaskCard(
    task: Task,
    onDelete: () -> Unit,
    onToggleCompleted: () -> Unit
) {
    Card {
        Text(task.title)
    }
}