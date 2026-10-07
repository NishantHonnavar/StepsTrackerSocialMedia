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
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BlueCard
import com.example.ui.theme.BlueCardLight
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.UnlockGreen
import com.example.ui.theme.WarningAmber

object SocialShareHelper {

    fun generateShareText(achievement: Achievement, profile: UserProfile): String {
        return buildString {
            append("🏆 I just unlocked the '${achievement.title}' badge on Scroll Tax! 🚶‍♂️⚡\n\n")
            append("${achievement.badgeEmoji} Tier: ${achievement.tier.title} (${achievement.category.title})\n")
            append("📜 Milestone: ${achievement.description}\n")
            append("🔥 Active Streak: ${profile.currentStreak} Days\n")
            append("👟 Total Steps Today: ${profile.dailySteps}\n")
            append("⏱️ Reward: +${achievement.bonusMinutesReward}m screen time credited to Instagram vault!\n\n")
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

    fun copyToClipboard(context: Context, text: String, label: String = "Scroll Tax Badge") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "Copied achievement to clipboard! 📋", Toast.LENGTH_SHORT).show()
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
                    "com.twitter.android" -> "X / Twitter"
                    "com.whatsapp" -> "WhatsApp"
                    else -> "Target app"
                }
                Toast.makeText(context, "$appName not found, opening share options...", Toast.LENGTH_SHORT).show()
            }
        }

        val chooser = Intent.createChooser(baseIntent, "Share Badge to Social Media")
        chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "No app available to share", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun SocialShareBadgeDialog(
    achievement: Achievement,
    profile: UserProfile,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val tierColor = Color(achievement.tier.colorHex)
    var copied by remember { mutableStateOf(false) }

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
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, tierColor)
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
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Share Milestone Badge",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(tierColor.copy(alpha = 0.2f))
                            .border(0.8.dp, tierColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = achievement.tier.title.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = tierColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Social Post Preview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF131F33),
                                    Color(0xFF0B1322),
                                    Color(0xFF070D17)
                                )
                            )
                        )
                        .border(1.dp, tierColor.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                        .testTag("social_post_preview_card")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Badge Icon
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(achievement.tier.bgHex))
                                .border(2.dp, tierColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = achievement.badgeEmoji, fontSize = 32.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = achievement.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimaryDark,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = achievement.description,
                            fontSize = 12.sp,
                            color = TextSecondaryDark,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                        )

                        // Stats row in preview card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceVariantDark.copy(alpha = 0.6f))
                                .padding(vertical = 8.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = Color(0xFFFF9800),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${profile.currentStreak}d",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryDark
                                    )
                                }
                                Text("Streak", fontSize = 10.sp, color = TextMutedDark)
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(22.dp)
                                    .background(SurfaceVariantDark)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${profile.dailySteps}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                                Text("Steps Today", fontSize = 10.sp, color = TextMutedDark)
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(22.dp)
                                    .background(SurfaceVariantDark)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "+${achievement.bonusMinutesReward}m",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldNeon
                                )
                                Text("Vault Reward", fontSize = 10.sp, color = TextMutedDark)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // App Footer in Card
                        Text(
                            text = "⚡ Earned with Scroll Tax • Pay Your Scroll Tax in Steps",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyanAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Post to Platform",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryDark,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Platform Action Buttons Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Instagram Button
                    Button(
                        onClick = {
                            SocialShareHelper.shareAchievement(
                                context = context,
                                achievement = achievement,
                                profile = profile,
                                targetPackage = "com.instagram.android"
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_instagram_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1306C)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            text = "📸 Instagram",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Twitter / X Button
                    Button(
                        onClick = {
                            SocialShareHelper.shareAchievement(
                                context = context,
                                achievement = achievement,
                                profile = profile,
                                targetPackage = "com.twitter.android"
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_twitter_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DA1F2)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            text = "🐦 X / Twitter",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // WhatsApp Button
                    Button(
                        onClick = {
                            SocialShareHelper.shareAchievement(
                                context = context,
                                achievement = achievement,
                                profile = profile,
                                targetPackage = "com.whatsapp"
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_whatsapp_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            text = "💬 WhatsApp",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // System Share (Any App) Button
                Button(
                    onClick = {
                        SocialShareHelper.shareAchievement(
                            context = context,
                            achievement = achievement,
                            profile = profile,
                            targetPackage = null
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("share_any_app_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Icon(
                        imageVector = Icons.Default.IosShare,
                        contentDescription = null,
                        tint = BackgroundDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share to Any App (System Chooser)",
                        color = BackgroundDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Copy Text Button & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val text = SocialShareHelper.generateShareText(achievement, profile)
                            SocialShareHelper.copyToClipboard(context, text)
                            copied = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_badge_text_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = if (copied) EmeraldNeon else CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (copied) "Copied!" else "Copy Text",
                            fontSize = 12.sp,
                            color = if (copied) EmeraldNeon else CyanAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("close_share_dialog_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Done",
                            fontSize = 12.sp,
                            color = TextSecondaryDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
