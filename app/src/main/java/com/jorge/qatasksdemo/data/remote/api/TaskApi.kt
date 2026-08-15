package com.jorge.qatasksdemo.data.remote.api

import com.jorge.qatasksdemo.data.remote.dto.TaskDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TaskApi {

    @GET("tasks")
    suspend fun getTasks(): List<TaskDto>

    @POST("tasks")
    suspend fun createTask(
        @Body body: CreateTaskRequest
    ): TaskDto

    @PUT("tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: Int,
        @Body body: UpdateTaskRequest
    ): TaskDto

    @DELETE("tasks/{id}")
    suspend fun deleteTask(
        @Path("id") id: Int
    )
}

data class CreateTaskRequest(
    val title: String,
    val description: String
)

data class UpdateTaskRequest(
    val title: String,
    val description: String,
    val completed: Boolean
)
