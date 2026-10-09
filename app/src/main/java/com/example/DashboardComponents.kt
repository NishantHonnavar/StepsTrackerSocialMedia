package com.example

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CrimsonCoral
import com.example.ui.theme.DarkSlateNavy
import com.example.ui.theme.DeepLavender
import com.example.ui.theme.DeepVoidNavy
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.InnerCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmberGold
import java.util.Locale

/**
 * Scale-down bounce feedback on button taps (scale = 0.97)
 */
fun Modifier.bounceClickable(onClick: () -> Unit): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "bounce"
    )
    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}

// ============================================================================
// 0A. ACTIVE MODE BANNER (PROMINENT MODE SWITCHER)
// ============================================================================

@Composable
fun ActiveModeBanner(
    activeProfile: UserProfile,
    onChangeModeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profileColor = Color(activeProfile.colorHex)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("active_mode_banner"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateNavy),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    profileColor.copy(alpha = 0.6f),
                    ElectricCyan.copy(alpha = 0.3f),
                    InnerCardBorder
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(profileColor.copy(alpha = 0.2f))
                        .border(1.dp, profileColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = activeProfile.emoji, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ACTIVE MODE:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = TextMuted,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = activeProfile.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "${activeProfile.stepsPerMinute} steps = 1m • Goal: ${String.format(Locale.getDefault(), "%,d", activeProfile.dailyStepGoal)}",
                        fontSize = 11.sp,
                        color = profileColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onChangeModeClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan.copy(alpha = 0.15f),
                    contentColor = ElectricCyan
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.6f)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("change_mode_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Change Mode",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Change",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ============================================================================
// 0B. QUICK MODE SWITCHER BOTTOM SHEET
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSwitcherBottomSheet(
    profiles: List<UserProfile>,
    activeProfileId: String,
    onDismiss: () -> Unit,
    onSelectProfile: (String) -> Unit,
    onManageProfiles: () -> Unit
) {
    BackHandler { onDismiss() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepVoidNavy,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Change Active Mode",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = "Switch between walking paces and screen time targets",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "SAVED MODES & PROFILES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.weight(weight = 1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(profiles, key = { it.id }) { profile ->
                    val isSelected = profile.id == activeProfileId
                    val profileColor = Color(profile.colorHex)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectProfile(profile.id)
                                onDismiss()
                            }
                            .testTag("mode_item_${profile.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) DarkSlateNavy else DeepVoidNavy
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) profileColor else InnerCardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(profileColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = profile.emoji, fontSize = 20.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = profile.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(profileColor)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "ACTIVE",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = DeepVoidNavy
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${profile.stepsPerMinute} steps = 1m • Goal: ${String.format(Locale.getDefault(), "%,d", profile.dailyStepGoal)}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Active",
                                    tint = profileColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Switch",
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Manage / Add Profile Button
            OutlinedButton(
                onClick = {
                    onDismiss()
                    onManageProfiles()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("manage_modes_button"),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, InnerCardBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = ElectricCyan
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Manage All Modes & Custom Profiles",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }
    }
}

// ============================================================================
// 0C. RATE MODE SWITCHER DIALOG (TIME-OF-DAY RATE TOGGLE)
// ============================================================================

@Composable
fun RateModeSwitcherDialog(
    currentPeriod: TimeOfDayPeriod,
    isSimulated: Boolean,
    onSelectPeriod: (TimeOfDayPeriod?) -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler { onDismiss() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("rate_mode_switcher_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateNavy),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ElectricCyan.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Change Rate Mode",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = "Adjust step cost per minute of screen time",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Option 0: Auto Clock Sync
                RateModeOptionCard(
                    title = "Automatic (Clock Sync)",
                    subtitle = "Changes with time of day (Golden, Standard, Night)",
                    badge = "AUTO",
                    color = ElectricCyan,
                    isSelected = !isSimulated,
                    onClick = {
                        onSelectPeriod(null)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Option 1: Golden Hour
                RateModeOptionCard(
                    title = "☀️ Golden Hour (2x Bonus)",
                    subtitle = "50 steps = 1 minute screen time (Normal 06:00-08:00)",
                    badge = "2X BOOST",
                    color = WarmAmberGold,
                    isSelected = isSimulated && currentPeriod == TimeOfDayPeriod.GOLDEN_HOUR,
                    onClick = {
                        onSelectPeriod(TimeOfDayPeriod.GOLDEN_HOUR)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Option 2: Standard Day
                RateModeOptionCard(
                    title = "⚡ Standard Active Rate",
                    subtitle = "100 steps = 1 minute screen time (Normal 08:00-22:00)",
                    badge = "NORMAL",
                    color = ElectricCyan,
                    isSelected = isSimulated && currentPeriod == TimeOfDayPeriod.STANDARD_DAY,
                    onClick = {
                        onSelectPeriod(TimeOfDayPeriod.STANDARD_DAY)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Option 3: Night Surge
                RateModeOptionCard(
                    title = "🌙 Sleep Protection Surge",
                    subtitle = "250 steps = 1 minute screen time (Strict bedtime lock)",
                    badge = "STRICT",
                    color = DeepLavender,
                    isSelected = isSimulated && currentPeriod == TimeOfDayPeriod.NIGHT_SURGE,
                    onClick = {
                        onSelectPeriod(TimeOfDayPeriod.NIGHT_SURGE)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
fun RateModeOptionCard(
    title: String,
    subtitle: String,
    badge: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.15f) else DarkSlateNavy
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) color else InnerCardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) color else TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(color.copy(alpha = 0.2f))
                    .border(1.dp, color, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = color
                )
            }
        }
    }
}

// ============================================================================
// 0D. INTERACTIVE RANK LEVEL SWITCHER BAR
// ============================================================================

@Composable
fun RankLevelSwitcherBar(
    currentRank: UserRank,
    onSelectRank: (UserRank) -> Unit,
    onOpenRoadmap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tierColor = Color(currentRank.colorHex)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("rank_level_switcher_bar"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateNavy),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    tierColor.copy(alpha = 0.6f),
                    ElectricCyan.copy(alpha = 0.3f),
                    InnerCardBorder
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(tierColor.copy(alpha = 0.2f))
                            .border(1.dp, tierColor, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = currentRank.badgeEmoji, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LEVEL ${currentRank.level}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = tierColor,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "•  ${currentRank.title}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = currentRank.rankPerk,
                            fontSize = 10.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(InnerCardBorder.copy(alpha = 0.6f))
                        .clickable { onOpenRoadmap() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Roadmap",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open Roadmap",
                        tint = ElectricCyan,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal interactive row with all 6 tiers with clean badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UserRank.entries.forEach { rank ->
                    val isSelected = rank == currentRank
                    val rankColor = Color(rank.colorHex)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) rankColor.copy(alpha = 0.25f)
                                else InnerCardBorder.copy(alpha = 0.45f)
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) rankColor else InnerCardBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectRank(rank) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = rank.badgeEmoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "L${rank.level} ${rank.title}",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                color = if (isSelected) rankColor else TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 1. CARD 1: TODAY'S MOVEMENT (HERO)
// ============================================================================

@Composable
fun TodayMovementHeroCard(
    dailySteps: Int,
    dailyStepGoal: Int,
    currentRate: Int,
    period: TimeOfDayPeriod,
    onShareClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val targetProgress = if (dailyStepGoal > 0) {
        (dailySteps.toFloat() / dailyStepGoal.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(targetProgress) {
        animatedProgress.animateTo(
            targetValue = targetProgress,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val accentColor by animateColorAsState(
        targetValue = when (period) {
            TimeOfDayPeriod.GOLDEN_HOUR -> WarmAmberGold
            TimeOfDayPeriod.STANDARD_DAY -> ElectricCyan
            TimeOfDayPeriod.NIGHT_SURGE -> DeepLavender
        },
        animationSpec = tween(400),
        label = "periodColor"
    )

    val distanceKm = dailySteps * 0.00078f
    val activeCalories = (dailySteps * 0.04f).toInt()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_movement_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateNavy),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            Brush.verticalGradient(
                listOf(accentColor.copy(alpha = 0.5f), InnerCardBorder)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header row with title, goal badge and share button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TODAY'S MOVEMENT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(InnerCardBorder)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${String.format(Locale.getDefault(), "%,d", dailySteps)} / ${String.format(Locale.getDefault(), "%,d", dailyStepGoal)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .testTag("share_daily_steps_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Steps",
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Central Circular Progress Ring (Sweep Animation with Ambient Glow)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                // Ambient Radial Glow
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(accentColor.copy(alpha = 0.15f), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )

                Canvas(modifier = Modifier.size(200.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                    val arcSize = Size(diameter, diameter)

                    // Track background
                    drawArc(
                        color = InnerCardBorder,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Animated Progress arc with gradient
                    if (animatedProgress.value > 0f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                0.0f to accentColor.copy(alpha = 0.6f),
                                0.6f to accentColor,
                                1.0f to EmeraldNeon
                            ),
                            startAngle = -90f,
                            sweepAngle = animatedProgress.value * 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Digital Step Counter in Center
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format(Locale.getDefault(), "%,d", dailySteps),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = "STEPS TODAY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(targetProgress * 100).toInt()}% OF DAILY GOAL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            HorizontalDivider(color = InnerCardBorder, thickness = 1.dp)

            Spacer(modifier = Modifier.height(18.dp))

            // 3-Column Sub-Metrics Row: Distance, Active Burn, Current Rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Metric 1: Distance
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Route,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "DISTANCE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "%.2f km", distanceKm),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Vertical Divider
                Box(modifier = Modifier.width(1.dp).height(30.dp).background(InnerCardBorder))

                // Metric 2: Active Burn
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Fireplace,
                            contentDescription = null,
                            tint = WarmAmberGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ACTIVE BURN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$activeCalories kcal",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Vertical Divider
                Box(modifier = Modifier.width(1.dp).height(30.dp).background(InnerCardBorder))

                // Metric 3: Current Pace / Rate
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "CURRENT RATE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currentRate steps / 1m",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Quick Step Action Row: Instant test steps buttons & sensor live status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { StepLockRepository.addSteps(100) },
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("+100 Steps", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { StepLockRepository.addSteps(500) },
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldNeon.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldNeon),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("+500 Steps", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { StepLockRepository.addSteps(1000) },
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = DeepVoidNavy),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("+1,000 ⚡", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

// ============================================================================
// 2. CARD 2: SCROLL TAX VAULT (SCREEN TIME BANK)
// ============================================================================

@Composable
fun ScrollTaxVaultCard(
    bankedMinutes: Int,
    bankedSecondsRemainder: Int,
    isLocked: Boolean,
    stepsToNextMinute: Int,
    currentRate: Int,
    progressToNextMinute: Float,
    currentPeriod: TimeOfDayPeriod,
    onOpenInstagram: () -> Unit,
    onChangeRateMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val periodAccentColor by animateColorAsState(
        targetValue = when (currentPeriod) {
            TimeOfDayPeriod.GOLDEN_HOUR -> WarmAmberGold
            TimeOfDayPeriod.STANDARD_DAY -> ElectricCyan
            TimeOfDayPeriod.NIGHT_SURGE -> DeepLavender
        },
        animationSpec = tween(500),
        label = "pillAccentColor"
    )

    val periodGlowColor by animateColorAsState(
        targetValue = when (currentPeriod) {
            TimeOfDayPeriod.GOLDEN_HOUR -> WarmAmberGold.copy(alpha = 0.18f)
            TimeOfDayPeriod.STANDARD_DAY -> ElectricCyan.copy(alpha = 0.18f)
            TimeOfDayPeriod.NIGHT_SURGE -> DeepLavender.copy(alpha = 0.18f)
        },
        animationSpec = tween(500),
        label = "pillGlowColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("scroll_tax_vault_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateNavy),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            Brush.verticalGradient(
                listOf(if (isLocked) CrimsonCoral.copy(alpha = 0.5f) else ElectricCyan.copy(alpha = 0.5f), InnerCardBorder)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Title & Lock Status Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = if (isLocked) CrimsonCoral else ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SCROLL TAX VAULT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                }

                // Status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isLocked) CrimsonCoral.copy(alpha = 0.2f) else EmeraldNeon.copy(alpha = 0.2f))
                        .border(
                            1.dp,
                            if (isLocked) CrimsonCoral.copy(alpha = 0.5f) else EmeraldNeon.copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isLocked) "LOCKED" else "UNLOCKED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isLocked) CrimsonCoral else EmeraldNeon,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Time Available Display (e.g. 3m 00s Available)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isLocked) "0m 00s Available" else "${bankedMinutes}m ${String.format(Locale.getDefault(), "%02d", bankedSecondsRemainder)}s Available",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isLocked) CrimsonCoral else TextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = if (isLocked) "Instagram is locked • Walk to earn screen time" else "Instagram screen time currently unlocked",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                if (!isLocked) {
                    IconButton(
                        onClick = onOpenInstagram,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan)
                            .testTag("launch_instagram_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open Instagram",
                            tint = DeepVoidNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Dynamic Time-of-Day Status Pill (Interactive Mode Switcher)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(periodGlowColor)
                    .border(1.dp, periodAccentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { onChangeRateMode() }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("rate_mode_pill")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = when (currentPeriod) {
                                TimeOfDayPeriod.GOLDEN_HOUR -> Icons.Default.WbSunny
                                TimeOfDayPeriod.STANDARD_DAY -> Icons.Default.Speed
                                TimeOfDayPeriod.NIGHT_SURGE -> Icons.Default.NightlightRound
                            },
                            contentDescription = null,
                            tint = periodAccentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = currentPeriod.badgeLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = periodAccentColor
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(periodAccentColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Change ▾",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = periodAccentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Micro progress bar toward next earned minute
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$stepsToNextMinute steps to +1 min credit",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${(progressToNextMinute * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = periodAccentColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(InnerCardBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressToNextMinute.coerceIn(0f, 1f))
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(periodAccentColor.copy(alpha = 0.7f), periodAccentColor)
                                )
                            )
                    )
                }
            }
        }
    }
}

// ============================================================================
// 3. CARD 3: ACTIVE WALK COMPANION (ACTION TRIGGER)
// ============================================================================

@Composable
fun ActiveWalkCompanionCard(
    session: ActiveWalkSession,
    onStartWalk: () -> Unit,
    onStopWalk: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("active_walk_companion_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateNavy),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            Brush.verticalGradient(
                listOf(
                    if (session.isActive) EmeraldNeon.copy(alpha = 0.7f) else ElectricCyan.copy(alpha = 0.4f),
                    InnerCardBorder
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                        contentDescription = null,
                        tint = if (session.isActive) EmeraldNeon else ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ACTIVE WALK COMPANION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                }

                if (session.isActive) {
                    val infiniteTransition = rememberInfiniteTransition(label = "walk_pulse")
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EmeraldNeon.copy(alpha = pulseAlpha))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = EmeraldNeon,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!session.isActive) {
                Text(
                    text = "Ready to earn your screen time?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Dedicated walking triggers milestone haptic pulses every 500 steps to keep you focused.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onStartWalk,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .bounceClickable(onClick = onStartWalk)
                        .testTag("start_dedicated_walk_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = DeepVoidNavy
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = null,
                        tint = DeepVoidNavy,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start Dedicated Walk",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepVoidNavy
                    )
                }
            } else {
                val mins = session.durationSeconds / 60
                val secs = session.durationSeconds % 60
                val timeStr = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)

                val nextMilestone = ((session.sessionSteps / 500) + 1) * 500
                val progressInMilestone = (session.sessionSteps % 500).toFloat() / 500f

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DURATION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = timeStr,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SESSION STEPS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%,d", session.sessionSteps),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = ElectricCyan
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TIME EARNED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "+${session.sessionMinutesEarned}m",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = EmeraldNeon
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Milestone progress bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${session.sessionSteps % 500} / 500 steps to next haptic pulse",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Goal: $nextMilestone",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ElectricCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progressInMilestone },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = EmeraldNeon,
                        trackColor = InnerCardBorder
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedButton(
                    onClick = onStopWalk,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .bounceClickable(onClick = onStopWalk)
                        .testTag("stop_dedicated_walk_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, InnerCardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = null,
                        tint = CrimsonCoral,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Finish Dedicated Walk",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

// ============================================================================
// 4. CLEAN SETTINGS & PERMISSIONS MODAL
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsModalBottomSheet(
    state: StepLockData,
    onDismiss: () -> Unit,
    onOpenUsageSettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onRequestActivityPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onSwitchProfileClick: () -> Unit,
    onTuneStepGoal: (Int) -> Unit
) {
    BackHandler { onDismiss() }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var stepGoalSlider by remember { mutableFloatStateOf(state.dailyStepGoal.toFloat()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepVoidNavy,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Settings & Permissions",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = "Configure Scroll Tax enforcement and hardware access",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 1. Permissions Checklist
            Text(
                text = "SYSTEM PERMISSIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateNavy),
                border = androidx.compose.foundation.BorderStroke(1.dp, InnerCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SettingsPermissionRow(
                        title = "Usage Access",
                        subtitle = "Detects when Instagram is opened",
                        isGranted = state.hasUsagePermission,
                        actionLabel = "Open Settings",
                        onClick = onOpenUsageSettings
                    )

                    HorizontalDivider(color = InnerCardBorder, modifier = Modifier.padding(vertical = 10.dp))

                    SettingsPermissionRow(
                        title = "Draw Over Other Apps",
                        subtitle = "Displays fullscreen lock over Instagram",
                        isGranted = state.hasOverlayPermission,
                        actionLabel = "Open Settings",
                        onClick = onOpenOverlaySettings
                    )

                    HorizontalDivider(color = InnerCardBorder, modifier = Modifier.padding(vertical = 10.dp))

                    SettingsPermissionRow(
                        title = "Activity Recognition",
                        subtitle = "Connects to device step sensors",
                        isGranted = state.hasActivityPermission,
                        actionLabel = "Grant",
                        onClick = onRequestActivityPermission
                    )

                    HorizontalDivider(color = InnerCardBorder, modifier = Modifier.padding(vertical = 10.dp))

                    SettingsPermissionRow(
                        title = "Foreground Service",
                        subtitle = "Maintains continuous background protection",
                        isGranted = state.hasNotificationPermission,
                        actionLabel = "Grant",
                        onClick = onRequestNotificationPermission
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Daily Step Goal Tuner
            Text(
                text = "DAILY STEP GOAL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlateNavy),
                border = androidx.compose.foundation.BorderStroke(1.dp, InnerCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Target Steps / Day",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${stepGoalSlider.toInt()} steps",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = stepGoalSlider,
                        onValueChange = { stepGoalSlider = it },
                        onValueChangeFinished = { onTuneStepGoal(stepGoalSlider.toInt()) },
                        valueRange = 2000f..15000f,
                        steps = 25,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricCyan,
                            activeTrackColor = ElectricCyan,
                            inactiveTrackColor = InnerCardBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Switch Profile Button
            OutlinedButton(
                onClick = {
                    onDismiss()
                    onSwitchProfileClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, InnerCardBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Text(
                    text = "Switch Profile (${state.activeProfile.emoji} ${state.activeProfile.name})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingsPermissionRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    actionLabel: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Security,
                    contentDescription = null,
                    tint = if (isGranted) EmeraldNeon else CrimsonCoral,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.padding(start = 22.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        if (isGranted) {
            Text(
                text = "Active",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldNeon
            )
        } else {
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = actionLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepVoidNavy
                )
            }
        }
    }
}

// ============================================================================
// 5. ONBOARDING PERMISSION PROMPT MODAL (ONE-TIME)
// ============================================================================

@Composable
fun OnboardingPermissionsDialog(
    hasUsage: Boolean,
    hasOverlay: Boolean,
    onOpenUsageSettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler { onDismiss() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateNavy),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ElectricCyan.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ElectricCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Welcome to Scroll Tax",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "To monitor Instagram screen time and lock access until you earn steps, Scroll Tax requires standard Android permissions:",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("1. Usage Access", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Detects when Instagram is opened", fontSize = 11.sp, color = TextSecondary)
                    }
                    if (hasUsage) {
                        Text("Granted", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldNeon)
                    } else {
                        Button(
                            onClick = onOpenUsageSettings,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                        ) {
                            Text("Enable", fontSize = 11.sp, color = DeepVoidNavy, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("2. Display Over Apps", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Displays the lock screen over Instagram", fontSize = 11.sp, color = TextSecondary)
                    }
                    if (hasOverlay) {
                        Text("Granted", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldNeon)
                    } else {
                        Button(
                            onClick = onOpenOverlaySettings,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                        ) {
                            Text("Enable", fontSize = 11.sp, color = DeepVoidNavy, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (hasUsage && hasOverlay) ElectricCyan else InnerCardBorder)
                ) {
                    Text(
                        text = if (hasUsage && hasOverlay) "Get Started ⚡" else "Continue to Dashboard",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasUsage && hasOverlay) DeepVoidNavy else TextPrimary
                    )
                }
            }
        }
    }
}

// ============================================================================
// 6. DEDICATED SETTINGS SCREEN (PROPER NAVIGATION ROUTE)
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: StepLockData,
    onNavigateBack: () -> Unit,
    onOpenUsageSettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onRequestActivityPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onSwitchProfileClick: () -> Unit,
    onManageBlockedApps: () -> Unit = {},
    onTuneStepGoal: (Int) -> Unit,
    onSignInGoogle: () -> Unit = {},
    onSignOutGoogle: () -> Unit = {},
    onSyncFirebaseNow: () -> Unit = {}
) {
    BackHandler { onNavigateBack() }
    var stepGoalSlider by remember { mutableFloatStateOf(state.dailyStepGoal.toFloat()) }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("settings_screen"),
        containerColor = DeepVoidNavy,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Settings & Permissions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = "Hardware access & profile configuration",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Dashboard",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepVoidNavy)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "SYSTEM PERMISSIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateNavy),
                    border = androidx.compose.foundation.BorderStroke(1.dp, InnerCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        SettingsPermissionRow(
                            title = "Usage Access",
                            subtitle = "Detects when Instagram is opened",
                            isGranted = state.hasUsagePermission,
                            actionLabel = "Open Settings",
                            onClick = onOpenUsageSettings
                        )

                        HorizontalDivider(color = InnerCardBorder, modifier = Modifier.padding(vertical = 10.dp))

                        SettingsPermissionRow(
                            title = "Draw Over Other Apps",
                            subtitle = "Displays fullscreen lock over Instagram",
                            isGranted = state.hasOverlayPermission,
                            actionLabel = "Open Settings",
                            onClick = onOpenOverlaySettings
                        )

                        HorizontalDivider(color = InnerCardBorder, modifier = Modifier.padding(vertical = 10.dp))

                        SettingsPermissionRow(
                            title = "Activity Recognition",
                            subtitle = "Connects to device step sensors",
                            isGranted = state.hasActivityPermission,
                            actionLabel = "Grant",
                            onClick = onRequestActivityPermission
                        )

                        HorizontalDivider(color = InnerCardBorder, modifier = Modifier.padding(vertical = 10.dp))

                        SettingsPermissionRow(
                            title = "Foreground Service",
                            subtitle = "Maintains continuous background protection",
                            isGranted = state.hasNotificationPermission,
                            actionLabel = "Grant",
                            onClick = onRequestNotificationPermission
                        )
                    }
                }
            }

            item {
                Text(
                    text = "FIREBASE CLOUD DATABASE & SYNC",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("firebase_cloud_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateNavy),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (state.isFirebaseConnected) EmeraldNeon.copy(alpha = 0.5f) else InnerCardBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (state.isFirebaseConnected) EmeraldNeon.copy(alpha = 0.2f)
                                            else ElectricCyan.copy(alpha = 0.2f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (state.isFirebaseConnected) "☁️" else "🔥",
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Firestore Cloud Sync",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (state.isFirebaseConnected)
                                            (state.firebaseUserEmail ?: "Cloud account active")
                                        else "Offline local storage",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (state.isFirebaseConnected) EmeraldNeon.copy(alpha = 0.15f)
                                        else TextMuted.copy(alpha = 0.2f)
                                    )
                                    .border(
                                        1.dp,
                                        if (state.isFirebaseConnected) EmeraldNeon else InnerCardBorder,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (state.isFirebaseConnected) "CONNECTED" else "LOCAL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (state.isFirebaseConnected) EmeraldNeon else TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (state.isFirebaseConnected) {
                            Text(
                                text = "Your step goals, banked screen time, and custom modes are continuously synchronized with Google Cloud Firestore.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onSyncFirebaseNow,
                                    modifier = Modifier.weight(1f).height(40.dp).testTag("sync_firebase_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElectricCyan,
                                        contentColor = DeepVoidNavy
                                    )
                                ) {
                                    Text(
                                        text = if (state.isFirebaseSyncing) "Syncing..." else "Sync Now",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = onSignOutGoogle,
                                    modifier = Modifier.weight(1f).height(40.dp).testTag("sign_out_firebase_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, InnerCardBorder),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                                ) {
                                    Text("Sign Out", fontSize = 12.sp)
                                }
                            }
                        } else {
                            Text(
                                text = "Sign in with your Google account to back up and restore your walking data across devices.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onSignInGoogle,
                                modifier = Modifier.fillMaxWidth().height(44.dp).testTag("sign_in_google_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricCyan,
                                    contentColor = DeepVoidNavy
                                )
                            ) {
                                Text(
                                    text = "Sign in with Google",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "DAILY STEP GOAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateNavy),
                    border = androidx.compose.foundation.BorderStroke(1.dp, InnerCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Target Steps / Day",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${stepGoalSlider.toInt()} steps",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Slider(
                            value = stepGoalSlider,
                            onValueChange = { stepGoalSlider = it },
                            onValueChangeFinished = { onTuneStepGoal(stepGoalSlider.toInt()) },
                            valueRange = 2000f..15000f,
                            steps = 25,
                            colors = SliderDefaults.colors(
                                thumbColor = ElectricCyan,
                                activeTrackColor = ElectricCyan,
                                inactiveTrackColor = InnerCardBorder
                            )
                        )
                    }
                }
            }

            item {
                Text(
                    text = "TRACKING ENGINE MODE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateNavy),
                    border = androidx.compose.foundation.BorderStroke(1.dp, InnerCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (state.isSimulateMode) "Simulation Mode" else "Hardware Sensor Mode",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.isSimulateMode) WarmAmberGold else EmeraldNeon
                                )
                                Text(
                                    text = if (state.isSimulateMode) "Simulates steps automatically. Toggle switch to change back to real pedometer sensor." else "Using built-in Android hardware sensor. Toggle to switch to simulation mode.",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Switch(
                                checked = state.isSimulateMode,
                                onCheckedChange = { StepLockRepository.setSimulateMode(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = WarmAmberGold,
                                    checkedTrackColor = WarmAmberGold.copy(alpha = 0.3f),
                                    uncheckedThumbColor = EmeraldNeon,
                                    uncheckedTrackColor = EmeraldNeon.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = onManageBlockedApps,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("manage_blocked_apps_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Text(
                        text = "Manage Blocked Apps (${state.blockedApps.count { it.isBlocked }} Active)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                OutlinedButton(
                    onClick = onSwitchProfileClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, InnerCardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Text(
                        text = "Change Active Mode (${state.activeProfile.emoji} ${state.activeProfile.name})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
