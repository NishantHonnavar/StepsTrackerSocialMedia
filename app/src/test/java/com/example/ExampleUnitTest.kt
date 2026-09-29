package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun userProfile_calculatesVaultAndProgressCorrectly() {
        val profile = UserProfile(
            id = "test_profile",
            name = "Test Walker",
            dailySteps = 250,
            bankedSeconds = 120,
            stepsPerMinute = 100
        )

        assertEquals(2, profile.bankedMinutes)
        assertEquals(0, profile.bankedSecondsRemainder)
        assertFalse(profile.isLocked)
        assertEquals(50, profile.stepsToNextMinute)
        assertEquals(0.5f, profile.progressToNextMinute, 0.01f)
    }

    @Test
    fun userProfile_isLockedWhenZeroBankedSeconds() {
        val lockedProfile = UserProfile(
            id = "locked",
            name = "Locked Walker",
            dailySteps = 50,
            bankedSeconds = 0,
            stepsPerMinute = 100
        )
        assertTrue(lockedProfile.isLocked)
        assertEquals(0, lockedProfile.bankedMinutes)
    }

    @Test
    fun userProfile_dailyStepGoalProgressAndBonusCalculations() {
        val profile = UserProfile(
            id = "goal_test",
            name = "Goal Walker",
            dailySteps = 4500,
            bankedSeconds = 300,
            stepsPerMinute = 100,
            dailyStepGoal = 6000,
            bonusMinutes = 15
        )

        assertFalse(profile.isGoalReached)
        assertEquals(1500, profile.stepsRemainingToGoal)
        assertEquals(75, profile.goalPercentage)
        assertEquals(0.75f, profile.goalProgress, 0.01f)

        val completedProfile = profile.copy(dailySteps = 6200)
        assertTrue(completedProfile.isGoalReached)
        assertEquals(0, completedProfile.stepsRemainingToGoal)
        assertEquals(100, completedProfile.goalPercentage)
        assertEquals(1.0f, completedProfile.goalProgress, 0.01f)
    }
}
