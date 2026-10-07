package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.DarkSlateNavy
import com.example.ui.theme.DeepVoidNavy
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.InnerCardBorder
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmberGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {

    private var stepSensorManager: StepSensorManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        StepLockRepository.init(this)
        stepSensorManager = StepSensorManager(this)

        // Automatically start the background monitor service
        try {
            AppMonitorService.start(this)
        } catch (e: Exception) {
            // Service start handled gracefully
        }

        // Silent background auto sign-in with Google/Firebase
        FirebaseAuthManager.attemptAutoSignIn(
            context = this,
            scope = lifecycleScope,
            onSuccess = {
                StepLockRepository.syncWithFirebase()
            }
        )

        setContent {
            MyApplicationTheme(darkTheme = true) {
                val state by StepLockRepository.state.collectAsState()
                val navController = rememberAppNavController(AppRoute.Dashboard)
                val context = LocalContext.current
                var showInstagramOptionsDialog by remember { mutableStateOf(false) }

                // GLOBAL NAVIGATION CONTROLLER BACK-HANDLER:
                // When moving between levels, roadmap, settings, trends, or achievements,
                // pressing Back navigates back through the stack to the Dashboard, NEVER exiting the app.
                BackHandler(enabled = !navController.isAtRoot()) {
                    navController.popBack()
                }

                // Root Dashboard double-back exit safety
                var lastBackPressTimestamp by remember { mutableLongStateOf(0L) }
                BackHandler(enabled = navController.isAtRoot()) {
                    val now = System.currentTimeMillis()
                    if (now - lastBackPressTimestamp < 2000L) {
                        finish()
                    } else {
                        lastBackPressTimestamp = now
                        Toast.makeText(context, "Press back again to exit Scroll Tax", Toast.LENGTH_SHORT).show()
                    }
                }

                // Initial Startup Profile Selection
                if (!state.hasSelectedProfileOnStartup) {
                    ProfileSelectionScreen(
                        onProfileSelected = { profileId ->
                            StepLockRepository.selectProfileAndDismissStartup(profileId)
                        }
                    )
                } else {
                    // HashRouter-style Route Switcher
                    when (val route = navController.currentRoute) {
                        is AppRoute.Dashboard -> {
                            MainDashboardScreen(
                                state = state,
                                navController = navController,
                                onLaunchInstagram = {
                                    val launchIntent = packageManager.getLaunchIntentForPackage("com.instagram.android")
                                    if (launchIntent != null) {
                                        startActivity(launchIntent)
                                    } else {
                                        showInstagramOptionsDialog = true
                                    }
                                }
                            )
                        }

                        is AppRoute.LevelsRoadmap -> {
                            LevelsRoadmapScreen(
                                state = state,
                                onNavigateBack = { navController.popBack() },
                                onSimulateXp = { StepLockRepository.simulateAddXp(it) },
                                onSetRankDirectly = { selectedRank ->
                                    StepLockRepository.switchRankLevel(selectedRank)
                                    Toast.makeText(
                                        context,
                                        "Activated Level ${selectedRank.level}: ${selectedRank.title} ⚡",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }

                        is AppRoute.LevelDetail -> {
                            LevelsRoadmapScreen(
                                state = state,
                                onNavigateBack = { navController.popBack() },
                                onSimulateXp = { StepLockRepository.simulateAddXp(it) },
                                onSetRankDirectly = { selectedRank ->
                                    StepLockRepository.switchRankLevel(selectedRank)
                                }
                            )
                        }

                        is AppRoute.Settings -> {
                            SettingsScreen(
                                state = state,
                                onNavigateBack = { navController.popBack() },
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
                                onRequestActivityPermission = {
                                    // Handled in settings UI
                                },
                                onRequestNotificationPermission = {
                                    // Handled in settings UI
                                },
                                onSwitchProfileClick = {
                                    navController.navigateTo(AppRoute.ProfileSelection)
                                },
                                onTuneStepGoal = { newGoal ->
                                    StepLockRepository.updateDailyStepGoal(newGoal)
                                },
                                onSignInGoogle = {
                                    FirebaseAuthManager.signInWithGoogle(
                                        activity = this@MainActivity,
                                        scope = lifecycleScope,
                                        onSuccess = { user ->
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Connected to Firebase as ${user.displayName ?: user.email} ☁️",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            StepLockRepository.syncWithFirebase()
                                        },
                                        onError = { errorMsg ->
                                            Toast.makeText(this@MainActivity, "Sign-in error: $errorMsg", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                onSignOutGoogle = {
                                    FirebaseAuthManager.signOut(
                                        context = this@MainActivity,
                                        scope = lifecycleScope,
                                        onComplete = {
                                            Toast.makeText(this@MainActivity, "Signed out from Google account", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                },
                                onSyncFirebaseNow = {
                                    StepLockRepository.syncWithFirebase()
                                    Toast.makeText(this@MainActivity, "Synced to Firestore cloud database ☁️", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        is AppRoute.ProfileSelection -> {
                            ProfileSelectionScreen(
                                onProfileSelected = { profileId ->
                                    StepLockRepository.selectProfileAndDismissStartup(profileId)
                                    navController.popBack()
                                }
                            )
                        }

                        is AppRoute.Trends -> {
                            TrendsScreen(
                                onNavigateBack = { navController.popBack() }
                            )
                        }

                        is AppRoute.Achievements -> {
                            AchievementsScreen(
                                onNavigateBack = { navController.popBack() }
                            )
                        }

                        is AppRoute.WebCompanion -> {
                            WebCompanionScreen(
                                onNavigateBack = { navController.popBack() }
                            )
                        }
                    }

                    if (showInstagramOptionsDialog) {
                        InstagramOptionsDialog(
                            onDismiss = { showInstagramOptionsDialog = false },
                            onOpenWeb = {
                                showInstagramOptionsDialog = false
                                try {
                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com")).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    startActivity(webIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(this@MainActivity, "Could not open browser for Instagram", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onInstallPlayStore = {
                                showInstagramOptionsDialog = false
                                try {
                                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.instagram.android")).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    })
                                } catch (e: Exception) {
                                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.instagram.android")).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    })
                                }
                            },
                            onTestLockOverlay = {
                                showInstagramOptionsDialog = false
                                val lockIntent = Intent(this@MainActivity, LockScreenActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                startActivity(lockIntent)
                            }
                        )
                    }
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

@Composable
fun MainDashboardScreen(
    state: StepLockData,
    navController: AppNavController,
    onLaunchInstagram: () -> Unit
) {
    val context = LocalContext.current
    var hasDismissedOnboarding by remember { mutableStateOf(false) }
    var showModeSwitcherSheet by remember { mutableStateOf(false) }
    var showRateModeDialog by remember { mutableStateOf(false) }

    // Haptic feedback listener
    LaunchedEffect(state.triggerMilestoneHapticEvent) {
        if (state.triggerMilestoneHapticEvent > 0L) {
            triggerVibration(context)
        }
    }

    // Active walk session duration ticker
    LaunchedEffect(state.activeWalkSession.isActive) {
        if (state.activeWalkSession.isActive) {
            while (isActive) {
                delay(1000)
                StepLockRepository.tickActiveWalkDuration()
            }
        }
    }

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

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_screen_scaffold"),
        containerColor = DeepVoidNavy
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ================================================================
            // TOP APP BAR: Minimalist Vector Logo, Brand, Rank & Settings
            // ================================================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Minimalist Vector Logo & App Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { navController.navigateTo(AppRoute.LevelsRoadmap) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSlateNavy)
                                .border(1.2.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_scroll_tax_logo),
                                contentDescription = "Scroll Tax Logo",
                                tint = ElectricCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Scroll Tax",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Walk to Earn Screen Time",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }
                    }

                    // Right: Mode Switcher, Compact Rank Badge & Settings Icon
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Quick Mode Switcher Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSlateNavy)
                                .border(1.dp, Color(state.activeProfile.colorHex).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .clickable { showModeSwitcherSheet = true }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .testTag("top_bar_mode_pill")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = state.activeProfile.emoji, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = state.activeProfile.name.split(" ").first(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(state.activeProfile.colorHex)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Cloud Sync & Web Companion Indicator
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSlateNavy)
                                .border(
                                    1.dp,
                                    if (state.isFirebaseConnected) EmeraldNeon.copy(alpha = 0.5f) else InnerCardBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { navController.navigateTo(AppRoute.WebCompanion) }
                                .testTag("top_bar_cloud_indicator"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (state.isFirebaseConnected) "🌐" else "☁️",
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        CompactRankBadge(
                            rank = state.rank,
                            onClick = { navController.navigateTo(AppRoute.LevelsRoadmap) }
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = { navController.navigateTo(AppRoute.Settings) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSlateNavy)
                                .border(1.dp, InnerCardBorder, RoundedCornerShape(10.dp))
                                .testTag("open_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = TextSecondary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }

            // ================================================================
            // PROMINENT ACTIVE MODE & PROFILE BANNER (CHANGE BACK ANYTIME)
            // ================================================================
            item {
                ActiveModeBanner(
                    activeProfile = state.activeProfile,
                    onChangeModeClick = { showModeSwitcherSheet = true }
                )
            }

            // ================================================================
            // INTERACTIVE LEVEL SWITCHER BAR (SWITCH BETWEEN LEVELS DIRECTLY)
            // ================================================================
            item {
                RankLevelSwitcherBar(
                    currentRank = state.rank,
                    onSelectRank = { targetRank ->
                        StepLockRepository.switchRankLevel(targetRank)
                        Toast.makeText(
                            context,
                            "Switched to Level ${targetRank.level}: ${targetRank.title} ⚡",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onOpenRoadmap = { navController.navigateTo(AppRoute.LevelsRoadmap) }
                )
            }

            // ================================================================
            // CARD 1: TODAY'S MOVEMENT (HERO)
            // ================================================================
            item {
                TodayMovementHeroCard(
                    dailySteps = state.dailySteps,
                    dailyStepGoal = state.dailyStepGoal,
                    currentRate = state.currentStepsPerMinute,
                    period = state.currentPeriod
                )
            }

            // ================================================================
            // CARD 2: SCROLL TAX VAULT (SCREEN TIME BANK)
            // ================================================================
            item {
                ScrollTaxVaultCard(
                    bankedMinutes = state.bankedMinutes,
                    bankedSecondsRemainder = state.bankedSecondsRemainder,
                    isLocked = state.isLocked,
                    stepsToNextMinute = state.stepsToNextMinute,
                    currentRate = state.currentStepsPerMinute,
                    progressToNextMinute = state.progressToNextMinute,
                    currentPeriod = state.currentPeriod,
                    onOpenInstagram = onLaunchInstagram,
                    onChangeRateMode = { showRateModeDialog = true }
                )
            }

            // ================================================================
            // CARD 3: ACTIVE WALK COMPANION (ACTION TRIGGER)
            // ================================================================
            item {
                ActiveWalkCompanionCard(
                    session = state.activeWalkSession,
                    onStartWalk = {
                        StepLockRepository.startActiveWalkSession()
                        Toast.makeText(context, "Dedicated Walk Started! Haptics every 500 steps.", Toast.LENGTH_SHORT).show()
                    },
                    onStopWalk = {
                        StepLockRepository.stopActiveWalkSession()
                        Toast.makeText(context, "Walk session completed! Screen time earned.", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // ================================================================
            // CARD 4: WEB COMPANION ACCESS CARD (PORTAL & REMOTE DASHBOARD)
            // ================================================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigateTo(AppRoute.WebCompanion) }
                        .testTag("dashboard_web_companion_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateNavy),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                ElectricCyan.copy(alpha = 0.5f),
                                InnerCardBorder
                            )
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ElectricCyan.copy(alpha = 0.15f))
                                    .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🌐", fontSize = 22.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Web Companion Portal",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(EmeraldNeon.copy(alpha = 0.2f))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "SYNCED",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = EmeraldNeon
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Monitor steps & lock status on desktop browser",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open Web Companion",
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ================================================================
            // QUICK NAVIGATION TILE: TRENDS & ACHIEVEMENTS
            // ================================================================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { navController.navigateTo(AppRoute.Trends) }
                            .testTag("dashboard_trends_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSlateNavy),
                        border = androidx.compose.foundation.BorderStroke(1.dp, InnerCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "📊", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Weekly Trends",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Heatmaps & stats",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { navController.navigateTo(AppRoute.Achievements) }
                            .testTag("dashboard_achievements_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSlateNavy),
                        border = androidx.compose.foundation.BorderStroke(1.dp, InnerCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🏆", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Achievements",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${state.unlockedAchievementsCount} Unlocked",
                                    fontSize = 10.sp,
                                    color = WarmAmberGold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // One-Time Clean Onboarding Permissions Dialog
    if (!hasDismissedOnboarding && (!state.hasUsagePermission || !state.hasOverlayPermission)) {
        OnboardingPermissionsDialog(
            hasUsage = state.hasUsagePermission,
            hasOverlay = state.hasOverlayPermission,
            onOpenUsageSettings = {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Could not open Usage Settings", Toast.LENGTH_SHORT).show()
                }
            },
            onOpenOverlaySettings = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                    }
                }
            },
            onDismiss = { hasDismissedOnboarding = true }
        )
    }

    // Rank Promotion Celebration Dialog
    if (state.rankLevelUpCelebration != null) {
        RankLevelUpCelebrationDialog(
            rank = state.rankLevelUpCelebration!!,
            onDismiss = { StepLockRepository.dismissRankLevelUpCelebration() }
        )
    }

    // Milestone Badge Celebration Dialog
    if (state.unlockedBadgeCelebration != null) {
        BadgeCelebrationDialog(
            achievement = state.unlockedBadgeCelebration!!,
            onDismiss = { StepLockRepository.dismissBadgeCelebration() }
        )
    }

    // Quick Mode Switcher Bottom Sheet
    if (showModeSwitcherSheet) {
        ModeSwitcherBottomSheet(
            profiles = state.profiles,
            activeProfileId = state.activeProfileId,
            onDismiss = { showModeSwitcherSheet = false },
            onSelectProfile = { profileId ->
                StepLockRepository.switchProfile(profileId)
                Toast.makeText(context, "Active mode switched ⚡", Toast.LENGTH_SHORT).show()
            },
            onManageProfiles = {
                navController.navigateTo(AppRoute.ProfileSelection)
            }
        )
    }

    // Rate Mode Switcher Dialog
    if (showRateModeDialog) {
        RateModeSwitcherDialog(
            currentPeriod = state.currentPeriod,
            isSimulated = state.simulatedPeriod != null,
            onSelectPeriod = { period ->
                StepLockRepository.setSimulatedPeriod(period)
                Toast.makeText(context, "Tax rate mode updated!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showRateModeDialog = false }
        )
    }
}

fun triggerVibration(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(
                VibrationEffect.createOneShot(100L, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(100L, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(100L)
            }
        }
    } catch (e: Exception) {
        // Safe fallback
    }
}

@Composable
fun BadgeCelebrationDialog(
    achievement: Achievement,
    onDismiss: () -> Unit
) {
    BackHandler { onDismiss() }
    val tierColor = Color(achievement.tier.colorHex)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("badge_celebration_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, tierColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎉 MILESTONE UNLOCKED",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = tierColor,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(tierColor.copy(alpha = 0.2f))
                        .border(2.dp, tierColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = achievement.badgeEmoji, fontSize = 38.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = achievement.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = achievement.description,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dismiss_celebration_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = tierColor)
                ) {
                    Text(
                        text = "Claim Reward & Continue",
                        color = DeepVoidNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

/**
 * Beautiful dialog presented when the native Instagram application is not detected.
 * Provides web browser fallback, Play Store download, and lock screen test options.
 */
@Composable
fun InstagramOptionsDialog(
    onDismiss: () -> Unit,
    onOpenWeb: () -> Unit,
    onInstallPlayStore: () -> Unit,
    onTestLockOverlay: () -> Unit
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
                .testTag("instagram_options_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlateNavy),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ElectricCyan.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCB045))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "📸", fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Instagram Access",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "The native Instagram app isn't installed on this device/emulator. Choose how you'd like to proceed:",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Option 1: Open in Web Browser (instagram.com)
                Button(
                    onClick = onOpenWeb,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("open_instagram_web_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = DeepVoidNavy,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Open Instagram Web",
                        color = DeepVoidNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option 2: Test Scroll Tax Lock Overlay
                OutlinedButton(
                    onClick = onTestLockOverlay,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("test_lock_overlay_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, InnerCardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Test Lock Screen Overlay",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option 3: Install from Play Store
                OutlinedButton(
                    onClick = onInstallPlayStore,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("install_instagram_play_store_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, InnerCardBorder.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Text(
                        text = "Install from Google Play",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
