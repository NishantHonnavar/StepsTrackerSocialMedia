package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Web Companion Portal Screen for Scroll Tax.
 * 
 * Provides:
 * 1. Live Web Companion View: Responsive desktop/tablet/web browser dashboard view simulating
 *    the browser companion interface synced with Firebase Firestore.
 * 2. Multi-Device Cloud Sync Status: Live state representation between mobile device and web client.
 * 3. Quick Action Sync Bridge: Ability to log walking sessions or redeem screen time seamlessly.
 * 4. Web Portal URL & Access Key: Easy sharing and clipboard copy for opening on desktop browsers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebCompanionScreen(
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current
    val state by StepLockRepository.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Web Dashboard, 1: Cloud Sync Bridge, 2: Remote Control
    var isManualSyncing by remember { mutableStateOf(false) }

    val companionUrl = "https://ais-dev-v2m63grvwx2stndswnoosa-312870885302.asia-southeast1.run.app"
    val syncKey = remember { "STX-" + state.activeProfile.id.take(4).uppercase() + "-8891" }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("web_companion_scaffold"),
        containerColor = DeepVoidNavy,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSlateNavy)
                                .border(1.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Web Portal",
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Web Companion",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "Desktop & Browser Portal Sync",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("web_companion_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isManualSyncing = true
                            StepLockRepository.syncWithFirebase()
                            Toast.makeText(context, "Web companion cloud database refreshed ☁️", Toast.LENGTH_SHORT).show()
                            isManualSyncing = false
                        },
                        modifier = Modifier.testTag("web_companion_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Sync",
                            tint = ElectricCyan
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
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ================================================================
            // HERO BANNER: WEB APP CONNECTION STATUS
            // ================================================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("web_portal_hero_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateNavy),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        Brush.horizontalGradient(
                            listOf(
                                ElectricCyan.copy(alpha = 0.7f),
                                EmeraldNeon.copy(alpha = 0.5f),
                                InnerCardBorder
                            )
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (state.isFirebaseConnected) EmeraldNeon else WarmAmberGold)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (state.isFirebaseConnected) "CLOUD COMPANION READY" else "LOCAL SIMULATION COMPANION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = if (state.isFirebaseConnected) EmeraldNeon else WarmAmberGold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ElectricCyan.copy(alpha = 0.15f))
                                    .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "LIVE SYNC",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Access Scroll Tax Anywhere",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            letterSpacing = (-0.5).sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Use this web companion on laptop or desktop browsers to monitor physical activity, track your banked screen time, and manage Instagram lock budgets from any web screen.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // URL Display and Copy Bar
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DeepVoidNavy),
                            border = androidx.compose.foundation.BorderStroke(1.dp, InnerCardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "WEB DASHBOARD URL",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextMuted,
                                        letterSpacing = 0.8.sp
                                    )
                                    Text(
                                        text = companionUrl,
                                        fontSize = 12.sp,
                                        color = ElectricCyan,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Scroll Tax Web URL", companionUrl)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Web companion URL copied to clipboard 📋", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Web URL",
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ================================================================
            // TAB SELECTOR: BROWSER VIEW / CLOUD SYNC / REMOTE LOGGING
            // ================================================================
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DarkSlateNavy,
                    contentColor = ElectricCyan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = ElectricCyan,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "🖥️ Web View",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "☁️ Sync Bridge",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                text = "⚡ Quick Actions",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            // ================================================================
            // TAB CONTENT
            // ================================================================
            when (selectedTab) {
                0 -> {
                    // TAB 0: INTERACTIVE RESPONSIVE WEB COMPANION PREVIEW
                    item {
                        WebBrowserPreviewCard(state = state)
                    }
                }
                1 -> {
                    // TAB 1: CLOUD SYNC BRIDGE DETAILS
                    item {
                        CloudSyncBridgeCard(
                            state = state,
                            syncKey = syncKey,
                            onTriggerSync = {
                                StepLockRepository.syncWithFirebase()
                                Toast.makeText(context, "Full cloud Firestore sync completed ☁️", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
                2 -> {
                    // TAB 2: REMOTE CONTROL & QUICK SIMULATOR BRIDGE
                    item {
                        WebRemoteActionsCard(
                            state = state,
                            onAddSteps = { amount ->
                                StepLockRepository.addSteps(amount)
                                Toast.makeText(context, "+$amount steps synced to web app 🚶‍♂️", Toast.LENGTH_SHORT).show()
                            },
                            onResetDaily = {
                                StepLockRepository.resetActiveProfileStats()
                                Toast.makeText(context, "Daily stats reset across web and mobile", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // ================================================================
            // CROSS-PLATFORM FEATURES LIST
            // ================================================================
            item {
                Text(
                    text = "CROSS-PLATFORM CAPABILITIES",
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
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        FeatureRowItem(
                            icon = "🔄",
                            title = "Real-Time Step Mirroring",
                            description = "Every step logged by your Android hardware pedometer reflects in real-time in the web companion."
                        )
                        HorizontalDivider(color = InnerCardBorder)
                        FeatureRowItem(
                            icon = "⏱️",
                            title = "Earned Allowance Calculator",
                            description = "Recalculates available Instagram screen time using active Golden Hour, Standard, and Sleep rates."
                        )
                        HorizontalDivider(color = InnerCardBorder)
                        FeatureRowItem(
                            icon = "📊",
                            title = "Weekly Analytics & Heatmaps",
                            description = "Full desktop charts for hourly activity heatmaps, streak analysis, and XP level roadmaps."
                        )
                        HorizontalDivider(color = InnerCardBorder)
                        FeatureRowItem(
                            icon = "🔒",
                            title = "Lock Overlay Remote Enforcement",
                            description = "When banked time reaches 0m 00s, the mobile device enforces lock overlay while the web companion warns you."
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/**
 * Card simulating the web browser dashboard (Responsive Web Companion Mockup).
 */
@Composable
fun WebBrowserPreviewCard(state: StepLockData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("web_browser_preview_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateNavy),
        border = androidx.compose.foundation.BorderStroke(1.dp, InnerCardBorder)
    ) {
        Column {
            // Mock Browser Chrome Titlebar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFF10B981)))
                    Spacer(modifier = Modifier.width(12.dp))

                    // Mock URL Address Bar
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "https://scrolltax.app/companion",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "SSL Secure",
                        tint = EmeraldNeon,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "HTTPS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldNeon
                    )
                }
            }

            // Web Dashboard Body
            Column(modifier = Modifier.padding(16.dp)) {
                // Header in Web Dashboard
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Scroll Tax Web Dashboard",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = "Synced: ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())} • User: ${state.firebaseUserDisplayName ?: state.activeProfile.name}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricCyan.copy(alpha = 0.2f))
                            .border(1.dp, ElectricCyan, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "MODE: ${state.activeProfile.emoji} ${state.activeProfile.name}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2-Column Responsive Web Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Left Column: Steps & Goal
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DeepVoidNavy),
                        border = androidx.compose.foundation.BorderStroke(1.dp, InnerCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "TODAY'S STEPS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%,d", state.dailySteps),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { state.goalProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = ElectricCyan,
                                trackColor = InnerCardBorder
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Goal: ${String.format(Locale.getDefault(), "%,d", state.dailyStepGoal)} (${state.goalPercentage}%)",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    // Right Column: Banked Minutes & Lock State
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DeepVoidNavy),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (state.isLocked) CrimsonCoral.copy(alpha = 0.5f) else EmeraldNeon.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "BANKED SCREEN TIME",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${state.bankedMinutes}m ${String.format(Locale.getDefault(), "%02d", state.bankedSecondsRemainder)}s",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = if (state.isLocked) CrimsonCoral else EmeraldNeon
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (state.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = if (state.isLocked) CrimsonCoral else EmeraldNeon,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (state.isLocked) "Instagram Locked" else "Unlocked",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.isLocked) CrimsonCoral else EmeraldNeon
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom row in Web preview: Rate & Tier info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepVoidNavy),
                    border = androidx.compose.foundation.BorderStroke(1.dp, InnerCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = state.rank.badgeEmoji, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Rank: Level ${state.rank.level} - ${state.rank.title}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(state.rank.colorHex)
                                )
                                Text(
                                    text = "XP: ${String.format(Locale.getDefault(), "%,d", state.lifetimeXp)}",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Text(
                            text = "${state.currentStepsPerMinute} steps / 1m",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }
                }
            }
        }
    }
}

/**
 * Cloud Sync Bridge Card details.
 */
@Composable
fun CloudSyncBridgeCard(
    state: StepLockData,
    syncKey: String,
    onTriggerSync: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cloud_sync_bridge_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateNavy),
        border = androidx.compose.foundation.BorderStroke(1.dp, InnerCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Cloud Bridge Status",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Connecting mobile sensor stream with cloud web companion storage",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sync metrics table
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SyncBridgeStatusRow(
                    label = "Backend Provider",
                    value = "Google Cloud Firestore",
                    statusColor = EmeraldNeon
                )
                SyncBridgeStatusRow(
                    label = "Sync Key",
                    value = syncKey,
                    statusColor = ElectricCyan
                )
                SyncBridgeStatusRow(
                    label = "Device Status",
                    value = if (state.isSimulateMode) "Simulation Engine" else "Hardware Pedometer",
                    statusColor = if (state.isSimulateMode) WarmAmberGold else EmeraldNeon
                )
                SyncBridgeStatusRow(
                    label = "Connected Account",
                    value = state.firebaseUserEmail ?: "Local Anonymous Guest",
                    statusColor = if (state.isFirebaseConnected) EmeraldNeon else TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onTriggerSync,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("trigger_bridge_sync_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = DeepVoidNavy
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    tint = DeepVoidNavy,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Force Cloud Synchronization",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Web Remote Actions Card.
 */
@Composable
fun WebRemoteActionsCard(
    state: StepLockData,
    onAddSteps: (Int) -> Unit,
    onResetDaily: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("web_remote_actions_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateNavy),
        border = androidx.compose.foundation.BorderStroke(1.dp, InnerCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Web Companion Simulator Controls",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Simulate activity from the web interface to test screen time redemption",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SIMULATE INCOMING STEPS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onAddSteps(100) },
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, InnerCardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan)
                ) {
                    Text("+100", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onAddSteps(500) },
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, InnerCardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan)
                ) {
                    Text("+500", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onAddSteps(1000) },
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, InnerCardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldNeon)
                ) {
                    Text("+1,000", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onResetDaily,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonCoral.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonCoral)
            ) {
                Text(
                    text = "Reset Daily Counters (New Day)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun SyncBridgeStatusRow(
    label: String,
    value: String,
    statusColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = statusColor
        )
    }
}

@Composable
fun FeatureRowItem(
    icon: String,
    title: String,
    description: String
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}
