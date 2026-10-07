package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun ActivityHeatmapCard(
    profile: UserProfile,
    modifier: Modifier = Modifier
) {
    val heatmap = remember(profile.id, profile.dailySteps, profile.dailyStepGoal) {
        HourlyActivityHeatmap.generateForProfile(profile)
    }

    var selectedHour by remember { mutableIntStateOf(heatmap.bestEarningWindow.hour) }
    val selectedSlot = heatmap.slots.getOrNull(selectedHour) ?: heatmap.bestEarningWindow
    var viewMode by remember { mutableStateOf("TIMELINE") } // "TIMELINE" or "SEGMENTS"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("activity_heatmap_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, CyanAccent.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0x3300E5FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Activity Heatmap",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Most Active Screen-Time Earning Hours",
                            fontSize = 12.sp,
                            color = CyanAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // View Mode Toggle (Timeline / Segments)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceVariantDark)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (viewMode == "TIMELINE") CyanAccent else Color.Transparent)
                            .clickable { viewMode = "TIMELINE" }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("heatmap_view_timeline")
                    ) {
                        Text(
                            text = "24h",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (viewMode == "TIMELINE") BackgroundDark else TextSecondaryDark
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (viewMode == "SEGMENTS") CyanAccent else Color.Transparent)
                            .clickable { viewMode = "SEGMENTS" }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("heatmap_view_segments")
                    ) {
                        Text(
                            text = "Periods",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (viewMode == "SEGMENTS") BackgroundDark else TextSecondaryDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subtitle banner explaining earning probability
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x2200E5FF))
                    .border(1.dp, CyanAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Optimal walk window: ${heatmap.bestEarningWindow.timeRangeLabel} (Golden Hour 2x yields +${heatmap.bestEarningWindow.minutesEarned}m vault)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. VISUAL HEATMAP DISPLAY
            if (viewMode == "TIMELINE") {
                HourlyTimelineHeatmap(
                    slots = heatmap.slots,
                    selectedHour = selectedHour,
                    onSelectHour = { selectedHour = it }
                )
            } else {
                PeriodsSegmentHeatmap(
                    slots = heatmap.slots,
                    selectedHour = selectedHour,
                    onSelectHour = { selectedHour = it }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Heatmap Intensity Color Scale Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Activity Level:", fontSize = 11.sp, color = TextMutedDark)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = Color(0xFF1E293B), label = "Rest")
                    LegendItem(color = Color(0xFF0284C7), label = "Low")
                    LegendItem(color = Color(0xFF00E5FF), label = "Active")
                    LegendItem(color = Color(0xFF00E699), label = "Peak 2x")
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. SELECTED HOUR INTERACTIVE DETAILS CARD
            SelectedHourInsightCard(slot = selectedSlot)

            Spacer(modifier = Modifier.height(16.dp))

            // 3. TOP 3 PEAK SCREEN-TIME EARNING WINDOWS
            Text(
                text = "Top Peak Earning Windows for ${profile.name}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
            Spacer(modifier = Modifier.height(10.dp))

            heatmap.peakSlots.forEachIndexed { index, slot ->
                val medal = when (index) {
                    0 -> "🥇"
                    1 -> "🥈"
                    else -> "🥉"
                }
                PeakWindowRowItem(
                    slot = slot,
                    medal = medal,
                    isSelected = slot.hour == selectedHour,
                    onClick = { selectedHour = slot.hour }
                )
                if (index < heatmap.peakSlots.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun HourlyTimelineHeatmap(
    slots: List<HourlyActivitySlot>,
    selectedHour: Int,
    onSelectHour: (Int) -> Unit
) {
    Column {
        Text(
            text = "24-Hour Intensity (Tap any hour to inspect earning potential):",
            fontSize = 12.sp,
            color = TextSecondaryDark,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Scrollable row or full width grid of 24 bars/tiles
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceVariantDark)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            slots.forEach { slot ->
                val isSelected = slot.hour == selectedHour
                val barColor = when {
                    slot.period == TimeOfDayPeriod.GOLDEN_HOUR && slot.relativeIntensity > 0.4f -> Color(0xFF00E699)
                    slot.relativeIntensity >= 0.7f -> Color(0xFF00E5FF)
                    slot.relativeIntensity >= 0.4f -> Color(0xFF0284C7)
                    slot.relativeIntensity >= 0.2f -> Color(0xFF0E435E)
                    else -> Color(0xFF1E293B)
                }

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CyanAccent.copy(alpha = 0.25f) else Color.Transparent)
                        .border(
                            width = if (isSelected) 1.5.dp else 0.dp,
                            color = if (isSelected) CyanAccent else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectHour(slot.hour) }
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                        .testTag("heatmap_hour_${slot.hour}"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Bar height based on intensity
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(SurfaceDark),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((56 * slot.relativeIntensity).dp.coerceAtLeast(6.dp))
                                .clip(RoundedCornerShape(4.dp))
                                .background(barColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${slot.hour}h",
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) CyanAccent else TextMutedDark
                    )

                    if (slot.period == TimeOfDayPeriod.GOLDEN_HOUR) {
                        Text(text = "2x", fontSize = 8.sp, fontWeight = FontWeight.Black, color = WarningAmber)
                    } else {
                        Text(text = " ", fontSize = 8.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodsSegmentHeatmap(
    slots: List<HourlyActivitySlot>,
    selectedHour: Int,
    onSelectHour: (Int) -> Unit
) {
    val morningSlots = slots.filter { it.hour in 6..11 }
    val afternoonSlots = slots.filter { it.hour in 12..17 }
    val eveningSlots = slots.filter { it.hour in 18..21 }
    val nightSlots = slots.filter { it.hour !in 6..21 }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PeriodSegmentRow("🌅 Morning & Golden Hour (06:00 – 12:00)", morningSlots, selectedHour, onSelectHour)
        PeriodSegmentRow("☀️ Afternoon (12:00 – 18:00)", afternoonSlots, selectedHour, onSelectHour)
        PeriodSegmentRow("🌆 Evening Prime (18:00 – 22:00)", eveningSlots, selectedHour, onSelectHour)
        PeriodSegmentRow("🌙 Night Surge / Sleep (22:00 – 06:00)", nightSlots, selectedHour, onSelectHour)
    }
}

@Composable
fun PeriodSegmentRow(
    title: String,
    periodSlots: List<HourlyActivitySlot>,
    selectedHour: Int,
    onSelectHour: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariantDark.copy(alpha = 0.5f))
            .padding(10.dp)
    ) {
        Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            periodSlots.forEach { slot ->
                val isSelected = slot.hour == selectedHour
                val barColor = when {
                    slot.period == TimeOfDayPeriod.GOLDEN_HOUR && slot.relativeIntensity > 0.4f -> Color(0xFF00E699)
                    slot.relativeIntensity >= 0.7f -> Color(0xFF00E5FF)
                    slot.relativeIntensity >= 0.4f -> Color(0xFF0284C7)
                    else -> Color(0xFF1E293B)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) CyanAccent.copy(alpha = 0.3f) else barColor)
                        .border(
                            width = if (isSelected) 1.5.dp else 0.dp,
                            color = if (isSelected) CyanAccent else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onSelectHour(slot.hour) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${slot.hour}h",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (slot.relativeIntensity > 0.5f) BackgroundDark else TextPrimaryDark
                    )
                }
            }
        }
    }
}

@Composable
fun SelectedHourInsightCard(slot: HourlyActivitySlot) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("selected_hour_insight_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, BlueCardLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = slot.timeRangeLabel,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimaryDark
                    )
                }

                // Period Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(slot.period.badgeBgColor))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = slot.periodBadgeName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(slot.period.badgeTextColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stat Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Average Activity", fontSize = 10.sp, color = TextMutedDark)
                    Text(
                        text = "${slot.averageSteps} steps",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Conversion Rate", fontSize = 10.sp, color = TextMutedDark)
                    Text(
                        text = "${slot.conversionRate} steps/min",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Screen Time Yield", fontSize = 10.sp, color = TextMutedDark)
                    Text(
                        text = "+${slot.minutesEarned}m",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = EmeraldNeon
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Recommendation tip
            Text(
                text = slot.earningPotentialDescription,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondaryDark,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun PeakWindowRowItem(
    slot: HourlyActivitySlot,
    medal: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) Color(0x3300E5FF) else SurfaceVariantDark)
            .border(
                1.dp,
                if (isSelected) CyanAccent else Color.Transparent,
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("peak_window_item_${slot.hour}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = medal, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = slot.timeRangeLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Text(
                    text = "${slot.periodBadgeName} • ~${slot.averageSteps} steps",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "+${slot.minutesEarned}m",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = EmeraldNeon
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "vault",
                fontSize = 11.sp,
                color = TextMutedDark
            )
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, color = TextMutedDark)
    }
}
