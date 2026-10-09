package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.DarkSlateNavy
import com.example.ui.theme.DeepLavender
import com.example.ui.theme.DeepVoidNavy
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.InnerCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividPurple
import java.util.Locale

object SocialShareHelper {

    fun generateDailyStatsShareText(state: StepLockData): String {
        val profile = state.activeProfile
        val distanceKm = state.dailySteps * 0.00078f
        val activeCalories = (state.dailySteps * 0.04f).toInt()
        val blockedAppsNames = state.blockedApps.filter { it.isBlocked }.joinToString(", ") { it.appName }

        return buildString {
            append("👟 My Daily Activity on Scroll Tax! 🏃‍♂️⚡\n\n")
            append("• Steps Today: %,d / %,d (%d%%)\n".format(Locale.getDefault(), state.dailySteps, state.dailyStepGoal, state.goalPercentage))
            append("• Distance Walked: %.2f km\n".format(Locale.getDefault(), distanceKm))
            append("• Active Burn: %d kcal\n".format(Locale.getDefault(), activeCalories))
            append("• Screen Time Banked: %dm %ds\n".format(Locale.getDefault(), state.bankedMinutes, state.bankedSecondsRemainder))
            append("• Active Streak: %d Days 🔥\n".format(Locale.getDefault(), state.currentStreak))
            append("• Experience Rank: Level %d (%s %s)\n".format(Locale.getDefault(), state.rank.level, state.rank.title, state.rank.badgeEmoji))
            if (blockedAppsNames.isNotBlank()) {
                append("• Distraction Lock: %s\n".format(Locale.getDefault(), blockedAppsNames))
            }
            append("\nEarning my screen time strictly with steps. No free scrolling! 🚀\n")
            append("#ScrollTax #DigitalDetox #DailySteps #WalkToEarn #FitnessJourney")
        }
    }

    fun generateShareText(achievement: Achievement, profile: UserProfile): String {
        return buildString {
            append("🏆 I just unlocked the '${achievement.title}' badge on Scroll Tax! 🚶‍♂️⚡\n\n")
            append("${achievement.badgeEmoji} Tier: ${achievement.tier.title} (${achievement.category.title})\n")
            append("📜 Milestone: ${achievement.description}\n")
            append("🔥 Active Streak: ${profile.currentStreak} Days\n")
            append("👟 Total Steps Today: ${profile.dailySteps}\n")
            append("⏱️ Reward: +${achievement.bonusMinutesReward}m screen time credited to vault!\n\n")
            append("Pay your scroll tax with steps! #ScrollTax #WalkToEarn #DigitalWellness #FitnessGoals")
        }
    }

    fun generateTrophyCabinetShareText(
        profile: UserProfile,
        unlockedCount: Int,
        totalCount: Int,
        achievements: List<Achievement>
    ): String {
        val unlockedBadges = achievements.filter { it.isUnlocked }
        val emojis = unlockedBadges.joinToString(" ") { it.badgeEmoji }

        return buildString {
            append("🏅 My Scroll Tax Milestones & Trophy Cabinet! 🏃‍♂️✨\n\n")
            append("👤 Profile: ${profile.name} ${profile.emoji}\n")
            append("🎯 Badges Unlocked: $unlockedCount of $totalCount\n")
            append("🔥 Current Streak: ${profile.currentStreak} Days (Best: ${profile.bestStreak} Days)\n")
            append("👟 Today's Steps: ${profile.dailySteps} / ${profile.dailyStepGoal}\n")
            if (emojis.isNotEmpty()) {
                append("🎖️ Unlocked Badges: $emojis\n\n")
            }
            append("Earning screen time one step at a time! 🚀\n")
            append("#ScrollTax #MilestoneAchievements #FitnessStreak #DigitalDetox")
        }
    }

    fun shareDailyStats(
        context: Context,
        state: StepLockData,
        targetPackage: String? = null
    ) {
        val shareText = generateDailyStatsShareText(state)
        executeShareIntent(context, shareText, "Scroll Tax Daily Stats: %,d Steps".format(Locale.getDefault(), state.dailySteps), targetPackage)
    }

    fun shareAchievement(
        context: Context,
        achievement: Achievement,
        profile: UserProfile,
        targetPackage: String? = null
    ) {
        val shareText = generateShareText(achievement, profile)
        executeShareIntent(context, shareText, "Scroll Tax Badge: ${achievement.title}", targetPackage)
    }

