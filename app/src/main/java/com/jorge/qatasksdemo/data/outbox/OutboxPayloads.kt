package com.jorge.qatasksdemo.data.outbox

import com.jorge.qatasksdemo.data.remote.api.CreateTaskRequest
import com.jorge.qatasksdemo.data.remote.api.UpdateTaskRequest

// Typed wrappers for outbox JSON payloads

data class CreateOutboxPayload(
    val tempId: Int,
    val payload: CreateTaskRequest
)

data class UpdateOutboxPayload(
    val id: Int,
    val payload: UpdateTaskRequest
)

data class DeleteOutboxPayload(
    val id: Int
)
