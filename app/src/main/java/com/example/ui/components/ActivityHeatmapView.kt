package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskEntity
import com.example.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ActivityHeatmapView(
    allTasks: List<TaskEntity>,
    selectedDateStr: String?,
    onDateSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val monthYearFullFormatter = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val monthYearAbbrevFormatter = remember { SimpleDateFormat("MMM yyyy", Locale.getDefault()) }

    var currentCalendar by remember {
        mutableStateOf(Calendar.getInstance())
    }

    val currentMonthYearFull = monthYearFullFormatter.format(currentCalendar.time)
    val currentMonthYearAbbrev = monthYearAbbrevFormatter.format(currentCalendar.time)

    // Calculate completed task count per date string yyyy-MM-dd
    val taskCompletionCounts = remember(allTasks) {
        val map = mutableMapOf<String, Int>()
        allTasks.forEach { task ->
            if (task.isCompleted) {
                map[task.date] = (map[task.date] ?: 0) + 1
            }
        }
        map
    }

    // Grid details for current month
    val monthCal = (currentCalendar.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val daysInMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = monthCal.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed (Sun=0)

    // Count total completed tasks in this browsed month
    val completedInMonth = remember(allTasks, currentCalendar.timeInMillis) {
        val prefix = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(currentCalendar.time)
        allTasks.count { it.isCompleted && it.date.startsWith(prefix) }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("activity_heatmap_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title & Month Browser
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = stringResource(R.string.activity_heatmap),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.activity_heatmap),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.completed_in_month, completedInMonth, currentMonthYearAbbrev),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Prev / Next Month Controls with abbreviated month label
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            currentCalendar = (currentCalendar.clone() as Calendar).apply {
                                add(Calendar.MONTH, -1)
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("heatmap_prev_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.previous_month)
                        )
                    }

                    Text(
                        text = currentMonthYearAbbrev,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 2.dp),
                        maxLines = 1
                    )

                    IconButton(
                        onClick = {
                            currentCalendar = (currentCalendar.clone() as Calendar).apply {
                                add(Calendar.MONTH, 1)
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("heatmap_next_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.next_month)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Day Labels Row (S, M, T, W, T, F, S)
            Row(modifier = Modifier.fillMaxWidth()) {
                val days = listOf("S", "M", "T", "W", "T", "F", "S")
                days.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Heatmap Grid
            val totalCells = firstDayOfWeek + daysInMonth
            val rows = (totalCells + 6) / 7

            for (r in 0 until rows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (c in 0 until 7) {
                        val cellIndex = r * 7 + c
                        val dayNumber = cellIndex - firstDayOfWeek + 1

                        if (dayNumber in 1..daysInMonth) {
                            val dayCal = (currentCalendar.clone() as Calendar).apply {
                                set(Calendar.DAY_OF_MONTH, dayNumber)
                            }
                            val dateStr = dateFormatter.format(dayCal.time)
                            val completedCount = taskCompletionCounts[dateStr] ?: 0
                            val isSelected = dateStr == selectedDateStr

                            // Color Intensity mapping
                            val squareBgColor = when {
                                completedCount == 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                completedCount == 1 -> Color(0xFF93C5FD) // Light Sky Blue
                                completedCount == 2 -> Color(0xFF3B82F6) // Vibrant Blue
                                else -> Color(0xFF1D4ED8)                // Deep Blue
                            }

                            val textColor = when {
                                completedCount >= 2 -> Color.White
                                completedCount == 1 -> Color(0xFF1E3A8A)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(squareBgColor)
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(
                                                width = 2.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                        } else Modifier
                                    )
                                    .clickable {
                                        if (isSelected) {
                                            onDateSelected(null) // Toggle off
                                        } else {
                                            onDateSelected(dateStr)
                                        }
                                    }
                                    .testTag("heatmap_square_$dateStr"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNumber.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (completedCount > 0 || isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    ),
                                    color = textColor
                                )
                            }
                        } else {
                            // Blank offset cell
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend at bottom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedDateStr != null) {
                    Text(
                        text = stringResource(R.string.selected_heatmap_date, selectedDateStr),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.heatmap_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.less),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )

                    val legendColors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        Color(0xFF93C5FD),
                        Color(0xFF3B82F6),
                        Color(0xFF1D4ED8)
                    )

                    legendColors.forEach { col ->
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(col)
                        )
                    }

                    Text(
                        text = stringResource(R.string.more),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
