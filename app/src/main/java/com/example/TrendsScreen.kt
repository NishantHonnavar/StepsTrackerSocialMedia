package com.example

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.ui.theme.LockRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.UnlockGreen
import com.example.ui.theme.WarningAmber

@Composable
fun TrendsScreen(
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    val state by StepLockRepository.state.collectAsState()
    val weeklyTrends = state.weeklyTrends.ifEmpty {
        DayTrend.createSampleWeek(state.dailySteps, state.totalEarnedSeconds / 60)
    }

    var selectedDayIndex by remember { mutableIntStateOf(weeklyTrends.size - 1) }
    val selectedDay = weeklyTrends.getOrNull(selectedDayIndex) ?: weeklyTrends.last()

    val totalWeeklySteps = weeklyTrends.sumOf { it.steps }
    val averageDailySteps = if (weeklyTrends.isNotEmpty()) totalWeeklySteps / weeklyTrends.size else 0
    val totalWeeklyMinutesEarned = weeklyTrends.sumOf { it.minutesEarned }
    val bestDay = weeklyTrends.maxByOrNull { it.steps } ?: selectedDay
    var currentViewTab by remember { mutableStateOf("ALL") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("trends_screen_scaffold"),
        containerColor = BackgroundDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceDark)
                                .testTag("trends_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = CyanAccent
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Weekly Trends",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "Fitness & Earned Screen Time",
                                fontSize = 12.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }

                    // Profile Badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceDark)
                            .border(1.dp, BlueCardLight, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = state.activeProfile.emoji, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = state.activeProfile.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                    }
                }
            }

            // Summary Stats Cards (2x2 Grid)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Steps
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsWalk,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "WEEKLY STEPS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMutedDark)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$totalWeeklySteps",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "Avg: $averageDailySteps / day",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }

                    // Total Minutes Earned
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HourglassBottom,
                                    contentDescription = null,
                                    tint = EmeraldNeon,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "EARNED TIME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMutedDark)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${totalWeeklyMinutesEarned}m",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldNeon
                            )
                            Text(
                                text = "Unlocked for Instagram",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }
                }
            }

            // View Mode Selector Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "ALL" to "All Analytics",
                        "HEATMAP" to "Activity Heatmap 🔥",
                        "WEEKLY" to "Weekly Trends 📊"
                    ).forEach { (tabKey, label) ->
                        val isSelected = currentViewTab == tabKey
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) CyanAccent else SurfaceDark)
                                .clickable { currentViewTab = tabKey }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("trends_tab_$tabKey"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) BackgroundDark else TextSecondaryDark
                            )
                        }
                    }
                }
            }

            // Activity Heatmap Card (Most Active Times of the Day)
            if (currentViewTab == "ALL" || currentViewTab == "HEATMAP") {
                item {
                    ActivityHeatmapCard(
                        profile = state.activeProfile
                    )
                }
            }

            // Interactive Weekly Step Count & Time Trend Chart Card
            if (currentViewTab == "ALL" || currentViewTab == "WEEKLY") {
                item {
                    WeeklyTrendChartCard(
                        trends = weeklyTrends,
                        selectedIndex = selectedDayIndex,
                        onSelectDay = { selectedDayIndex = it }
                    )
                }

                // Selected Day Detailed Breakdown Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("selected_day_detail_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BlueCardLight)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = CyanAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (selectedDay.isToday) "${selectedDay.dayLabel} (Today)" else selectedDay.dayLabel,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryDark
                                    )
                                }

                                if (selectedDay.dayLabel == bestDay.dayLabel) {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0x33FFD700))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Best Day", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Steps Walked", fontSize = 11.sp, color = TextMutedDark)
                                    Text(
                                        text = "${selectedDay.steps}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = CyanAccent
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Time Earned", fontSize = 11.sp, color = TextMutedDark)
                                    Text(
                                        text = "+${selectedDay.minutesEarned}m",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = EmeraldNeon
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Instagram Used", fontSize = 11.sp, color = TextMutedDark)
                                    Text(
                                        text = "-${selectedDay.minutesUsed}m",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = LockRed
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Time-of-Day Walking Engine Rules Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Dynamic Incentive Engine",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Golden Hour rule
                        IncentiveRuleRow(
                            title = "Golden Hour (06:00 – 08:00)",
                            description = "2x Bonus: 25 steps = 1 minute Instagram",
                            color = Color(0xFFFFD700),
                            isActive = state.currentPeriod == TimeOfDayPeriod.GOLDEN_HOUR
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Standard Day rule
                        IncentiveRuleRow(
                            title = "Standard Day (08:00 – 22:00)",
                            description = "50 steps = 1 minute Instagram",
                            color = CyanAccent,
                            isActive = state.currentPeriod == TimeOfDayPeriod.STANDARD_DAY
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Night Surge rule
                        IncentiveRuleRow(
                            title = "Night Surge (22:00 – 06:00)",
                            description = "Sleep Protection: 150 steps = 1 minute Instagram",
                            color = Color(0xFFA5B4FC),
                            isActive = state.currentPeriod == TimeOfDayPeriod.NIGHT_SURGE
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyTrendChartCard(
    trends: List<DayTrend>,
    selectedIndex: Int,
    onSelectDay: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weekly_trends_chart_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Weekly Activity & Time Earned",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                }

                // Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CyanAccent))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Steps", fontSize = 10.sp, color = TextSecondaryDark)
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldNeon))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Time", fontSize = 10.sp, color = TextSecondaryDark)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Canvas Bar & Trendline Chart
            val maxSteps = (trends.maxOfOrNull { it.steps } ?: 10000).coerceAtLeast(8000)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceVariantDark.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(trends) {
                            detectTapGestures { offset ->
                                val slotWidth = size.width / trends.size
                                val clickedIndex = (offset.x / slotWidth).toInt().coerceIn(0, trends.size - 1)
                                onSelectDay(clickedIndex)
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val barWidth = 24.dp.toPx()
                    val slotWidth = width / trends.size

                    // Grid lines
                    val gridSteps = listOf(0.25f, 0.5f, 0.75f, 1.0f)
                    gridSteps.forEach { fraction ->
                        val y = height * (1f - fraction)
                        drawLine(
                            color = Color(0x1AFFFFFF),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Draw Bars
                    trends.forEachIndexed { index, day ->
                        val isSelected = index == selectedIndex
                        val centerX = slotWidth * index + slotWidth / 2f
                        val barHeightRatio = (day.steps.toFloat() / maxSteps.toFloat()).coerceIn(0.05f, 1f)
                        val barHeight = height * barHeightRatio * 0.75f
                        val topY = height - barHeight - 20.dp.toPx()

                        // Bar background
                        val barBrush = Brush.verticalGradient(
                            colors = if (isSelected) {
                                listOf(CyanAccent, EmeraldNeon)
                            } else {
                                listOf(CyanAccent.copy(alpha = 0.5f), BlueCardLight)
                            }
                        )

                        drawRoundRect(
                            brush = barBrush,
                            topLeft = Offset(centerX - barWidth / 2f, topY),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )

                        // Highlight border for selected day
                        if (isSelected) {
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(centerX - barWidth / 2f - 2f, topY - 2f),
                                size = Size(barWidth + 4f, barHeight + 4f),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }

                    // Draw Time-Earned Trend Line
                    val linePath = Path()
                    trends.forEachIndexed { index, day ->
                        val centerX = slotWidth * index + slotWidth / 2f
                        val ratio = (day.minutesEarned.toFloat() / 100f).coerceIn(0.1f, 1f)
                        val y = height * 0.7f * (1f - ratio) + 10.dp.toPx()

                        if (index == 0) {
                            linePath.moveTo(centerX, y)
                        } else {
                            linePath.lineTo(centerX, y)
                        }

                        // Dot
                        drawCircle(
                            color = EmeraldNeon,
                            radius = if (index == selectedIndex) 5.dp.toPx() else 3.dp.toPx(),
                            center = Offset(centerX, y)
                        )
                    }

                    drawPath(
                        path = linePath,
                        color = EmeraldNeon,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Day Labels Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                trends.forEachIndexed { index, day ->
                    val isSelected = index == selectedIndex
                    Text(
                        text = day.dayLabel,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) CyanAccent else TextMutedDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectDay(index) }
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun IncentiveRuleRow(
    title: String,
    description: String,
    color: Color,
    isActive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) color.copy(alpha = 0.15f) else SurfaceVariantDark)
            .border(
                1.dp,
                if (isActive) color.copy(alpha = 0.6f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = TextSecondaryDark,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        if (isActive) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "ACTIVE NOW",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = BackgroundDark
                )
            }
        }
    }
}
