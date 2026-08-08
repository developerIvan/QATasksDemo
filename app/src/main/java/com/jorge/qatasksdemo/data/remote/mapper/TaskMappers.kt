package com.jorge.qatasksdemo.data.remote.mapper

import com.jorge.qatasksdemo.data.remote.dto.TaskDto
import com.jorge.qatasksdemo.domain.model.Task

fun TaskDto.toDomain(): Task =
    Task(
        id = id,
        title = title,
        description = description,
        completed = completed,
        priority = 0
    )
