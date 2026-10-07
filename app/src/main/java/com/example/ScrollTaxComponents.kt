package com.example

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
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
import com.example.ui.theme.LockRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.UnlockGreen
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private val GoldTax = Color(0xFFFFB800)
private val TaxPurple = Color(0xFFA855F7)

/**
 * Animated High-Tech Scroll Tax Vault Card
 * Features glowing infinite gradient borders, spring animated number counters,
 * real-time scroll tax progress bar, and tactile quick-pay buttons.
 */
@Composable
fun ScrollTaxVaultCard(
    bankedMinutes: Int,
    bankedSecondsRemainder: Int,
    isLocked: Boolean,
    stepsToNextMinute: Int,
    currentRate: Int,
    progressToNextMinute: Float,
    dailyInstagramMinutesUsed: Int,
    currentPeriod: TimeOfDayPeriod,
    onLockNow: () -> Unit,
    onTestLockScreen: () -> Unit,
    onPayTaxSteps: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showReceiptDialog by remember { mutableStateOf(false) }

    // Pulsing gradient animation for the border
    val infiniteTransition = rememberInfiniteTransition(label = "vaultBorder")
    val borderPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val animatedMinutes by animateIntAsState(
        targetValue = bankedMinutes,
        animationSpec = spring(stiffness = 300f),
        label = "minutesAnim"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progressToNextMinute,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "progressAnim"
    )

    val statusColor by animateColorAsState(
        targetValue = if (isLocked) LockRed else EmeraldNeon,
        animationSpec = tween(400),
        label = "statusColor"
    )

    val borderColor = if (isLocked) {
        LockRed.copy(alpha = 0.4f + 0.5f * borderPulse)
    } else {
        CyanAccent.copy(alpha = 0.4f + 0.5f * borderPulse)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("scroll_tax_vault_card"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (isLocked) Color(0xFF280B13) else Color(0xFF07212C),
                            SurfaceDark
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column {
                // Header with Tax Badge & Status
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
                                .background(if (isLocked) LockRed.copy(alpha = 0.2f) else EmeraldNeon.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SCROLL TAX VAULT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                color = if (isLocked) LockRed else CyanAccent
                            )
                            Text(
                                text = "Instagram Screen Time Balance",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        }
                    }

                    // Live Status Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .border(1.dp, statusColor.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (isLocked) "TAX UNPAID" else "TAX CLEARED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = statusColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Big Animated Numeric Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "AVAILABLE TO SCROLL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMutedDark,
                            letterSpacing = 0.8.sp
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            AnimatedContent(
                                targetState = animatedMinutes,
                                transitionSpec = {
                                    if (targetState > initialState) {
                                        (slideInVertically { height -> height } + fadeIn())
                                            .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                                    } else {
                                        (slideInVertically { height -> -height } + fadeIn())
                                            .togetherWith(slideOutVertically { height -> height } + fadeOut())
                                    }
                                },
                                label = "minutesCount"
                            ) { mins ->
                                Text(
                                    text = "$mins",
                                    fontSize = 46.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isLocked) TextMutedDark else TextPrimaryDark,
                                    letterSpacing = (-1).sp
                                )
                            }
                            Text(
                                text = "m",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLocked) TextMutedDark else CyanAccent,
                                modifier = Modifier.padding(start = 2.dp, end = 6.dp)
                            )
                            Text(
                                text = String.format(Locale.getDefault(), "%02ds", bankedSecondsRemainder),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondaryDark
                            )
                        }
                    }

                    // Instagram Used Today Chip
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TODAY'S SCROLL USAGE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMutedDark
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceVariantDark)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${dailyInstagramMinutesUsed}m used",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LockRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar toward next minute of scroll time
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isLocked) {
                                "Walk $stepsToNextMinute steps to unlock 1 min"
                            } else {
                                "$stepsToNextMinute steps to +1 min tax credit"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isLocked) WarningAmber else TextSecondaryDark
                        )
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (isLocked) WarningAmber else CyanAccent,
                        trackColor = SurfaceVariantDark,
                        strokeCap = StrokeCap.Round
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dynamic Tax Rate Banner (Golden Hour / Peak / Standard)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(currentPeriod.badgeBgColor))
                        .border(1.dp, Color(currentPeriod.badgeTextColor).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = Color(currentPeriod.badgeTextColor),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tax Rate: $currentRate steps = 1 min",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(currentPeriod.badgeTextColor)
                        )
                    }
                    Text(
                        text = currentPeriod.badgeLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(currentPeriod.badgeTextColor)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Quick-Pay Tax Buttons
                Text(
                    text = "PAY SCROLL TAX (QUICK ACTIONS)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMutedDark,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TactileBounceButton(
                        text = "+100 Steps",
                        subtitle = "+${(100 / currentRate).coerceAtLeast(1)}m scroll",
                        color = CyanAccent,
                        onClick = {
                            onPayTaxSteps(100)
                            Toast.makeText(context, "Paid Scroll Tax: +100 steps!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).testTag("pay_tax_100_button")
                    )

                    TactileBounceButton(
                        text = "+250 Steps",
                        subtitle = "+${(250 / currentRate).coerceAtLeast(1)}m scroll",
                        color = EmeraldNeon,
                        onClick = {
                            onPayTaxSteps(250)
                            Toast.makeText(context, "Paid Scroll Tax: +250 steps!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).testTag("pay_tax_250_button")
                    )

                    TactileBounceButton(
                        text = "Clear 15m",
                        subtitle = "${15 * currentRate} steps",
                        color = GoldTax,
                        onClick = {
                            val stepsNeeded = 15 * currentRate
                            onPayTaxSteps(stepsNeeded)
                            Toast.makeText(context, "Cleared 15 min tax ($stepsNeeded steps)!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).testTag("pay_tax_15m_button")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom row: Receipt & Lock Testing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showReceiptDialog = true },
                        modifier = Modifier.weight(1f).height(38.dp).testTag("view_tax_receipt_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BlueCardLight),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimaryDark)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = GoldTax, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tax Receipt", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                    }

                    OutlinedButton(
                        onClick = onTestLockScreen,
                        modifier = Modifier.weight(1f).height(38.dp).testTag("test_lock_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BlueCardLight),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondaryDark)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Lock UI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (!isLocked) {
                        OutlinedButton(
                            onClick = onLockNow,
                            modifier = Modifier.height(38.dp).testTag("lock_now_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LockRed.copy(alpha = 0.7f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LockRed)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = LockRed, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }

    if (showReceiptDialog) {
        val state = StepLockRepository.state.value
        ScrollTaxReceiptDialog(
            state = state,
            onDismiss = { showReceiptDialog = false }
        )
    }
}

/**
 * Tactile Button with interactive scale on press effect
 */
@Composable
fun TactileBounceButton(
    text: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "bounceScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariantDark)
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondaryDark
            )
        }
    }
}

/**
 * Interactive Scroll Tax Calculator Card
 * Lets the user select how many minutes of Instagram scrolling they want
 * and live-calculates the required steps, calories, and deficit.
 */
@Composable
fun ScrollTaxCalculatorCard(
    currentRate: Int,
    bankedMinutes: Int,
    onStartTaxWalk: () -> Unit,
    onPayCalculatedTax: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedMinutes by remember { mutableFloatStateOf(15f) }
    val presets = listOf(5f, 15f, 30f, 45f, 60f)

    val targetMins = selectedMinutes.roundToInt()
    val requiredSteps = targetMins * currentRate
    val stepsDeficit = (requiredSteps - (bankedMinutes * currentRate)).coerceAtLeast(0)
    val isFullyCovered = stepsDeficit <= 0
    val estimatedCalories = (requiredSteps * 0.04).roundToInt()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("scroll_tax_calculator_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, BlueCardLight)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x33A855F7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = TaxPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Scroll Tax Calculator",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Calculate steps needed to scroll",
                            fontSize = 12.sp,
                            color = TextSecondaryDark
                        )
                    }
                }

                // Desired time display
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x2600E5FF))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$targetMins min scroll",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = CyanAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Presets row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presets.forEach { preset ->
                    val isSelected = selectedMinutes == preset
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedMinutes = preset },
                        label = {
                            Text(
                                text = "${preset.toInt()}m",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TaxPurple,
                            selectedLabelColor = Color.White,
                            containerColor = SurfaceVariantDark,
                            labelColor = TextSecondaryDark
                        ),
                        border = null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Continuous Slider
            Slider(
                value = selectedMinutes,
                onValueChange = { selectedMinutes = it },
                valueRange = 1f..90f,
                steps = 88,
                colors = SliderDefaults.colors(
                    thumbColor = TaxPurple,
                    activeTrackColor = TaxPurple,
                    inactiveTrackColor = SurfaceVariantDark
                ),
                modifier = Modifier.padding(vertical = 4.dp).testTag("scroll_tax_slider")
            )

            // Calculation Results Matrix
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceVariantDark)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TAX REQUIRED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMutedDark
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%,d steps", requiredSteps),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "~$estimatedCalories kcal burned",
                        fontSize = 10.sp,
                        color = WarningAmber
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isFullyCovered) "TAX STATUS" else "REMAINING STEPS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMutedDark
                    )
                    if (isFullyCovered) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldNeon, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Covered in Vault!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldNeon
                            )
                        }
                    } else {
                        Text(
                            text = String.format(Locale.getDefault(), "%,d more", stepsDeficit),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = LockRed
                        )
                    }
                    Text(
                        text = "Rate: $currentRate steps/m",
                        fontSize = 10.sp,
                        color = TextMutedDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onStartTaxWalk,
                    modifier = Modifier.weight(1f).height(42.dp).testTag("start_tax_walk_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TaxPurple),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TaxPurple)
                ) {
                    Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Start Walk", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        onPayCalculatedTax(requiredSteps)
                        Toast.makeText(context, "Paid $requiredSteps steps! Unlocked $targetMins mins Instagram.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1.3f).height(42.dp).testTag("pay_calculated_tax_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TaxPurple)
                ) {
                    Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pay Tax Now", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

/**
 * High-fidelity Digital "Scroll Tax Receipt" Modal
 */
@Composable
fun ScrollTaxReceiptDialog(
    state: StepLockData,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val profile = state.activeProfile
    val dateStr = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date())
    val receiptId = remember { "ST-" + (1000..9999).random() }

    val totalSteps = profile.dailySteps
    val rate = state.currentStepsPerMinute
    val earnedMins = profile.totalEarnedSeconds / 60
    val spentMins = state.dailyInstagramMinutesUsed
    val caloriesBurned = (totalSteps * 0.04).roundToInt()
    val savedMins = (earnedMins - spentMins).coerceAtLeast(0)

    val receiptPlainText = buildString {
        append("🧾 OFFICIAL SCROLL TAX RECEIPT\n")
        append("ID: #$receiptId | Date: $dateStr\n")
        append("Taxpayer: ${profile.name} ${profile.emoji}\n")
        append("Audited Platform: Instagram\n")
        append("─────────────────────────────\n")
        append("• Current Tax Rate: $rate steps / min\n")
        append("• Steps Deposited Today: %,d steps\n".format(totalSteps))
        append("• Calories Burned Paying Tax: %d kcal\n".format(caloriesBurned))
        append("• Screen Time Earned: %d minutes\n".format(earnedMins))
        append("• Instagram Time Used: %d minutes\n".format(spentMins))
        append("• Doomscrolling Minutes Saved: %d min\n".format(savedMins))
        append("• Morning Auto-Lock: Enforced\n")
        append("─────────────────────────────\n")
        append("STATUS: " + if (state.isLocked) "TAX DEBT OUTSTANDING ❌" else "PAID IN FULL & AUDITED ✅" + "\n")
        append("Pay your scroll tax with steps! #ScrollTax\n")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 640.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("scroll_tax_receipt_dialog"),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldTax.copy(alpha = 0.7f))
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Receipt Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🧾", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "SCROLL TAX RECEIPT",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldTax,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Tax Audit ID: #$receiptId",
                                fontSize = 11.sp,
                                color = TextMutedDark,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMutedDark)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Thermal receipt paper container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(BackgroundDark)
                        .border(1.dp, BlueCardLight, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Taxpayer: ${profile.name} ${profile.emoji}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                            Text(text = dateStr, fontSize = 10.sp, color = TextMutedDark)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BlueCardLight))
                        Spacer(modifier = Modifier.height(10.dp))

                        ReceiptLineItem("Audited Platform", "Instagram (@instagram)")
                        ReceiptLineItem("Current Tax Rate", "$rate steps = 1 min")
                        ReceiptLineItem("Steps Deposited", String.format(Locale.getDefault(), "%,d steps", totalSteps), CyanAccent)
                        ReceiptLineItem("Calories Burned", "~$caloriesBurned kcal", WarningAmber)
                        ReceiptLineItem("Screen Time Earned", "+${earnedMins}m", EmeraldNeon)
                        ReceiptLineItem("Instagram Screen Time Used", "-${spentMins}m", LockRed)
                        ReceiptLineItem("Doomscroll Minutes Saved", "+${savedMins}m", CyanAccent)
                        ReceiptLineItem("Morning Auto-Lock", "ENFORCED 06:00 AM", GoldTax)

                        Spacer(modifier = Modifier.height(10.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BlueCardLight))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Status Badge inside Receipt
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "AUDIT STATUS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMutedDark)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (state.isLocked) Color(0x33F43F5E) else Color(0x3310B981))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (state.isLocked) "TAX DELINQUENT (LOCKED)" else "PAID IN FULL (UNLOCKED)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (state.isLocked) LockRed else EmeraldNeon
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions: Share Receipt + Copy
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            SocialShareHelper.copyToClipboard(context, receiptPlainText)
                        },
                        modifier = Modifier.weight(1f).height(44.dp).testTag("copy_receipt_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BlueCardLight)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                    }

                    Button(
                        onClick = {
                            SocialShareHelper.sharePlainText(context, "Scroll Tax Receipt", receiptPlainText)
                        },
                        modifier = Modifier.weight(1.4f).height(44.dp).testTag("share_receipt_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldTax)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Receipt", fontSize = 12.sp, fontWeight = FontWeight.Black, color = BackgroundDark)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptLineItem(
    label: String,
    value: String,
    valueColor: Color = TextPrimaryDark
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = TextSecondaryDark)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
