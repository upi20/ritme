package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import android.app.Activity
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.TransitionEntity
import com.example.data.model.SpeedtestState
import com.example.ui.viewmodel.RitmeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// High Density Premium Material 3 Light Color Palette
private val BgLight = Color(0xFFFEF7FF)
private val CardLightBase = Color(0xFFF3EDF7)
private val CardLightAccent = Color(0xFFE8DEF8)
private val BorderLight = Color(0xFFCAC4D0)
private val PurpleM3Primary = Color(0xFF6750A4)
private val PurpleM3Deep = Color(0xFF21005D)
private val GreenOnlineLight = Color(0xFF1D6B2A)
private val GreenOnlineSpark = Color(0xFFB6F1AD)
private val RedOfflineLight = Color(0xFFB3261E)
private val RedOfflineSpark = Color(0xFFF9DEDC)
private val YellowWarnLight = Color(0xFF8A6500)
private val TextPrimaryLight = Color(0xFF1D1B20)
private val TextSecondaryLight = Color(0xFF49454F)
private val CardWhite = Color(0xFFFFFFFF)
private val SpeedtestBg = Color(0xFFF7F2FA)

// Compatibility aliases for layout templates
private val BgDark = BgLight
private val CardDark = CardLightBase
private val BorderDark = BorderLight
private val CyanAccent = PurpleM3Primary
private val GreenOnline = GreenOnlineLight
private val RedOffline = RedOfflineLight
private val YellowWarn = YellowWarnLight
private val TextWhite = TextPrimaryLight
private val TextMuted = TextSecondaryLight

