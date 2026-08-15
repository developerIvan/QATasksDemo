package com.jorge.qatasksdemo

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jorge.qatasksdemo.data.local.AppDatabase
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration23Test {

    private lateinit var context: Context
    private val DB_NAME = "test_migration.db"

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(DB_NAME)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(DB_NAME)
    }

    @Test
    fun migrate_2_to_3_adds_priority_and_outbox() {
        // Create database at version 2
        val openHelper = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(DB_NAME)
                .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(2) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE IF NOT EXISTS tasks (id INTEGER NOT NULL PRIMARY KEY, title TEXT NOT NULL, description TEXT NOT NULL, completed INTEGER NOT NULL)")
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()
        )
        openHelper.writableDatabase.close()

        // Run migration 2 -> 3
        val db = Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
            .addMigrations(object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE tasks ADD COLUMN priority INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("CREATE TABLE IF NOT EXISTS outbox (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, operation TEXT NOT NULL, payload TEXT NOT NULL, createdAt INTEGER NOT NULL, retries INTEGER NOT NULL)")
                }
            })
            .build()

        db.openHelper.readableDatabase.use { sqlite ->
            // Check 'priority' column exists
            val cursor = sqlite.query("PRAGMA table_info('tasks')")
            var hasPriority = false
            while (cursor.moveToNext()) {
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                if (name == "priority") {
                    hasPriority = true
                    break
                }
            }
            cursor.close()
            assertTrue(hasPriority)

            // Check outbox table exists
            val c2 = sqlite.query("SELECT name FROM sqlite_master WHERE type='table' AND name='outbox'")
            val exists = c2.moveToFirst()
            c2.close()
            assertTrue(exists)
        }

        db.close()
    }
}
