package com.example

import android.content.Context
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val TAG = "FirebaseStepLockRepo"

class FirebaseStepLockRepository(private val db: FirebaseFirestore) {

    // Convenience constructor resolving the named database ID from string resources
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    /**
     * Creates or updates the user document in /users/{userId}.
     */
    suspend fun syncUserAccount(
        user: FirebaseUser,
        activeProfileId: String,
        lifetimeSteps: Long,
        lifetimeXp: Long,
        currentStreak: Int,
        bestStreak: Int
    ) {
        try {
            val userRef = db.collection("users").document(user.uid)
            val doc = userRef.get().await()

            val baseData = mutableMapOf<String, Any>(
                "userId" to user.uid,
                "email" to (user.email ?: "anonymous@steplock.app"),
                "displayName" to (user.displayName ?: "Athlete"),
                "activeProfileId" to activeProfileId,
                "lifetimeSteps" to lifetimeSteps,
                "lifetimeXp" to lifetimeXp,
                "currentStreak" to currentStreak,
                "bestStreak" to bestStreak,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            if (!doc.exists()) {
                baseData["createdAt"] = FieldValue.serverTimestamp()
                userRef.set(baseData).await()
            } else {
                userRef.set(baseData, SetOptions.merge()).await()
            }
            Log.d(TAG, "Successfully synced user account to Firestore for ${user.uid}")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync user account to Firestore: ${e.message}", e)
        }
    }

    /**
     * Saves a walking profile mode to /users/{userId}/profiles/{profileId}.
     */
    suspend fun saveProfile(userId: String, profile: UserProfile) {
        try {
            val profileRef = db.collection("users")
                .document(userId)
                .collection("profiles")
                .document(profile.id)

            val doc = profileRef.get().await()

            val data = mutableMapOf<String, Any>(
                "profileId" to profile.id,
                "userId" to userId,
                "name" to profile.name,
                "emoji" to profile.emoji,
                "colorHex" to profile.colorHex,
                "dailySteps" to profile.dailySteps,
                "bankedSeconds" to profile.bankedSeconds,
                "stepsPerMinute" to profile.stepsPerMinute,
                "dailyStepGoal" to profile.dailyStepGoal,
                "bonusMinutes" to profile.bonusMinutes,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            if (!doc.exists()) {
                data["createdAt"] = FieldValue.serverTimestamp()
                profileRef.set(data).await()
            } else {
                profileRef.set(data, SetOptions.merge()).await()
            }
            Log.d(TAG, "Saved profile ${profile.id} to Firestore")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save profile to Firestore: ${e.message}", e)
        }
    }

    /**
     * Records a daily history entry into /users/{userId}/history/{dateKey}.
     */
    suspend fun recordDayHistory(userId: String, trend: DayTrend) {
        try {
            val historyRef = db.collection("users")
                .document(userId)
                .collection("history")
                .document(trend.dateKey)

            val data = mapOf(
                "dateKey" to trend.dateKey,
                "userId" to userId,
                "dayLabel" to trend.dayLabel,
                "steps" to trend.steps,
                "minutesEarned" to trend.minutesEarned,
                "minutesUsed" to trend.minutesUsed,
                "isToday" to trend.isToday,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            historyRef.set(data, SetOptions.merge()).await()
            Log.d(TAG, "Recorded daily history for ${trend.dateKey}")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to record daily history in Firestore: ${e.message}", e)
        }
    }

    /**
     * Observes real-time profile list from Firestore.
     */
    fun observeProfiles(userId: String): Flow<List<UserProfile>> {
        return db.collection("users")
            .document(userId)
            .collection("profiles")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val id = doc.getString("profileId") ?: doc.id
                    val name = doc.getString("name") ?: return@mapNotNull null
                    UserProfile(
                        id = id,
                        name = name,
                        emoji = doc.getString("emoji") ?: "🏃",
                        colorHex = doc.getLong("colorHex") ?: 0xFF00E5FF,
                        dailySteps = (doc.getLong("dailySteps") ?: 0L).toInt(),
                        bankedSeconds = (doc.getLong("bankedSeconds") ?: 120L).toInt(),
                        stepsPerMinute = (doc.getLong("stepsPerMinute") ?: 100L).toInt(),
                        dailyStepGoal = (doc.getLong("dailyStepGoal") ?: 6000L).toInt(),
                        bonusMinutes = (doc.getLong("bonusMinutes") ?: 15L).toInt()
                    )
                }
            }
    }

    /**
     * Observes real-time history from Firestore.
     */
    fun observeHistory(userId: String): Flow<List<DayTrend>> {
        return db.collection("users")
            .document(userId)
            .collection("history")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val dateKey = doc.getString("dateKey") ?: doc.id
                    val dayLabel = doc.getString("dayLabel") ?: "Day"
                    DayTrend(
                        dayLabel = dayLabel,
                        dateKey = dateKey,
                        steps = (doc.getLong("steps") ?: 0L).toInt(),
                        minutesEarned = (doc.getLong("minutesEarned") ?: 0L).toInt(),
                        minutesUsed = (doc.getLong("minutesUsed") ?: 0L).toInt(),
                        isToday = doc.getBoolean("isToday") ?: false
                    )
                }
            }
    }
}
