package com.jorge.qatasksdemo.data.remote.dto

data class TaskDto(
    val id: Int,
    val title: String,
    val description: String,
    val completed: Boolean
)