    fun shareTrophyCabinet(
        context: Context,
        profile: UserProfile,
        unlockedCount: Int,
        totalCount: Int,
        achievements: List<Achievement>,
        targetPackage: String? = null
    ) {
        val shareText = generateTrophyCabinetShareText(profile, unlockedCount, totalCount, achievements)
        executeShareIntent(context, shareText, "My Scroll Tax Achievements & Badges", targetPackage)
    }

    fun sharePlainText(
        context: Context,
        subject: String,
        text: String,
        targetPackage: String? = null
    ) {
        executeShareIntent(context, text, subject, targetPackage)
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Scroll Tax Stats") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
    }

    private fun executeShareIntent(
        context: Context,
        text: String,
        subject: String,
        targetPackage: String?
    ) {
        val baseIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }

        if (targetPackage != null) {
            val isPackageInstalled = try {
                context.packageManager.getPackageInfo(targetPackage, 0)
                true
            } catch (e: Exception) {
                false
            }

            if (isPackageInstalled) {
                baseIntent.setPackage(targetPackage)
                try {
                    context.startActivity(baseIntent)
                    return
                } catch (e: Exception) {
                    // Fallback to chooser
                }
            } else {
                val appName = when (targetPackage) {
                    "com.instagram.android" -> "Instagram"
                    "com.whatsapp" -> "WhatsApp"
                    "com.facebook.katana" -> "Facebook"
                    "com.pinterest" -> "Pinterest"
                    "com.twitter.android" -> "X / Twitter"
                    else -> "Target app"
                }
                Toast.makeText(context, "$appName not found, opening share options...", Toast.LENGTH_SHORT).show()
            }
        }

        val chooser = Intent.createChooser(baseIntent, "Share Daily Stats")
        chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "No app available to share", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Dialog to share daily steps and movement stats to WhatsApp, Instagram, and any app.
 */
@Composable
fun SocialShareDailyStatsDialog(
    state: StepLockData,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }

