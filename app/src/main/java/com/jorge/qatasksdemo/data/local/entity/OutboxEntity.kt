package com.jorge.qatasksdemo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outbox")
data class OutboxEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operation: String, // CREATE, UPDATE, DELETE
    val payload: String,   // JSON payload
    val createdAt: Long = System.currentTimeMillis(),
    val retries: Int = 0
)
