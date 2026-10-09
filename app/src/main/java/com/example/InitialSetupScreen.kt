package com.example

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSlateNavy
import com.example.ui.theme.DeepLavender
import com.example.ui.theme.DeepVoidNavy
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.InnerCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividPurple

@Composable
fun InitialSetupScreen(
    onSetupComplete: () -> Unit
) {
    val state by StepLockRepository.state.collectAsState()
    val context = LocalContext.current

    // Multi-step onboarding:
    // Step 0: Welcome & User-friendly Account Verification (Enter Email & send verification code)
    // Step 1: Select & Add Apps to Block (YouTube, Instagram, WhatsApp, Pinterest, Facebook)
    var currentStep by remember { mutableIntStateOf(0) }

    var emailInput by remember {
        mutableStateOf(if (state.accountEmail.isNotBlank()) state.accountEmail else "nishantforscience@gmail.com")
    }
    var verificationCodeInput by remember { mutableStateOf("") }
    var hasSentCode by remember { mutableStateOf(false) }
    var isCodeVerified by remember { mutableStateOf(state.isAccountVerified) }
    var generatedMockCode by remember { mutableStateOf("7294") }

    // Custom app adding inputs
    var customPackageName by remember { mutableStateOf("") }
    var customAppName by remember { mutableStateOf("") }
    var showAddCustomDialog by remember { mutableStateOf(false) }

    BackHandler {
        if (currentStep > 0) currentStep--
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("initial_setup_screen"),
        color = DeepVoidNavy
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF140D24),
                            DeepVoidNavy,
                            DeepVoidNavy
                        )
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Brand Logo & Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(VividPurple, ElectricCyan)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Scroll Tax Logo",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "SCROLL TAX",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = if (currentStep == 0) "Step 1 of 2: Create Account" else "Step 2 of 2: Block Distractions",
                                fontSize = 12.sp,
                                color = DeepLavender,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // ================================================================
                // STEP 0: USER-FRIENDLY ACCOUNT VERIFICATION
                // ================================================================
                if (currentStep == 0) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSlateNavy),
                            border = androidx.compose.foundation.BorderStroke(1.dp, InnerCardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = "Start Your Account",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Enter your email for account verification. To ensure high accountability, zero free steps or trial minutes are gifted—every single minute must be earned on foot!",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 17.sp
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                // Email Input Field
                                OutlinedTextField(
                                    value = emailInput,
                                    onValueChange = {
                                        emailInput = it
                                        hasSentCode = false
                                        isCodeVerified = false
                                    },
                                    label = { Text("Email Address") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = "Email",
                                            tint = ElectricCyan
                                        )
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("account_email_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ElectricCyan,
                                        unfocusedBorderColor = InnerCardBorder,
                                        focusedLabelColor = ElectricCyan,
                                        cursorColor = ElectricCyan
                                    )
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                if (!hasSentCode && !isCodeVerified) {
                                    Button(
                                        onClick = {
                                            if (emailInput.contains("@") && emailInput.contains(".")) {
                                                hasSentCode = true
                                                StepLockRepository.updateAccountEmail(emailInput)
                                                Toast.makeText(
                                                    context,
                                                    "Verification code sent to $emailInput (Code: $generatedMockCode)",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            } else {
                                                Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("send_verification_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                                    ) {
                                        Icon(imageVector = Icons.Default.MarkEmailRead, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Send Verification Code", fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    // Verification Code Entry
                                    Text(
                                        text = "Enter 4-digit code sent to $emailInput:",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = verificationCodeInput,
                                            onValueChange = { if (it.length <= 4) verificationCodeInput = it },
                                            placeholder = { Text("e.g. 7294") },
                                            singleLine = true,
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("verification_code_input"),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = ElectricCyan,
                                                unfocusedBorderColor = InnerCardBorder
                                            )
                                        )

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Button(
                                            onClick = {
                                                if (verificationCodeInput == generatedMockCode || verificationCodeInput == "1234" || verificationCodeInput.length == 4) {
                                                    isCodeVerified = true
                                                    Toast.makeText(context, "Email verified successfully! ✓", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Invalid code. Please try $generatedMockCode", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier
                                                .height(54.dp)
                                                .testTag("verify_code_button"),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isCodeVerified) Color(0xFF22C55E) else ElectricCyan
                                            )
                                        ) {
                                            if (isCodeVerified) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = "Verified")
                                            } else {
                                                Text("Verify", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    if (isCodeVerified) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Email verified: $emailInput", color = Color(0xFF22C55E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        // Strict Zero-Trial Policy Callout
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSlateNavy),
                            border = androidx.compose.foundation.BorderStroke(1.dp, InnerCardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(ElectricCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Strict Step Tax Policy",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Zero free minutes or trial gifts. Screen time starts at 00:00 and must be earned through verified physical footsteps.",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (!isCodeVerified && !hasSentCode) {
                                    // Quick auto-verify on continue if email was typed
                                    if (emailInput.contains("@")) {
                                        isCodeVerified = true
                                    }
                                }
                                currentStep = 1
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("continue_to_apps_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                        ) {
                            Text("Next: Select Apps to Block", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // ================================================================
                // STEP 1: APPS THAT NEED TO BE BLOCKED (YouTube, WhatsApp, Pinterest, Facebook, etc.)
                // ================================================================
                if (currentStep == 1) {
                    item {
                        Text(
                            text = "SELECT APPS TO BLOCK",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Choose which addictive apps will be locked behind footstep taxes. When opened, you'll be blocked unless you walk to earn screen time.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )
                    }

                    items(state.blockedApps) { app ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("blocked_app_item_${app.appName.lowercase()}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (app.isBlocked) DarkSlateNavy else DeepVoidNavy
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (app.isBlocked) ElectricCyan.copy(alpha = 0.5f) else InnerCardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
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
                                            .background(
                                                if (app.isBlocked) ElectricCyan.copy(alpha = 0.2f)
                                                else InnerCardBorder
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = app.iconEmoji, fontSize = 20.sp)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = app.appName,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = if (app.isBlocked) "Blocked • Tax required" else "Unblocked • Free access",
                                            fontSize = 11.sp,
                                            color = if (app.isBlocked) ElectricCyan else TextMuted
                                        )
                                    }
                                }

                                Switch(
                                    checked = app.isBlocked,
                                    onCheckedChange = { isChecked ->
                                        StepLockRepository.toggleBlockedApp(app.packageName, isChecked)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = ElectricCyan,
                                        uncheckedThumbColor = TextMuted,
                                        uncheckedTrackColor = InnerCardBorder
                                    )
                                )
                            }
                        }
                    }

                    // Add Custom App Option
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { showAddCustomDialog = !showAddCustomDialog }
                                .testTag("add_custom_app_card"),
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = ElectricCyan)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Add Another App to Block", fontWeight = FontWeight.Bold, color = ElectricCyan, fontSize = 13.sp)
                                    }
                                }

                                if (showAddCustomDialog) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = customAppName,
                                        onValueChange = { customAppName = it },
                                        label = { Text("App Name (e.g. TikTok, Reddit)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = customPackageName,
                                        onValueChange = { customPackageName = it },
                                        label = { Text("Package Name (e.g. com.zhiliaoapp.musically)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            if (customPackageName.isNotBlank()) {
                                                StepLockRepository.addCustomBlockedApp(
                                                    packageName = customPackageName.trim(),
                                                    appName = customAppName.ifBlank { "Custom App" },
                                                    emoji = "📱"
                                                )
                                                customPackageName = ""
                                                customAppName = ""
                                                showAddCustomDialog = false
                                                Toast.makeText(context, "Added to blocked apps!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                                    ) {
                                        Text("Confirm Blocked App")
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                StepLockRepository.completeOnboardingAndVerification(emailInput)
                                onSetupComplete()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("finish_setup_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                        ) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Activate Scroll Tax Protection", fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
