package com.example

import android.content.Context
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

@Composable
fun GmailSyncCard(
    state: StepLockData,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSending by remember { mutableStateOf(false) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var showEditEmailDialog by remember { mutableStateOf(false) }
    var lastStatusMessage by remember { mutableStateOf(state.lastWeeklyReportStatus) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("gmail_sync_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEA4335).copy(alpha = 0.6f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF2B1111), SurfaceDark)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0x33EA4335)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = Color(0xFFEA4335),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Gmail Weekly Sync",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimaryDark
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x2610B981))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "OAUTH ENABLED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldNeon
                                    )
                                }
                            }
                            Text(
                                text = "Weekly Fitness & Screen-Time Updates",
                                fontSize = 12.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }

                    Switch(
                        checked = state.isGmailSyncEnabled,
                        onCheckedChange = { StepLockRepository.setGmailSyncEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BackgroundDark,
                            checkedTrackColor = Color(0xFFEA4335),
                            uncheckedThumbColor = TextMutedDark,
                            uncheckedTrackColor = SurfaceVariantDark
                        ),
                        modifier = Modifier.testTag("gmail_sync_toggle")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Recipient Email Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceVariantDark)
                        .clickable { showEditEmailDialog = true }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SYNCED GMAIL ACCOUNT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMutedDark
                        )
                        Text(
                            text = state.gmailUserEmail,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyanAccent
                        )
                    }
                    IconButton(
                        onClick = { showEditEmailDialog = true },
                        modifier = Modifier.size(28.dp).testTag("edit_gmail_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Gmail",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Information highlight
                Text(
                    text = "Automatically compiles and emails you a weekly digest of your steps walked, Instagram screen time saved vs used, milestones achieved, and consistency streaks.",
                    fontSize = 12.sp,
                    color = TextSecondaryDark,
                    lineHeight = 18.sp
                )

                // Last sent info or status message
                if (state.lastWeeklyReportSentTimestamp > 0L || lastStatusMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x1A00E5FF))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val formattedDate = if (state.lastWeeklyReportSentTimestamp > 0L) {
                            SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(state.lastWeeklyReportSentTimestamp))
                        } else "Pending"
                        Text(
                            text = "Last report sent: $formattedDate • ${lastStatusMessage.ifEmpty { "Active" }}",
                            fontSize = 11.sp,
                            color = CyanAccent,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons (Send Now + Preview)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            isSending = true
                            coroutineScope.launch {
                                val result = GmailSyncManager.sendWeeklyReport(
                                    context = context,
                                    recipientEmail = state.gmailUserEmail,
                                    state = state
                                )
                                isSending = false
                                lastStatusMessage = result.second
                                Toast.makeText(context, result.second, Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("send_weekly_report_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA4335)),
                        enabled = !isSending
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = TextPrimaryDark,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = TextPrimaryDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Send Report Now",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showPreviewDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("preview_report_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BlueCardLight),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimaryDark)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Preview Email",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }
                }
            }
        }
    }

    if (showPreviewDialog) {
        GmailWeeklyReportPreviewDialog(
            state = state,
            onDismiss = { showPreviewDialog = false },
            onSend = {
                showPreviewDialog = false
                isSending = true
                coroutineScope.launch {
                    val result = GmailSyncManager.sendWeeklyReport(
                        context = context,
                        recipientEmail = state.gmailUserEmail,
                        state = state
                    )
                    isSending = false
                    lastStatusMessage = result.second
                    Toast.makeText(context, result.second, Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    if (showEditEmailDialog) {
        EditGmailDialog(
            currentEmail = state.gmailUserEmail,
            onDismiss = { showEditEmailDialog = false },
            onSave = { newEmail ->
                StepLockRepository.setGmailUserEmail(newEmail)
                showEditEmailDialog = false
                Toast.makeText(context, "Synced email updated to $newEmail", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun MorningLockCard(
    state: StepLockData,
    onTestMorningLock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLocked = state.isLocked
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("morning_lock_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isLocked) LockRed.copy(alpha = 0.7f) else EmeraldNeon.copy(alpha = 0.5f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (isLocked) Color(0xFF2A0C13) else Color(0xFF092922),
                            SurfaceDark
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isLocked) Color(0x33F43F5E) else Color(0x3310B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = if (isLocked) LockRed else EmeraldNeon,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Morning Auto-Lock",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = if (isLocked) "Locked for the morning" else "Unlocked by steps",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLocked) LockRed else EmeraldNeon
                            )
                        }
                    }

                    // Status badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isLocked) Color(0x33F43F5E) else Color(0x3310B981))
                            .border(1.dp, if (isLocked) LockRed else EmeraldNeon, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isLocked) "LOCKED" else "UNLOCKED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isLocked) LockRed else EmeraldNeon
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Explanation
                Text(
                    text = "Instagram is automatically locked every morning with 0 banked minutes. Steps walked continuously in the background automatically unlock Instagram at ${state.currentStepsPerMinute} steps per minute.",
                    fontSize = 12.sp,
                    color = TextSecondaryDark,
                    lineHeight = 17.sp
                )

                // Today screen time stats
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceVariantDark)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "INSTAGRAM USED TODAY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMutedDark)
                        Text(
                            text = "${state.dailyInstagramMinutesUsed}m ${state.dailyInstagramSecondsRemainder}s",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LockRed
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "STEPS COUNTED (BACKGROUND)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMutedDark)
                        Text(
                            text = "${state.dailySteps} steps",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Test Morning Lock Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onTestMorningLock,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("test_morning_lock_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LockRed.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LockRed)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = LockRed, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enforce Morning Lock Now", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LockRed)
                    }

                    Button(
                        onClick = {
                            StepLockRepository.addSteps(state.currentStepsPerMinute)
                            Toast.makeText(context, "Walked steps! Unlocked +1 min Instagram.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("walk_unlock_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldNeon)
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Walk to Unlock (+${state.currentStepsPerMinute})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BackgroundDark)
                    }
                }
            }
        }
    }
}

@Composable
fun GmailWeeklyReportPreviewDialog(
    state: StepLockData,
    onDismiss: () -> Unit,
    onSend: () -> Unit
) {
    val reportText = remember(state) { GmailSyncManager.generateWeeklyReportPlainText(state) }
    val subject = remember(state) { GmailSyncManager.generateWeeklyReportSubject(state) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 620.dp)
                .clip(RoundedCornerShape(22.dp))
                .testTag("gmail_report_preview_dialog"),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEA4335).copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MarkEmailRead,
                            contentDescription = null,
                            tint = Color(0xFFEA4335),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Email Preview",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMutedDark)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Email metadata
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceVariantDark)
                        .padding(10.dp)
                ) {
                    Column {
                        Text(text = "To: ${state.gmailUserEmail}", fontSize = 12.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                        Text(text = "Subject: $subject", fontSize = 12.sp, color = TextPrimaryDark, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Report text
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BackgroundDark)
                        .border(1.dp, BlueCardLight, RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = reportText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondaryDark,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BlueCardLight)
                    ) {
                        Text("Close", color = TextSecondaryDark)
                    }

                    Button(
                        onClick = onSend,
                        modifier = Modifier.weight(1.5f).height(42.dp).testTag("preview_confirm_send_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA4335))
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = TextPrimaryDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send via Gmail", fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                    }
                }
            }
        }
    }
}

@Composable
fun EditGmailDialog(
    currentEmail: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var emailInput by remember { mutableStateOf(currentEmail) }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("edit_gmail_dialog"),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, CyanAccent.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Update Sync Gmail Account",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Text(
                    text = "Weekly fitness and screen-time updates will be sent to this email address.",
                    fontSize = 12.sp,
                    color = TextSecondaryDark,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = {
                        emailInput = it
                        isError = false
                    },
                    label = { Text("Gmail Address") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gmail_input_field"),
                    isError = isError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = BlueCardLight
                    )
                )

                if (isError) {
                    Text(
                        text = "Please enter a valid email address.",
                        color = LockRed,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = TextSecondaryDark)
                    }

                    Button(
                        onClick = {
                            val clean = emailInput.trim()
                            if (clean.contains("@") && clean.contains(".")) {
                                onSave(clean)
                            } else {
                                isError = true
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("save_gmail_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                    ) {
                        Text("Save Email", color = BackgroundDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
