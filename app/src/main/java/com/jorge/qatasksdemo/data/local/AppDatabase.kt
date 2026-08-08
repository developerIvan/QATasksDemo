package com.jorge.qatasksdemo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jorge.qatasksdemo.data.local.dao.TaskDao
import com.jorge.qatasksdemo.data.local.entity.TaskEntity

@Database(
    entities = [TaskEntity::class, com.jorge.qatasksdemo.data.local.entity.OutboxEntity::class],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun outboxDao(): com.jorge.qatasksdemo.data.local.dao.OutboxDao
}
