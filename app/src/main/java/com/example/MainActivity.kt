package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.TaskViewModel
import com.example.ui.components.AddTaskBottomSheet
import com.example.ui.components.NotificationSettingsDialog
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.screens.UpcomingScreen
import com.example.ui.theme.DailyScheduleTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DailyScheduleTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TaskViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val todayDateStr by viewModel.todayDateStr.collectAsStateWithLifecycle()
    val todayTasks by viewModel.todayTasks.collectAsStateWithLifecycle()
    val allTodayTasks by viewModel.allTodayTasks.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val selectedCalendarDate by viewModel.selectedCalendarDate.collectAsStateWithLifecycle()
    val selectedDateTasks by viewModel.selectedDateTasks.collectAsStateWithLifecycle()
    val upcomingTasks by viewModel.upcomingTasks.collectAsStateWithLifecycle()
    val historyTasks by viewModel.historyTasks.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val todayCategoryFilter by viewModel.todayCategoryFilter.collectAsStateWithLifecycle()
    val historyCategoryFilter by viewModel.historyCategoryFilter.collectAsStateWithLifecycle()

    val showTaskSheet by viewModel.showTaskSheet.collectAsStateWithLifecycle()
    val editingTask by viewModel.editingTask.collectAsStateWithLifecycle()
    val isPendingSummaryEnabled by viewModel.isPendingSummaryEnabled.collectAsStateWithLifecycle()

    var showNotificationSettingsDialog by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LifecycleResumeEffect(Unit) {
        viewModel.refreshToday()
        onPauseOrDispose { }
    }

    // Permission launcher for Notifications on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                scope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.notifications_enabled))
                }
            }
        }
    )

    // Request notification permission on first launch if needed
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { viewModel.setTab(0) },
                    icon = { Icon(Icons.Default.Checklist, contentDescription = stringResource(R.string.today)) },
                    label = { Text(stringResource(R.string.today)) },
                    modifier = Modifier.testTag("tab_today")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { viewModel.setTab(1) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = stringResource(R.string.upcoming)) },
                    label = { Text(stringResource(R.string.upcoming)) },
                    modifier = Modifier.testTag("tab_upcoming")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { viewModel.setTab(2) },
                    icon = { Icon(Icons.Default.History, contentDescription = stringResource(R.string.history)) },
                    label = { Text(stringResource(R.string.history)) },
                    modifier = Modifier.testTag("tab_history")
                )
            }
        },
        floatingActionButton = {
            if (currentTab != 2) {
                FloatingActionButton(
                    onClick = {
                        val targetDate = if (currentTab == 1) selectedCalendarDate else todayDateStr
                        viewModel.openAddTaskSheet(targetDate)
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_add_task")
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_task))
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> TodayScreen(
                    todayTasks = todayTasks,
                    allTodayTasks = allTodayTasks,
                    selectedCategory = todayCategoryFilter,
                    onCategorySelected = { viewModel.setTodayCategoryFilter(it) },
                    onToggleCompletion = { viewModel.toggleTaskCompletion(it) },
                    onEditTask = { viewModel.openEditTaskSheet(it) },
                    onDeleteTask = { viewModel.deleteTask(it) },
                    onAddTask = { viewModel.openAddTaskSheet(todayDateStr) },
                    onTestNotification = { viewModel.testNotification() },
                    onOpenNotificationSettings = { showNotificationSettingsDialog = true }
                )
                1 -> UpcomingScreen(
                    selectedDateStr = selectedCalendarDate,
                    allTasks = allTasks,
                    selectedDateTasks = selectedDateTasks,
                    upcomingTasks = upcomingTasks,
                    onDateSelected = { viewModel.setSelectedCalendarDate(it) },
                    onToggleCompletion = { viewModel.toggleTaskCompletion(it) },
                    onEditTask = { viewModel.openEditTaskSheet(it) },
                    onDeleteTask = { viewModel.deleteTask(it) },
                    onAddTaskForSelectedDate = { date -> viewModel.openAddTaskSheet(date) }
                )
                2 -> HistoryScreen(
                    allTasks = allTasks,
                    historyTasks = historyTasks,
                    searchQuery = searchQuery,
                    categoryFilter = historyCategoryFilter,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onCategoryFilterChange = { viewModel.setHistoryCategoryFilter(it) },
                    onToggleCompletion = { viewModel.toggleTaskCompletion(it) },
                    onEditTask = { viewModel.openEditTaskSheet(it) },
                    onDeleteTask = { viewModel.deleteTask(it) },
                    onClearCompleted = { viewModel.clearCompletedTasks() }
                )
            }
        }

        if (showTaskSheet) {
            AddTaskBottomSheet(
                sheetState = sheetState,
                editingTask = editingTask,
                defaultDateStr = selectedCalendarDate,
                onDismiss = { viewModel.closeTaskSheet() },
                onSave = { id, title, notes, date, time, hasReminder, reminderMins, category ->
                    viewModel.saveTask(
                        id = id,
                        title = title,
                        notes = notes,
                        date = date,
                        time = time,
                        hasReminder = hasReminder,
                        reminderMinutesBefore = reminderMins,
                        category = category
                    )
                }
            )
        }

        if (showNotificationSettingsDialog) {
            NotificationSettingsDialog(
                isPendingSummaryEnabled = isPendingSummaryEnabled,
                onTogglePendingSummary = { enabled ->
                    viewModel.setPendingSummaryEnabled(enabled)
                },
                onTestNotification = {
                    viewModel.testNotification()
                },
                onDismiss = {
                    showNotificationSettingsDialog = false
                }
            )
        }
    }
}
