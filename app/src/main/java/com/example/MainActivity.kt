package com.example

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BlueCard
import com.example.ui.theme.BlueCardLight
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.LockRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.UnlockGreen
import com.example.ui.theme.WarningAmber

class MainActivity : ComponentActivity() {

    private var stepSensorManager: StepSensorManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        StepLockRepository.init(this)
        stepSensorManager = StepSensorManager(this)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                val state by StepLockRepository.state.collectAsState()

                if (!state.hasSelectedProfileOnStartup) {
                    ProfileSelectionScreen(
                        onProfileSelected = { profileId ->
                            StepLockRepository.selectProfileAndDismissStartup(profileId)
                        }
                    )
                } else {
                    MainScreen(
                        onStartService = {
                            AppMonitorService.start(this)
                        },
                        onStopService = {
                            AppMonitorService.stop(this)
                        },
                        onOpenUsageSettings = {
                            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            try {
                                startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(this, "Could not open Usage Settings", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onOpenOverlaySettings = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:$packageName")
                                ).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                try {
                                    startActivity(intent)
                                } catch (e: Exception) {
                                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                                }
                            }
                        },
                        onLaunchInstagram = {
                            val launchIntent = packageManager.getLaunchIntentForPackage("com.instagram.android")
                            if (launchIntent != null) {
                                startActivity(launchIntent)
                            } else {
                                Toast.makeText(
                                    this,
                                    "Instagram is not installed. Testing with StepLock overlay.",
                                    Toast.LENGTH_LONG
                                ).show()
                                val lockIntent = Intent(this, LockScreenActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                startActivity(lockIntent)
                            }
                        },
                        onOpenProfileSelection = {
                            StepLockRepository.showProfileSelection()
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        StepLockRepository.syncPermissions(this)
        stepSensorManager?.startListening()
    }

    override fun onPause() {
        super.onPause()
        if (!StepLockRepository.state.value.isServiceRunning) {
            stepSensorManager?.stopListening()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    onOpenUsageSettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onLaunchInstagram: () -> Unit,
    onOpenProfileSelection: () -> Unit
) {
    val context = LocalContext.current
    val state by StepLockRepository.state.collectAsState()

    var showProfileSheet by remember { mutableStateOf(false) }
    var showRateDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                StepLockRepository.syncPermissions(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val activityRecognitionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        StepLockRepository.syncPermissions(context)
        if (granted) {
            Toast.makeText(context, "Step tracking permission granted!", Toast.LENGTH_SHORT).show()
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        StepLockRepository.syncPermissions(context)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_screen_scaffold"),
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
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CyanAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsRun,
                                    contentDescription = null,
                                    tint = BackgroundDark,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "StepLock",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = TextPrimaryDark
                            )
                        }
                        Text(
                            text = "Physical step gate for Instagram",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Service Running Indicator Badge
                    val serviceBadgeColor by animateColorAsState(
                        targetValue = if (state.isServiceRunning) UnlockGreen else TextMutedDark,
                        label = "badgeColor"
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceDark)
                            .border(1.dp, serviceBadgeColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(serviceBadgeColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.isServiceRunning) "RUNNING" else "STOPPED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = serviceBadgeColor
                        )
                    }
                }
            }

            // Goal Achieved Celebration Banner (if newly unlocked)
            state.recentGoalUnlockedMessage?.let { goalMsg ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("goal_celebration_banner"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A2F)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldNeon)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = goalMsg,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                            }
                            IconButton(
                                onClick = { StepLockRepository.clearGoalUnlockedMessage() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = TextSecondaryDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // MULTI-PROFILE BAR (Switch & Manage Profiles)
            item {
                ActiveProfileHeaderCard(
                    activeProfile = state.activeProfile,
                    totalProfiles = state.profiles.size,
                    onSwitchClick = { showProfileSheet = true },
                    onTuneClick = { showRateDialog = true }
                )
            }

            // DAILY STEP GOAL & BONUS MINUTES PROGRESS CARD (NEW FEATURE)
            item {
                DailyStepGoalProgressCard(
                    profileName = state.activeProfile.name,
                    dailySteps = state.dailySteps,
                    dailyStepGoal = state.dailyStepGoal,
                    bonusMinutes = state.bonusMinutes,
                    isGoalReached = state.isGoalReached,
                    goalProgress = state.goalProgress,
                    goalPercentage = state.goalPercentage,
                    stepsRemaining = state.stepsRemainingToGoal,
                    hasClaimedBonus = state.hasClaimedGoalBonus,
                    onConfigureGoal = { showGoalDialog = true }
                )
            }

            // Banked Time Vault Card (Hero Visual for Active Profile)
            item {
                BankedTimeVaultCard(
                    profileName = state.activeProfile.name,
                    bankedSeconds = state.bankedSeconds,
                    bankedMinutes = state.bankedMinutes,
                    bankedRemainder = state.bankedSecondsRemainder,
                    dailySteps = state.dailySteps,
                    stepsPerMinute = state.stepsPerMinute,
                    totalEarnedMinutes = state.totalEarnedSeconds / 60,
                    totalSpentMinutes = state.totalSpentSeconds / 60,
                    onLockNow = {
                        StepLockRepository.setBankedSeconds(0)
                        Toast.makeText(context, "Vault emptied! ${state.activeProfile.name} is now locked.", Toast.LENGTH_SHORT).show()
                    },
                    onTestLockScreen = {
                        val lockIntent = Intent(context, LockScreenActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(lockIntent)
                    }
                )
            }

            // Foreground Monitoring Service Card
            item {
                ServiceControlCard(
                    isRunning = state.isServiceRunning,
                    onStart = onStartService,
                    onStop = onStopService,
                    allPermissionsGranted = state.hasUsagePermission && state.hasOverlayPermission
                )
            }

            // Physical Step Counter Card
            item {
                StepCounterCard(
                    dailySteps = state.dailySteps,
                    stepsToNextMinute = state.stepsToNextMinute,
                    progress = state.progressToNextMinute,
                    isSensorAvailable = state.isStepSensorAvailable,
                    sensorName = state.stepSensorName,
                    hasActivityPermission = state.hasActivityPermission,
                    onRequestPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            activityRecognitionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                        }
                    }
                )
            }

            // Simulate Steps Section
            item {
                StepSimulationCard(
                    isSimulateMode = state.isSimulateMode,
                    profileName = state.activeProfile.name,
                    onToggle = { StepLockRepository.setSimulateMode(it) },
                    onAddSteps = { count ->
                        StepLockRepository.addSteps(count)
                        val mins = count / state.stepsPerMinute
                        Toast.makeText(
                            context,
                            "+$count steps added to ${state.activeProfile.name}! Earned $mins minute(s)",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onReset = {
                        StepLockRepository.resetActiveProfileStats()
                        Toast.makeText(context, "Steps & vault reset for ${state.activeProfile.name}", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Required Permissions Checklist
            item {
                PermissionsControlCard(
                    state = state,
                    onOpenUsageSettings = onOpenUsageSettings,
                    onOpenOverlaySettings = onOpenOverlaySettings,
                    onRequestActivityPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            activityRecognitionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                        }
                    },
                    onRequestNotificationPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
            }

            // Target App Launch / Test Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Target App Verification",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Guarding com.instagram.android. Active profile '${state.activeProfile.name}' has ${state.bankedMinutes}m ${state.bankedSecondsRemainder}s remaining.",
                            fontSize = 13.sp,
                            color = TextSecondaryDark,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onLaunchInstagram,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("launch_instagram_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BlueCardLight)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Launch Instagram (Test Lock)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Daily Step Goal Configuration Dialog
    if (showGoalDialog) {
        var tempGoal by remember { mutableIntStateOf(state.dailyStepGoal) }
        var tempBonus by remember { mutableIntStateOf(state.bonusMinutes) }

        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            containerColor = SurfaceDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Daily Goal Configuration",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Target for '${state.activeProfile.name}'. Hitting this step goal awards bonus screen time to your vault.",
                        fontSize = 13.sp,
                        color = TextSecondaryDark
                    )

                    // Step Goal Preset Selector
                    Column {
                        Text(
                            text = "Target Step Goal: $tempGoal steps",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyanAccent
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(3000, 5000, 6000, 8000, 10000).forEach { goal ->
                                Button(
                                    onClick = { tempGoal = goal },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (tempGoal == goal) CyanAccent else BlueCard
                                    )
                                ) {
                                    Text(
                                        text = "${goal / 1000}k",
                                        fontSize = 11.sp,
                                        color = if (tempGoal == goal) BackgroundDark else TextPrimaryDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Bonus Minutes Selector
                    Column {
                        Text(
                            text = "Bonus Vault Minutes on Completion: +$tempBonus minutes",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WarningAmber
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(10, 15, 20, 30).forEach { bonus ->
                                Button(
                                    onClick = { tempBonus = bonus },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (tempBonus == bonus) WarningAmber else BlueCard
                                    )
                                ) {
                                    Text(
                                        text = "+${bonus}m",
                                        fontSize = 12.sp,
                                        color = if (tempBonus == bonus) BackgroundDark else TextPrimaryDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        StepLockRepository.updateActiveProfileGoalSettings(tempGoal, tempBonus)
                        showGoalDialog = false
                        Toast.makeText(context, "Goal updated: $tempGoal steps (+${tempBonus}m bonus)", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    modifier = Modifier.testTag("save_goal_config_button")
                ) {
                    Text("Save Goal", color = BackgroundDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            }
        )
    }

    // Profile Switcher Sheet
    if (showProfileSheet) {
        ModalBottomSheet(
            onDismissRequest = { showProfileSheet = false },
            containerColor = SurfaceDark,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Switch Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )

                    Button(
                        onClick = {
                            showProfileSheet = false
                            showCreateDialog = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        modifier = Modifier.testTag("sheet_new_profile_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Profile", fontSize = 12.sp, color = BackgroundDark, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                state.profiles.forEach { profile ->
                    val isSelected = profile.id == state.activeProfileId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) SurfaceVariantDark else Color.Transparent)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) CyanAccent else BlueCardLight,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                StepLockRepository.switchProfile(profile.id)
                                showProfileSheet = false
                                Toast.makeText(context, "Switched to ${profile.name}", Toast.LENGTH_SHORT).show()
                            }
                            .padding(14.dp)
                            .testTag("switch_to_profile_${profile.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(profile.colorHex).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = profile.emoji, fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = profile.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                                Text(
                                    text = "${profile.bankedMinutes}m ${profile.bankedSecondsRemainder}s vault • Goal: ${profile.dailyStepGoal} steps",
                                    fontSize = 12.sp,
                                    color = TextSecondaryDark
                                )
                            }
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                    onClick = {
                        showProfileSheet = false
                        onOpenProfileSelection()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = CyanAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Manage & View All Profiles Screen", color = CyanAccent)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Rate / Difficulty Settings Dialog
    if (showRateDialog) {
        var tempRate by remember { mutableIntStateOf(state.activeProfile.stepsPerMinute) }

        AlertDialog(
            onDismissRequest = { showRateDialog = false },
            containerColor = SurfaceDark,
            title = {
                Text(
                    text = "Profile Difficulty: ${state.activeProfile.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            },
            text = {
                Column {
                    Text(
                        text = "Customize the step conversion rate for this profile:",
                        fontSize = 13.sp,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    listOf(
                        50 to "Casual (50 steps = 1 min)",
                        80 to "Moderate (80 steps = 1 min)",
                        100 to "Standard (100 steps = 1 min)",
                        150 to "Detox (150 steps = 1 min)",
                        200 to "Extreme (200 steps = 1 min)"
                    ).forEach { (rate, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (tempRate == rate) CyanAccent.copy(alpha = 0.2f) else SurfaceVariantDark)
                                .clickable { tempRate = rate }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (tempRate == rate) FontWeight.Bold else FontWeight.Normal,
                                color = if (tempRate == rate) CyanAccent else TextPrimaryDark
                            )
                            if (tempRate == rate) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        StepLockRepository.updateActiveProfileSettings(tempRate)
                        showRateDialog = false
                        Toast.makeText(context, "Updated difficulty to $tempRate steps/min", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Save", color = BackgroundDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRateDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            }
        )
    }

    if (showCreateDialog) {
        CreateProfileDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, emoji, rate, initialMinutes, dailyGoal, bonusMinutes ->
                StepLockRepository.createProfile(name, emoji, rate, initialMinutes, dailyGoal, bonusMinutes)
                showCreateDialog = false
                Toast.makeText(context, "Profile '$name' created!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun DailyStepGoalProgressCard(
    profileName: String,
    dailySteps: Int,
    dailyStepGoal: Int,
    bonusMinutes: Int,
    isGoalReached: Boolean,
    goalProgress: Float,
    goalPercentage: Int,
    stepsRemaining: Int,
    hasClaimedBonus: Boolean,
    onConfigureGoal: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = goalProgress,
        animationSpec = tween(600),
        label = "goalProgressAnim"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_step_goal_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isGoalReached) EmeraldNeon.copy(alpha = 0.8f) else CyanAccent.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = if (isGoalReached) {
                            listOf(Color(0xFF0F3124), SurfaceDark)
                        } else {
                            listOf(Color(0xFF14243A), SurfaceDark)
                        }
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                // Top Header Row with Goal title and Edit Goal Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isGoalReached) WarningAmber else Color(0x3300E5FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = if (isGoalReached) BackgroundDark else CyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "DAILY STEP GOAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isGoalReached) EmeraldNeon else CyanAccent,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Target: $dailyStepGoal steps",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimaryDark
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onConfigureGoal,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp).testTag("configure_goal_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit Goal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Big Progress Metric Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$dailySteps",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isGoalReached) EmeraldNeon else TextPrimaryDark,
                                modifier = Modifier.testTag("goal_current_steps_text")
                            )
                            Text(
                                text = " / $dailyStepGoal",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondaryDark,
                                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                            )
                        }
                        Text(
                            text = if (isGoalReached) "Goal Achieved!" else "$stepsRemaining steps to go",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isGoalReached) EmeraldNeon else TextSecondaryDark
                        )
                    }

                    // Percentage Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isGoalReached) EmeraldNeon else CyanAccent)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$goalPercentage%",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = BackgroundDark,
                            modifier = Modifier.testTag("goal_percentage_badge")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar with smooth animation
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .testTag("daily_goal_progress_bar"),
                    color = if (isGoalReached) EmeraldNeon else CyanAccent,
                    trackColor = SurfaceVariantDark
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Bonus Minutes Status Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isGoalReached) Color(0x3310B981) else SurfaceVariantDark)
                        .border(
                            1.dp,
                            if (isGoalReached) EmeraldNeon.copy(alpha = 0.5f) else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isGoalReached) {
                                "Bonus Reward: +$bonusMinutes Minutes Added to Vault"
                            } else {
                                "Reward: Reach goal to unlock +$bonusMinutes Bonus Minutes"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGoalReached) EmeraldNeon else TextPrimaryDark
                        )
                    }

                    if (isGoalReached) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Claimed",
                            tint = EmeraldNeon,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveProfileHeaderCard(
    activeProfile: UserProfile,
    totalProfiles: Int,
    onSwitchClick: () -> Unit,
    onTuneClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_profile_header_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(activeProfile.colorHex).copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSwitchClick() }
            ) {
                // Emoji avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(activeProfile.colorHex).copy(alpha = 0.2f))
                        .border(1.5.dp, Color(activeProfile.colorHex), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = activeProfile.emoji, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = activeProfile.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "($totalProfiles available)",
                            fontSize = 11.sp,
                            color = TextMutedDark
                        )
                    }
                    Text(
                        text = "Rate: ${activeProfile.stepsPerMinute} steps/min • Goal: ${activeProfile.dailyStepGoal}",
                        fontSize = 12.sp,
                        color = CyanAccent
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onTuneClick,
                    modifier = Modifier.size(36.dp).testTag("tune_profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Profile Difficulty",
                        tint = TextSecondaryDark,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Button(
                    onClick = onSwitchClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("switch_profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Switch",
                        fontSize = 12.sp,
                        color = CyanAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceControlCard(
    isRunning: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    allPermissionsGranted: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("service_control_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AppMonitorService",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = if (isRunning) {
                            "1-second interval monitor active • Tracking Instagram"
                        } else {
                            "Background service is offline. Tap start to begin enforcement."
                        },
                        fontSize = 12.sp,
                        color = if (isRunning) EmeraldNeon else TextSecondaryDark,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = if (isRunning) onStop else onStart,
                    modifier = Modifier
                        .height(46.dp)
                        .testTag("service_toggle_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) LockRed else CyanAccent
                    )
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (isRunning) Color.White else BackgroundDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRunning) "Stop" else "Start",
                        color = if (isRunning) Color.White else BackgroundDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            if (!allPermissionsGranted) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x22F59E0B))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Grant Usage Access & Draw Over Apps below for auto-lock.",
                        fontSize = 11.sp,
                        color = WarningAmber
                    )
                }
            }
        }
    }
}

@Composable
fun BankedTimeVaultCard(
    profileName: String,
    bankedSeconds: Int,
    bankedMinutes: Int,
    bankedRemainder: Int,
    dailySteps: Int,
    stepsPerMinute: Int,
    totalEarnedMinutes: Int,
    totalSpentMinutes: Int,
    onLockNow: () -> Unit,
    onTestLockScreen: () -> Unit
) {
    val isLocked = bankedSeconds <= 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vault_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (isLocked) Color(0xFF2A0F15) else Color(0xFF0C243C),
                            SurfaceDark
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = if (isLocked) LockRed else CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VAULT: $profileName",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLocked) LockRed else CyanAccent,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "$stepsPerMinute steps = 1 min",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondaryDark
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time Counter Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "${bankedMinutes}m ${bankedRemainder}s",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isLocked) LockRed else EmeraldNeon,
                        letterSpacing = (-1).sp,
                        modifier = Modifier.testTag("vault_time_display")
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isLocked) "ACCESS LOCKED" else "REMAINING",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLocked) LockRed else UnlockGreen,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceVariantDark)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Total Earned", fontSize = 11.sp, color = TextMutedDark)
                        Text(
                            text = "${totalEarnedMinutes}m",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(BlueCardLight)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Total Spent", fontSize = 11.sp, color = TextMutedDark)
                        Text(
                            text = "${totalSpentMinutes}m",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(BlueCardLight)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Profile Steps", fontSize = 11.sp, color = TextMutedDark)
                        Text(
                            text = "$dailySteps",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons for testing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onLockNow,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("empty_vault_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LockRed)
                    ) {
                        Text("Lock Now (0s)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onTestLockScreen,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("view_lock_screen_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueCardLight)
                    ) {
                        Text("Preview Lock UI", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun StepCounterCard(
    dailySteps: Int,
    stepsToNextMinute: Int,
    progress: Float,
    isSensorAvailable: Boolean,
    sensorName: String,
    hasActivityPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("step_counter_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
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
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Physical Step Tracker",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                }

                // Sensor Status Chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceVariantDark)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        tint = if (isSensorAvailable) UnlockGreen else WarningAmber,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSensorAvailable) "HARDWARE SENSOR" else "SIMULATED SENSOR",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSensorAvailable) UnlockGreen else WarningAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "$dailySteps",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimaryDark,
                        modifier = Modifier.testTag("daily_steps_text")
                    )
                    Text(
                        text = "Steps walked today",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$stepsToNextMinute steps",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                    Text(
                        text = "to earn next minute",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = CyanAccent,
                trackColor = SurfaceVariantDark,
            )

            if (!hasActivityPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .testTag("grant_activity_permission_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Grant Physical Activity Permission", fontSize = 12.sp, color = BackgroundDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StepSimulationCard(
    isSimulateMode: Boolean,
    profileName: String,
    onToggle: (Boolean) -> Unit,
    onAddSteps: (Int) -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("simulate_steps_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Simulate Steps",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Test walking & goal progress for $profileName",
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    }
                }

                Switch(
                    checked = isSimulateMode,
                    onCheckedChange = onToggle,
                    modifier = Modifier.testTag("simulate_steps_toggle"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyanAccent,
                        checkedTrackColor = BlueCard
                    )
                )
            }

            AnimatedVisibility(visible = isSimulateMode) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "Quick walk simulation controls:",
                        fontSize = 12.sp,
                        color = TextSecondaryDark,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onAddSteps(100) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("simulate_100_steps_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                        ) {
                            Text(
                                text = "+100",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = BackgroundDark,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { onAddSteps(500) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("simulate_500_steps_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldNeon)
                        ) {
                            Text(
                                text = "+500",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = BackgroundDark,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { onAddSteps(1000) },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp)
                                .testTag("simulate_1000_steps_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WarningAmber)
                        ) {
                            Text(
                                text = "+1,000 (Goal Boost)",
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                color = BackgroundDark,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = onReset,
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("reset_steps_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset",
                                tint = TextSecondaryDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionsControlCard(
    state: StepLockData,
    onOpenUsageSettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onRequestActivityPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("permissions_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Permission Controls",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Required for StepLock to detect active foreground apps and display the lock screen.",
                fontSize = 12.sp,
                color = TextSecondaryDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Usage Access Permission
            PermissionRow(
                title = "Usage Access",
                subtitle = "Detects when Instagram is opened",
                isGranted = state.hasUsagePermission,
                actionLabel = "Open Settings",
                testTag = "open_usage_settings_button",
                onClick = onOpenUsageSettings
            )

            HorizontalDivider(
                color = SurfaceVariantDark,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            // 2. Draw Over Other Apps / Overlay Permission
            PermissionRow(
                title = "Draw Over Other Apps",
                subtitle = "Displays fullscreen lock over Instagram",
                isGranted = state.hasOverlayPermission,
                actionLabel = "Open Settings",
                testTag = "open_overlay_settings_button",
                onClick = onOpenOverlaySettings
            )

            HorizontalDivider(
                color = SurfaceVariantDark,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            // 3. Activity Recognition
            PermissionRow(
                title = "Physical Activity Tracking",
                subtitle = "Hardware step counter integration",
                isGranted = state.hasActivityPermission,
                actionLabel = "Grant",
                testTag = "grant_activity_button",
                onClick = onRequestActivityPermission
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                HorizontalDivider(
                    color = SurfaceVariantDark,
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                // 4. Notifications
                PermissionRow(
                    title = "Foreground Notification",
                    subtitle = "Maintains persistent 1-second monitoring",
                    isGranted = state.hasNotificationPermission,
                    actionLabel = "Grant",
                    testTag = "grant_notification_button",
                    onClick = onRequestNotificationPermission
                )
            }
        }
    }
}

@Composable
fun PermissionRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    actionLabel: String,
    testTag: String,
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
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isGranted) UnlockGreen else WarningAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimaryDark
                )
            }
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondaryDark,
                modifier = Modifier.padding(start = 22.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        if (isGranted) {
            Text(
                text = "Granted",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = UnlockGreen,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        } else {
            Button(
                onClick = onClick,
                modifier = Modifier
                    .height(36.dp)
                    .testTag(testTag),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                Text(
                    text = actionLabel,
                    fontSize = 11.sp,
                    color = BackgroundDark,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
