package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import com.example.data.TaskEntity
import com.example.R
import com.example.util.TimeUtils
import com.example.util.TaskStatus
import com.example.util.calculateTaskStatus
import com.example.ui.theme.CategoryFinance
import com.example.ui.theme.CategoryGeneral
import com.example.ui.theme.CategoryHealth
import com.example.ui.theme.CategoryPersonal
import com.example.ui.theme.CategoryStudy
import com.example.ui.theme.CategoryWork
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun TaskItemCard(
    task: TaskEntity,
    onToggleCompletion: (TaskEntity) -> Unit,
    onEditTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var nowMillis by remember(task.id, task.dueTimestamp) {
        mutableLongStateOf(System.currentTimeMillis())
    }
    LaunchedEffect(task.id, task.dueTimestamp, task.isCompleted) {
        while (isActive) {
            nowMillis = System.currentTimeMillis()
            delay(30_000L)
        }
    }
    val status = calculateTaskStatus(task, nowMillis)
    val isCompleted = status == TaskStatus.COMPLETED
    val isMissed = status == TaskStatus.MISSED
    val completionStateDescription = stringResource(
        when (status) {
            TaskStatus.COMPLETED -> R.string.completed
            TaskStatus.MISSED -> R.string.missed
            TaskStatus.PENDING -> R.string.not_completed
        }
    )

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.delete_task_question)) },
            text = { Text(stringResource(R.string.delete_task_message, task.title)) },
            confirmButton = {
                Button(onClick = {
                    showDeleteConfirmation = false
                    onDeleteTask(task)
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    val cardBgColor by animateColorAsState(
        targetValue = when (status) {
            TaskStatus.COMPLETED -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
            TaskStatus.MISSED -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            TaskStatus.PENDING -> MaterialTheme.colorScheme.surface
        },
        label = "cardBgColor"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = when (status) {
            TaskStatus.COMPLETED -> 0.78f
            TaskStatus.MISSED -> 0.62f
            TaskStatus.PENDING -> 1f
        },
        label = "contentAlpha"
    )

    val borderColor by animateColorAsState(
        targetValue = when (status) {
            TaskStatus.COMPLETED -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.65f)
            TaskStatus.MISSED -> MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)
            TaskStatus.PENDING -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        },
        label = "borderColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("task_item_${task.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (status == TaskStatus.PENDING) 1.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) MaterialTheme.colorScheme.secondary
                        else Color.Transparent
                    )
                    .border(
                        width = 2.dp,
                        color = when {
                            isCompleted -> MaterialTheme.colorScheme.secondary
                            isMissed -> MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                            else -> MaterialTheme.colorScheme.outline
                        },
                        shape = CircleShape
                    )
                    .semantics {
                        stateDescription = completionStateDescription
                    }
                    .toggleable(
                        value = isCompleted,
                        role = Role.Checkbox,
                        onValueChange = { onToggleCompletion(task) }
                    )
                    .testTag("checkbox_task_${task.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.completed),
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Task details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(contentAlpha)
            ) {
                // Title
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (isMissed) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Time pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isMissed) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = TimeUtils.ensure12HourFormat(task.time),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isMissed) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    // Category Pill
                    CategoryTag(category = task.category)

                    // Reminder Icon
                    if (task.hasReminder && status == TaskStatus.PENDING) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = stringResource(R.string.reminder_set),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Options menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.testTag("menu_task_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.task_actions),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.edit_task)) },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        },
                        onClick = {
                            showMenu = false
                            onEditTask(task)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete_task)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            showMenu = false
                            showDeleteConfirmation = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryTag(category: String) {
    val (bgColor, textColor) = when (category) {
        "Work" -> CategoryWork.copy(alpha = 0.15f) to CategoryWork
        "Personal" -> CategoryPersonal.copy(alpha = 0.15f) to CategoryPersonal
        "Health" -> CategoryHealth.copy(alpha = 0.15f) to CategoryHealth
        "Finance" -> CategoryFinance.copy(alpha = 0.15f) to CategoryFinance
        "Study" -> CategoryStudy.copy(alpha = 0.15f) to CategoryStudy
        else -> CategoryGeneral.copy(alpha = 0.15f) to CategoryGeneral
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = textColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
