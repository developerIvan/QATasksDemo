package com.jorge.qatasksdemo.data.local.mapper

import com.jorge.qatasksdemo.data.local.entity.TaskEntity
import com.jorge.qatasksdemo.data.remote.dto.TaskDto
import com.jorge.qatasksdemo.domain.model.Task

fun TaskEntity.toDomain(): Task =
    Task(id = id, title = title, description = description, completed = completed, priority = priority)

fun Task.toEntity(): TaskEntity =
    TaskEntity(id = id, title = title, description = description, completed = completed, priority = priority)

fun TaskDto.toEntity(): TaskEntity =
    TaskEntity(id = id, title = title, description = description, completed = completed, priority = 0)
