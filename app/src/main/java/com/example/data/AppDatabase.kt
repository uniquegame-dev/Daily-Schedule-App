package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [TaskEntity::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `tasks_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        `date` TEXT NOT NULL,
                        `time` TEXT NOT NULL,
                        `dueTimestamp` INTEGER NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `completedAtTimestamp` INTEGER,
                        `hasReminder` INTEGER NOT NULL,
                        `reminderMinutesBefore` INTEGER NOT NULL,
                        `category` TEXT NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO `tasks_new` (`id`, `title`, `notes`, `date`, `time`, `dueTimestamp`, `isCompleted`, `completedAtTimestamp`, `hasReminder`, `reminderMinutesBefore`, `category`)
                    SELECT `id`, `title`, `notes`, `date`, `time`, `dueTimestamp`, `isCompleted`, `completedAtTimestamp`, `hasReminder`, `reminderMinutesBefore`, `category` FROM `tasks`
                """.trimIndent())

                db.execSQL("DROP TABLE IF EXISTS `tasks`")
                db.execSQL("ALTER TABLE `tasks_new` RENAME TO `tasks`")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "daily_schedule_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
