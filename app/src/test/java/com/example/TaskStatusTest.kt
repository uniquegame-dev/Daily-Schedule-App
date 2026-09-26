package com.example

import com.example.data.TaskEntity
import com.example.util.TaskStatus
import com.example.util.calculateTaskStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskStatusTest {
    private fun task(dueTimestamp: Long, isCompleted: Boolean = false) = TaskEntity(
        id = 1,
        title = "Task",
        date = "2026-09-26",
        time = "09:00 AM",
        dueTimestamp = dueTimestamp,
        isCompleted = isCompleted
    )

    @Test
    fun completedTakesPriorityEvenAfterDueTime() {
        assertEquals(TaskStatus.COMPLETED, calculateTaskStatus(task(1_000, true), 2_000))
    }

    @Test
    fun incompletePastTaskIsMissed() {
        assertEquals(TaskStatus.MISSED, calculateTaskStatus(task(1_000), 2_000))
    }

    @Test
    fun incompleteFutureTaskIsPending() {
        assertEquals(TaskStatus.PENDING, calculateTaskStatus(task(3_000), 2_000))
    }
}