@Composable
fun RitmeDashboardScreen(
    viewModel: RitmeViewModel,
    modifier: Modifier = Modifier
) {
    val target by viewModel.target.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val suksesCount by viewModel.suksesCount.collectAsState()
    val gagalCount by viewModel.gagalCount.collectAsState()
    val latencies by viewModel.latencies.collectAsState()
    val sparklineData by viewModel.sparklineData.collectAsState()
    val statusTerakhir by viewModel.statusTerakhir.collectAsState()
    val waktuPerubahan by viewModel.waktuPerubahan.collectAsState()
    val sessionStart by viewModel.sessionStart.collectAsState()
    val speedtestState by viewModel.speedtestState.collectAsState()
    val transitionLogs by viewModel.transitionLogs.collectAsState()
    val speedtestHistory by viewModel.speedtestHistory.collectAsState()

    var customTargetInput by remember { mutableStateOf(target) }
    val focusManager = LocalFocusManager.current

    // Keep custom input text synced if changed elsewhere
    LaunchedEffect(target) {
        customTargetInput = target
    }

    var currentTime by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            delay(500)
        }
    }

    var isFullscreen by remember { mutableStateOf(false) }
    var isMonitorMode by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity
    
    val toggleFullscreen: () -> Unit = {
        val next = !isFullscreen
        isFullscreen = next
        activity?.let { act ->
            val window = act.window
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (next) {
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    val toggleMonitorMode: () -> Unit = {
        val next = !isMonitorMode
        isMonitorMode = next
        if (next && !isFullscreen) {
            toggleFullscreen()
        } else if (!next && isFullscreen) {
            toggleFullscreen()
        }
    }

    // Calculations
    val totalPing = suksesCount + gagalCount
    val lossPct = if (totalPing > 0) (gagalCount.toDouble() / totalPing * 100) else 0.0
    val uptimePct = 100.0 - lossPct

    val avgLat = if (latencies.isNotEmpty()) latencies.average() else 0.0
    val minLat = if (latencies.isNotEmpty()) latencies.minOrNull() ?: 0.0 else 0.0
    val maxLat = if (latencies.isNotEmpty()) latencies.maxOrNull() ?: 0.0 else 0.0
    val jitter = viewModel.getJitter()

    // Status Styling
    val statusColor = when {
        isPaused -> YellowWarn
        statusTerakhir == null -> YellowWarn
        statusTerakhir == true -> GreenOnline
        else -> RedOffline
    }
    val statusText = when {
        isPaused -> "PAUSED"
        statusTerakhir == null -> "MENUNGGU"
        statusTerakhir == true -> "ONLINE"
        else -> "TERPUTUS"
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isMonitorMode) {
        MonitorModeView(
            statusColor = statusColor,
            statusText = statusText,
            sparklineData = sparklineData,
            isPaused = isPaused,
            onExitMonitorMode = toggleMonitorMode,
            target = target,
            currentTime = currentTime,
            uptimePct = uptimePct,
            lossPct = lossPct,
            avgLat = avgLat,
            viewModel = viewModel
        )
    } else if (isLandscape) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(BgDark)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Column (Live rhythm, statistics, actions)
            LazyColumn(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp, top = 16.dp)
            ) {
                item {
                    HeaderSection(
                        statusColor = statusColor,
                        statusText = statusText,
                        currentTime = currentTime,
                        uptimePct = uptimePct,
                        sessionStart = sessionStart,
                        viewModel = viewModel,
                        isFullscreen = isFullscreen,
                        onToggleFullscreen = toggleFullscreen,
                        onToggleMonitorMode = toggleMonitorMode
                    )
                }

                item {
                    StatisticsGrid(
                        suksesCount = suksesCount,
                        gagalCount = gagalCount,
                        lossPct = lossPct,
                        avgLat = avgLat,
                        minLat = minLat,
                        maxLat = maxLat,
                        jitter = jitter
                    )
                }

                item {
                    SparklineCard(
                        sparklineData = sparklineData,
                        statusTerakhir = statusTerakhir,
                        waktuPerubahan = waktuPerubahan,
                        isPaused = isPaused,
                        viewModel = viewModel
                    )
                }

                item {
                    ControlActionsRow(
                        isPaused = isPaused,
                        onTogglePause = { viewModel.togglePause() },
                        onResetStats = { viewModel.resetStats() },
                        onStartSpeedtest = { viewModel.startSpeedtest() },
                        speedtestState = speedtestState
                    )
                }
            }

            // Right Column (Configuration, analysis, logs, speedtest)
            LazyColumn(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp, top = 16.dp)
            ) {
                item {
                    ConfigurationCard(
                        customTargetInput = customTargetInput,
                        onTargetInputChange = { customTargetInput = it },
                        onApplyTarget = {
                            viewModel.updateTarget(customTargetInput)
                            focusManager.clearFocus()
                        },
                        viewModel = viewModel
                    )
                }

                item {
                    HistogramCard(viewModel = viewModel)
                }

                item {
                    SpeedtestCard(
                        speedtestState = speedtestState,
                        speedtestHistory = speedtestHistory,
                        onStartSpeedtest = { viewModel.startSpeedtest() }
                    )
                }

                item {
                    TransitionLogsCard(
                        transitionLogs = transitionLogs,
                        onClearHistory = { viewModel.resetStats() }
                    )
                }
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(BgDark)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp, top = 16.dp)
        ) {
            // --- HEADER SECTION ---
            item {
                HeaderSection(
                    statusColor = statusColor,
                    statusText = statusText,
                    currentTime = currentTime,
                    uptimePct = uptimePct,
                    sessionStart = sessionStart,
                    viewModel = viewModel,
                    isFullscreen = isFullscreen,
                    onToggleFullscreen = toggleFullscreen,
                    onToggleMonitorMode = toggleMonitorMode
                )
            }

            // --- SPARKLINE HISTORY ---
            item {
                SparklineCard(
                    sparklineData = sparklineData,
                    statusTerakhir = statusTerakhir,
                    waktuPerubahan = waktuPerubahan,
                    isPaused = isPaused,
                    viewModel = viewModel
                )
            }

            // --- CONFIGURATION TARGET ---
            item {
                ConfigurationCard(
                    customTargetInput = customTargetInput,
                    onTargetInputChange = { customTargetInput = it },
                    onApplyTarget = {
                        viewModel.updateTarget(customTargetInput)
                        focusManager.clearFocus()
                    },
                    viewModel = viewModel
                )
            }

            // --- PING STATISTICS ---
            item {
                StatisticsGrid(
                    suksesCount = suksesCount,
                    gagalCount = gagalCount,
                    lossPct = lossPct,
                    avgLat = avgLat,
                    minLat = minLat,
                    maxLat = maxLat,
                    jitter = jitter
                )
            }

            // --- HISTOGRAM ---
            item {
                HistogramCard(viewModel = viewModel)
            }

            // --- SPEEDTEST CONTROL ---
            item {
                SpeedtestCard(
                    speedtestState = speedtestState,
                    speedtestHistory = speedtestHistory,
                    onStartSpeedtest = { viewModel.startSpeedtest() }
                )
            }

            // --- LOGS TRANSISI ---
            item {
                TransitionLogsCard(
                    transitionLogs = transitionLogs,
                    onClearHistory = { viewModel.resetStats() }
                )
            }

            // --- CONTROL BUTTONS BOTTOM ---
            item {
                ControlActionsRow(
                    isPaused = isPaused,
                    onTogglePause = { viewModel.togglePause() },
                    onResetStats = { viewModel.resetStats() },
                    onStartSpeedtest = { viewModel.startSpeedtest() },
                    speedtestState = speedtestState
                )
            }
        }
    }
}

