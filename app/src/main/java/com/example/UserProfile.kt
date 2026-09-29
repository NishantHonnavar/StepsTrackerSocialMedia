package com.example

import org.json.JSONObject

data class UserProfile(
    val id: String,
    val name: String,
    val emoji: String = "🏃",
    val colorHex: Long = 0xFF00E5FF,
    val dailySteps: Int = 0,
    val bankedSeconds: Int = 120, // default 2 minutes
    val stepsPerMinute: Int = 100, // default 100 steps = 1 minute
    val totalEarnedSeconds: Int = 120,
    val totalSpentSeconds: Int = 0,
    val targetApp: String = "com.instagram.android",
    val dailyStepGoal: Int = 6000, // Daily step goal (e.g. 6,000 steps)
    val bonusMinutes: Int = 15, // Bonus minutes granted on hitting the goal
    val hasClaimedGoalBonus: Boolean = false
) {
    val bankedMinutes: Int get() = bankedSeconds / 60
    val bankedSecondsRemainder: Int get() = bankedSeconds % 60
    val stepsToNextMinute: Int get() {
        val remainder = dailySteps % stepsPerMinute
        return if (remainder == 0 && dailySteps > 0) stepsPerMinute else stepsPerMinute - remainder
    }
    val progressToNextMinute: Float get() {
        val remainder = dailySteps % stepsPerMinute
        return (remainder.toFloat() / stepsPerMinute.toFloat()).coerceIn(0f, 1f)
    }
    val isLocked: Boolean get() = bankedSeconds <= 0

    val isGoalReached: Boolean get() = dailySteps >= dailyStepGoal
    val goalProgress: Float get() = if (dailyStepGoal > 0) {
        (dailySteps.toFloat() / dailyStepGoal.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val goalPercentage: Int get() = (goalProgress * 100).toInt()
    val stepsRemainingToGoal: Int get() = (dailyStepGoal - dailySteps).coerceAtLeast(0)

    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("emoji", emoji)
            put("colorHex", colorHex)
            put("dailySteps", dailySteps)
            put("bankedSeconds", bankedSeconds)
            put("stepsPerMinute", stepsPerMinute)
            put("totalEarnedSeconds", totalEarnedSeconds)
            put("totalSpentSeconds", totalSpentSeconds)
            put("targetApp", targetApp)
            put("dailyStepGoal", dailyStepGoal)
            put("bonusMinutes", bonusMinutes)
            put("hasClaimedGoalBonus", hasClaimedGoalBonus)
        }
    }

    companion object {
        fun fromJsonObject(json: JSONObject): UserProfile {
            return UserProfile(
                id = json.optString("id", "default"),
                name = json.optString("name", "Personal"),
                emoji = json.optString("emoji", "🏃"),
                colorHex = json.optLong("colorHex", 0xFF00E5FF),
                dailySteps = json.optInt("dailySteps", 0),
                bankedSeconds = json.optInt("bankedSeconds", 120),
                stepsPerMinute = json.optInt("stepsPerMinute", 100),
                totalEarnedSeconds = json.optInt("totalEarnedSeconds", 120),
                totalSpentSeconds = json.optInt("totalSpentSeconds", 0),
                targetApp = json.optString("targetApp", "com.instagram.android"),
                dailyStepGoal = json.optInt("dailyStepGoal", 6000),
                bonusMinutes = json.optInt("bonusMinutes", 15),
                hasClaimedGoalBonus = json.optBoolean("hasClaimedGoalBonus", false)
            )
        }

        fun createDefaultProfiles(): List<UserProfile> {
            return listOf(
                UserProfile(
                    id = "profile_personal",
                    name = "Personal (Balanced)",
                    emoji = "🏃",
                    colorHex = 0xFF00E5FF,
                    dailySteps = 0,
                    bankedSeconds = 120, // 2 mins banked
                    stepsPerMinute = 100, // 100 steps = 1 min
                    totalEarnedSeconds = 120,
                    dailyStepGoal = 6000,
                    bonusMinutes = 15
                ),
                UserProfile(
                    id = "profile_detox",
                    name = "Strict Detox",
                    emoji = "⚡",
                    colorHex = 0xFFFF4D4F,
                    dailySteps = 0,
                    bankedSeconds = 0, // starts locked
                    stepsPerMinute = 150, // 150 steps = 1 min
                    totalEarnedSeconds = 0,
                    dailyStepGoal = 8000,
                    bonusMinutes = 20
                ),
                UserProfile(
                    id = "profile_chill",
                    name = "Light Walker",
                    emoji = "🌿",
                    colorHex = 0xFF10B981,
                    dailySteps = 0,
                    bankedSeconds = 300, // 5 mins banked
                    stepsPerMinute = 80, // 80 steps = 1 min
                    totalEarnedSeconds = 300,
                    dailyStepGoal = 4000,
                    bonusMinutes = 10
                )
            )
        }
    }
}
