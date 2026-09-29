package com.example

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import java.util.UUID

object StepLockRepository {
    private const val PREFS_NAME = "steplock_prefs"
    private const val KEY_PROFILES_JSON = "key_profiles_json"
    private const val KEY_ACTIVE_PROFILE_ID = "key_active_profile_id"
    private const val KEY_SIMULATE_MODE = "key_simulate_mode"

    private var prefs: SharedPreferences? = null

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

            _state.update {
                it.copy(
                    profiles = profiles,
                    activeProfileId = activeId,
                    isSimulateMode = savedSimulateMode,
                    hasSelectedProfileOnStartup = false
                )
            }
            saveProfilesToPrefs(profiles, activeId)
        }
        syncPermissions(context)
    }

    private fun saveProfilesToPrefs(profiles: List<UserProfile>, activeId: String) {
        prefs?.edit()?.apply {
            val jsonArray = JSONArray()
            profiles.forEach { jsonArray.put(it.toJsonObject()) }
            putString(KEY_PROFILES_JSON, jsonArray.toString())
            putString(KEY_ACTIVE_PROFILE_ID, activeId)
            apply()
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
                current.copy(
                    activeProfileId = profileId,
                    recentGoalUnlockedMessage = null
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
            hasClaimedGoalBonus = false
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
        _state.update { current ->
            val active = current.activeProfile
            val oldSteps = active.dailySteps
            val newSteps = oldSteps + count
            val rate = active.stepsPerMinute

            // Standard step conversions
            val oldMilestones = oldSteps / rate
            val newMilestones = newSteps / rate
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

            val updatedActive = active.copy(
                dailySteps = newSteps,
                bankedSeconds = newBankedSeconds,
                totalEarnedSeconds = newTotalEarned,
                hasClaimedGoalBonus = claimedBonus
            )

            val updatedProfiles = current.profiles.map {
                if (it.id == active.id) updatedActive else it
            }

            saveProfilesToPrefs(updatedProfiles, current.activeProfileId)
            current.copy(
                profiles = updatedProfiles,
                recentGoalUnlockedMessage = if (goalNewlyReached) goalMessage else current.recentGoalUnlockedMessage
            )
        }
    }

    fun clearGoalUnlockedMessage() {
        _state.update { it.copy(recentGoalUnlockedMessage = null) }
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
            saveProfilesToPrefs(updatedProfiles, current.activeProfileId)
            current.copy(profiles = updatedProfiles)
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
            saveProfilesToPrefs(updatedProfiles, current.activeProfileId)
            current.copy(
                profiles = updatedProfiles,
                recentGoalUnlockedMessage = null
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
}
