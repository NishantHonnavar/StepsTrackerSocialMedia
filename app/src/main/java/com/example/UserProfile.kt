package com.example

import org.json.JSONArray
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
    val hasClaimedGoalBonus: Boolean = false,
    val currentStreak: Int = 1,
    val bestStreak: Int = 1,
    val goldenHourSteps: Int = 0,
    val unlockedAchievementIds: Set<String> = emptySet(),
    val lifetimeSteps: Long = 0L,
    val lifetimeXp: Long = 0L,
    val lastCelebratedRankLevel: Int = 1
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

    // Experience Points (XP) & Rank Progression
    val rank: UserRank get() = UserRank.fromXp(lifetimeXp)
    val rankProgress: Float get() = rank.progressFraction(lifetimeXp)
    val xpNeededForNextRank: Long get() = rank.xpNeededForNextLevel(lifetimeXp)

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
            put("currentStreak", currentStreak)
            put("bestStreak", bestStreak)
            put("goldenHourSteps", goldenHourSteps)
            put("lifetimeSteps", lifetimeSteps)
            put("lifetimeXp", lifetimeXp)
            put("lastCelebratedRankLevel", lastCelebratedRankLevel)
            val jsonArray = JSONArray()
            unlockedAchievementIds.forEach { jsonArray.put(it) }
            put("unlockedAchievementIds", jsonArray)
        }
    }

    companion object {
        fun fromJsonObject(json: JSONObject): UserProfile {
            val achievementsSet = mutableSetOf<String>()
            val achievementsArray = json.optJSONArray("unlockedAchievementIds")
            if (achievementsArray != null) {
                for (i in 0 until achievementsArray.length()) {
                    achievementsSet.add(achievementsArray.getString(i))
                }
            }

            val daily = json.optInt("dailySteps", 0)
            val lifetimeStepsFallback = json.optLong("lifetimeSteps", daily.toLong())
            val lifetimeXpFallback = json.optLong("lifetimeXp", lifetimeStepsFallback)

            return UserProfile(
                id = json.optString("id", "default"),
                name = json.optString("name", "Personal"),
                emoji = json.optString("emoji", "🏃"),
                colorHex = json.optLong("colorHex", 0xFF00E5FF),
                dailySteps = daily,
                bankedSeconds = json.optInt("bankedSeconds", 120),
                stepsPerMinute = json.optInt("stepsPerMinute", 100),
                totalEarnedSeconds = json.optInt("totalEarnedSeconds", 120),
                totalSpentSeconds = json.optInt("totalSpentSeconds", 0),
                targetApp = json.optString("targetApp", "com.instagram.android"),
                dailyStepGoal = json.optInt("dailyStepGoal", 6000),
                bonusMinutes = json.optInt("bonusMinutes", 15),
                hasClaimedGoalBonus = json.optBoolean("hasClaimedGoalBonus", false),
                currentStreak = json.optInt("currentStreak", 1),
                bestStreak = json.optInt("bestStreak", 1),
                goldenHourSteps = json.optInt("goldenHourSteps", 0),
                unlockedAchievementIds = achievementsSet,
                lifetimeSteps = lifetimeStepsFallback,
                lifetimeXp = lifetimeXpFallback,
                lastCelebratedRankLevel = json.optInt("lastCelebratedRankLevel", 1)
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
                    bankedSeconds = 120,
                    stepsPerMinute = 100,
                    totalEarnedSeconds = 120,
                    dailyStepGoal = 6000,
                    bonusMinutes = 15,
                    currentStreak = 2,
                    bestStreak = 4,
                    lifetimeSteps = 8450L,
                    lifetimeXp = 8450L,
                    lastCelebratedRankLevel = 4
                ),
                UserProfile(
                    id = "profile_detox",
                    name = "Strict Detox",
                    emoji = "⚡",
                    colorHex = 0xFFFF4D4F,
                    dailySteps = 0,
                    bankedSeconds = 0,
                    stepsPerMinute = 150,
                    totalEarnedSeconds = 0,
                    dailyStepGoal = 8000,
                    bonusMinutes = 20,
                    currentStreak = 1,
                    bestStreak = 3,
                    lifetimeSteps = 3800L,
                    lifetimeXp = 3800L,
                    lastCelebratedRankLevel = 3
                ),
                UserProfile(
                    id = "profile_chill",
                    name = "Light Walker",
                    emoji = "🌿",
                    colorHex = 0xFF10B981,
                    dailySteps = 0,
                    bankedSeconds = 300,
                    stepsPerMinute = 80,
                    totalEarnedSeconds = 300,
                    dailyStepGoal = 4000,
                    bonusMinutes = 10,
                    currentStreak = 5,
                    bestStreak = 7,
                    unlockedAchievementIds = setOf("first_step", "streak_3"),
                    lifetimeSteps = 650L,
                    lifetimeXp = 650L,
                    lastCelebratedRankLevel = 1
                )
            )
        }
    }
}
