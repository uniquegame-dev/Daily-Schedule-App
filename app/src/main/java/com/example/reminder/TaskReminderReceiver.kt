package com.example.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.util.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "daily_schedule_reminders"
        const val CHANNEL_NAME = "Daily Schedule Reminders"
        
        const val ACTION_REMINDER = "com.example.ACTION_TASK_REMINDER"
        const val ACTION_MARK_DONE = "com.example.ACTION_MARK_TASK_DONE"
        
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_TASK_NOTES = "extra_task_notes"
        const val EXTRA_TASK_TIME = "extra_task_time"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)

        when (action) {
            ACTION_REMINDER -> {
                val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task Reminder"
                val notes = intent.getStringExtra(EXTRA_TASK_NOTES) ?: ""
                val time = intent.getStringExtra(EXTRA_TASK_TIME) ?: ""

                showNotification(context, taskId, title, notes, time)
            }
            ACTION_MARK_DONE -> {
                if (taskId != -1L) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val db = AppDatabase.getInstance(context)
                            val task = db.taskDao().getTaskById(taskId)
                            if (task != null) {
                                db.taskDao().updateTask(
                                    task.copy(
                                        isCompleted = true,
                                        completedAtTimestamp = System.currentTimeMillis()
                                    )
                                )
                                // Refresh pending tasks summary notification
                                val todayDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                                val todayTasks = db.taskDao().getTasksForDateList(todayDateStr)
                                PendingSummaryNotificationManager.updatePendingSummary(context, todayTasks)
                            }
                        } finally {
                            pendingResult.finish()
                        }
                    }
                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(taskId.toInt())
                }
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                // Alarms are cleared on boot; user opening app will reschedule active ones
            }
        }
    }

    private fun showNotification(
        context: Context,
        taskId: Long,
        title: String,
        notes: String,
        time: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for scheduled tasks"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tap notification to open app
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingContentIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action button to mark as completed directly from notification
        val markDoneIntent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = ACTION_MARK_DONE
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val pendingMarkDoneIntent = PendingIntent.getBroadcast(
            context,
            (taskId + 10000).toInt(),
            markDoneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedTime = TimeUtils.ensure12HourFormat(time)
        val contentText = if (notes.isNotBlank()) {
            "$formattedTime • $notes"
        } else if (formattedTime.isNotBlank()) {
            "Scheduled for $formattedTime"
        } else {
            "Time for your scheduled task"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("⏰ $title")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingContentIntent)
            .addAction(
                android.R.drawable.checkbox_on_background,
                "Mark as Done",
                pendingMarkDoneIntent
            )
            .build()

        notificationManager.notify(taskId.toInt(), notification)
    }
}
