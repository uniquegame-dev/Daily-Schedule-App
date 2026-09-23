package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import com.example.util.TimeUtils
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.TaskEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTaskBottomSheet(
    sheetState: SheetState,
    editingTask: TaskEntity?,
    defaultDateStr: String,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        title: String,
        notes: String,
        date: String,
        time: String,
        hasReminder: Boolean,
        reminderMinutesBefore: Int,
        category: String
    ) -> Unit
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    var title by remember(editingTask) { mutableStateOf(editingTask?.title ?: "") }
    var notes by remember(editingTask) { mutableStateOf(editingTask?.notes ?: "") }
    var dateStr by remember(editingTask, defaultDateStr) {
        mutableStateOf(editingTask?.date ?: defaultDateStr)
    }

    // 12-Hour Time Picker State
    val parsedTimeState = remember(editingTask) {
        TimeUtils.parse12HourTimeState(editingTask?.time ?: "09:00 AM")
    }
    var hour12 by remember(editingTask) { mutableIntStateOf(parsedTimeState.first) }
    var selectedMinute by remember(editingTask) { mutableIntStateOf(parsedTimeState.second) }
    var amPm by remember(editingTask) { mutableStateOf(parsedTimeState.third) }

    var timeStr by remember(hour12, selectedMinute, amPm) {
        mutableStateOf(TimeUtils.format12HourTime(hour12, selectedMinute, amPm))
    }

    var hasReminder by remember(editingTask) { mutableStateOf(editingTask?.hasReminder ?: true) }
    var reminderMinutesBefore by remember(editingTask) {
        mutableIntStateOf(editingTask?.reminderMinutesBefore ?: 15)
    }
    var category by remember(editingTask) { mutableStateOf(editingTask?.category ?: "General") }

    var titleError by remember { mutableStateOf(false) }

    // Date Picker Dialog trigger
    val openDatePicker = {
        val cal = Calendar.getInstance()
        try {
            val parsed = dateFormatter.parse(dateStr)
            if (parsed != null) cal.time = parsed
        } catch (_: Exception) {}

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                dateStr = dateFormatter.format(selectedCal.time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Time Picker Dialog trigger (opens system 12-hour clock view)
    val openTimePicker = {
        val initial24Hour = when {
            amPm == "PM" && hour12 < 12 -> hour12 + 12
            amPm == "AM" && hour12 == 12 -> 0
            else -> hour12
        }

        TimePickerDialog(
            context,
            { _, hourOfDay, minuteVal ->
                if (hourOfDay >= 12) {
                    amPm = "PM"
                    hour12 = if (hourOfDay > 12) hourOfDay - 12 else 12
                } else {
                    amPm = "AM"
                    hour12 = if (hourOfDay == 0) 12 else hourOfDay
                }
                selectedMinute = minuteVal
                timeStr = TimeUtils.format12HourTime(hour12, selectedMinute, amPm)
            },
            initial24Hour,
            selectedMinute,
            false // 12-Hour View
        ).show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.testTag("add_task_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (editingTask != null) "Edit Task" else "Add New Task",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title input
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (it.isNotBlank()) titleError = false
                },
                label = { Text("Task Title *") },
                isError = titleError,
                supportingText = if (titleError) {
                    { Text("Title is required") }
                } else null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_task_title"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Date & Time Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date picker trigger field
                Surface(
                    onClick = openDatePicker,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_select_date"),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Date",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Date",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = dateStr,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Selected Time Summary Display & Clock Picker Dialog Trigger
                Surface(
                    onClick = openTimePicker,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_select_time"),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Time",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Time (12-Hr)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 12-Hour Interactive Time Picker Card (AM/PM Toggle, Hour, Minute)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_12hr_time_picker")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Select Time",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // AM / PM Segmented Toggle Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = amPm == "AM",
                                onClick = {
                                    amPm = "AM"
                                    timeStr = TimeUtils.format12HourTime(hour12, selectedMinute, "AM")
                                },
                                label = {
                                    Text("AM", fontWeight = FontWeight.Bold)
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("chip_ampm_am")
                            )

                            FilterChip(
                                selected = amPm == "PM",
                                onClick = {
                                    amPm = "PM"
                                    timeStr = TimeUtils.format12HourTime(hour12, selectedMinute, "PM")
                                },
                                label = {
                                    Text("PM", fontWeight = FontWeight.Bold)
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("chip_ampm_pm")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Hour Row (1 to 12)
                    Text(
                        text = "Hour (1 - 12):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items((1..12).toList()) { h ->
                            FilterChip(
                                selected = hour12 == h,
                                onClick = {
                                    hour12 = h
                                    timeStr = TimeUtils.format12HourTime(h, selectedMinute, amPm)
                                },
                                label = { Text(String.format(Locale.US, "%02d", h)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("chip_hour_$h")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Minute Row (:00, :15, :30, :45)
                    Text(
                        text = "Minute:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val presetMinutes = listOf(0, 15, 30, 45)
                    val displayMinutes = if (presetMinutes.contains(selectedMinute)) {
                        presetMinutes
                    } else {
                        (presetMinutes + selectedMinute).sorted()
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        displayMinutes.forEach { m ->
                            FilterChip(
                                selected = selectedMinute == m,
                                onClick = {
                                    selectedMinute = m
                                    timeStr = TimeUtils.format12HourTime(hour12, m, amPm)
                                },
                                label = { Text(String.format(Locale.US, ":%02d", m)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("chip_minute_$m")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selection
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("General", "Work", "Personal", "Health", "Finance", "Study").forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat) },
                        modifier = Modifier.testTag("chip_category_$cat")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reminder Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { hasReminder = !hasReminder },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = "Reminder",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Set Notification Reminder",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Get an alert on your device",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Switch(
                    checked = hasReminder,
                    onCheckedChange = { hasReminder = it },
                    modifier = Modifier.testTag("switch_reminder")
                )
            }

            if (hasReminder) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Alert Offset",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val offsets = listOf(
                        0 to "At task time",
                        5 to "5 minutes before",
                        15 to "15 minutes before"
                    )
                    offsets.forEach { (mins, label) ->
                        FilterChip(
                            selected = reminderMinutesBefore == mins,
                            onClick = { reminderMinutesBefore = mins },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("chip_reminder_$mins")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes area
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes & Description (Optional)") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_task_notes"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("btn_cancel_task")
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        if (title.isBlank()) {
                            titleError = true
                        } else {
                            onSave(
                                editingTask?.id ?: 0L,
                                title,
                                notes,
                                dateStr,
                                timeStr,
                                hasReminder,
                                reminderMinutesBefore,
                                category
                            )
                        }
                    },
                    modifier = Modifier.testTag("btn_save_task"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (editingTask != null) "Update Task" else "Save Task")
                }
            }
        }
    }
}
