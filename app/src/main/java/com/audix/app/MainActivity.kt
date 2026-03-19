package com.audix.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.audix.app.data.UserPreferencesRepository
import com.audix.app.service.AudioEngineServiceLocal
import com.audix.app.state.SongState
import com.audix.app.ui.theme.AudixTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userPreferencesRepository = UserPreferencesRepository(applicationContext)
        
        enableEdgeToEdge()
        setContent {
            AudixTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        EqControls(
                            userPreferencesRepository = userPreferencesRepository,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqControls(userPreferencesRepository: UserPreferencesRepository, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var isPermissionGranted by remember {
        mutableStateOf(NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName))
    }

    val powerManager = context.getSystemService(android.content.Context.POWER_SERVICE) as PowerManager
    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(powerManager.isIgnoringBatteryOptimizations(context.packageName))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isPermissionGranted = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
                isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(context.packageName)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Observe active song state from detection service
    val currentSong by SongState.currentSong.collectAsState()
    val isServiceConnected by SongState.isServiceConnected.collectAsState()

    // Preferences
    val eqIntensity by userPreferencesRepository.eqIntensityFlow.collectAsState(initial = 1.0f)
    var eqIntensityPosition by remember(eqIntensity) { mutableStateOf(eqIntensity) }

    val autoEqEnabledPref by userPreferencesRepository.autoEqEnabledFlow.collectAsState(initial = true)
    var isAutoEqEnabled by remember(autoEqEnabledPref) { mutableStateOf(autoEqEnabledPref) }

    val customTuningEnabledPref by userPreferencesRepository.customTuningEnabledFlow.collectAsState(initial = false)
    var isCustomTuningEnabled by remember(customTuningEnabledPref) { mutableStateOf(customTuningEnabledPref) }

    val customBassPref by userPreferencesRepository.customBassFlow.collectAsState(initial = 0.0f)
    var customBassPosition by remember(customBassPref) { mutableStateOf(customBassPref) }

    val customVocalsPref by userPreferencesRepository.customVocalsFlow.collectAsState(initial = 0.0f)
    var customVocalsPosition by remember(customVocalsPref) { mutableStateOf(customVocalsPref) }

    val customTreblePref by userPreferencesRepository.customTrebleFlow.collectAsState(initial = 0.0f)
    var customTreblePosition by remember(customTreblePref) { mutableStateOf(customTreblePref) }

    // Manage Foreground Engine Lifecycle
    LaunchedEffect(Unit) {
        val startIntent = Intent(context, AudioEngineServiceLocal::class.java)
        ContextCompat.startForegroundService(context, startIntent)
    }

    var showSettingsDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isPermissionGranted || !isServiceConnected) {
                // Permission Warning Header
                androidx.compose.material3.Card(
                    colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (!isPermissionGranted) "Notification Access Required" else "Service Disconnected",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        }) {
                            Text("Open Settings")
                        }
                    }
                }
            }

            // 1. Hero Section
            val isPlaying = currentSong != null
            val title = currentSong?.title ?: "No Song Detected"
            val artist = currentSong?.artist ?: "Play music in Spotify or YouTube Music"
            val genre = currentSong?.genre
            
            com.audix.app.ui.components.HeroSection(
                title = title,
                artist = artist,
                genre = genre,
                isPlaying = isPlaying,
                modifier = Modifier.padding(vertical = 24.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 2. Primary Card: Auto EQ
            com.audix.app.ui.components.EqEngineCard(
                isAutoEqEnabled = isAutoEqEnabled,
                onAutoEqChange = { 
                    isAutoEqEnabled = it
                    coroutineScope.launch {
                        userPreferencesRepository.saveAutoEqEnabled(it)
                        if (it) userPreferencesRepository.saveCustomTuningEnabled(false)
                    }
                },
                eqIntensity = eqIntensityPosition,
                onIntensityChange = { eqIntensityPosition = it },
                onIntensityChangeFinished = { coroutineScope.launch { userPreferencesRepository.saveEqIntensity(eqIntensityPosition) } },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Secondary Card: Custom Tuning
            com.audix.app.ui.components.CustomTuningCard(
                isCustomTuningEnabled = isCustomTuningEnabled,
                onCustomTuningChange = {
                    isCustomTuningEnabled = it
                    coroutineScope.launch {
                        userPreferencesRepository.saveCustomTuningEnabled(it)
                        if (it) userPreferencesRepository.saveAutoEqEnabled(false)
                    }
                },
                customBass = customBassPosition,
                onCustomBassChange = { customBassPosition = it },
                onCustomBassChangeFinished = { coroutineScope.launch { userPreferencesRepository.saveCustomBass(customBassPosition) } },
                customVocals = customVocalsPosition,
                onCustomVocalsChange = { customVocalsPosition = it },
                onCustomVocalsChangeFinished = { coroutineScope.launch { userPreferencesRepository.saveCustomVocals(customVocalsPosition) } },
                customTreble = customTreblePosition,
                onCustomTrebleChange = { customTreblePosition = it },
                onCustomTrebleChangeFinished = { coroutineScope.launch { userPreferencesRepository.saveCustomTreble(customTreblePosition) } },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(80.dp)) // padding for bottom icon
        }

        // Settings Icon (Bottom Left)
        IconButton(
            onClick = { showSettingsDialog = true },
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        ) {
            Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Settings & Permissions") },
            text = {
                Column {
                    SettingsRow(
                        title = "Notification Access",
                        description = "Required to detect playing songs",
                        buttonText = if (isPermissionGranted) "Granted" else "Grant",
                        enabled = !isPermissionGranted,
                        onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsRow(
                        title = "Autostart / App Info",
                        description = "Enable autostart for robust background operation",
                        buttonText = "Manage",
                        enabled = true,
                        onClick = {
                            val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
                            context.startActivity(intent)
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsRow(
                        title = "Battery Saver Exemption",
                        description = "Prevent system from killing the background EQ",
                        buttonText = if (isIgnoringBatteryOptimizations) "Ignored" else "Ignore",
                        enabled = !isIgnoringBatteryOptimizations,
                        onClick = {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun SettingsRow(title: String, description: String, buttonText: String, enabled: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
        Button(onClick = onClick, enabled = enabled) {
            Text(buttonText)
        }
    }
}