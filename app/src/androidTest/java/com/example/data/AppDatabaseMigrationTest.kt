package com.example.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = InstrumentationRegistry.getInstrumentation(),
        databaseClass = AppDatabase::class.java
    )

    @Test
    fun migrate1To2_preservesTaskData() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL(
                """INSERT INTO tasks
                    (id, title, notes, date, time, dueTimestamp, isCompleted,
                     completedAtTimestamp, hasReminder, reminderMinutesBefore, category)
                    VALUES (7, 'Existing task', 'Keep me', '2026-09-26', '09:30 AM',
                            1790395200000, 1, 1790397000000, 1, 15, 'Work')"""
            )
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 2, true, AppDatabase.MIGRATION_1_2).use { db ->
            db.query("SELECT * FROM tasks WHERE id = 7").use { cursor ->
                check(cursor.moveToFirst())
                assertEquals("Existing task", cursor.getString(cursor.getColumnIndexOrThrow("title")))
                assertEquals("Keep me", cursor.getString(cursor.getColumnIndexOrThrow("notes")))
                assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("isCompleted")))
                assertEquals("Work", cursor.getString(cursor.getColumnIndexOrThrow("category")))
            }
        }
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
