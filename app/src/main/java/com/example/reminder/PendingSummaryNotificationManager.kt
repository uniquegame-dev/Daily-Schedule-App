package com.example.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.TaskEntity
import com.example.util.TimeUtils

object PendingSummaryNotificationManager {

    const val CHANNEL_ID = "daily_pending_tasks_summary"
    const val CHANNEL_NAME = "Daily Pending Tasks Summary"
    const val PENDING_SUMMARY_NOTIFICATION_ID = 88888

    private const val PREFS_NAME = "notification_settings"
    private const val KEY_PENDING_SUMMARY_ENABLED = "key_pending_summary_enabled"

    fun isPendingSummaryEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PENDING_SUMMARY_ENABLED, true)
    }

    fun setPendingSummaryEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_PENDING_SUMMARY_ENABLED, enabled).apply()
    }

    fun updatePendingSummary(
        context: Context,
        todayTasks: List<TaskEntity>,
        isEnabledOverride: Boolean? = null
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val isEnabled = isEnabledOverride ?: isPendingSummaryEnabled(context)
        val pendingTasks = todayTasks.filter { !it.isCompleted }

        if (!isEnabled || pendingTasks.isEmpty()) {
            notificationManager.cancel(PENDING_SUMMARY_NOTIFICATION_ID)
            return
        }

        // Create notification channel on Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Daily summary of incomplete pending tasks"
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingCount = pendingTasks.size
        val titleText = if (pendingCount == 1) {
            "You have 1 pending task today"
        } else {
            "You have $pendingCount pending tasks today"
        }

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingContentIntent = PendingIntent.getActivity(
            context,
            PENDING_SUMMARY_NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(titleText)
            .setSummaryText("Daily Schedule Summary")

        // Format pending tasks list with name and time for expanded notification panel
        pendingTasks.take(12).forEach { task ->
            val formattedTime = TimeUtils.ensure12HourFormat(task.time)
            val timeLabel = if (formattedTime.isNotBlank()) "$formattedTime - " else ""
            inboxStyle.addLine("• $timeLabel${task.title}")
        }
        if (pendingTasks.size > 12) {
            inboxStyle.addLine("+ ${pendingTasks.size - 12} more pending")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(titleText)
            .setContentText("Swipe down to expand and list remaining tasks")
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) // Appears in notification panel when user swipes down
            .setOnlyAlertOnce(true) // Updates silently without alerting on every task change
            .setContentIntent(pendingContentIntent)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .build()

        try {
            notificationManager.notify(PENDING_SUMMARY_NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
