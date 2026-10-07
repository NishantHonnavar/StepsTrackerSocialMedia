package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Scroll Tax", appName)
    }

    @Test
    fun userProfile_jsonSerializationRoundTripWithGoals() {
        val original = UserProfile(
            id = "test_1",
            name = "Work Focus",
            emoji = "💼",
            colorHex = 0xFFFF4D4F,
            dailySteps = 450,
            bankedSeconds = 360,
            stepsPerMinute = 150,
            totalEarnedSeconds = 360,
            totalSpentSeconds = 60,
            dailyStepGoal = 8000,
            bonusMinutes = 20,
            hasClaimedGoalBonus = true
        )

        val json = original.toJsonObject()
        val restored = UserProfile.fromJsonObject(json)

        assertEquals(original.id, restored.id)
        assertEquals(original.name, restored.name)
        assertEquals(original.emoji, restored.emoji)
        assertEquals(original.colorHex, restored.colorHex)
        assertEquals(original.dailySteps, restored.dailySteps)
        assertEquals(original.bankedSeconds, restored.bankedSeconds)
        assertEquals(original.stepsPerMinute, restored.stepsPerMinute)
        assertEquals(original.totalEarnedSeconds, restored.totalEarnedSeconds)
        assertEquals(original.totalSpentSeconds, restored.totalSpentSeconds)
        assertEquals(original.dailyStepGoal, restored.dailyStepGoal)
        assertEquals(original.bonusMinutes, restored.bonusMinutes)
        assertEquals(original.hasClaimedGoalBonus, restored.hasClaimedGoalBonus)
    }

    @Test
    fun hourlyActivityHeatmap_generates24SlotsAndPeakWindows() {
        val profile = UserProfile(
            id = "profile_personal",
            name = "Personal",
            emoji = "🏃‍♂️",
            colorHex = 0xFF00E5FF,
            dailySteps = 5000,
            dailyStepGoal = 10000
        )

        val heatmap = HourlyActivityHeatmap.generateForProfile(profile, currentHour = 7)
        assertEquals(24, heatmap.slots.size)
        assertTrue(heatmap.peakSlots.isNotEmpty())
        assertEquals(3, heatmap.peakSlots.size)
        assertTrue(heatmap.mostActiveHour.averageSteps > 0)
        assertTrue(heatmap.totalDailyStepsEstimate > 0)
        // Hour 7 is in Golden Hour (06:00 to 08:00) with 50 steps per min
        val slot7 = heatmap.slots[7]
        assertEquals(TimeOfDayPeriod.GOLDEN_HOUR, slot7.period)
        assertEquals(50, slot7.conversionRate)
    }

    @Test
    fun socialShareHelper_generatesBadgeAndCabinetShareText() {
        val profile = UserProfile(
            id = "test_user",
            name = "Alex",
            emoji = "👟",
            colorHex = 0xFF00E5FF,
            dailySteps = 10250,
            currentStreak = 5,
            bestStreak = 7
        )

        val achievement = Achievement(
            id = "steps_10k",
            title = "10k Steps Master",
            description = "Walk 10,000 steps in a single day",
            category = AchievementCategory.STEPS,
            tier = BadgeTier.GOLD,
            badgeEmoji = "🏆",
            currentProgress = 10250,
            maxProgress = 10000,
            isUnlocked = true,
            bonusMinutesReward = 25
        )

        val shareText = SocialShareHelper.generateShareText(achievement, profile)
        assertTrue(shareText.contains("10k Steps Master"))
        assertTrue(shareText.contains("Gold"))
        assertTrue(shareText.contains("5 Days"))
        assertTrue(shareText.contains("#ScrollTax"))

        val cabinetText = SocialShareHelper.generateTrophyCabinetShareText(
            profile = profile,
            unlockedCount = 1,
            totalCount = 6,
            achievements = listOf(achievement)
        )
        assertTrue(cabinetText.contains("Alex"))
        assertTrue(cabinetText.contains("1 of 6"))
        assertTrue(cabinetText.contains("🏆"))
    }

    @Test
    fun gmailSyncManager_generatesWeeklyReportWithScreenTimeAndSteps() {
        val profile = UserProfile(
            id = "test_gmail_profile",
            name = "Nishant",
            emoji = "🏃‍♂️",
            colorHex = 0xFF00E5FF,
            dailySteps = 8500,
            bankedSeconds = 1200,
            totalSpentSeconds = 1800,
            currentStreak = 4
        )
        val state = StepLockData(
            profiles = listOf(profile),
            activeProfileId = profile.id,
            weeklyTrends = listOf(
                DayTrend(dayLabel = "Mon", dateKey = "2026-09-28", steps = 7000, minutesEarned = 140, minutesUsed = 30, isToday = false),
                DayTrend(dayLabel = "Tue", dateKey = "2026-09-29", steps = 8500, minutesEarned = 170, minutesUsed = 25, isToday = true)
            ),
            gmailUserEmail = "nishantforscience@gmail.com"
        )

        val subject = GmailSyncManager.generateWeeklyReportSubject(state)
        val plainText = GmailSyncManager.generateWeeklyReportPlainText(state)
        val htmlText = GmailSyncManager.generateWeeklyReportHtml(state)

        assertTrue(subject.contains("Scroll Tax Weekly Report"))
        assertTrue(subject.contains("Steps Walked"))
        assertTrue(plainText.contains("Nishant"))
        assertTrue(plainText.contains("Instagram Screen Time Used"))
        assertTrue(plainText.contains("MORNING AUTO-LOCK STATUS"))
        assertTrue(htmlText.contains("Scroll Tax Weekly Report"))
        assertTrue(htmlText.contains("Morning Auto-Lock"))
    }

    @Test
    fun morningLock_locksInstagramAndUnlocksWithSteps() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        StepLockRepository.init(context)

        // Enforce morning lock
        StepLockRepository.triggerMorningLockNow()
        val lockedState = StepLockRepository.state.value
        assertEquals(0, lockedState.bankedSeconds)
        assertTrue(lockedState.isLocked)
        assertTrue(lockedState.morningLockStatusMessage?.contains("Morning Auto-Lock") == true)

        // Walking steps earns screen time and unlocks Instagram
        StepLockRepository.addSteps(200)
        val unlockedState = StepLockRepository.state.value
        assertTrue(unlockedState.bankedSeconds > 0)
        assertFalse(unlockedState.isLocked)
        assertTrue(unlockedState.morningLockStatusMessage?.contains("Unlocked") == true)
    }

    @Test
    fun bootReceiver_handlesBootCompleted() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val receiver = BootReceiver()
        val intent = android.content.Intent(android.content.Intent.ACTION_BOOT_COMPLETED)
        receiver.onReceive(context, intent)
        // Verify repository initialized without crashing
        assertTrue(StepLockRepository.state.value.profiles.isNotEmpty())
    }

    @Test
    fun userRank_calculatesCorrectTiersFromXp() {
        assertEquals(UserRank.NOVICE_SCROLLER, UserRank.fromXp(0L))
        assertEquals(UserRank.NOVICE_SCROLLER, UserRank.fromXp(500L))
        assertEquals(UserRank.CASUAL_STRIDER, UserRank.fromXp(1000L))
        assertEquals(UserRank.CASUAL_STRIDER, UserRank.fromXp(2500L))
        assertEquals(UserRank.PACE_SETTER, UserRank.fromXp(3000L))
        assertEquals(UserRank.PACE_SETTER, UserRank.fromXp(6000L))
        assertEquals(UserRank.DISTANCE_CRUSHER, UserRank.fromXp(7500L))
        assertEquals(UserRank.DISTANCE_CRUSHER, UserRank.fromXp(12000L))
        assertEquals(UserRank.SCROLL_TAX_ELITE, UserRank.fromXp(15000L))
        assertEquals(UserRank.SCROLL_TAX_ELITE, UserRank.fromXp(25000L))
        assertEquals(UserRank.WALKING_MASTER, UserRank.fromXp(30000L))
        assertEquals(UserRank.WALKING_MASTER, UserRank.fromXp(100000L))

        // Check progression fraction
        val noviceRank = UserRank.NOVICE_SCROLLER
        assertEquals(0.5f, noviceRank.progressFraction(500L), 0.01f)
        assertEquals(500L, noviceRank.xpNeededForNextLevel(500L))

        val masterRank = UserRank.WALKING_MASTER
        assertTrue(masterRank.isMaxRank)
        assertEquals(1f, masterRank.progressFraction(50000L), 0.01f)
        assertEquals(0L, masterRank.xpNeededForNextLevel(50000L))
    }

    @Test
    fun repository_xpProgressionAndPromotion() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        StepLockRepository.init(context)

        // Reset rank for testing
        StepLockRepository.resetRankXp()
        val initialProfile = StepLockRepository.state.value.activeProfile
        assertEquals(0L, initialProfile.lifetimeXp)
        assertEquals(UserRank.NOVICE_SCROLLER, initialProfile.rank)

        // Add 1,200 XP to trigger promotion to Level 2 (Casual Strider)
        StepLockRepository.simulateAddXp(1200L)
        val promotedState = StepLockRepository.state.value
        assertEquals(UserRank.CASUAL_STRIDER, promotedState.rank)
        assertEquals(UserRank.CASUAL_STRIDER, promotedState.rankLevelUpCelebration)

        // Dismiss celebration
        StepLockRepository.dismissRankLevelUpCelebration()
        assertNull(StepLockRepository.state.value.rankLevelUpCelebration)

        // Promote directly to Walking Master
        StepLockRepository.setLevelDirectly(UserRank.WALKING_MASTER)
        val masterState = StepLockRepository.state.value
        assertEquals(UserRank.WALKING_MASTER, masterState.rank)
        assertTrue(masterState.rank.isMaxRank)
        assertEquals(UserRank.WALKING_MASTER, masterState.rankLevelUpCelebration)
    }
}
