package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object GmailSyncManager {

    private const val GMAIL_SEND_URL = "https://gmail.googleapis.com/gmail/v1/users/me/messages/send"
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    fun generateWeeklyReportSubject(state: StepLockData): String {
        val totalSteps = state.weeklyTrends.sumOf { it.steps }.coerceAtLeast(state.dailySteps)
        return "Scroll Tax Weekly Report: $totalSteps Steps Walked • ${state.activeProfile.name}"
    }

    fun generateWeeklyReportPlainText(state: StepLockData): String {
        val profile = state.activeProfile
        val trends = state.weeklyTrends
        val totalSteps = trends.sumOf { it.steps }.coerceAtLeast(profile.dailySteps)
        val totalEarnedMins = trends.sumOf { it.minutesEarned }
        val totalUsedMins = trends.sumOf { it.minutesUsed } + state.dailyInstagramMinutesUsed
        val avgSteps = if (trends.isNotEmpty()) totalSteps / trends.size else totalSteps
        val dateRangeStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date())

        return buildString {
            append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            append("⚡ SCROLL TAX WEEKLY FITNESS & SCREEN-TIME AUDIT\n")
            append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")
            append("👤 Profile: ${profile.name} ${profile.emoji}\n")
            append("📅 Date: $dateRangeStr\n")
            append("🔥 Active Walking Streak: ${profile.currentStreak} Days (Best: ${profile.bestStreak} Days)\n\n")

            append("📊 WEEKLY HIGHLIGHTS:\n")
            append("• Total Steps Walked: %,d steps\n".format(totalSteps))
            append("• Daily Average: %,d steps/day\n".format(avgSteps))
            append("• Screen Time Earned: %d minutes\n".format(totalEarnedMins))
            append("• Instagram Screen Time Used: %d minutes\n".format(totalUsedMins))
            append("• Instagram Time Protected/Saved: %d minutes\n\n".format((totalEarnedMins - totalUsedMins).coerceAtLeast(0)))

            append("🔒 MORNING AUTO-LOCK STATUS:\n")
            append("• Morning Lock: Active (Instagram locked every morning until steps are walked)\n")
            append("• Current Vault Balance: ${state.bankedMinutes}m ${state.bankedSecondsRemainder}s\n")
            append("• Current Rate: ${state.currentStepsPerMinute} steps = 1 min Instagram access\n\n")

            append("🏆 UNLOCKED MILESTONES & BADGES (%d/%d):\n".format(state.unlockedAchievementsCount, state.totalAchievementsCount))
            val unlockedBadges = state.achievements.filter { it.isUnlocked }
            if (unlockedBadges.isEmpty()) {
                append("• Keep walking to unlock your first milestone badge!\n")
            } else {
                unlockedBadges.forEach { badge ->
                    append("• ${badge.badgeEmoji} ${badge.title} (${badge.tier.title} Tier): +${badge.bonusMinutesReward}m vault bonus\n")
                }
            }
            append("\n")

            append("📅 7-DAY WALK & SCREEN-TIME BREAKDOWN:\n")
            trends.forEach { day ->
                val todayMarker = if (day.isToday) " (Today)" else ""
                append("• %s%s: %,d steps | +%dm earned | -%dm Instagram\n".format(day.dayLabel, todayMarker, day.steps, day.minutesEarned, day.minutesUsed))
            }
            append("\n")
            append("Keep up the great discipline! Pay your scroll tax in steps with Scroll Tax.\n")
            append("Sent via Scroll Tax Gmail Sync Engine\n")
        }
    }

    fun generateWeeklyReportHtml(state: StepLockData): String {
        val profile = state.activeProfile
        val trends = state.weeklyTrends
        val totalSteps = trends.sumOf { it.steps }.coerceAtLeast(profile.dailySteps)
        val totalEarnedMins = trends.sumOf { it.minutesEarned }
        val totalUsedMins = trends.sumOf { it.minutesUsed } + state.dailyInstagramMinutesUsed
        val avgSteps = if (trends.isNotEmpty()) totalSteps / trends.size else totalSteps
        val dateRangeStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date())

        val tableRows = trends.joinToString("\n") { day ->
            val todayBadge = if (day.isToday) "<span style='background:#00e5ff;color:#0b1329;padding:2px 6px;border-radius:4px;font-size:10px;font-weight:bold;'>TODAY</span>" else ""
            """
            <tr style="border-bottom: 1px solid #1e293b;">
                <td style="padding: 10px 12px; font-weight: bold; color: #f8fafc;">${day.dayLabel} $todayBadge</td>
                <td style="padding: 10px 12px; color: #00e5ff; font-weight: bold;">%,d</td>
                <td style="padding: 10px 12px; color: #10b981; font-weight: bold;">+${day.minutesEarned}m</td>
                <td style="padding: 10px 12px; color: #f43f5e; font-weight: bold;">-${day.minutesUsed}m</td>
            </tr>
            """.trimIndent().format(day.steps)
        }

        val unlockedBadges = state.achievements.filter { it.isUnlocked }
        val badgesHtml = if (unlockedBadges.isEmpty()) {
            "<p style='color:#94a3b8; font-size:13px;'>Keep walking to unlock milestone badges and vault screen-time rewards!</p>"
        } else {
            unlockedBadges.joinToString("") { badge ->
                """
                <div style="display:inline-block; background:#1e293b; border:1px solid #334155; border-radius:10px; padding:8px 12px; margin:4px;">
                    <span style="font-size:18px;">${badge.badgeEmoji}</span>
                    <strong style="color:#f8fafc; font-size:13px; margin-left:6px;">${badge.title}</strong>
                    <span style="color:#fbbf24; font-size:11px; margin-left:4px;">(+${badge.bonusMinutesReward}m)</span>
                </div>
                """.trimIndent()
            }
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
        </head>
        <body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #0b1329; color: #f8fafc; margin: 0; padding: 20px;">
            <div style="max-width: 600px; margin: 0 auto; background: #0f172a; border: 1px solid #1e293b; border-radius: 18px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.5);">
                
                <!-- Header Banner -->
                <div style="background: linear-gradient(135deg, #0284c7 0%, #00e5ff 100%); padding: 28px 24px; text-align: center;">
                    <h1 style="margin: 0; color: #0b1329; font-size: 24px; font-weight: 900; letter-spacing: 0.5px;">Scroll Tax Weekly Report</h1>
                    <p style="margin: 6px 0 0 0; color: #032a3f; font-size: 14px; font-weight: 600;">Fitness & Instagram Screen-Time Report • $dateRangeStr</p>
                </div>

                <div style="padding: 24px;">
                    <!-- User Profile & Streak Card -->
                    <div style="background: #1e293b; border-radius: 14px; padding: 16px; margin-bottom: 20px; display: flex; align-items: center; justify-content: space-between;">
                        <div>
                            <span style="font-size: 20px;">${profile.emoji}</span>
                            <strong style="color: #f8fafc; font-size: 16px; margin-left: 8px;">${profile.name}</strong>
                            <div style="color: #94a3b8; font-size: 12px; margin-top: 4px;">Goal: ${String.format(Locale.getDefault(), "%,d", profile.dailyStepGoal)} steps/day</div>
                        </div>
                        <div style="text-align: right;">
                            <div style="color: #fb923c; font-weight: 900; font-size: 16px;">🔥 ${profile.currentStreak} Days</div>
                            <div style="color: #94a3b8; font-size: 11px;">Active Streak</div>
                        </div>
                    </div>

                    <!-- 3-Column Metric Highlights -->
                    <table style="width: 100%; border-collapse: separate; border-spacing: 8px; margin-bottom: 20px;">
                        <tr>
                            <td style="background: #1e293b; border-radius: 12px; padding: 14px; text-align: center; width: 33%;">
                                <div style="color: #94a3b8; font-size: 10px; font-weight: bold; text-transform: uppercase;">Total Steps</div>
                                <div style="color: #00e5ff; font-size: 20px; font-weight: 900; margin-top: 4px;">${String.format(Locale.getDefault(), "%,d", totalSteps)}</div>
                                <div style="color: #64748b; font-size: 10px;">Avg: ${String.format(Locale.getDefault(), "%,d", avgSteps)} / day</div>
                            </td>
                            <td style="background: #1e293b; border-radius: 12px; padding: 14px; text-align: center; width: 33%;">
                                <div style="color: #94a3b8; font-size: 10px; font-weight: bold; text-transform: uppercase;">Time Earned</div>
                                <div style="color: #10b981; font-size: 20px; font-weight: 900; margin-top: 4px;">+${totalEarnedMins}m</div>
                                <div style="color: #64748b; font-size: 10px;">Vault credited</div>
                            </td>
                            <td style="background: #1e293b; border-radius: 12px; padding: 14px; text-align: center; width: 33%;">
                                <div style="color: #94a3b8; font-size: 10px; font-weight: bold; text-transform: uppercase;">Instagram Used</div>
                                <div style="color: #f43f5e; font-size: 20px; font-weight: 900; margin-top: 4px;">${totalUsedMins}m</div>
                                <div style="color: #64748b; font-size: 10px;">Screen time</div>
                            </td>
                        </tr>
                    </table>

                    <!-- Morning Auto-Lock Section -->
                    <div style="background: #132039; border: 1px solid #1e3a5f; border-radius: 14px; padding: 16px; margin-bottom: 20px;">
                        <div style="font-weight: bold; color: #00e5ff; font-size: 14px;">🔒 Morning Auto-Lock Enforcement</div>
                        <p style="color: #94a3b8; font-size: 12px; line-height: 18px; margin: 6px 0 0 0;">
                            Instagram is automatically locked every morning with 0 banked minutes. Screen time access is only unlocked after walking physical steps.
                        </p>
                    </div>

                    <!-- 7-Day Activity Table -->
                    <h3 style="color: #f8fafc; font-size: 15px; margin: 0 0 10px 0;">📅 Weekly Daily Breakdown</h3>
                    <table style="width: 100%; border-collapse: collapse; background: #131c31; border-radius: 12px; overflow: hidden; font-size: 13px; margin-bottom: 20px;">
                        <thead>
                            <tr style="background: #1e293b; color: #94a3b8; text-align: left;">
                                <th style="padding: 10px 12px;">Day</th>
                                <th style="padding: 10px 12px;">Steps</th>
                                <th style="padding: 10px 12px;">Earned</th>
                                <th style="padding: 10px 12px;">Used</th>
                            </tr>
                        </thead>
                        <tbody>
                            $tableRows
                        </tbody>
                    </table>

                    <!-- Milestone Badges -->
                    <h3 style="color: #f8fafc; font-size: 15px; margin: 0 0 10px 0;">🏆 Milestone Badges & Achievements (${state.unlockedAchievementsCount}/${state.totalAchievementsCount})</h3>
                    <div style="margin-bottom: 20px;">
                        $badgesHtml
                    </div>

                    <!-- Footer -->
                    <div style="border-top: 1px solid #1e293b; padding-top: 16px; text-align: center; color: #64748b; font-size: 12px;">
                        <p style="margin: 0;">Scroll Tax • Smart physical activity screen-time lock</p>
                        <p style="margin: 4px 0 0 0;">Synchronized with your Gmail for weekly fitness & digital wellness accountability.</p>
                    </div>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * Creates an RFC 2822 MIME message and sends it via the Gmail REST API if an OAuth token is available.
     * If unauthenticated or token is expired, falls back gracefully to opening in Gmail app with pre-filled content.
     */
    suspend fun sendWeeklyReport(
        context: Context,
        recipientEmail: String,
        state: StepLockData,
        oauthAccessToken: String? = null
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val subject = generateWeeklyReportSubject(state)
        val bodyHtml = generateWeeklyReportHtml(state)
        val bodyPlain = generateWeeklyReportPlainText(state)

        if (!oauthAccessToken.isNullOrBlank()) {
            try {
                // Construct RFC 2822 MIME format
                val rawMime = buildString {
                    append("To: $recipientEmail\r\n")
                    append("Subject: =?utf-8?B?${Base64.encodeToString(subject.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)}?=\r\n")
                    append("MIME-Version: 1.0\r\n")
                    append("Content-Type: text/html; charset=utf-8\r\n")
                    append("Content-Transfer-Encoding: base64\r\n\r\n")
                    append(Base64.encodeToString(bodyHtml.toByteArray(Charsets.UTF_8), Base64.NO_WRAP))
                }

                val encodedRaw = Base64.encodeToString(
                    rawMime.toByteArray(Charsets.UTF_8),
                    Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
                )

                val jsonBody = JSONObject().apply {
                    put("raw", encodedRaw)
                }

                val request = Request.Builder()
                    .url(GMAIL_SEND_URL)
                    .addHeader("Authorization", "Bearer $oauthAccessToken")
                    .addHeader("Content-Type", "application/json")
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseCode = response.code
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    StepLockRepository.recordWeeklyReportSent("Sent successfully via Gmail API to $recipientEmail")
                    return@withContext Pair(true, "Weekly report sent successfully to $recipientEmail via Gmail API! 📬")
                } else {
                    Log.w("GmailSyncManager", "Gmail API send returned $responseCode: $responseBody. Falling back to Intent.")
                }
            } catch (e: Exception) {
                Log.e("GmailSyncManager", "Exception calling Gmail API: ${e.message}", e)
            }
        }

        // Fallback: Launch Gmail app / Mail intent with prefilled subject and body
        withContext(Dispatchers.Main) {
            launchEmailComposer(context, recipientEmail, subject, bodyPlain)
            StepLockRepository.recordWeeklyReportSent("Prepared in Gmail composer for $recipientEmail")
        }
        return@withContext Pair(true, "Weekly report opened in Gmail for $recipientEmail! Ready to review and send. ✉️")
    }

    fun launchEmailComposer(
        context: Context,
        recipientEmail: String,
        subject: String,
        bodyText: String
    ) {
        try {
            val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, bodyText)
            }

            // Check if Gmail app is installed
            val gmailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage("com.google.android.gm")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, bodyText)
            }

            val pm = context.packageManager
            if (gmailIntent.resolveActivity(pm) != null) {
                gmailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(gmailIntent)
            } else {
                mailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(Intent.createChooser(mailIntent, "Send Weekly Update via Gmail"))
            }
        } catch (e: Exception) {
            Log.e("GmailSyncManager", "Error launching email composer: ${e.message}", e)
            Toast.makeText(context, "Please configure an email app to send report.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Checks if it is time to send the weekly update (e.g. 7 days passed or Sunday).
     */
    fun shouldTriggerWeeklyReport(state: StepLockData): Boolean {
        if (!state.isGmailSyncEnabled) return false
        val now = System.currentTimeMillis()
        val sevenDaysMillis = 7 * 24 * 60 * 60 * 1000L
        if (state.lastWeeklyReportSentTimestamp == 0L) {
            return false // don't auto-send on brand new install immediately
        }
        return (now - state.lastWeeklyReportSentTimestamp) >= sevenDaysMillis
    }
}