    val distanceKm = state.dailySteps * 0.00078f
    val activeCalories = (state.dailySteps * 0.04f).toInt()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("social_share_daily_stats_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateNavy),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ElectricCyan.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ElectricCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Share Daily Activity",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "Broadcast steps & digital tax stats",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricCyan.copy(alpha = 0.2f))
                            .border(1.dp, ElectricCyan, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${state.goalPercentage}% GOAL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ElectricCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Activity Preview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF1E1235),
                                    Color(0xFF140D24),
                                    DeepVoidNavy
                                )
                            )
                        )
                        .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                        .testTag("daily_stats_preview_card")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "👟 TODAY'S STRIDE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DeepLavender,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${state.rank.badgeEmoji} Level ${state.rank.level} ${state.rank.title}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = String.format(Locale.getDefault(), "%,d", state.dailySteps),
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = "Daily Step Goal: %,d steps".format(Locale.getDefault(), state.dailyStepGoal),
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Grid in Preview
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(InnerCardBorder.copy(alpha = 0.6f))
                                .padding(vertical = 8.dp, horizontal = 10.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "%.2f km".format(Locale.getDefault(), distanceKm),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text("Distance", fontSize = 10.sp, color = TextMuted)
                            }

                            Box(modifier = Modifier.width(1.dp).height(20.dp).background(InnerCardBorder))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "%d kcal".format(Locale.getDefault(), activeCalories),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text("Calories", fontSize = 10.sp, color = TextMuted)
                            }

                            Box(modifier = Modifier.width(1.dp).height(20.dp).background(InnerCardBorder))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "%dm %ds".format(Locale.getDefault(), state.bankedMinutes, state.bankedSecondsRemainder),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                                Text("Vault", fontSize = 10.sp, color = TextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "⚡ Scroll Tax • Screen time earned by physical movement",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepLavender
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Share Directly To",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Platform Buttons (WhatsApp, Instagram, Facebook, X / Twitter)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // WhatsApp
                    Button(
                        onClick = {
                            SocialShareHelper.shareDailyStats(context, state, "com.whatsapp")
                        },
                        modifier = Modifier.weight(1f).testTag("share_stats_whatsapp_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Text(text = "💬 WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Instagram
                    Button(
                        onClick = {
                            SocialShareHelper.shareDailyStats(context, state, "com.instagram.android")
                        },
                        modifier = Modifier.weight(1f).testTag("share_stats_instagram_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1306C)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Text(text = "📸 Instagram", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Facebook
                    Button(
                        onClick = {
                            SocialShareHelper.shareDailyStats(context, state, "com.facebook.katana")
                        },
                        modifier = Modifier.weight(1f).testTag("share_stats_facebook_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Text(text = "👥 Facebook", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Twitter / X
                    Button(
                        onClick = {
                            SocialShareHelper.shareDailyStats(context, state, "com.twitter.android")
                        },
                        modifier = Modifier.weight(1f).testTag("share_stats_twitter_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DA1F2)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Text(text = "🐦 X / Twitter", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // System Share (Any Other App) Button
                Button(
                    onClick = {
                        SocialShareHelper.shareDailyStats(context, state, null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("share_stats_any_app_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Icon(
                        imageVector = Icons.Default.IosShare,
                        contentDescription = null,
                        tint = DeepVoidNavy,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share to Any Other Platform",
                        color = DeepVoidNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Copy Text & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val text = SocialShareHelper.generateDailyStatsShareText(state)
                            SocialShareHelper.copyToClipboard(context, text, "Daily Steps")
                            copied = true
                        },
                        modifier = Modifier.weight(1f).testTag("copy_stats_text_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = if (copied) EmeraldNeon else ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (copied) "Copied!" else "Copy Stats",
                            fontSize = 12.sp,
                            color = if (copied) EmeraldNeon else ElectricCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).testTag("close_stats_share_dialog_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Done",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog to share milestone achievement badges to WhatsApp, Instagram, Facebook, and any other app.
 */
@Composable
fun SocialShareBadgeDialog(
    achievement: Achievement,
    profile: UserProfile,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    val tierColor = Color(achievement.tier.colorHex)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("social_share_badge_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateNavy),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, tierColor.copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(tierColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = tierColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Milestone Unlocked!",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "Share your victory badge",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(tierColor.copy(alpha = 0.2f))
                            .border(1.dp, tierColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = achievement.tier.title.uppercase(Locale.getDefault()),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = tierColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Badge Spotlight Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF20133A),
                                    Color(0xFF140D24),
                                    DeepVoidNavy
                                )
                            )
                        )
                        .border(1.dp, tierColor.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                        .testTag("badge_share_spotlight_card")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Badge Emoji with glowing circular backdrop
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(tierColor.copy(alpha = 0.15f))
                                .border(1.5.dp, tierColor.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = achievement.badgeEmoji,
                                fontSize = 34.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = achievement.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = achievement.description,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(InnerCardBorder)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎁 Reward: +${achievement.bonusMinutesReward}m Screen Time",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldNeon
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Share Directly To",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Platform Buttons (WhatsApp, Instagram)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            SocialShareHelper.shareAchievement(context, achievement, profile, "com.whatsapp")
                        },
                        modifier = Modifier.weight(1f).testTag("share_badge_whatsapp_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Text(text = "💬 WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = {
                            SocialShareHelper.shareAchievement(context, achievement, profile, "com.instagram.android")
                        },
                        modifier = Modifier.weight(1f).testTag("share_badge_instagram_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1306C)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Text(text = "📸 Instagram", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Platform Buttons (Facebook, X / Twitter)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            SocialShareHelper.shareAchievement(context, achievement, profile, "com.facebook.katana")
                        },
                        modifier = Modifier.weight(1f).testTag("share_badge_facebook_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Text(text = "👥 Facebook", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = {
                            SocialShareHelper.shareAchievement(context, achievement, profile, "com.twitter.android")
                        },
                        modifier = Modifier.weight(1f).testTag("share_badge_twitter_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DA1F2)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Text(text = "🐦 X / Twitter", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Any Platform Button
                Button(
                    onClick = {
                        SocialShareHelper.shareAchievement(context, achievement, profile, null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("share_badge_any_app_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Icon(
                        imageVector = Icons.Default.IosShare,
                        contentDescription = null,
                        tint = DeepVoidNavy,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share to Any Other Platform",
                        color = DeepVoidNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Copy Text & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val text = SocialShareHelper.generateShareText(achievement, profile)
                            SocialShareHelper.copyToClipboard(context, text, "Badge Achievement")
                            copied = true
                        },
                        modifier = Modifier.weight(1f).testTag("copy_badge_share_text_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = if (copied) EmeraldNeon else ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (copied) "Copied!" else "Copy Text",
                            fontSize = 12.sp,
                            color = if (copied) EmeraldNeon else ElectricCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).testTag("close_badge_share_dialog_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Done",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
