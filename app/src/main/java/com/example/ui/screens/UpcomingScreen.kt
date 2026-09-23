package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.TaskEntity
import com.example.ui.components.SimpleCalendarView
import com.example.ui.components.TaskItemCard
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun UpcomingScreen(
    selectedDateStr: String,
    allTasks: List<TaskEntity>,
    selectedDateTasks: List<TaskEntity>,
    upcomingTasks: List<TaskEntity>,
    onDateSelected: (String) -> Unit,
    onToggleCompletion: (TaskEntity) -> Unit,
    onEditTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onAddTaskForSelectedDate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayDateFormatted = remember(selectedDateStr) {
        try {
            val formatInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatOutput = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
            val parsed = formatInput.parse(selectedDateStr)
            if (parsed != null) formatOutput.format(parsed) else selectedDateStr
        } catch (_: Exception) {
            selectedDateStr
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title Header
        item {
            Text(
                text = "Calendar & Upcoming",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Calendar Component
        item {
            SimpleCalendarView(
                selectedDateStr = selectedDateStr,
                allTasks = allTasks,
                onDateSelected = onDateSelected
            )
        }

        // Header for selected date tasks
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Schedule for Selected Date",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = displayDateFormatted,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                OutlinedButton(
                    onClick = { onAddTaskForSelectedDate(selectedDateStr) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_add_for_date")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Task")
                }
            }
        }

        // List of tasks on selected date
        if (selectedDateTasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No tasks scheduled for this date.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(selectedDateTasks, key = { it.id }) { task ->
                TaskItemCard(
                    task = task,
                    onToggleCompletion = onToggleCompletion,
                    onEditTask = onEditTask,
                    onDeleteTask = onDeleteTask
                )
            }
        }

        // Upcoming Future Tasks Header
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "All Future Upcoming Tasks",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (upcomingTasks.isEmpty()) {
            item {
                Text(
                    text = "No future upcoming tasks scheduled.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(upcomingTasks, key = { "upcoming_${it.id}" }) { task ->
                TaskItemCard(
                    task = task,
                    onToggleCompletion = onToggleCompletion,
                    onEditTask = onEditTask,
                    onDeleteTask = onDeleteTask
                )
            }
        }
    }
}
