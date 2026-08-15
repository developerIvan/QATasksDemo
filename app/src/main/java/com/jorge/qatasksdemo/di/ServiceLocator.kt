package com.jorge.qatasksdemo.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jorge.qatasksdemo.BuildConfig
import com.jorge.qatasksdemo.data.local.AppDatabase
import com.jorge.qatasksdemo.data.remote.api.TaskApi
import com.jorge.qatasksdemo.data.repository.TaskRepositoryImpl
import com.jorge.qatasksdemo.domain.repository.TaskRepository
import androidx.work.*
import java.util.concurrent.TimeUnit
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ServiceLocator {

    private val loggingInterceptor: HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    private val okHttpClient: OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

    private val moshi: Moshi =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    private val retrofit: Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    private val taskApi: TaskApi = retrofit.create(TaskApi::class.java)

    // Expose for Worker (internal use)
    fun taskApi(): TaskApi = taskApi

    @Volatile private var database: AppDatabase? = null

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // No-op
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add priority column with default 0
            db.execSQL("ALTER TABLE tasks ADD COLUMN priority INTEGER NOT NULL DEFAULT 0")
            // Create outbox table
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS outbox (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    operation TEXT NOT NULL,
                    payload TEXT NOT NULL,
                    createdAt INTEGER NOT NULL,
                    retries INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    fun provideDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "qa_tasks_demo.db"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
             .build()
             .also { database = it }
        }
    }

    private fun scheduleOutboxSyncInternal(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val work = OneTimeWorkRequestBuilder<com.jorge.qatasksdemo.data.work.OutboxSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "outbox_sync_once",
            ExistingWorkPolicy.KEEP,
            work
        )
    }

    fun provideTaskRepository(context: Context): TaskRepository {
        val db = provideDatabase(context)
        return TaskRepositoryImpl(
            taskApi,
            db.taskDao(),
            db.outboxDao()
        ) { scheduleOutboxSyncInternal(context) }
    }
}
