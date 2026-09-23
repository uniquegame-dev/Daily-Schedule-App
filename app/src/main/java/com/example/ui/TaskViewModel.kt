package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.TaskEntity
import com.example.data.TaskRepository
import com.example.reminder.PendingSummaryNotificationManager
import com.example.reminder.ReminderScheduler
import com.example.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = TaskRepository(db.taskDao())
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayDateFormatter = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

    // Current today date string
    val todayDateStr: String = dateFormatter.format(Date())

    // Active Navigation Tab
    private val _currentTab = MutableStateFlow(0) // 0: Today, 1: Upcoming, 2: History
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Selected Date in Calendar View (defaults to Today)
    private val _selectedCalendarDate = MutableStateFlow(todayDateStr)
    val selectedCalendarDate: StateFlow<String> = _selectedCalendarDate.asStateFlow()

    // Add / Edit Task Dialog/Sheet visibility
    private val _showTaskSheet = MutableStateFlow(false)
    val showTaskSheet: StateFlow<Boolean> = _showTaskSheet.asStateFlow()

    private val _editingTask = MutableStateFlow<TaskEntity?>(null)
    val editingTask: StateFlow<TaskEntity?> = _editingTask.asStateFlow()

    // Search & Filter state for History / Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _categoryFilter = MutableStateFlow("All")
    val categoryFilter: StateFlow<String> = _categoryFilter.asStateFlow()

    private val _isPendingSummaryEnabled = MutableStateFlow(
        PendingSummaryNotificationManager.isPendingSummaryEnabled(application)
    )
    val isPendingSummaryEnabled: StateFlow<Boolean> = _isPendingSummaryEnabled.asStateFlow()

    // Unfiltered today tasks for pending summary notification
    val allTodayTasks: StateFlow<List<TaskEntity>> = repository.getTasksForDate(todayDateStr)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Seed sample tasks if empty on first boot
        viewModelScope.launch {
            val count = db.taskDao().getAllTasks().first().size
            if (count == 0) {
                seedInitialTasks()
            }
        }

        // Automatically maintain pending summary notification in status bar / panel
        viewModelScope.launch {
            allTodayTasks.collect { tasks ->
                PendingSummaryNotificationManager.updatePendingSummary(
                    getApplication(),
                    tasks,
                    _isPendingSummaryEnabled.value
                )
            }
        }
    }

    fun setPendingSummaryEnabled(enabled: Boolean) {
        _isPendingSummaryEnabled.value = enabled
        PendingSummaryNotificationManager.setPendingSummaryEnabled(getApplication(), enabled)
        PendingSummaryNotificationManager.updatePendingSummary(
            getApplication(),
            allTodayTasks.value,
            enabled
        )
    }

    // Today Tasks Flow
    val todayTasks: StateFlow<List<TaskEntity>> = repository.getTasksForDate(todayDateStr)
        .combine(categoryFilter) { tasks, cat ->
            if (cat == "All") tasks else tasks.filter { it.category == cat }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // All Tasks Flow (used for calendar indicators)
    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Selected Date Tasks for Calendar View
    val selectedDateTasks: StateFlow<List<TaskEntity>> = _selectedCalendarDate
        .flatMapLatest { dateStr -> repository.getTasksForDate(dateStr) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Upcoming Tasks Flow (Date > today)
    val upcomingTasks: StateFlow<List<TaskEntity>> = repository.getUpcomingTasks(todayDateStr)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Completed / History Tasks Flow
    val historyTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .combine(_searchQuery) { tasks, query ->
            tasks.filter { task ->
                val matchesQuery = query.isBlank() ||
                        task.title.contains(query, ignoreCase = true) ||
                        task.notes.contains(query, ignoreCase = true)
                // In history, we show completed tasks OR past overdue tasks
                val isPastOrCompleted = task.isCompleted || task.date < todayDateStr
                matchesQuery && isPastOrCompleted
            }
        }
        .combine(categoryFilter) { tasks, cat ->
            if (cat == "All") tasks else tasks.filter { it.category == cat }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    fun setSelectedCalendarDate(dateStr: String) {
        _selectedCalendarDate.value = dateStr
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String) {
        _categoryFilter.value = category
    }

    fun openAddTaskSheet(forDateStr: String? = null) {
        _editingTask.value = null
        if (forDateStr != null) {
            _selectedCalendarDate.value = forDateStr
        }
        _showTaskSheet.value = true
    }

    fun openEditTaskSheet(task: TaskEntity) {
        _editingTask.value = task
        _showTaskSheet.value = true
    }

    fun closeTaskSheet() {
        _showTaskSheet.value = false
        _editingTask.value = null
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            val updated = task.copy(
                isCompleted = !task.isCompleted,
                completedAtTimestamp = if (!task.isCompleted) System.currentTimeMillis() else null
            )
            repository.updateTask(updated)
            
            // Cancel or reschedule reminder
            if (updated.isCompleted) {
                ReminderScheduler.cancelTaskReminder(getApplication(), updated.id)
            } else if (updated.hasReminder) {
                ReminderScheduler.scheduleTaskReminder(getApplication(), updated)
            }
        }
    }

    fun saveTask(
        id: Long = 0,
        title: String,
        notes: String,
        date: String,
        time: String,
        hasReminder: Boolean,
        reminderMinutesBefore: Int,
        category: String
    ) {
        if (title.isBlank()) return

        viewModelScope.launch {
            val dueTimestamp = parseDateAndTimeToMillis(date, time)
            val task = TaskEntity(
                id = id,
                title = title.trim(),
                notes = notes.trim(),
                date = date,
                time = time,
                dueTimestamp = dueTimestamp,
                isCompleted = false,
                hasReminder = hasReminder,
                reminderMinutesBefore = reminderMinutesBefore,
                category = category
            )

            val taskId = if (id == 0L) {
                repository.insertTask(task)
            } else {
                repository.updateTask(task)
                id
            }

            // Schedule alarm if requested
            if (hasReminder) {
                ReminderScheduler.scheduleTaskReminder(getApplication(), task.copy(id = taskId))
            } else {
                ReminderScheduler.cancelTaskReminder(getApplication(), taskId)
            }

            closeTaskSheet()
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            ReminderScheduler.cancelTaskReminder(getApplication(), task.id)
        }
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            repository.clearCompletedTasks()
        }
    }

    fun testNotification() {
        ReminderScheduler.showTestNotification(getApplication())
    }

    private fun parseDateAndTimeToMillis(dateStr: String, timeStr: String): Long {
        return TimeUtils.parseDateAndTimeToMillis(dateStr, timeStr)
    }

    private suspend fun seedInitialTasks() {
        val cal = Calendar.getInstance()
        val today = dateFormatter.format(cal.time)
        
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = dateFormatter.format(cal.time)

        val initialTasks = listOf(
            TaskEntity(
                title = "Morning Standup Meeting",
                notes = "Review daily sprint goals and discuss blockers.",
                date = today,
                time = "09:30 AM",
                dueTimestamp = parseDateAndTimeToMillis(today, "09:30 AM"),
                isCompleted = false,
                hasReminder = true,
                reminderMinutesBefore = 15,
                category = "Work"
            ),
            TaskEntity(
                title = "Hydration & Health Check",
                notes = "Drink 500ml water and stretch for 10 minutes.",
                date = today,
                time = "11:00 AM",
                dueTimestamp = parseDateAndTimeToMillis(today, "11:00 AM"),
                isCompleted = true,
                completedAtTimestamp = System.currentTimeMillis(),
                hasReminder = false,
                category = "Health"
            ),
            TaskEntity(
                title = "Focus Block: Core Logic",
                notes = "Finish implementing local storage & offline database features.",
                date = today,
                time = "02:00 PM",
                dueTimestamp = parseDateAndTimeToMillis(today, "02:00 PM"),
                isCompleted = false,
                hasReminder = true,
                reminderMinutesBefore = 0,
                category = "Work"
            ),
            TaskEntity(
                title = "Gym Session & Workout",
                notes = "Leg day and 20 min cardio.",
                date = today,
                time = "06:00 PM",
                dueTimestamp = parseDateAndTimeToMillis(today, "06:00 PM"),
                isCompleted = false,
                hasReminder = true,
                reminderMinutesBefore = 15,
                category = "Health"
            ),
            TaskEntity(
                title = "Weekly Grocery Shopping",
                notes = "Buy fresh veggies, oat milk, and fruits.",
                date = tomorrow,
                time = "10:00 AM",
                dueTimestamp = parseDateAndTimeToMillis(tomorrow, "10:00 AM"),
                isCompleted = false,
                hasReminder = true,
                reminderMinutesBefore = 15,
                category = "Personal"
            )
        )

        for (task in initialTasks) {
            repository.insertTask(task)
        }
    }
}