@Composable
fun HeaderSection(
    statusColor: Color,
    statusText: String,
    currentTime: String,
    uptimePct: Double,
    sessionStart: Long,
    viewModel: RitmeViewModel,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    onToggleMonitorMode: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
            .testTag("header_card"),
        colors = CardDefaults.cardColors(containerColor = CardLightAccent),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title and glowing indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlowingStatusIndicator(color = statusColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RITME",
                        color = PurpleM3Deep,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                }

                // Current time text & Fullscreen Button & Monitor Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = currentTime,
                        color = PurpleM3Deep,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PurpleM3Primary)
                            .clickable { onToggleFullscreen() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isFullscreen) "NORMAL" else "LAYAR PENUH",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PurpleM3Deep)
                            .clickable { onToggleMonitorMode() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "MONITOR",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "STATUS JARINGAN", color = TextSecondaryLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "UPTIME", color = TextSecondaryLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f%%", uptimePct),
                        color = if (uptimePct > 95) GreenOnlineLight else if (uptimePct > 80) YellowWarnLight else RedOfflineLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Sejak: " + SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(sessionStart)),
                    color = TextSecondaryLight,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )

                val elapsed = (System.currentTimeMillis() - sessionStart) / 1000
                Text(
                    text = "Sesi: " + viewModel.formatDuration(elapsed),
                    color = TextSecondaryLight,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun GlowingStatusIndicator(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .size(16.dp)
            .background(color.copy(alpha = alpha), CircleShape)
            .border(2.dp, color, CircleShape)
    )
}

