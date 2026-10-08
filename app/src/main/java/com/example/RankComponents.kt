package com.example

import com.example.ui.theme.*
import androidx.activity.compose.BackHandler

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.Locale

/**
 * Hero Rank Badge card on the main dashboard showing current rank title,
 * level emblem, XP progress to next rank, and lifetime steps accumulated.
 */
@Composable
fun RankBadgeDashboardCard(
    profile: UserProfile,
    onOpenRoadmap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rank = profile.rank
    val rankColor = Color(rank.colorHex)
    val secondaryColor = Color(rank.secondaryColorHex)
    val progress = rank.progressFraction(profile.lifetimeXp)

    val infiniteTransition = rememberInfiniteTransition(label = "rankGlow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onOpenRoadmap)
            .testTag("dashboard_rank_badge_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, rankColor.copy(alpha = 0.6f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            rankColor.copy(alpha = 0.22f),
                            SurfaceDark,
                            secondaryColor.copy(alpha = 0.12f)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                // Top Row: Micro Header + Clickable Roadmap Callout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(rankColor.copy(alpha = 0.25f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "LEVEL ${rank.level}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = rankColor,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "EXPERIENCE RANK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondaryDark,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceVariantDark.copy(alpha = 0.7f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "All Ranks",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = rankColor
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "View all ranks",
                            tint = rankColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Middle Row: Rank Emblem + Title & Perk
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Glowing Rank Emblem
                    Box(
                        modifier = Modifier
                            .scale(if (rank.isMaxRank) pulseScale else 1f)
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(rankColor, secondaryColor)
                                )
                            )
                            .border(2.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rank.badgeEmoji,
                            fontSize = 26.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = rank.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimaryDark,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = rank.rankPerk,
                            fontSize = 12.sp,
                            color = rankColor,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // XP Progress Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (rank.isMaxRank) "MAX LEVEL REACHED" else "${String.format(Locale.getDefault(), "%,d", profile.lifetimeXp)} / ${String.format(Locale.getDefault(), "%,d", rank.maxXp)} XP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = if (rank.isMaxRank) "MASTER" else "${(progress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = rankColor
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(SurfaceVariantDark)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(secondaryColor, rankColor)
                                    )
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Stats Row: Lifetime Steps + XP needed for promotion
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${String.format(Locale.getDefault(), "%,d", profile.lifetimeSteps)} Lifetime Steps",
                            fontSize = 11.sp,
                            color = TextSecondaryDark,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = if (rank.isMaxRank) "Pinnacle Prestige 👑" else "${String.format(Locale.getDefault(), "%,d", rank.xpNeededForNextLevel(profile.lifetimeXp))} XP to Level ${rank.level + 1}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (rank.isMaxRank) Color(0xFFFFD700) else TextMutedDark
                    )
                }
            }
        }
    }
}

/**
 * Compact Rank Badge for headers and top bars.
 */
@Composable
fun CompactRankBadge(
    rank: UserRank,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rankColor = Color(rank.colorHex)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceDark)
            .border(1.dp, rankColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("compact_rank_badge"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = rank.badgeEmoji, fontSize = 13.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "L${rank.level}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = rankColor
        )
    }
}

