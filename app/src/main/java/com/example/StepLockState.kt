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
    val recentGoalUnlockedMessage: String? = null
) {
    val activeProfile: UserProfile
        get() = profiles.firstOrNull { it.id == activeProfileId }
            ?: profiles.firstOrNull()
            ?: UserProfile(id = "default", name = "Primary Profile")

    val dailySteps: Int get() = activeProfile.dailySteps
    val bankedSeconds: Int get() = activeProfile.bankedSeconds
    val bankedMinutes: Int get() = activeProfile.bankedMinutes
    val bankedSecondsRemainder: Int get() = activeProfile.bankedSecondsRemainder
    val stepsPerMinute: Int get() = activeProfile.stepsPerMinute
    val totalEarnedSeconds: Int get() = activeProfile.totalEarnedSeconds
    val totalSpentSeconds: Int get() = activeProfile.totalSpentSeconds
    val targetApp: String get() = activeProfile.targetApp

    val stepsToNextMinute: Int get() = activeProfile.stepsToNextMinute
    val progressToNextMinute: Float get() = activeProfile.progressToNextMinute
    val isLocked: Boolean get() = activeProfile.isLocked

    val dailyStepGoal: Int get() = activeProfile.dailyStepGoal
    val bonusMinutes: Int get() = activeProfile.bonusMinutes
    val isGoalReached: Boolean get() = activeProfile.isGoalReached
    val goalProgress: Float get() = activeProfile.goalProgress
    val goalPercentage: Int get() = activeProfile.goalPercentage
    val stepsRemainingToGoal: Int get() = activeProfile.stepsRemainingToGoal
    val hasClaimedGoalBonus: Boolean get() = activeProfile.hasClaimedGoalBonus
}
