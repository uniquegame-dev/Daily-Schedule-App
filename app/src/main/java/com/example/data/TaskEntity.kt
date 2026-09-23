package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val date: String, // Format: YYYY-MM-DD e.g. "2026-09-23"
    val time: String, // Format: HH:mm e.g. "14:30"
    val dueTimestamp: Long, // Timestamp in Epoch Millis for sorting & scheduling
    val isCompleted: Boolean = false,
    val completedAtTimestamp: Long? = null,
    val hasReminder: Boolean = false,
    val reminderMinutesBefore: Int = 0, // 0 = exact time, 15 = 15 min prior, 60 = 1 hr prior
    val category: String = "General" // General, Work, Personal, Health, Finance, Study
)
