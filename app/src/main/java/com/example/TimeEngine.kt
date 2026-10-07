package com.example

import java.util.Calendar

enum class TimeOfDayPeriod(
    val id: String,
    val title: String,
    val stepsPerMinute: Int,
    val badgeLabel: String,
    val timeRangeDescription: String,
    val badgeTextColor: Long,
    val badgeBgColor: Long,
    val glowColor: Long
) {
    GOLDEN_HOUR(
        id = "golden_hour",
        title = "Golden Hour",
        stepsPerMinute = 50,
        badgeLabel = "Golden Hour (2x Bonus) • 50 steps = 1 min",
        timeRangeDescription = "06:00 – 08:00",
        badgeTextColor = 0xFFFFAA00,
        badgeBgColor = 0x33FFAA00,
        glowColor = 0x66FFAA00
    ),
    STANDARD_DAY(
        id = "standard_day",
        title = "Standard Active Rate",
        stepsPerMinute = 100,
        badgeLabel = "Standard Active Rate • 100 steps = 1 min",
        timeRangeDescription = "08:00 – 22:00",
        badgeTextColor = 0xFF00E5FF,
        badgeBgColor = 0x2600E5FF,
        glowColor = 0x3300E5FF
    ),
    NIGHT_SURGE(
        id = "night_surge",
        title = "Sleep Protection Surge",
        stepsPerMinute = 250,
        badgeLabel = "Sleep Protection Surge • 250 steps = 1 min",
        timeRangeDescription = "22:00 – 06:00",
        badgeTextColor = 0xFF7C4DFF,
        badgeBgColor = 0x337C4DFF,
        glowColor = 0x4D7C4DFF
    );

    companion object {
        fun getPeriodForHour(hour: Int): TimeOfDayPeriod {
            return when (hour) {
                in 6..7 -> GOLDEN_HOUR // 06:00 to 07:59
                in 8..21 -> STANDARD_DAY // 08:00 to 21:59
                else -> NIGHT_SURGE // 22:00 to 05:59
            }
        }

        fun getCurrentPeriod(calendar: Calendar = Calendar.getInstance()): TimeOfDayPeriod {
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            return getPeriodForHour(hour)
        }
    }
}

data class ActiveWalkSession(
    val isActive: Boolean = false,
    val sessionSteps: Int = 0,
    val sessionMinutesEarned: Int = 0,
    val durationSeconds: Int = 0,
    val lastMilestoneAnnounced: Int = 0
)