/**
 * Full Experience Points (XP) Roadmap & Ranks Detail Bottom Sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankRoadmapSheet(
    state: StepLockData,
    onDismiss: () -> Unit,
    onSimulateXp: (Long) -> Unit,
    onSetRankDirectly: (UserRank) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val currentRank = state.activeProfile.rank
    val currentXp = state.activeProfile.lifetimeXp
    val currentSteps = state.activeProfile.lifetimeSteps

    BackHandler { onDismiss() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundDark,
        dragHandle = null
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "XP Ranks & Roadmap",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black
                            ),
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Level up from Novice Scroller to Walking Master",
                            fontSize = 12.sp,
                            color = TextSecondaryDark
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMutedDark)
                    }
                }
            }

            // Current Standing Hero Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(currentRank.colorHex))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = currentRank.badgeEmoji, fontSize = 32.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = currentRank.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimaryDark
                                )
                                Text(
                                    text = "Current Level: ${currentRank.level} of 6",
                                    fontSize = 12.sp,
                                    color = Color(currentRank.colorHex),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total XP", fontSize = 11.sp, color = TextMutedDark)
                                Text("${String.format(Locale.getDefault(), "%,d", currentXp)} XP", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                            }
                            Column {
                                Text("Lifetime Steps", fontSize = 11.sp, color = TextMutedDark)
                                Text(String.format(Locale.getDefault(), "%,d", currentSteps), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                            }
                            Column {
                                Text("Next Rank", fontSize = 11.sp, color = TextMutedDark)
                                Text(
                                    if (currentRank.isMaxRank) "Max Rank" else "${String.format(Locale.getDefault(), "%,d", currentRank.xpNeededForNextLevel(currentXp))} XP",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFB800)
                                )
                            }
                        }
                    }
                }
            }

            // How to Earn XP Rules Explainer
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "⚡ HOW TO EARN XP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyanAccent,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• 1 Physical Step = 1 Base XP", fontSize = 12.sp, color = TextPrimaryDark)
                        Text("• ☀️ Golden Hour Walks = 2x Double XP", fontSize = 12.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.SemiBold)
                        Text("• 🎯 Daily Step Goal Achieved = +500 Bonus XP", fontSize = 12.sp, color = EmeraldNeon)
                        Text("• 🏆 Unlocking Milestone Badges = +250 Bonus XP", fontSize = 12.sp, color = TextPrimaryDark)
                        Text("• 🔥 3-Day & 7-Day Walking Streaks = Extra XP Multipliers", fontSize = 12.sp, color = TextSecondaryDark)
                    }
                }
            }

            // Section Header
            item {
                Text(
                    text = "ROADMAP TIERS (1 TO 6)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextSecondaryDark,
                    letterSpacing = 1.sp
                )
            }

            // All 6 Tiers in Progression Order
            items(UserRank.entries) { rankTier ->
                val isUnlocked = currentXp >= rankTier.minXp
                val isCurrent = currentRank == rankTier
                val tierColor = Color(rankTier.colorHex)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rank_tier_card_${rankTier.level}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) SurfaceDark else BackgroundDark
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        if (isCurrent) 2.dp else 1.dp,
                        if (isCurrent) tierColor else if (isUnlocked) tierColor.copy(alpha = 0.4f) else Color(0xFF1E293B)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = rankTier.badgeEmoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Level ${rankTier.level}: ${rankTier.title}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUnlocked) TextPrimaryDark else TextMutedDark
                                        )
                                        if (isCurrent) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(tierColor)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("CURRENT", fontSize = 9.sp, fontWeight = FontWeight.Black, color = BackgroundDark)
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${String.format(Locale.getDefault(), "%,d", rankTier.minXp)} - ${if (rankTier.isMaxRank) "30,000+ XP" else "${String.format(Locale.getDefault(), "%,d", rankTier.maxXp)} XP"}",
                                        fontSize = 11.sp,
                                        color = if (isUnlocked) tierColor else TextMutedDark
                                    )
                                }
                            }

                            if (isUnlocked) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Unlocked",
                                    tint = if (isCurrent) tierColor else EmeraldNeon,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = TextMutedDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = rankTier.loreDescription,
                            fontSize = 12.sp,
                            color = if (isUnlocked) TextSecondaryDark else TextMutedDark
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "🎁 Perk: ${rankTier.rankPerk}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isUnlocked) tierColor else TextMutedDark
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isCurrent) {
                            Button(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    disabledContainerColor = tierColor.copy(alpha = 0.25f),
                                    disabledContentColor = tierColor
                                )
                            ) {
                                Text("Active Level ⚡", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    onSetRankDirectly(rankTier)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .testTag("switch_to_level_${rankTier.level}_button"),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, tierColor),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = tierColor)
                            ) {
                                Text("Switch to Level ${rankTier.level} (${rankTier.title})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Developer / QA Simulation Tools to Test Rank Advancement
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "🛠️ SIMULATE XP GAIN & TEST PROMOTIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMutedDark,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onSimulateXp(500L) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("+500 XP", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { onSimulateXp(2500L) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("+2.5k XP", fontSize = 11.sp)
                            }
                            Button(
                                onClick = { onSetRankDirectly(UserRank.WALKING_MASTER) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2A85))
                            ) {
                                Text("👑 Master", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Animated Celebration Dialog shown when the user levels up to a new Rank tier.
 */
