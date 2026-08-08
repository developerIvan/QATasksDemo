package com.jorge.qatasksdemo.domain.repository

import com.jorge.qatasksdemo.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    // Remote sync that pushes server snapshot into the local DB and returns the resulting list (or local fallback on error)
    suspend fun fetchTasks(): List<Task>

    // Reactive stream of tasks from the local DB
    fun observeTasks(): Flow<List<Task>>

    // Mutations (remote + local persistence)
    suspend fun createTask(
        title: String,
        description: String
    ): Task

    suspend fun updateTask(task: Task): Task

    suspend fun deleteTask(id: Int)

    // Outbox enqueue for offline sync
    suspend fun enqueueCreate(tempId: Int, title: String, description: String)
    suspend fun enqueueUpdate(task: Task)
    suspend fun enqueueDelete(id: Int)

    // Local-only helpers for optimistic UI
    suspend fun upsertLocal(task: Task)
    suspend fun upsertLocal(tasks: List<Task>)
    suspend fun deleteLocal(id: Int)

    // Demo utility: clears DB and inserts seed tasks
    suspend fun resetDemo()
}
