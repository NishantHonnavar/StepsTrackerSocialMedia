package com.example

data class StepLockData(
    val profiles: List<UserProfile> = emptyList(),
    val activeProfileId: String = "",
    val isServiceRunning: Boolean = false,
    val isInstagramActive: Boolean = false,
    val isStepSensorAvailable: Boolean = false,
    val stepSensorName: String = "",
    val hasUsagePermission: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val hasActivityPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val isSimulateMode: Boolean = true,
    val hasSelectedProfileOnStartup: Boolean = false,
    val recentGoalUnlockedMessage: String? = null,
    val simulatedPeriod: TimeOfDayPeriod? = null,
    val activeWalkSession: ActiveWalkSession = ActiveWalkSession(),
    val weeklyTrends: List<DayTrend> = emptyList(),
    val triggerMilestoneHapticEvent: Long = 0L,
    val unlockedBadgeCelebration: Achievement? = null,
    val isMorningAutoLockEnabled: Boolean = true,
    val lastMorningLockDate: String = "",
    val morningLockStatusMessage: String? = null,
    val isGmailSyncEnabled: Boolean = true,
    val gmailUserEmail: String = "nishantforscience@gmail.com",
    val lastWeeklyReportSentTimestamp: Long = 0L,
    val lastWeeklyReportStatus: String = "",
    val rankLevelUpCelebration: UserRank? = null
) {
    val activeProfile: UserProfile
        get() = profiles.firstOrNull { it.id == activeProfileId }
            ?: profiles.firstOrNull()
            ?: UserProfile(id = "default", name = "Primary Profile")

    // Dynamic Time-of-day conversion period
    val currentPeriod: TimeOfDayPeriod
        get() = simulatedPeriod ?: TimeOfDayPeriod.getCurrentPeriod()

    val currentStepsPerMinute: Int
        get() = currentPeriod.stepsPerMinute

    val dailySteps: Int get() = activeProfile.dailySteps
    val bankedSeconds: Int get() = activeProfile.bankedSeconds
    val bankedMinutes: Int get() = bankedSeconds / 60
    val bankedSecondsRemainder: Int get() = bankedSeconds % 60
    val totalEarnedSeconds: Int get() = activeProfile.totalEarnedSeconds
    val totalSpentSeconds: Int get() = activeProfile.totalSpentSeconds
    val dailyInstagramMinutesUsed: Int get() = totalSpentSeconds / 60
    val dailyInstagramSecondsRemainder: Int get() = totalSpentSeconds % 60
    val targetApp: String get() = activeProfile.targetApp

    // Experience Points (XP) & Rank Progression
    val rank: UserRank get() = activeProfile.rank
    val lifetimeSteps: Long get() = activeProfile.lifetimeSteps
    val lifetimeXp: Long get() = activeProfile.lifetimeXp
    val rankProgress: Float get() = activeProfile.rankProgress
    val xpNeededForNextRank: Long get() = activeProfile.xpNeededForNextRank

    val stepsToNextMinute: Int
        get() {
            val rate = currentStepsPerMinute
            val remainder = dailySteps % rate
            return if (remainder == 0 && dailySteps > 0) rate else rate - remainder
        }

    val progressToNextMinute: Float
        get() {
            val rate = currentStepsPerMinute
            val remainder = dailySteps % rate
            return (remainder.toFloat() / rate.toFloat()).coerceIn(0f, 1f)
        }

    val isLocked: Boolean get() = bankedSeconds <= 0

    val dailyStepGoal: Int get() = activeProfile.dailyStepGoal
    val bonusMinutes: Int get() = activeProfile.bonusMinutes
    val isGoalReached: Boolean get() = activeProfile.isGoalReached
    val goalProgress: Float get() = if (dailyStepGoal > 0) {
        (dailySteps.toFloat() / dailyStepGoal.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val goalPercentage: Int get() = (goalProgress * 100).toInt()
    val stepsRemainingToGoal: Int get() = (dailyStepGoal - dailySteps).coerceAtLeast(0)
    val hasClaimedGoalBonus: Boolean get() = activeProfile.hasClaimedGoalBonus

    val currentStreak: Int get() = activeProfile.currentStreak
    val bestStreak: Int get() = activeProfile.bestStreak

    val achievements: List<Achievement>
        get() = AchievementDefinitions.computeAchievementsForProfile(
            profile = activeProfile,
            activeWalkSessionSteps = activeWalkSession.sessionSteps,
            goldenHourSteps = activeProfile.goldenHourSteps
        )

    val unlockedAchievementsCount: Int
        get() = achievements.count { it.isUnlocked }

    val totalAchievementsCount: Int
        get() = achievements.size
}