@Composable
fun SparklineCard(
    sparklineData: List<Boolean>,
    statusTerakhir: Boolean?,
    waktuPerubahan: Long,
    isPaused: Boolean,
    viewModel: RitmeViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight.copy(alpha = 0.4f), RoundedCornerShape(28.dp))
            .testTag("sparkline_card"),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            val speedtestState by viewModel.speedtestState.collectAsState()
            val speedtestHistory by viewModel.speedtestHistory.collectAsState()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RITME SPARKLINE (100 PING TERAKHIR)",
                    color = TextSecondaryLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                val speedText = when (val state = speedtestState) {
                    is SpeedtestState.Downloading -> String.format(Locale.getDefault(), "↓ %.1f Mbps", state.currentSpeedMbps)
                    is SpeedtestState.Uploading -> String.format(Locale.getDefault(), "↑ %.1f Mbps", state.currentSpeedMbps)
                    is SpeedtestState.Pinging -> "Menguji ping..."
                    else -> {
                        val last = speedtestHistory.firstOrNull()
                        if (last != null) {
                            String.format(Locale.getDefault(), "↓ %.1f Mbps | ↑ %.1f Mbps", last.downloadSpeedMbps, last.uploadSpeedMbps)
                        } else {
                            "Belum ada tes"
                        }
                    }
                }
                Text(
                    text = speedText,
                    color = PurpleM3Primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable Grid of Sparkline Points
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardLightBase.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .border(1.dp, BorderLight.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (sparklineData.isEmpty()) {
                    item {
                        Text(
                            text = "Menunggu data...",
                            color = TextSecondaryLight,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                } else {
                    items(sparklineData) { online ->
                        Box(
                            modifier = Modifier
                                .size(width = 12.dp, height = 24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (online) GreenOnlineSpark else RedOfflineSpark)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Duration in current state
            val durationSec = (System.currentTimeMillis() - waktuPerubahan) / 1000
            val statusDurationStr = viewModel.formatDuration(durationSec)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DURASI STATUS SEKARANG",
                    color = TextSecondaryLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (isPaused) "PAUSED" else if (statusTerakhir == true) "ONLINE $statusDurationStr" else if (statusTerakhir == false) "TERPUTUS $statusDurationStr" else "MENUNGGU",
                    color = if (isPaused) YellowWarnLight else if (statusTerakhir == true) GreenOnlineLight else if (statusTerakhir == false) RedOfflineLight else TextSecondaryLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun ConfigurationCard(
    customTargetInput: String,
    onTargetInputChange: (String) -> Unit,
    onApplyTarget: () -> Unit,
    viewModel: RitmeViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
            .testTag("configuration_card"),
        colors = CardDefaults.cardColors(containerColor = CardLightBase),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "TARGET MONITOR & INTERVAL",
                color = TextSecondaryLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customTargetInput,
                    onValueChange = onTargetInputChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("target_input"),
                    label = { Text("IP / Hostname Target", color = TextSecondaryLight) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryLight,
                        unfocusedTextColor = TextPrimaryLight,
                        focusedBorderColor = PurpleM3Primary,
                        unfocusedBorderColor = BorderLight,
                        cursorColor = PurpleM3Primary,
                        focusedLabelColor = PurpleM3Primary,
                        unfocusedLabelColor = TextSecondaryLight
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onApplyTarget() })
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onApplyTarget,
                    modifier = Modifier
                        .height(56.dp)
                        .testTag("apply_target_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleM3Primary)
                ) {
                    Text(text = "TERAPKAN", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Presets
            Text(text = "PRESET TARGET", color = TextSecondaryLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("8.8.8.8", "1.1.1.1", "speed.cloudflare.com").forEach { preset ->
                    val isPresetSelected = customTargetInput == preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isPresetSelected) PurpleM3Primary.copy(alpha = 0.15f) else Color.White,
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.dp,
                                if (isPresetSelected) PurpleM3Primary else BorderLight.copy(alpha = 0.5f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                viewModel.updateTarget(preset)
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (preset == "speed.cloudflare.com") "Cloudflare" else preset,
                            color = if (isPresetSelected) PurpleM3Primary else TextPrimaryLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interval Controller
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "INTERVAL PING", color = TextSecondaryLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1L, 2L, 5L).forEach { sec ->
                        val intervalMs by viewModel.pingIntervalMs.collectAsState()
                        val isSelected = intervalMs == sec * 1000L
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) PurpleM3Primary else Color.White,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) PurpleM3Primary else BorderLight.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.updateInterval(sec) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${sec}s",
                                color = if (isSelected) Color.White else TextPrimaryLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticsGrid(
    suksesCount: Int,
    gagalCount: Int,
    lossPct: Double,
    avgLat: Double,
    minLat: Double,
    maxLat: Double,
    jitter: Double
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // First Row: OK vs RTO vs LOSS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "PING SUKSES",
                value = suksesCount.toString(),
                color = GreenOnlineLight,
                icon = { Icon(Icons.Default.CheckCircle, "OK", tint = GreenOnlineLight, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "PING GAGAL (RTO)",
                value = gagalCount.toString(),
                color = RedOfflineLight,
                icon = { Icon(Icons.Default.Close, "RTO", tint = RedOfflineLight, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "PACKET LOSS",
                value = String.format(Locale.getDefault(), "%.1f%%", lossPct),
                color = if (lossPct == 0.0) GreenOnlineLight else if (lossPct < 5.0) YellowWarnLight else RedOfflineLight,
                icon = { Icon(Icons.Default.Warning, "LOSS", tint = YellowWarnLight, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.weight(1.2f)
            )
        }

        // Second Row: Latencies
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LatencySubCard(title = "MIN LATENCY", value = String.format(Locale.getDefault(), "%.0fms", minLat), modifier = Modifier.weight(1f))
            LatencySubCard(title = "AVG LATENCY", value = String.format(Locale.getDefault(), "%.0fms", avgLat), modifier = Modifier.weight(1.1f), isPrimary = true)
            LatencySubCard(title = "MAX LATENCY", value = String.format(Locale.getDefault(), "%.0fms", maxLat), modifier = Modifier.weight(1f))
            LatencySubCard(title = "JITTER", value = String.format(Locale.getDefault(), "%.0fms", jitter), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    color: Color,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, BorderLight.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardLightBase),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = title, color = TextSecondaryLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                icon()
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = color,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun LatencySubCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    Card(
        modifier = modifier.border(
            1.dp,
            if (isPrimary) PurpleM3Primary else BorderLight.copy(alpha = 0.3f),
            RoundedCornerShape(16.dp)
        ),
        colors = CardDefaults.cardColors(containerColor = CardLightBase),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = if (isPrimary) PurpleM3Primary else TextSecondaryLight,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = if (isPrimary) PurpleM3Primary else PurpleM3Deep,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun HistogramCard(viewModel: RitmeViewModel) {
    val buckets = viewModel.getLatencyBucketCounts()
    val maxBucket = buckets.maxOrNull()?.coerceAtLeast(1) ?: 1

    val labels = listOf("<10ms", "10-25", "25-50", "50-100", "100-200", "200-500", "500+")
    val colors = listOf(GreenOnlineLight, GreenOnlineLight, YellowWarnLight, YellowWarnLight, YellowWarnLight, RedOfflineLight, RedOfflineLight)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
            .testTag("histogram_card"),
        colors = CardDefaults.cardColors(containerColor = CardLightBase),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "DISTRIBUSI LATENSI JARINGAN",
                color = TextSecondaryLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                buckets.forEachIndexed { index, count ->
                    val fraction = count.toFloat() / maxBucket
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Value above the bar
                        if (count > 0) {
                            Text(
                                text = count.toString(),
                                color = TextPrimaryLight,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((fraction * 70).coerceAtLeast(4f).dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(colors[index])
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Labels Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEach { label ->
                    Text(
                        text = label,
                        color = TextSecondaryLight,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun SpeedtestCard(
    speedtestState: SpeedtestState,
    speedtestHistory: List<com.example.data.database.SpeedtestEntity>,
    onStartSpeedtest: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
            .testTag("speedtest_card"),
        colors = CardDefaults.cardColors(containerColor = SpeedtestBg),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "SPEEDTEST INTERNET CLOUDFLARE",
                color = TextSecondaryLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Current Test execution UI
            when (speedtestState) {
                is SpeedtestState.Idle -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Siap Melakukan Pengujian Kecepatan",
                            color = TextPrimaryLight,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onStartSpeedtest,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleM3Primary),
                            modifier = Modifier.fillMaxWidth().testTag("start_speedtest_button")
                        ) {
                            Text(text = "MULAI SPEEDTEST", color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }
                }

                is SpeedtestState.Pinging -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Mengukur Ping Server...", color = YellowWarnLight, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(color = YellowWarnLight, trackColor = BorderLight.copy(alpha = 0.3f), modifier = Modifier.fillMaxWidth())
                    }
                }

                is SpeedtestState.Downloading -> {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "MENGUNDUH DATA (DOWNLOAD)...", color = GreenOnlineLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f Mbps", speedtestState.currentSpeedMbps),
                                color = GreenOnlineLight,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { speedtestState.progress },
                            color = GreenOnlineLight,
                            trackColor = BorderLight.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                is SpeedtestState.Uploading -> {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "MENGUNGGAH DATA (UPLOAD)...", color = PurpleM3Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f Mbps", speedtestState.currentSpeedMbps),
                                color = PurpleM3Primary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { speedtestState.progress },
                            color = PurpleM3Primary,
                            trackColor = BorderLight.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                is SpeedtestState.Completed -> {
                    Column {
                        Text(text = "PENGUJIAN SELESAI!", color = GreenOnlineLight, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "DOWNLOAD", color = TextSecondaryLight, fontSize = 10.sp)
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1f Mbps", speedtestState.downloadMbps),
                                    color = GreenOnlineLight,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "UPLOAD", color = TextSecondaryLight, fontSize = 10.sp)
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1f Mbps", speedtestState.uploadMbps),
                                    color = PurpleM3Primary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "PING", color = TextSecondaryLight, fontSize = 10.sp)
                                Text(
                                    text = String.format(Locale.getDefault(), "%.0f ms", speedtestState.latencyMs),
                                    color = PurpleM3Deep,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onStartSpeedtest,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleM3Primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "UJI ULANG", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                is SpeedtestState.Failed -> {
                    Column {
                        Text(text = "SPEEDTEST GAGAL", color = RedOfflineLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = speedtestState.error, color = TextPrimaryLight, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onStartSpeedtest,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleM3Primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "COBA LAGI", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // History logs list
            if (speedtestHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "RIWAYAT SPEEDTEST (TERBARU)", color = TextSecondaryLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, BorderLight.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    speedtestHistory.take(3).forEach { test ->
                        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(test.timestamp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = timeStr, color = TextSecondaryLight, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text(
                                text = String.format(Locale.getDefault(), "↓ %.1fM  ↑ %.1fM  P:%.0fms", test.downloadSpeedMbps, test.uploadSpeedMbps, test.pingLatencyMs),
                                color = TextPrimaryLight,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransitionLogsCard(
    transitionLogs: List<TransitionEntity>,
    onClearHistory: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
            .testTag("transition_logs_card"),
        colors = CardDefaults.cardColors(containerColor = CardLightBase),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LOG TRANSISI PUTUS-NYAMBUNG",
                    color = TextSecondaryLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                if (transitionLogs.isNotEmpty()) {
                    Text(
                        text = "CLEAR ALL",
                        color = RedOfflineLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.clickable { onClearHistory() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (transitionLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, BorderLight.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "STABIL - BELUM ADA TRANSISI",
                        color = TextSecondaryLight,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, BorderLight.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    transitionLogs.take(5).forEach { log ->
                        val formattedMsg = log.formattedLog
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(if (log.status) GreenOnlineLight else RedOfflineLight, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                  text = formattedMsg,
                                  color = if (log.status) GreenOnlineLight else RedOfflineLight,
                                  fontSize = 11.sp,
                                  fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ControlActionsRow(
    isPaused: Boolean,
    onTogglePause: () -> Unit,
    onResetStats: () -> Unit,
    onStartSpeedtest: () -> Unit,
    speedtestState: SpeedtestState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Pause Button
        Button(
            onClick = onTogglePause,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPaused) GreenOnlineLight else YellowWarnLight,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("toggle_pause_button")
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = if (isPaused) "Resume" else "Pause",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isPaused) "RESUME" else "PAUSE",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
        }

        // Reset Button
        Button(
            onClick = onResetStats,
            colors = ButtonDefaults.buttonColors(containerColor = CardLightBase),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("reset_stats_button")
        ) {
            Icon(Icons.Default.Refresh, "Reset", tint = TextSecondaryLight)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "RESET", color = TextSecondaryLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

suspend fun delay(timeMillis: Long) {
    kotlinx.coroutines.delay(timeMillis)
}

@Composable
fun MonitorModeView(
    statusColor: Color,
    statusText: String,
    sparklineData: List<Boolean>,
    isPaused: Boolean,
    onExitMonitorMode: () -> Unit,
    target: String,
    currentTime: String,
    uptimePct: Double,
    lossPct: Double,
    avgLat: Double,
    viewModel: RitmeViewModel
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Define custom high-contrast vibrant colors specifically for dark mode
    val isOnline = statusText == "ONLINE" || statusText == "SANGAT BAIK" || statusText == "BAIK" || statusText == "SANGAT STABIL" || statusText == "STABIL"
    val monitorStatusColor = if (isPaused) {
        Color(0xFFFFC107) // Vibrant Amber/Yellow
    } else if (isOnline) {
        Color(0xFF2ECC71) // Vibrant Emerald Green
    } else {
        Color(0xFFE74C3C) // Vibrant Scarlet Red
    }

    val speedtestState by viewModel.speedtestState.collectAsState()
    val speedtestHistory by viewModel.speedtestHistory.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF09080C))
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Info (Target, Time) and Close/Exit Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MONITORING TARGET",
                        color = Color(0xFF8B8A99),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = target,
                        color = Color(0xFFD0BCFF),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Center/Right: Current Time & Close
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = currentTime,
                        color = Color(0xFFEADDFF),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE74C3C))
                            .clickable { onExitMonitorMode() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "KEMBALI",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Middle Section: Massive status display
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(monitorStatusColor.copy(alpha = 0.15f))
                    .border(3.dp, monitorStatusColor, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val lastTest = speedtestHistory.firstOrNull()
                    val currentDlSpeed = when (val state = speedtestState) {
                        is SpeedtestState.Downloading -> state.currentSpeedMbps.toInt()
                        else -> lastTest?.downloadSpeedMbps?.toInt() ?: 0
                    }
                    val currentUlSpeed = when (val state = speedtestState) {
                        is SpeedtestState.Uploading -> state.currentSpeedMbps.toInt()
                        else -> lastTest?.uploadSpeedMbps?.toInt() ?: 0
                    }

                    if (isLandscape) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Left Speed Indicator (Download)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "↓",
                                    color = Color(0xFF2ECC71),
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text(
                                    text = "$currentDlSpeed",
                                    color = Color(0xFFECE6F0),
                                    fontSize = 50.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            // Center Status Text
                            val statusFontSize = when {
                                isPaused -> 72.sp
                                statusText.length > 8 -> 44.sp
                                else -> 72.sp
                            }
                            Text(
                                text = if (isPaused) "PAUSED" else statusText,
                                color = monitorStatusColor,
                                fontSize = statusFontSize,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                letterSpacing = 2.sp,
                                fontFamily = FontFamily.SansSerif,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            // Right Speed Indicator (Upload)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "↑",
                                    color = Color(0xFF3498DB),
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text(
                                    text = "$currentUlSpeed",
                                    color = Color(0xFFECE6F0),
                                    fontSize = 50.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    } else {
                        // Portrait Layout: Stack status and speed indicators beautifully
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            val statusFontSize = when {
                                isPaused -> 52.sp
                                statusText.length > 8 -> 32.sp
                                else -> 52.sp
                            }
                            Text(
                                text = if (isPaused) "PAUSED" else statusText,
                                color = monitorStatusColor,
                                fontSize = statusFontSize,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                letterSpacing = 1.5.sp,
                                fontFamily = FontFamily.SansSerif
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Download speed
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "↓",
                                        color = Color(0xFF2ECC71),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(end = 2.dp)
                                    )
                                    Text(
                                        text = "$currentDlSpeed",
                                        color = Color(0xFFECE6F0),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = " Mbps",
                                        color = Color(0xFF8B8A99),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Elegant divider
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(20.dp)
                                        .background(Color(0xFF3E3D45))
                                )

                                // Upload speed
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "↑",
                                        color = Color(0xFF3498DB),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(end = 2.dp)
                                    )
                                    Text(
                                        text = "$currentUlSpeed",
                                        color = Color(0xFFECE6F0),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = " Mbps",
                                        color = Color(0xFF8B8A99),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "UPTIME: %.1f%%", uptimePct),
                            color = Color(0xFFECE6F0),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "LOSS: %.1f%%", lossPct),
                            color = if (lossPct > 0) Color(0xFFE74C3C) else Color(0xFF2ECC71),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "AVG: %.1f ms", avgLat),
                            color = Color(0xFFD0BCFF),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom Section: Scaled Sparkline & Speed Display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2B2930), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF14121A)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RITME SPARKLINE (100 PING TERAKHIR)",
                            color = Color(0xFF8B8A99),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        // Live / Last Completed Speed test result
                        val speedText = when (val state = speedtestState) {
                            is SpeedtestState.Downloading -> String.format(Locale.getDefault(), "DOWNLOAD: %.1f Mbps...", state.currentSpeedMbps)
                            is SpeedtestState.Uploading -> String.format(Locale.getDefault(), "UPLOAD: %.1f Mbps...", state.currentSpeedMbps)
                            is SpeedtestState.Pinging -> "SPEEDTEST: Pinging..."
                            else -> {
                                val last = speedtestHistory.firstOrNull()
                                if (last != null) {
                                    String.format(Locale.getDefault(), "SPEED: ↓ %.1f Mbps  ↑ %.1f Mbps", last.downloadSpeedMbps, last.uploadSpeedMbps)
                                } else {
                                    "SPEED: Belum ada data"
                                }
                            }
                        }
                        Text(
                            text = speedText,
                            color = Color(0xFFD0BCFF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0D0C11), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF222026), RoundedCornerShape(8.dp))
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (sparklineData.isEmpty()) {
                            item {
                                Text(
                                    text = "Menunggu data...",
                                    color = Color(0xFF8B8A99),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        } else {
                            items(sparklineData) { online ->
                                Box(
                                    modifier = Modifier
                                        .size(width = 12.dp, height = 32.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (online) Color(0xFF2ECC71) else Color(0xFFE74C3C))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
