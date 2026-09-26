package com.example

import com.example.util.TimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TimeUtilsTest {
    @Test
    fun parsesValidTwelveHourTime() {
        assertNotNull(TimeUtils.parseDateAndTimeToMillis("2026-09-26", "09:30 AM"))
    }

    @Test
    fun rejectsInvalidDateAndTime() {
        assertNull(TimeUtils.parseDateAndTimeToMillis("2026-02-30", "09:30 AM"))
        assertNull(TimeUtils.parseDateAndTimeToMillis("not-a-date", "not-a-time"))
    }

    @Test
    fun normalizesLegacyTwentyFourHourTime() {
        assertEquals("02:30 PM", TimeUtils.ensure12HourFormat("14:30"))
    }
}
