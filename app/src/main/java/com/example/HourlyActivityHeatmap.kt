package com.example

import java.util.Calendar

data class HourlyActivitySlot(
    val hour: Int,
    val hourLabel: String,
    val timeRangeLabel: String,
    val period: TimeOfDayPeriod,
    val averageSteps: Int,
    val relativeIntensity: Float, // 0.0f to 1.0f
    val conversionRate: Int,
    val minutesEarned: Int,
    val isPeakWindow: Boolean = false,
    val isCurrentHour: Boolean = false
) {
    val periodBadgeName: String
        get() = when (period) {
            TimeOfDayPeriod.GOLDEN_HOUR -> "🌅 Golden Hour (2x)"
            TimeOfDayPeriod.STANDARD_DAY -> "☀️ Standard Day"
            TimeOfDayPeriod.NIGHT_SURGE -> "🌙 Night Surge"
        }

    val earningPotentialDescription: String
        get() = when {
            period == TimeOfDayPeriod.GOLDEN_HOUR && relativeIntensity > 0.6f ->
                "⭐ MAXIMUM YIELD: High activity meets 2x Golden Hour rate (25 steps = 1 min)!"
            period == TimeOfDayPeriod.GOLDEN_HOUR ->
                "🌟 2x BONUS WINDOW: Earn screen time twice as fast (25 steps = 1 min)."
            relativeIntensity > 0.7f ->
                "🔥 HIGH ACTIVITY: Prime window to earn substantial screen time."
            period == TimeOfDayPeriod.NIGHT_SURGE ->
                "💤 SLEEP PRIORITY: Night surge active (150 steps = 1 min). Wind down for sleep."
            else ->
                "🚶 STEADY STRIDE: 50 steps earns 1 minute of screen time."
        }
}

data class HourlyActivityHeatmap(
    val slots: List<HourlyActivitySlot>,
    val peakSlots: List<HourlyActivitySlot>,
    val mostActiveHour: HourlyActivitySlot,
    val bestEarningWindow: HourlyActivitySlot,
    val totalDailyStepsEstimate: Int
) {
    companion object {
        fun generateForProfile(
            profile: UserProfile,
            currentHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        ): HourlyActivityHeatmap {
            // Profile specific hourly weight distribution (24 hours: 0..23)
            val hourlyWeights = when (profile.id) {
                "profile_detox" -> listOf(
                    // 0..5 Night
                    0.02f, 0.01f, 0.01f, 0.01f, 0.02f, 0.08f,
                    // 6..11 Morning (peak at 10-11)
                    0.25f, 0.55f, 0.40f, 0.65f, 0.90f, 0.75f,
                    // 12..17 Afternoon (peak at 16-17)
                    0.50f, 0.45f, 0.40f, 0.60f, 0.95f, 0.85f,
                    // 18..23 Evening & Night
                    0.60f, 0.40f, 0.25f, 0.15f, 0.05f, 0.02f
                )
                "profile_chill" -> listOf(
                    // 0..5 Night
                    0.01f, 0.01f, 0.01f, 0.01f, 0.01f, 0.03f,
                    // 6..11 Gentle Morning
                    0.15f, 0.35f, 0.45f, 0.50f, 0.65f, 0.80f,
                    // 12..17 Midday Strolls
                    0.85f, 0.80f, 0.75f, 0.60f, 0.55f, 0.65f,
                    // 18..23 Evening
                    0.70f, 0.50f, 0.30f, 0.15f, 0.08f, 0.03f
                )
                else -> listOf(
                    // Personal (Balanced): Peaks at Golden Hour (7-8 AM) & Evening (18-19)
                    0.02f, 0.01f, 0.01f, 0.01f, 0.02f, 0.05f,
                    // 6..8 Golden Hour Surge
                    0.45f, 0.95f, 0.70f, 0.40f, 0.35f, 0.50f,
                    // 12..14 Lunch Walks
                    0.65f, 0.55f, 0.35f, 0.40f, 0.55f, 0.75f,
                    // 18..20 Evening Workout Surge
                    0.90f, 0.80f, 0.50f, 0.25f, 0.10f, 0.03f
                )
            }

            val scaleMultiplier = maxOf(profile.dailyStepGoal, profile.dailySteps, 6000) / 10.0f

            val rawSlots = (0..23).map { hour ->
                val weight = hourlyWeights[hour]
                val period = TimeOfDayPeriod.getPeriodForHour(hour)
                val rate = period.stepsPerMinute

                val isCur = hour == currentHour
                // If today has steps and it's current hour, boost slightly
                val currentHourBonus = if (isCur && profile.dailySteps > 0) 150 else 0
                val steps = (weight * scaleMultiplier * 1.8f).toInt() + currentHourBonus
                val minutesEarned = if (rate > 0) steps / rate else 0

                Triple(hour, steps, minutesEarned)
            }

            val maxSteps = rawSlots.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

            // Sort by steps to find top 3 peak windows
            val top3Hours = rawSlots.sortedByDescending { it.second }.take(3).map { it.first }.toSet()

            val slots = rawSlots.map { (hour, steps, minutesEarned) ->
                val period = TimeOfDayPeriod.getPeriodForHour(hour)
                val rate = period.stepsPerMinute
                val intensity = (steps.toFloat() / maxSteps.toFloat()).coerceIn(0.05f, 1.0f)
                val hourStr = String.format("%02d:00", hour)
                val nextHourStr = String.format("%02d:00", (hour + 1) % 24)

                HourlyActivitySlot(
                    hour = hour,
                    hourLabel = hourStr,
                    timeRangeLabel = "$hourStr – $nextHourStr",
                    period = period,
                    averageSteps = steps,
                    relativeIntensity = intensity,
                    conversionRate = rate,
                    minutesEarned = minutesEarned,
                    isPeakWindow = top3Hours.contains(hour),
                    isCurrentHour = hour == currentHour
                )
            }

            val peakSlots = slots.filter { it.isPeakWindow }.sortedByDescending { it.averageSteps }
            val mostActive = slots.maxByOrNull { it.averageSteps } ?: slots.first()
            val bestYield = slots.maxByOrNull { it.minutesEarned } ?: slots.first()
            val totalEstimate = slots.sumOf { it.averageSteps }

            return HourlyActivityHeatmap(
                slots = slots,
                peakSlots = peakSlots,
                mostActiveHour = mostActive,
                bestEarningWindow = bestYield,
                totalDailyStepsEstimate = totalEstimate
            )
        }
    }
}
