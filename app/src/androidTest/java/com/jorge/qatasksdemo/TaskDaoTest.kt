package com.jorge.qatasksdemo

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jorge.qatasksdemo.data.local.AppDatabase
import com.jorge.qatasksdemo.data.local.dao.TaskDao
import com.jorge.qatasksdemo.data.local.entity.TaskEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: TaskDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.taskDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsert_and_get_tasks() = runBlocking {
        val t1 = TaskEntity(1, "A", "desc", false, 0)
        val t2 = TaskEntity(2, "B", "desc", true, 1)
        dao.upsertAll(listOf(t1, t2))

        val list = dao.getAllOnce()
        assertEquals(2, list.size)
        assertEquals(1, list[0].id)
        assertEquals(2, list[1].id)
    }

    @Test
    fun delete_task() = runBlocking {
        val t1 = TaskEntity(1, "A", "desc", false, 0)
        dao.upsert(t1)
        dao.deleteById(1)
        val list = dao.getAllOnce()
        assertEquals(0, list.size)
    }
}
