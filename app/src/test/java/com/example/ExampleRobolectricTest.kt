package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        assertEquals("StepLock", appName)
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
}
