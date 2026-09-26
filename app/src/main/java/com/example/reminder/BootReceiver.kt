package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.AlarmManager
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_DATE_CHANGED ||
            action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val pendingTasks = db.taskDao().getPendingTasksWithReminders()
                    val currentTime = System.currentTimeMillis()
                    
                    for (task in pendingTasks) {
                        val triggerTime = task.dueTimestamp - (task.reminderMinutesBefore * 60 * 1000L)
                        if (triggerTime > currentTime) {
                            ReminderScheduler.scheduleTaskReminder(context, task)
                        }
                    }
                    val today = java.text.SimpleDateFormat(
                        "yyyy-MM-dd",
                        java.util.Locale.US
                    ).format(java.util.Date())
                    PendingSummaryNotificationManager.updatePendingSummary(
                        context,
                        db.taskDao().getTasksForDateList(today)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
