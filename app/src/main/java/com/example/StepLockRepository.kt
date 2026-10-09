package com.example

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object StepLockRepository {
    private const val PREFS_NAME = "steplock_prefs"
    private const val KEY_PROFILES_JSON = "key_profiles_json"
    private const val KEY_ACTIVE_PROFILE_ID = "key_active_profile_id"
    private const val KEY_SIMULATE_MODE = "key_simulate_mode"
    private const val KEY_WEEKLY_TRENDS_JSON = "key_weekly_trends_json"
    private const val KEY_LAST_RECORDED_DATE = "key_last_recorded_date"
    private const val KEY_MORNING_LOCK_ENABLED = "key_morning_lock_enabled"
    private const val KEY_GMAIL_SYNC_ENABLED = "key_gmail_sync_enabled"
    private const val KEY_GMAIL_USER_EMAIL = "key_gmail_user_email"
    private const val KEY_LAST_WEEKLY_REPORT_TIME = "key_last_weekly_report_time"
    private const val KEY_LAST_WEEKLY_REPORT_STATUS = "key_last_weekly_report_status"
    private const val KEY_BLOCKED_APPS_JSON = "key_blocked_apps_json"
    private const val KEY_ACCOUNT_EMAIL = "key_account_email"
    private const val KEY_ACCOUNT_VERIFIED = "key_account_verified"
    private const val KEY_ONBOARDING_COMPLETED = "key_onboarding_completed"

    private var prefs: SharedPreferences? = null
    private var firebaseRepo: FirebaseStepLockRepository? = null
    private val repoScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _state = MutableStateFlow(StepLockData())
    val state: StateFlow<StepLockData> = _state.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val p = prefs!!

            val profilesJsonString = p.getString(KEY_PROFILES_JSON, null)
            val profiles = if (!profilesJsonString.isNullOrEmpty()) {
                try {
                    val jsonArray = JSONArray(profilesJsonString)
                    val list = mutableListOf<UserProfile>()
                    for (i in 0 until jsonArray.length()) {
                        list.add(UserProfile.fromJsonObject(jsonArray.getJSONObject(i)))
                    }
                    if (list.isEmpty()) UserProfile.createDefaultProfiles() else list
                } catch (e: Exception) {
                    UserProfile.createDefaultProfiles()
                }
            } else {
                UserProfile.createDefaultProfiles()
            }

            val savedActiveId = p.getString(KEY_ACTIVE_PROFILE_ID, null)
            val activeId = if (profiles.any { it.id == savedActiveId }) {
                savedActiveId!!
            } else {
                profiles.first().id
            }

            val savedSimulateMode = p.getBoolean(KEY_SIMULATE_MODE, true)
            val savedMorningLock = p.getBoolean(KEY_MORNING_LOCK_ENABLED, true)
            val savedGmailSync = p.getBoolean(KEY_GMAIL_SYNC_ENABLED, true)
            val savedGmailEmail = p.getString(KEY_GMAIL_USER_EMAIL, "nishantforscience@gmail.com") ?: "nishantforscience@gmail.com"
            val savedLastReportTime = p.getLong(KEY_LAST_WEEKLY_REPORT_TIME, 0L)
            val savedLastReportStatus = p.getString(KEY_LAST_WEEKLY_REPORT_STATUS, "") ?: ""
            val savedEmail = p.getString(KEY_ACCOUNT_EMAIL, "") ?: ""
            val savedVerified = p.getBoolean(KEY_ACCOUNT_VERIFIED, false)
            val savedOnboardingDone = p.getBoolean(KEY_ONBOARDING_COMPLETED, false)

            val blockedAppsJson = p.getString(KEY_BLOCKED_APPS_JSON, null)
            val blockedApps = if (!blockedAppsJson.isNullOrEmpty()) {
                try {
                    val array = JSONArray(blockedAppsJson)
                    val list = mutableListOf<BlockedAppInfo>()
                    for (i in 0 until array.length()) {
                        list.add(BlockedAppInfo.fromJsonObject(array.getJSONObject(i)))
                    }
                    if (list.isEmpty()) BlockedAppInfo.DEFAULT_BLOCKED_APPS else list
                } catch (e: Exception) {
                    BlockedAppInfo.DEFAULT_BLOCKED_APPS
                }
            } else {
                BlockedAppInfo.DEFAULT_BLOCKED_APPS
            }

            val activeProfile = profiles.firstOrNull { it.id == activeId } ?: profiles.first()

            val weeklyTrends = DayTrend.createSampleWeek(
                todaySteps = activeProfile.dailySteps,
                todayEarnedMinutes = activeProfile.totalEarnedSeconds / 60
            )

            _state.update {
                it.copy(
                    profiles = profiles,
                    activeProfileId = activeId,
                    isSimulateMode = savedSimulateMode,
                    hasSelectedProfileOnStartup = savedOnboardingDone,
                    hasCompletedAccountSetup = savedOnboardingDone,
                    accountEmail = savedEmail,
                    isAccountVerified = savedVerified,
                    blockedApps = blockedApps,
                    weeklyTrends = weeklyTrends,
                    isMorningAutoLockEnabled = savedMorningLock,
                    isGmailSyncEnabled = savedGmailSync,
                    gmailUserEmail = savedGmailEmail,
                    lastWeeklyReportSentTimestamp = savedLastReportTime,
                    lastWeeklyReportStatus = savedLastReportStatus
                )
            }
            saveProfilesToPrefs(profiles, activeId)
            checkDailyMidnightOrMorningReset()

            try {
                firebaseRepo = FirebaseStepLockRepository(context)
                repoScope.launch {
                    FirebaseAuthManager.authStateFlow().collect { user ->
                        _state.update {
                            it.copy(
                                isFirebaseConnected = user != null,
                                firebaseUserEmail = user?.email,
                                firebaseUserDisplayName = user?.displayName
                            )
                        }
                        if (user != null) {
                            syncWithFirebase()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("StepLockRepository", "Firebase initialization deferred: ${e.message}")
            }
        }
        syncPermissions(context)
    }

    fun syncWithFirebase() {
        val user = FirebaseAuthManager.currentUser ?: return
        val current = _state.value
        val repo = firebaseRepo ?: return

        repoScope.launch {
            _state.update { it.copy(isFirebaseSyncing = true) }
            try {
                repo.syncUserAccount(
                    user = user,
                    activeProfileId = current.activeProfileId,
                    lifetimeSteps = current.lifetimeSteps,
                    lifetimeXp = current.lifetimeXp,
                    currentStreak = current.activeProfile.currentStreak,
                    bestStreak = current.activeProfile.bestStreak
                )
                current.profiles.forEach { profile ->
                    repo.saveProfile(user.uid, profile)
                }
                current.weeklyTrends.forEach { trend ->
                    repo.recordDayHistory(user.uid, trend)
                }
            } catch (e: Exception) {
                Log.w("StepLockRepository", "Firebase sync error: ${e.message}")
            } finally {
                _state.update { it.copy(isFirebaseSyncing = false) }
            }
        }
    }

    private fun saveProfilesToPrefs(profiles: List<UserProfile>, activeId: String) {
        prefs?.edit()?.apply {
            val jsonArray = JSONArray()
            profiles.forEach { jsonArray.put(it.toJsonObject()) }
            putString(KEY_PROFILES_JSON, jsonArray.toString())
            putString(KEY_ACTIVE_PROFILE_ID, activeId)
            apply()
        }
        syncWithFirebase()
    }

    fun setSimulatedPeriod(period: TimeOfDayPeriod?) {
        _state.update { it.copy(simulatedPeriod = period) }
    }

    fun startActiveWalkSession() {
        _state.update { current ->
            current.copy(
                activeWalkSession = ActiveWalkSession(
                    isActive = true,
                    sessionSteps = 0,
                    sessionMinutesEarned = 0,
                    durationSeconds = 0,
                    lastMilestoneAnnounced = 0
                )
            )
        }
    }

    fun stopActiveWalkSession() {
        _state.update { current ->
            current.copy(
                activeWalkSession = current.activeWalkSession.copy(isActive = false)
            )
        }
    }

    fun tickActiveWalkDuration() {
        _state.update { current ->
            if (current.activeWalkSession.isActive) {
                current.copy(
                    activeWalkSession = current.activeWalkSession.copy(
                        durationSeconds = current.activeWalkSession.durationSeconds + 1
                    )
                )
            } else current
        }
    }

    fun selectProfileAndDismissStartup(profileId: String) {
        switchProfile(profileId)
        _state.update { it.copy(hasSelectedProfileOnStartup = true) }
    }

    fun showProfileSelection() {
        _state.update { it.copy(hasSelectedProfileOnStartup = false) }
    }

    fun switchProfile(profileId: String) {
        _state.update { current ->
            if (current.profiles.any { it.id == profileId }) {
                prefs?.edit()?.putString(KEY_ACTIVE_PROFILE_ID, profileId)?.apply()
                val targetProfile = current.profiles.first { it.id == profileId }
                val updatedTrends = DayTrend.createSampleWeek(
                    todaySteps = targetProfile.dailySteps,
                    todayEarnedMinutes = targetProfile.totalEarnedSeconds / 60
                )
                current.copy(
                    activeProfileId = profileId,
                    recentGoalUnlockedMessage = null,
                    weeklyTrends = updatedTrends
                )
            } else {
                current
            }
        }
    }

    fun createProfile(
        name: String,
        emoji: String,
        stepsPerMinute: Int,
        initialBankedMinutes: Int,
        dailyStepGoal: Int = 6000,
        bonusMinutes: Int = 15
    ): UserProfile {
        val newProfile = UserProfile(
            id = "profile_" + UUID.randomUUID().toString().take(8),
            name = name.ifBlank { "New Profile" },
            emoji = emoji.ifBlank { "⭐" },
            colorHex = when ((_state.value.profiles.size) % 4) {
                0 -> 0xFF00E5FF
                1 -> 0xFF00E699
                2 -> 0xFFFF4D4F
                else -> 0xFFF59E0B
            },
            dailySteps = 0,
            bankedSeconds = initialBankedMinutes * 60,
            stepsPerMinute = stepsPerMinute.coerceAtLeast(10),
            totalEarnedSeconds = initialBankedMinutes * 60,
            totalSpentSeconds = 0,
            dailyStepGoal = dailyStepGoal.coerceAtLeast(500),
            bonusMinutes = bonusMinutes.coerceAtLeast(1),
            hasClaimedGoalBonus = false,
            lifetimeSteps = 0L,
            lifetimeXp = 0L,
            lastCelebratedRankLevel = 1
        )

        _state.update { current ->
            val updatedProfiles = current.profiles + newProfile
            saveProfilesToPrefs(updatedProfiles, newProfile.id)
            current.copy(
                profiles = updatedProfiles,
                activeProfileId = newProfile.id,
                hasSelectedProfileOnStartup = true
            )
        }
        return newProfile
    }

    fun updateActiveProfileSettings(stepsPerMinute: Int) {
        _state.update { current ->
            val updatedProfiles = current.profiles.map { profile ->
                if (profile.id == current.activeProfileId) {
                    profile.copy(stepsPerMinute = stepsPerMinute.coerceAtLeast(10))
                } else {
                    profile
                }
            }
            saveProfilesToPrefs(updatedProfiles, current.activeProfileId)
            current.copy(profiles = updatedProfiles)
        }
    }

    fun updateActiveProfileGoalSettings(dailyStepGoal: Int, bonusMinutes: Int) {
        _state.update { current ->
            val updatedProfiles = current.profiles.map { profile ->
                if (profile.id == current.activeProfileId) {
                    profile.copy(
                        dailyStepGoal = dailyStepGoal.coerceAtLeast(500),
                        bonusMinutes = bonusMinutes.coerceAtLeast(1)
                    )
                } else {
                    profile
                }
            }
            saveProfilesToPrefs(updatedProfiles, current.activeProfileId)
            current.copy(profiles = updatedProfiles)
        }
    }

    fun updateDailyStepGoal(dailyStepGoal: Int) {
        val currentBonus = _state.value.activeProfile.bonusMinutes
        updateActiveProfileGoalSettings(dailyStepGoal, currentBonus)
    }

    fun deleteProfile(profileId: String) {
        _state.update { current ->
            if (current.profiles.size <= 1) return@update current

            val updatedProfiles = current.profiles.filterNot { it.id == profileId }
            val newActiveId = if (current.activeProfileId == profileId) {
                updatedProfiles.first().id
            } else {
                current.activeProfileId
            }

            saveProfilesToPrefs(updatedProfiles, newActiveId)
            current.copy(
                profiles = updatedProfiles,
                activeProfileId = newActiveId
            )
        }
    }

    fun addSteps(count: Int) {
        if (count <= 0) return
        checkDailyMidnightOrMorningReset()
        _state.update { current ->
            val active = current.activeProfile
            val oldSteps = active.dailySteps
            val newSteps = oldSteps + count

            // Time-of-day dynamic conversion rate (Golden Hour: 25, Standard Day: 50, Night Surge: 150)
            val dynamicRate = current.currentStepsPerMinute

            val oldMilestones = oldSteps / dynamicRate
            val newMilestones = newSteps / dynamicRate
            val minutesEarned = (newMilestones - oldMilestones).coerceAtLeast(0)
            var addedSeconds = minutesEarned * 60

            // Goal Milestone & Bonus Minutes Check
            var goalNewlyReached = false
            var claimedBonus = active.hasClaimedGoalBonus
            var goalMessage = current.recentGoalUnlockedMessage

            if (!claimedBonus && newSteps >= active.dailyStepGoal && active.dailyStepGoal > 0) {
                goalNewlyReached = true
                claimedBonus = true
                val bonusSeconds = active.bonusMinutes * 60
                addedSeconds += bonusSeconds
                goalMessage = "🎉 Goal reached! Unlocked +${active.bonusMinutes} bonus minutes for ${active.name}!"
            }

            val newBankedSeconds = active.bankedSeconds + addedSeconds
            val newTotalEarned = active.totalEarnedSeconds + addedSeconds

            // Update Active Walk Session if running
            var updatedWalk = current.activeWalkSession
            var triggerHaptic = if (minutesEarned > 0) System.currentTimeMillis() else current.triggerMilestoneHapticEvent

            if (current.activeWalkSession.isActive) {
                val newWalkSteps = current.activeWalkSession.sessionSteps + count
                val newWalkMins = current.activeWalkSession.sessionMinutesEarned + minutesEarned
                val prevMilestone = current.activeWalkSession.lastMilestoneAnnounced

                // Milestone haptic alert every 500 steps
                val newMilestone = (newWalkSteps / 500) * 500
                if (newMilestone > prevMilestone && newMilestone > 0) {
                    triggerHaptic = System.currentTimeMillis()
                }

                updatedWalk = current.activeWalkSession.copy(
                    sessionSteps = newWalkSteps,
                    sessionMinutesEarned = newWalkMins,
                    lastMilestoneAnnounced = newMilestone
                )
            }

            // Milestone Digital Badges & Achievements Check
            val newlyUnlockedBadges = mutableListOf<Achievement>()
            val unlockedIds = active.unlockedAchievementIds.toMutableSet()
            val goldenHourSteps = if (current.currentPeriod == TimeOfDayPeriod.GOLDEN_HOUR) active.goldenHourSteps + count else active.goldenHourSteps

            val checkList = listOf(
                "first_step" to (newSteps >= 100),
                "steps_5k" to (newSteps >= 5000),
                "steps_10k" to (newSteps >= 10000),
                "streak_3" to (active.currentStreak >= 3),
                "streak_7" to (active.currentStreak >= 7),
                "golden_hour" to (goldenHourSteps >= 500),
                "vault_master" to (newBankedSeconds / 60 >= 60),
                "active_walker" to (updatedWalk.sessionSteps >= 1500)
            )

            var achievementBonusSeconds = 0
            for ((id, condition) in checkList) {
                if (condition && !unlockedIds.contains(id)) {
                    unlockedIds.add(id)
                    val ach = AchievementDefinitions.ACHIEVEMENTS.firstOrNull { it.id == id }
                    if (ach != null) {
                        newlyUnlockedBadges.add(ach)
                        achievementBonusSeconds += ach.bonusMinutesReward * 60
                    }
                }
            }

            // Experience Points (XP) & Rank Level-Up Progression
            val newLifetimeSteps = active.lifetimeSteps + count
            var earnedXp = count.toLong() // 1 step = 1 base XP
            if (current.currentPeriod == TimeOfDayPeriod.GOLDEN_HOUR) {
                earnedXp *= 2 // 2x XP during Golden Hour!
            }
            if (goalNewlyReached) {
                earnedXp += 500L // +500 XP for completing daily step goal
            }
            if (newlyUnlockedBadges.isNotEmpty()) {
                earnedXp += (newlyUnlockedBadges.size * 250L) // +250 XP per unlocked badge
            }
            val newLifetimeXp = active.lifetimeXp + earnedXp

            val oldRank = UserRank.fromXp(active.lifetimeXp)
            val newRank = UserRank.fromXp(newLifetimeXp)
            var leveledUpRank: UserRank? = null
            var lastCelebrated = active.lastCelebratedRankLevel
            if (newRank.level > oldRank.level && newRank.level > active.lastCelebratedRankLevel) {
                leveledUpRank = newRank
                lastCelebrated = newRank.level
                achievementBonusSeconds += 15 * 60 // +15m bonus screen time on rank promotion!
            }

            val finalBankedSeconds = newBankedSeconds + achievementBonusSeconds
            val finalTotalEarned = newTotalEarned + achievementBonusSeconds

            val updatedActive = active.copy(
                dailySteps = newSteps,
                bankedSeconds = finalBankedSeconds,
                totalEarnedSeconds = finalTotalEarned,
                hasClaimedGoalBonus = claimedBonus,
                goldenHourSteps = goldenHourSteps,
                unlockedAchievementIds = unlockedIds,
                lifetimeSteps = newLifetimeSteps,
                lifetimeXp = newLifetimeXp,
                lastCelebratedRankLevel = lastCelebrated
            )

            val updatedProfiles = current.profiles.map {
                if (it.id == active.id) updatedActive else it
            }

            // Update today in weekly trends
            val updatedTrends = current.weeklyTrends.map { day ->
                if (day.isToday) {
                    day.copy(
                        steps = newSteps,
                        minutesEarned = finalTotalEarned / 60
                    )
                } else day
            }

            saveProfilesToPrefs(updatedProfiles, current.activeProfileId)
            current.copy(
                profiles = updatedProfiles,
                recentGoalUnlockedMessage = if (goalNewlyReached) goalMessage else current.recentGoalUnlockedMessage,
                activeWalkSession = updatedWalk,
                triggerMilestoneHapticEvent = if (newlyUnlockedBadges.isNotEmpty() || leveledUpRank != null) System.currentTimeMillis() else triggerHaptic,
                weeklyTrends = updatedTrends,
                unlockedBadgeCelebration = newlyUnlockedBadges.firstOrNull() ?: current.unlockedBadgeCelebration,
                rankLevelUpCelebration = leveledUpRank ?: current.rankLevelUpCelebration,
                morningLockStatusMessage = if (active.bankedSeconds <= 0 && finalBankedSeconds > 0) {
                    "🔓 Unlocked! You earned ${finalBankedSeconds / 60}m of Instagram by walking."
                } else current.morningLockStatusMessage
            )
        }
    }

    fun clearGoalUnlockedMessage() {
        _state.update { it.copy(recentGoalUnlockedMessage = null) }
    }

    fun dismissBadgeCelebration() {
        _state.update { it.copy(unlockedBadgeCelebration = null) }
    }

    fun simulateStreakIncrement(days: Int = 1) {
        _state.update { current ->
            val active = current.activeProfile
            val newStreak = (active.currentStreak + days).coerceAtLeast(1)
            val newBest = maxOf(newStreak, active.bestStreak)

            val unlockedIds = active.unlockedAchievementIds.toMutableSet()
            val newlyUnlocked = mutableListOf<Achievement>()
            var bonusSecs = 0

            if (newStreak >= 3 && !unlockedIds.contains("streak_3")) {
                unlockedIds.add("streak_3")
                AchievementDefinitions.ACHIEVEMENTS.find { it.id == "streak_3" }?.let {
                    newlyUnlocked.add(it)
                    bonusSecs += it.bonusMinutesReward * 60
                }
            }
            if (newStreak >= 7 && !unlockedIds.contains("streak_7")) {
                unlockedIds.add("streak_7")
                AchievementDefinitions.ACHIEVEMENTS.find { it.id == "streak_7" }?.let {
                    newlyUnlocked.add(it)
                    bonusSecs += it.bonusMinutesReward * 60
                }
            }

            val updatedActive = active.copy(
                currentStreak = newStreak,
                bestStreak = newBest,
                bankedSeconds = active.bankedSeconds + bonusSecs,
                totalEarnedSeconds = active.totalEarnedSeconds + bonusSecs,
                unlockedAchievementIds = unlockedIds
            )

            val updatedProfiles = current.profiles.map {
                if (it.id == active.id) updatedActive else it
            }

            saveProfilesToPrefs(updatedProfiles, current.activeProfileId)
            current.copy(
                profiles = updatedProfiles,
                unlockedBadgeCelebration = newlyUnlocked.firstOrNull() ?: current.unlockedBadgeCelebration,
                triggerMilestoneHapticEvent = if (newlyUnlocked.isNotEmpty()) System.currentTimeMillis() else current.triggerMilestoneHapticEvent
            )
        }
    }

    fun unlockAchievementDirectly(achievementId: String) {
        _state.update { current ->
            val active = current.activeProfile
            if (active.unlockedAchievementIds.contains(achievementId)) return@update current

            val ach = AchievementDefinitions.ACHIEVEMENTS.find { it.id == achievementId } ?: return@update current
            val unlockedIds = active.unlockedAchievementIds + achievementId
            val bonusSecs = ach.bonusMinutesReward * 60

            val updatedActive = active.copy(
                bankedSeconds = active.bankedSeconds + bonusSecs,
                totalEarnedSeconds = active.totalEarnedSeconds + bonusSecs,
                unlockedAchievementIds = unlockedIds
            )

            val updatedProfiles = current.profiles.map {
                if (it.id == active.id) updatedActive else it
            }

            saveProfilesToPrefs(updatedProfiles, current.activeProfileId)
            current.copy(
                profiles = updatedProfiles,
                unlockedBadgeCelebration = ach,
                triggerMilestoneHapticEvent = System.currentTimeMillis()
            )
        }
    }

    fun addBankedSeconds(seconds: Int) {
        if (seconds <= 0) return
        _state.update { current ->
            val active = current.activeProfile
            val updatedActive = active.copy(
                bankedSeconds = active.bankedSeconds + seconds,
                totalEarnedSeconds = active.totalEarnedSeconds + seconds
            )
            val updatedProfiles = current.profiles.map {
                if (it.id == active.id) updatedActive else it
            }
            saveProfilesToPrefs(updatedProfiles, current.activeProfileId)
            current.copy(profiles = updatedProfiles)
        }
    }

    fun consumeScreenTime(seconds: Int) {
        if (seconds <= 0) return
        checkDailyMidnightOrMorningReset()
        _state.update { current ->
            val active = current.activeProfile
            val actualDeduction = seconds.coerceAtMost(active.bankedSeconds)
            val newBanked = (active.bankedSeconds - actualDeduction).coerceAtLeast(0)
            val newSpent = active.totalSpentSeconds + actualDeduction

            val updatedActive = active.copy(
                bankedSeconds = newBanked,
                totalSpentSeconds = newSpent
            )
            val updatedProfiles = current.profiles.map {
                if (it.id == active.id) updatedActive else it
            }
            val updatedTrends = current.weeklyTrends.map { day ->
                if (day.isToday) day.copy(minutesUsed = newSpent / 60) else day
            }
            saveProfilesToPrefs(updatedProfiles, current.activeProfileId)
            current.copy(
                profiles = updatedProfiles,
                weeklyTrends = updatedTrends
            )
        }
    }

    fun checkDailyMidnightOrMorningReset() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastDate = prefs?.getString(KEY_LAST_RECORDED_DATE, null)

        if (lastDate != null && lastDate != todayStr) {
            _state.update { current ->
                val updatedProfiles = current.profiles.map { profile ->
                    profile.copy(
                        dailySteps = 0,
                        bankedSeconds = if (current.isMorningAutoLockEnabled) 0 else profile.bankedSeconds,
                        totalEarnedSeconds = 0,
                        totalSpentSeconds = 0,
                        hasClaimedGoalBonus = false,
                        goldenHourSteps = 0
                    )
                }

                val updatedTrends = DayTrend.createSampleWeek(
                    todaySteps = 0,
                    todayEarnedMinutes = 0
                )

                saveProfilesToPrefs(updatedProfiles, current.activeProfileId)
                prefs?.edit()?.putString(KEY_LAST_RECORDED_DATE, todayStr)?.apply()

                current.copy(
                    profiles = updatedProfiles,
                    lastMorningLockDate = todayStr,
                    morningLockStatusMessage = "🔒 Morning Auto-Lock: Instagram locked for the new day. Walk steps to unlock!",
                    weeklyTrends = updatedTrends
                )
            }
        } else if (lastDate == null) {
            prefs?.edit()?.putString(KEY_LAST_RECORDED_DATE, todayStr)?.apply()
            _state.update { it.copy(lastMorningLockDate = todayStr) }
        }
    }

    fun triggerMorningLockNow() {
        _state.update { current ->
            val active = current.activeProfile
            val updatedActive = active.copy(bankedSeconds = 0)
            val updatedProfiles = current.profiles.map {
                if (it.id == active.id) updatedActive else it
            }
            saveProfilesToPrefs(updatedProfiles, current.activeProfileId)
            current.copy(
                profiles = updatedProfiles,
                morningLockStatusMessage = "🔒 Morning Auto-Lock Active: Instagram locked! Walk steps to earn access."
            )
        }
    }

    fun dismissMorningLockMessage() {
        _state.update { it.copy(morningLockStatusMessage = null) }
    }

    fun setMorningAutoLockEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_MORNING_LOCK_ENABLED, enabled)?.apply()
        _state.update { it.copy(isMorningAutoLockEnabled = enabled) }
    }

    fun setGmailSyncEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_GMAIL_SYNC_ENABLED, enabled)?.apply()
        _state.update { it.copy(isGmailSyncEnabled = enabled) }
    }

    fun setGmailUserEmail(email: String) {
        val clean = email.trim()
        if (clean.isNotEmpty()) {
            prefs?.edit()?.putString(KEY_GMAIL_USER_EMAIL, clean)?.apply()
            _state.update { it.copy(gmailUserEmail = clean) }
        }
    }

    fun recordWeeklyReportSent(status: String) {
        val now = System.currentTimeMillis()
        prefs?.edit()?.apply {
            putLong(KEY_LAST_WEEKLY_REPORT_TIME, now)
            putString(KEY_LAST_WEEKLY_REPORT_STATUS, status)
            apply()
        }
        _state.update {
            it.copy(
                lastWeeklyReportSentTimestamp = now,
                lastWeeklyReportStatus = status
            )
        }
    }

    fun setBankedSeconds(seconds: Int) {
        val clamped = seconds.coerceAtLeast(0)
        _state.update { current ->
            val active = current.activeProfile
            val updatedActive = active.copy(bankedSeconds = clamped)
            val updatedProfiles = current.profiles.map {
                if (it.id == active.id) updatedActive else it
            }
            saveProfilesToPrefs(updatedProfiles, current.activeProfileId)
            current.copy(profiles = updatedProfiles)
        }
    }

    fun resetActiveProfileStats() {
        _state.update { current ->
            val active = current.activeProfile
            val updatedActive = active.copy(
                dailySteps = 0,
                bankedSeconds = 0,
                totalEarnedSeconds = 0,
                totalSpentSeconds = 0,
                hasClaimedGoalBonus = false
            )
            val updatedProfiles = current.profiles.map {
                if (it.id == active.id) updatedActive else it
            }
            val updatedTrends = current.weeklyTrends.map { day ->
                if (day.isToday) day.copy(steps = 0, minutesEarned = 0) else day
            }
            saveProfilesToPrefs(updatedProfiles, current.activeProfileId)
            current.copy(
                profiles = updatedProfiles,
                recentGoalUnlockedMessage = null,
                weeklyTrends = updatedTrends,
                activeWalkSession = ActiveWalkSession()
            )
        }
    }

    fun syncPermissions(context: Context) {
        val appContext = context.applicationContext
        val hasUsage = checkUsageStatsPermission(appContext)
        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(appContext)
        } else {
            true
        }
        val hasActivity = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        val hasNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        _state.update {
            it.copy(
                hasUsagePermission = hasUsage,
                hasOverlayPermission = hasOverlay,
                hasActivityPermission = hasActivity,
                hasNotificationPermission = hasNotification
            )
        }
    }

    private fun checkUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun setServiceRunning(isRunning: Boolean) {
        _state.update { it.copy(isServiceRunning = isRunning) }
    }

    fun setInstagramActive(isActive: Boolean) {
        _state.update { it.copy(isInstagramActive = isActive) }
    }

    fun setStepSensorInfo(available: Boolean, name: String) {
        _state.update {
            it.copy(
                isStepSensorAvailable = available,
                stepSensorName = name
            )
        }
    }

    fun setSimulateMode(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_SIMULATE_MODE, enabled)?.apply()
        _state.update { it.copy(isSimulateMode = enabled) }
    }

    fun dismissRankLevelUpCelebration() {
        _state.update { it.copy(rankLevelUpCelebration = null) }
    }

    fun simulateAddXp(xpCount: Long) {
        if (xpCount <= 0L) return
        _state.update { current ->
            val active = current.activeProfile
            val newLifetimeXp = active.lifetimeXp + xpCount
            val oldRank = UserRank.fromXp(active.lifetimeXp)
            val newRank = UserRank.fromXp(newLifetimeXp)
            var celebration: UserRank? = null
            var lastCelebrated = active.lastCelebratedRankLevel
            var bonusVaultSeconds = 0

            if (newRank.level > oldRank.level && newRank.level > active.lastCelebratedRankLevel) {
                celebration = newRank
                lastCelebrated = newRank.level
                bonusVaultSeconds = 15 * 60 // +15m bonus screen time on rank promotion
            }

            val updatedActive = active.copy(
                lifetimeXp = newLifetimeXp,
                lastCelebratedRankLevel = lastCelebrated,
                bankedSeconds = active.bankedSeconds + bonusVaultSeconds,
                totalEarnedSeconds = active.totalEarnedSeconds + bonusVaultSeconds
            )
            val updatedProfiles = current.profiles.map {
                if (it.id == active.id) updatedActive else it
            }
            saveProfilesToPrefs(updatedProfiles, current.activeProfileId)
            current.copy(
                profiles = updatedProfiles,
                rankLevelUpCelebration = celebration ?: current.rankLevelUpCelebration,
                triggerMilestoneHapticEvent = if (celebration != null) System.currentTimeMillis() else current.triggerMilestoneHapticEvent
            )
        }
    }

    fun setLevelDirectly(targetRank: UserRank) {
        switchRankLevel(targetRank, showCelebration = true)
    }

    fun switchRankLevel(targetRank: UserRank, showCelebration: Boolean = false) {
        _state.update { current ->
            val active = current.activeProfile
            val updatedActive = active.copy(
                lifetimeXp = targetRank.minXp + 150L,
                lifetimeSteps = (targetRank.minXp + 150L).coerceAtLeast(active.lifetimeSteps),
                lastCelebratedRankLevel = targetRank.level
            )
            val updatedProfiles = current.profiles.map {
                if (it.id == active.id) updatedActive else it
            }
            saveProfilesToPrefs(updatedProfiles, current.activeProfileId)
            current.copy(
                profiles = updatedProfiles,
                rankLevelUpCelebration = if (showCelebration) targetRank else null,
                triggerMilestoneHapticEvent = System.currentTimeMillis()
            )
        }
    }

    fun toggleBlockedApp(packageName: String, isBlocked: Boolean) {
        _state.update { current ->
            val updated = current.blockedApps.map {
                if (it.packageName == packageName) it.copy(isBlocked = isBlocked) else it
            }
            saveBlockedAppsToPrefs(updated)
            current.copy(blockedApps = updated)
        }
    }

    fun addCustomBlockedApp(packageName: String, appName: String, emoji: String = "📱") {
        if (packageName.isBlank()) return
        _state.update { current ->
            val exists = current.blockedApps.any { it.packageName.equals(packageName, ignoreCase = true) }
            val updated = if (exists) {
                current.blockedApps.map {
                    if (it.packageName.equals(packageName, ignoreCase = true)) it.copy(isBlocked = true) else it
                }
            } else {
                current.blockedApps + BlockedAppInfo(packageName.trim(), appName.ifBlank { packageName.trim() }, emoji, true)
            }
            saveBlockedAppsToPrefs(updated)
            current.copy(blockedApps = updated)
        }
    }

    private fun saveBlockedAppsToPrefs(apps: List<BlockedAppInfo>) {
        val array = JSONArray()
        apps.forEach { array.put(it.toJsonObject()) }
        prefs?.edit()?.putString(KEY_BLOCKED_APPS_JSON, array.toString())?.apply()
    }

    fun completeOnboardingAndVerification(email: String) {
        val clean = email.trim()
        prefs?.edit()?.apply {
            putString(KEY_ACCOUNT_EMAIL, clean)
            putBoolean(KEY_ACCOUNT_VERIFIED, true)
            putBoolean(KEY_ONBOARDING_COMPLETED, true)
            apply()
        }
        _state.update {
            it.copy(
                accountEmail = clean,
                isAccountVerified = true,
                hasCompletedAccountSetup = true,
                hasSelectedProfileOnStartup = true
            )
        }
    }

    fun updateAccountEmail(email: String) {
        val clean = email.trim()
        prefs?.edit()?.putString(KEY_ACCOUNT_EMAIL, clean)?.apply()
        _state.update { it.copy(accountEmail = clean) }
    }

    fun resetRankXp() {
        _state.update { current ->
            val active = current.activeProfile
            val updatedActive = active.copy(
                lifetimeXp = 0L,
                lifetimeSteps = 0L,
                lastCelebratedRankLevel = 1
            )
            val updatedProfiles = current.profiles.map {
                if (it.id == active.id) updatedActive else it
            }
            saveProfilesToPrefs(updatedProfiles, current.activeProfileId)
            current.copy(
                profiles = updatedProfiles,
                rankLevelUpCelebration = null
            )
        }
    }
}
