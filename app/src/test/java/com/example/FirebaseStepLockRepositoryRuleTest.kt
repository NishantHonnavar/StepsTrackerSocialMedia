package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.concurrent.CancellationException

class FirebaseStepLockRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun syncUserAccount_and_observeProfiles_worksForOwner() = runBlocking {
    val uid = signInTestUser("alice_rule@test.com")
    val repo = FirebaseStepLockRepository(firestore)

    val profile = UserProfile(
      id = "mode_runner",
      name = "Daily Sprint",
      emoji = "⚡",
      colorHex = 0xFF00E5FF,
      dailySteps = 1200,
      bankedSeconds = 180,
      stepsPerMinute = 120,
      dailyStepGoal = 8000,
      bonusMinutes = 20
    )

    withTimeout(5000L) {
      repo.saveProfile(uid, profile)
    }

    val profiles = withTimeout(5000L) {
      repo.observeProfiles(uid).first { list: List<UserProfile> -> list.any { p -> p.id == "mode_runner" } }
    }
    assertTrue("Profiles should contain mode_runner", profiles.any { it.id == "mode_runner" })
  }

  @Test
  fun crossUserAccess_profiles_rejectedWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser("alice_cross@test.com")
    val aliceRepo = FirebaseStepLockRepository(firestore)
    val profile = UserProfile(
      id = "alice_secret_mode",
      name = "Secret Mode",
      emoji = "🔒",
      colorHex = 0xFFFF0000,
      dailySteps = 100,
      bankedSeconds = 50,
      stepsPerMinute = 100,
      dailyStepGoal = 5000,
      bonusMinutes = 10
    )
    withTimeout(5000L) {
      aliceRepo.saveProfile(aliceUid, profile)
    }

    signInTestUser("bob_cross@test.com")
    val bobRepo = FirebaseStepLockRepository(firestore)

    try {
      withTimeout(5000L) {
        bobRepo.observeProfiles(aliceUid).first { list: List<UserProfile> -> list.isNotEmpty() }
      }
      fail("Expected FirebaseFirestoreException PERMISSION_DENIED")
    } catch (e: Exception) {
      // Flow cancellation wraps the underlying FirebaseFirestoreException
      val firestoreException = findFirestoreException(e)
      if (firestoreException != null) {
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
      } else {
        throw e
      }
    }
  }

  @Test
  fun unauthenticatedAccess_rejectedWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser("alice_unauth@test.com")
    val aliceRepo = FirebaseStepLockRepository(firestore)
    val profile = UserProfile(
      id = "alice_mode",
      name = "Mode 1",
      emoji = "🚶",
      colorHex = 0xFF00E5FF,
      dailySteps = 100,
      bankedSeconds = 50,
      stepsPerMinute = 100,
      dailyStepGoal = 5000,
      bonusMinutes = 10
    )
    withTimeout(5000L) {
      aliceRepo.saveProfile(aliceUid, profile)
    }

    auth.signOut()
    val unauthRepo = FirebaseStepLockRepository(firestore)

    try {
      withTimeout(5000L) {
        unauthRepo.observeProfiles(aliceUid).first { list: List<UserProfile> -> list.isNotEmpty() }
      }
      fail("Expected FirebaseFirestoreException PERMISSION_DENIED")
    } catch (e: Exception) {
      val firestoreException = findFirestoreException(e)
      if (firestoreException != null) {
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
      } else {
        throw e
      }
    }
  }

  private fun findFirestoreException(throwable: Throwable?): FirebaseFirestoreException? {
    var curr: Throwable? = throwable
    while (curr != null) {
      if (curr is FirebaseFirestoreException) return curr
      curr = curr.cause
    }
    return null
  }
}
