package com.jorge.qatasksdemo.data.repository

import com.jorge.qatasksdemo.data.local.dao.OutboxDao
import com.jorge.qatasksdemo.data.local.dao.TaskDao
import com.jorge.qatasksdemo.data.local.entity.OutboxEntity
import com.jorge.qatasksdemo.data.local.mapper.toDomain
import com.jorge.qatasksdemo.data.local.mapper.toEntity
import com.jorge.qatasksdemo.data.outbox.CreateOutboxPayload
import com.jorge.qatasksdemo.data.outbox.UpdateOutboxPayload
import com.jorge.qatasksdemo.data.outbox.DeleteOutboxPayload
import com.jorge.qatasksdemo.data.remote.api.CreateTaskRequest
import com.jorge.qatasksdemo.data.remote.api.TaskApi
import com.jorge.qatasksdemo.data.remote.api.UpdateTaskRequest
import com.jorge.qatasksdemo.data.remote.mapper.toDomain as dtoToDomain
import com.jorge.qatasksdemo.data.local.mapper.toEntity as dtoToEntity
import com.jorge.qatasksdemo.domain.model.Task
import com.jorge.qatasksdemo.domain.repository.TaskRepository
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl(
    private val api: TaskApi,
    private val taskDao: TaskDao,
    private val outboxDao: OutboxDao,
    private val scheduleSync: () -> Unit
) : TaskRepository {

    private val seedTasks: List<Task> = listOf(
        Task(
            id = 1001,
            title = "Seed: Write test cases",
            description = "Create basic test cases for login and tasks flow",
            completed = false
        ),
        Task(
            id = 1002,
            title = "Seed: Automate with Maestro",
            description = "Add a Maestro flow for add/edit/delete tasks",
            completed = false
        ),
        Task(
            id = 1003,
            title = "Seed: Add Room",
            description = "Persist tasks locally and test DAO operations",
            completed = false
        )
    )

    override fun observeTasks() = taskDao.observeAll().map { list ->
        list.map { it.toDomain() }
    }

    override suspend fun fetchTasks(): List<Task> {
        return try {
            val remote = api.getTasks()
            if (remote.isEmpty()) {
                val local = taskDao.getAllOnce()
                if (local.isEmpty()) {
                    // Seed the database only when both remote and local are empty
                    taskDao.clearAll()
                    taskDao.upsertAll(seedTasks.map { it.toEntity() })
                }
                return taskDao.getAllOnce().map { it.toDomain() }
            } else {
                taskDao.clearAll()
                taskDao.upsertAll(remote.map { it.dtoToEntity() })
                return remote.map { it.dtoToDomain() }
            }
        } catch (_: Throwable) {
            // Fallback to local cache
            taskDao.getAllOnce().map { it.toDomain() }
        }
    }

    override suspend fun createTask(
        title: String,
        description: String
    ): Task {
        val server = api.createTask(
            body = CreateTaskRequest(
                title = title,
                description = description
            )
        )
        val entity = server.dtoToEntity()
        taskDao.upsert(entity)
        return entity.toDomain()
    }

    override suspend fun updateTask(task: Task): Task {
        val server = api.updateTask(
            id = task.id,
            body = UpdateTaskRequest(
                title = task.title,
                description = task.description,
                completed = task.completed
            )
        )
        val entity = server.dtoToEntity()
        taskDao.upsert(entity)
        return entity.toDomain()
    }

    override suspend fun deleteTask(id: Int) {
        api.deleteTask(id)
        taskDao.deleteById(id)
    }

    override suspend fun upsertLocal(task: Task) {
        taskDao.upsert(task.toEntity())
    }

    override suspend fun upsertLocal(tasks: List<Task>) {
        taskDao.upsertAll(tasks.map { it.toEntity() })
    }

    override suspend fun deleteLocal(id: Int) {
        taskDao.deleteById(id)
    }

    override suspend fun enqueueCreate(tempId: Int, title: String, description: String) {
        val payload = CreateOutboxPayload(
            tempId = tempId,
            payload = CreateTaskRequest(title, description)
        )
        val moshi = com.squareup.moshi.Moshi.Builder().build()
        val json = moshi.adapter(CreateOutboxPayload::class.java).toJson(payload)
        outboxDao.insert(OutboxEntity(operation = "CREATE", payload = json))
        scheduleSync()
    }

    override suspend fun enqueueUpdate(task: Task) {
        val payload = UpdateOutboxPayload(
            id = task.id,
            payload = UpdateTaskRequest(task.title, task.description, task.completed)
        )
        val moshi = com.squareup.moshi.Moshi.Builder().build()
        val json = moshi.adapter(UpdateOutboxPayload::class.java).toJson(payload)
        outboxDao.insert(OutboxEntity(operation = "UPDATE", payload = json))
        scheduleSync()
    }

    override suspend fun enqueueDelete(id: Int) {
        val payload = DeleteOutboxPayload(id)
        val moshi = com.squareup.moshi.Moshi.Builder().build()
        val json = moshi.adapter(DeleteOutboxPayload::class.java).toJson(payload)
        outboxDao.insert(OutboxEntity(operation = "DELETE", payload = json))
        scheduleSync()
    }

    override suspend fun resetDemo() {
        taskDao.clearAll()
        taskDao.upsertAll(seedTasks.map { it.toEntity() })
    }
}
