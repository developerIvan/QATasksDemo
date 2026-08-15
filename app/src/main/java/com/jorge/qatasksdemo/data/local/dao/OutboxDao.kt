package com.jorge.qatasksdemo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jorge.qatasksdemo.data.local.entity.OutboxEntity

@Dao
interface OutboxDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: OutboxEntity): Long

    @Query("SELECT * FROM outbox ORDER BY id ASC LIMIT 1")
    suspend fun peek(): OutboxEntity?

    @Query("DELETE FROM outbox WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE outbox SET retries = retries + 1 WHERE id = :id")
    suspend fun incrementRetries(id: Long)

    @Query("SELECT COUNT(*) FROM outbox")
    suspend fun count(): Int
}
