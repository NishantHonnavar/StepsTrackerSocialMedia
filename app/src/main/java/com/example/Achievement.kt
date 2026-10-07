package com.example

import org.json.JSONArray
import org.json.JSONObject

enum class BadgeTier(
    val title: String,
    val colorHex: Long,
    val bgHex: Long
) {
    BRONZE("Bronze", 0xFFCD7F32, 0x33CD7F32),
    SILVER("Silver", 0xFFCBD5E1, 0x3394A3B8),
    GOLD("Gold", 0xFFFFD700, 0x33FFD700),
    PLATINUM("Platinum", 0xFFA78BFA, 0x338B5CF6)
}

enum class AchievementCategory(val title: String) {
    STEPS("Steps"),
    STREAK("Streak"),
    VAULT("Vault"),
    SPECIAL("Special")
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val badgeEmoji: String,
    val tier: BadgeTier,
    val category: AchievementCategory,
    val currentProgress: Int,
    val maxProgress: Int,
    val isUnlocked: Boolean,
    val bonusMinutesReward: Int
) {
    val progressFraction: Float get() = if (maxProgress > 0) {
        (currentProgress.toFloat() / maxProgress.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val progressPercentage: Int get() = (progressFraction * 100).toInt()
}

object AchievementDefinitions {
    val ACHIEVEMENTS = listOf(
        Achievement(
            id = "first_step",
            title = "First Step",
            description = "Pay your first 100 steps of Scroll Tax",
            badgeEmoji = "👟",
            tier = BadgeTier.BRONZE,
            category = AchievementCategory.STEPS,
            currentProgress = 0,
            maxProgress = 100,
            isUnlocked = false,
            bonusMinutesReward = 5
        ),
        Achievement(
            id = "steps_5k",
            title = "5k Strider",
            description = "Reach 5,000 steps in a single day",
            badgeEmoji = "🏃",
            tier = BadgeTier.SILVER,
            category = AchievementCategory.STEPS,
            currentProgress = 0,
            maxProgress = 5000,
            isUnlocked = false,
            bonusMinutesReward = 10
        ),
        Achievement(
            id = "steps_10k",
            title = "10k Steps in a Day",
            description = "Conquer the legendary 10,000 daily steps milestone",
            badgeEmoji = "🏆",
            tier = BadgeTier.GOLD,
            category = AchievementCategory.STEPS,
            currentProgress = 0,
            maxProgress = 10000,
            isUnlocked = false,
            bonusMinutesReward = 25
        ),
        Achievement(
            id = "streak_3",
            title = "3-Day Consistency",
            description = "Keep up a 3-day active walking streak",
            badgeEmoji = "⚡",
            tier = BadgeTier.SILVER,
            category = AchievementCategory.STREAK,
            currentProgress = 0,
            maxProgress = 3,
            isUnlocked = false,
            bonusMinutesReward = 15
        ),
        Achievement(
            id = "streak_7",
            title = "7-Day Streak Master",
            description = "Complete an entire 7-day walking streak without missing",
            badgeEmoji = "🔥",
            tier = BadgeTier.PLATINUM,
            category = AchievementCategory.STREAK,
            currentProgress = 0,
            maxProgress = 7,
            isUnlocked = false,
            bonusMinutesReward = 35
        ),
        Achievement(
            id = "golden_hour",
            title = "Golden Hour Strider",
            description = "Walk 500+ steps during Golden Hour (06:00 – 08:00)",
            badgeEmoji = "🌅",
            tier = BadgeTier.GOLD,
            category = AchievementCategory.SPECIAL,
            currentProgress = 0,
            maxProgress = 500,
            isUnlocked = false,
            bonusMinutesReward = 20
        ),
        Achievement(
            id = "vault_master",
            title = "Vault Hoarder",
            description = "Accumulate 60 or more minutes in your Instagram vault",
            badgeEmoji = "💎",
            tier = BadgeTier.GOLD,
            category = AchievementCategory.VAULT,
            currentProgress = 0,
            maxProgress = 60,
            isUnlocked = false,
            bonusMinutesReward = 20
        ),
        Achievement(
            id = "active_walker",
            title = "Active Trailblazer",
            description = "Complete an Active Walk session of at least 1,500 steps",
            badgeEmoji = "🎯",
            tier = BadgeTier.SILVER,
            category = AchievementCategory.SPECIAL,
            currentProgress = 0,
            maxProgress = 1500,
            isUnlocked = false,
            bonusMinutesReward = 15
        )
    )

    fun computeAchievementsForProfile(
        profile: UserProfile,
        activeWalkSessionSteps: Int = 0,
        goldenHourSteps: Int = 0
    ): List<Achievement> {
        val unlockedSet = profile.unlockedAchievementIds

        return ACHIEVEMENTS.map { template ->
            val isUnlocked = unlockedSet.contains(template.id)
            val currentProgress = when (template.id) {
                "first_step" -> if (isUnlocked) 100 else profile.dailySteps.coerceAtMost(100)
                "steps_5k" -> if (isUnlocked) 5000 else profile.dailySteps.coerceAtMost(5000)
                "steps_10k" -> if (isUnlocked) 10000 else profile.dailySteps.coerceAtMost(10000)
                "streak_3" -> if (isUnlocked) 3 else profile.currentStreak.coerceAtMost(3)
                "streak_7" -> if (isUnlocked) 7 else profile.currentStreak.coerceAtMost(7)
                "golden_hour" -> if (isUnlocked) 500 else goldenHourSteps.coerceAtMost(500)
                "vault_master" -> if (isUnlocked) 60 else (profile.bankedSeconds / 60).coerceAtMost(60)
                "active_walker" -> if (isUnlocked) 1500 else activeWalkSessionSteps.coerceAtMost(1500)
                else -> 0
            }

            template.copy(
                currentProgress = currentProgress,
                isUnlocked = isUnlocked || (currentProgress >= template.maxProgress)
            )
        }
    }
}
