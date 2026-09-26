package com.example.util

import java.text.SimpleDateFormat
import java.util.Locale

object TimeUtils {

    /**
     * Parses a time string (12-hour or 24-hour format) into (hour12 [1..12], minute [0..59], amPm ["AM"/"PM"]).
     */
    fun parse12HourTimeState(timeStr: String): Triple<Int, Int, String> {
        val trimmed = timeStr.trim().uppercase(Locale.US)
        if (trimmed.contains("AM") || trimmed.contains("PM")) {
            val isPm = trimmed.contains("PM")
            val clean = trimmed.replace("AM", "").replace("PM", "").trim()
            val parts = clean.split(":")
            val rawHour = parts.getOrNull(0)?.toIntOrNull() ?: 9
            val min = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val hour12 = when {
                rawHour == 0 -> 12
                rawHour > 12 -> rawHour - 12
                else -> rawHour
            }
            val amPm = if (isPm) "PM" else "AM"
            return Triple(hour12.coerceIn(1, 12), min.coerceIn(0, 59), amPm)
        } else {
            // 24-hour format string e.g. "14:30" or "09:00"
            val parts = trimmed.split(":")
            val rawHour = parts.getOrNull(0)?.toIntOrNull() ?: 9
            val min = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val isPm = rawHour >= 12
            val hour12 = when {
                rawHour == 0 -> 12
                rawHour > 12 -> rawHour - 12
                else -> rawHour
            }
            val amPm = if (isPm) "PM" else "AM"
            return Triple(hour12.coerceIn(1, 12), min.coerceIn(0, 59), amPm)
        }
    }

    /**
     * Formats 12-hour components into standard "hh:mm AM/PM" string.
     */
    fun format12HourTime(hour12: Int, minute: Int, amPm: String): String {
        val h = hour12.coerceIn(1, 12)
        val m = minute.coerceIn(0, 59)
        val period = if (amPm.uppercase(Locale.US) == "PM") "PM" else "AM"
        return String.format(Locale.US, "%02d:%02d %s", h, m, period)
    }

    /**
     * Ensures any time string (e.g. legacy "14:30") is converted to 12-hour AM/PM format.
     */
    fun ensure12HourFormat(timeStr: String): String {
        val (h, m, period) = parse12HourTimeState(timeStr)
        return format12HourTime(h, m, period)
    }

    /**
     * Converts date string ("yyyy-MM-dd") and time string ("02:30 PM" or "14:30") to epoch millis.
     */
    fun parseDateAndTimeToMillis(dateStr: String, timeStr: String): Long? {
        val trimmedTime = timeStr.trim()
        val tryFormats = listOf(
            SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US),
            SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.US),
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US),
            SimpleDateFormat("yyyy-MM-dd H:mm", Locale.US)
        )
        for (fmt in tryFormats) {
            fmt.isLenient = false
            try {
                val parsed = fmt.parse("$dateStr $trimmedTime")
                if (parsed != null) return parsed.time
            } catch (_: Exception) {}
        }
        return null
    }
}
