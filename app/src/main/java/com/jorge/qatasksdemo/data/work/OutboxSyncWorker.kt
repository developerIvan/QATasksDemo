package com.jorge.qatasksdemo.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jorge.qatasksdemo.di.ServiceLocator
import com.jorge.qatasksdemo.data.local.mapper.toEntity
import com.squareup.moshi.Moshi

class OutboxSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val db = ServiceLocator.provideDatabase(context)
        val outboxDao = db.outboxDao()
        val taskDao = db.taskDao()
        // repo not required here; we use TaskApi directly via ServiceLocator
        val moshi = Moshi.Builder().build()

        while (true) {
            val entry = outboxDao.peek() ?: break
            try {
                when (entry.operation) {
                    "CREATE" -> {
                        val adapter = moshi.adapter(com.jorge.qatasksdemo.data.outbox.CreateOutboxPayload::class.java)
                        val pl = adapter.fromJson(entry.payload)
                        if (pl != null) {
                            val server = ServiceLocator.taskApi().createTask(pl.payload)
                            // Reconcile: remove temp row and insert server row
                            taskDao.deleteById(pl.tempId)
                            taskDao.upsert(server.toEntity())
                        }
                        outboxDao.deleteById(entry.id)
                    }
                    "UPDATE" -> {
                        val adapter = moshi.adapter(com.jorge.qatasksdemo.data.outbox.UpdateOutboxPayload::class.java)
                        val pl = adapter.fromJson(entry.payload)
                        if (pl != null) {
                            val server = ServiceLocator.taskApi().updateTask(pl.id, pl.payload)
                            taskDao.upsert(server.toEntity())
                        }
                        outboxDao.deleteById(entry.id)
                    }
                    "DELETE" -> {
                        val adapter = moshi.adapter(com.jorge.qatasksdemo.data.outbox.DeleteOutboxPayload::class.java)
                        val pl = adapter.fromJson(entry.payload)
                        if (pl != null) {
                            ServiceLocator.taskApi().deleteTask(pl.id)
                        }
                        outboxDao.deleteById(entry.id)
                    }
                }
            } catch (t: Throwable) {
                outboxDao.incrementRetries(entry.id)
                return Result.retry()
            }
        }

        return Result.success()
    }
}
