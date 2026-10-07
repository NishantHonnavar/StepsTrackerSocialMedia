package com.example

import org.json.JSONArray
import org.json.JSONObject

data class DayTrend(
    val dayLabel: String,
    val dateKey: String,
    val steps: Int,
    val minutesEarned: Int,
    val minutesUsed: Int,
    val isToday: Boolean = false
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("dayLabel", dayLabel)
        put("dateKey", dateKey)
        put("steps", steps)
        put("minutesEarned", minutesEarned)
        put("minutesUsed", minutesUsed)
        put("isToday", isToday)
    }

    companion object {
        fun fromJsonObject(json: JSONObject): DayTrend = DayTrend(
            dayLabel = json.optString("dayLabel", "Mon"),
            dateKey = json.optString("dateKey", ""),
            steps = json.optInt("steps", 0),
            minutesEarned = json.optInt("minutesEarned", 0),
            minutesUsed = json.optInt("minutesUsed", 0),
            isToday = json.optBoolean("isToday", false)
        )

        fun createSampleWeek(todaySteps: Int, todayEarnedMinutes: Int): List<DayTrend> {
            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val baseSteps = listOf(4200, 6800, 5100, 7400, 6200, 8900, todaySteps.coerceAtLeast(3500))
            val baseEarned = listOf(42, 68, 51, 74, 62, 89, todayEarnedMinutes.coerceAtLeast(35))
            val baseUsed = listOf(30, 45, 40, 50, 45, 60, 20)

            return days.mapIndexed { index, day ->
                val isSun = index == 6
                DayTrend(
                    dayLabel = day,
                    dateKey = "2026-W39-$index",
                    steps = if (isSun) todaySteps else baseSteps[index],
                    minutesEarned = if (isSun) todayEarnedMinutes else baseEarned[index],
                    minutesUsed = baseUsed[index],
                    isToday = isSun
                )
            }
        }
    }
}
