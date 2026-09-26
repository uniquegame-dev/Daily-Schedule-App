package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.TaskEntity
import com.example.ui.components.TaskItemCard
import com.example.ui.theme.DailyScheduleTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [35])
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun greeting_screenshot() {
        val sampleTask = TaskEntity(
            id = 1,
            title = "Morning Standup Meeting",
            notes = "Review daily sprint goals",
            date = "2026-09-23",
            time = "09:30",
            dueTimestamp = Long.MAX_VALUE,
            isCompleted = false,
            hasReminder = true,
            category = "Work"
        )

        composeTestRule.setContent {
            DailyScheduleTheme {
                TaskItemCard(
                    task = sampleTask,
                    onToggleCompletion = {},
                    onEditTask = {},
                    onDeleteTask = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}
