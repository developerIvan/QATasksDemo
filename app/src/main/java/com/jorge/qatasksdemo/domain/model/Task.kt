package com.jorge.qatasksdemo.domain.model

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val completed: Boolean = false,
    val priority: Int = 0
)