@Composable
fun RankLevelUpCelebrationDialog(
    rank: UserRank,
    onDismiss: () -> Unit
) {
    val tierColor = Color(rank.colorHex)
    val secondaryColor = Color(rank.secondaryColorHex)

    val infiniteTransition = rememberInfiniteTransition(label = "celebrationHalo")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("rank_level_up_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(2.dp, tierColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                tierColor.copy(alpha = 0.35f),
                                SurfaceDark,
                                secondaryColor.copy(alpha = 0.2f)
                            )
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🎉 RANK PROMOTION!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD700),
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Huge pulsing emblem
                    Box(
                        modifier = Modifier
                            .scale(pulse)
                            .size(86.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(tierColor, secondaryColor)
                                )
                            )
                            .border(3.dp, Color.White, RoundedCornerShape(26.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = rank.badgeEmoji, fontSize = 42.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Level ${rank.level}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = tierColor
                    )

                    Text(
                        text = rank.title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimaryDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = rank.loreDescription,
                        fontSize = 13.sp,
                        color = TextSecondaryDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Unlocked Perks Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BackgroundDark.copy(alpha = 0.7f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "🎁 PROMOTION REWARDS:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = tierColor,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• +15 Minutes Screen Time bonus deposited into vault",
                                fontSize = 12.sp,
                                color = EmeraldNeon,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "• ${rank.rankPerk}",
                                fontSize = 12.sp,
                                color = TextPrimaryDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("claim_promotion_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = tierColor)
                    ) {
                        Text(
                            text = "CLAIM RANK & KEEP WALKING ⚡",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = BackgroundDark
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dedicated full-screen Levels Roadmap & Rank Switcher.
 * Back button navigates back to Dashboard instead of exiting the app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelsRoadmapScreen(
    state: StepLockData,
    onNavigateBack: () -> Unit,
    onSimulateXp: (Long) -> Unit,
    onSetRankDirectly: (UserRank) -> Unit
) {
    BackHandler { onNavigateBack() }
    val currentRank = state.activeProfile.rank
    val currentXp = state.activeProfile.lifetimeXp
    val currentSteps = state.activeProfile.lifetimeSteps

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("levels_roadmap_screen"),
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "XP Ranks & Roadmap",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Switch between levels or inspect perks",
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("levels_roadmap_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Dashboard",
                            tint = TextPrimaryDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Current Standing Hero Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(currentRank.colorHex))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = currentRank.badgeEmoji, fontSize = 32.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = currentRank.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimaryDark
                                )
                                Text(
                                    text = "Active Level: ${currentRank.level} of 6",
                                    fontSize = 12.sp,
                                    color = Color(currentRank.colorHex),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total XP", fontSize = 11.sp, color = TextMutedDark)
                                Text("${String.format(java.util.Locale.getDefault(), "%,d", currentXp)} XP", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                            }
                            Column {
                                Text("Lifetime Steps", fontSize = 11.sp, color = TextMutedDark)
                                Text(String.format(java.util.Locale.getDefault(), "%,d", currentSteps), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                            }
                            Column {
                                Text("Next Rank", fontSize = 11.sp, color = TextMutedDark)
                                Text(
                                    if (currentRank.isMaxRank) "Max Rank" else "${String.format(java.util.Locale.getDefault(), "%,d", currentRank.xpNeededForNextLevel(currentXp))} XP",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(currentRank.colorHex)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { currentRank.progressFraction(currentXp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = Color(currentRank.colorHex),
                            trackColor = Color(0xFF1E293B)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "ROADMAP TIERS (1 TO 6)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextSecondaryDark,
                    letterSpacing = 1.sp
                )
            }

            // All 6 Tiers in Progression Order
            items(UserRank.entries) { rankTier ->
                val isUnlocked = currentXp >= rankTier.minXp
                val isCurrent = currentRank == rankTier
                val tierColor = Color(rankTier.colorHex)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rank_tier_screen_card_${rankTier.level}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) SurfaceDark else BackgroundDark
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        if (isCurrent) 2.dp else 1.dp,
                        if (isCurrent) tierColor else if (isUnlocked) tierColor.copy(alpha = 0.4f) else Color(0xFF1E293B)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = rankTier.badgeEmoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Level ${rankTier.level}: ${rankTier.title}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUnlocked) TextPrimaryDark else TextMutedDark
                                        )
                                        if (isCurrent) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(tierColor)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("ACTIVE", fontSize = 9.sp, fontWeight = FontWeight.Black, color = BackgroundDark)
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${String.format(java.util.Locale.getDefault(), "%,d", rankTier.minXp)} - ${if (rankTier.isMaxRank) "30,000+ XP" else "${String.format(java.util.Locale.getDefault(), "%,d", rankTier.maxXp)} XP"}",
                                        fontSize = 11.sp,
                                        color = if (isUnlocked) tierColor else TextMutedDark
                                    )
                                }
                            }

                            if (isUnlocked) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Unlocked",
                                    tint = if (isCurrent) tierColor else EmeraldNeon,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = TextMutedDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = rankTier.loreDescription,
                            fontSize = 12.sp,
                            color = if (isUnlocked) TextSecondaryDark else TextMutedDark
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "🎁 Perk: ${rankTier.rankPerk}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isUnlocked) tierColor else TextMutedDark
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isCurrent) {
                            Button(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    disabledContainerColor = tierColor.copy(alpha = 0.25f),
                                    disabledContentColor = tierColor
                                )
                            ) {
                                Text("Active Level ⚡", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    onSetRankDirectly(rankTier)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .testTag("switch_to_level_${rankTier.level}_screen_button"),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, tierColor),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = tierColor)
                            ) {
                                Text("Switch to Level ${rankTier.level} (${rankTier.title})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
