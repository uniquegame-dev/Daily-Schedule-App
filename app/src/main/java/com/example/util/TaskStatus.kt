package com.example.util

import com.example.data.TaskEntity

enum class TaskStatus {
    PENDING,
    COMPLETED,
    MISSED
}

fun calculateTaskStatus(
    task: TaskEntity,
    nowMillis: Long = System.currentTimeMillis()
): TaskStatus = when {
    task.isCompleted -> TaskStatus.COMPLETED
    task.dueTimestamp < nowMillis -> TaskStatus.MISSED
    else -> TaskStatus.PENDING
}
