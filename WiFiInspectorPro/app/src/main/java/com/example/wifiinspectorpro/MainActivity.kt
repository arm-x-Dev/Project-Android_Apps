package com.example.wifiinspectorpro

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wifiinspectorpro.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// --- DATA MODELS ---
enum class TileTag(val label: String, val icon: ImageVector?) {
    NONE("", null),
    ROUTER("Router", Icons.Default.Place),
    DOOR("Door", Icons.Default.ExitToApp),
    WINDOW("Window", Icons.Default.Info),
    WALL("Wall", Icons.Default.Menu)
}

class RoomData(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
) {
    val gridSignals = mutableStateMapOf<Int, Int>()
    val gridTags = mutableStateMapOf<Int, TileTag>()
}

data class RoomSnapshot(
    val name: String,
    val grid: Map<Int, Int>,
    val tags: Map<Int, TileTag>
)

data class InspectionSession(
    val id: String = UUID.randomUUID().toString(),
    val date: String,
    val networkName: String,
    val signalHistory: List<Int>,
    val bssid: String,
    val band: String,
    val rooms: List<RoomSnapshot>
)

// --- VIEWMODEL ---
class WifiViewModel : ViewModel() {
    var selectedNetwork by mutableStateOf("Not Connected")
    val savedSessions = mutableStateListOf<InspectionSession>()
    val activeRooms = mutableStateListOf<RoomData>()
    var selectedRoomForMapping by mutableStateOf<RoomData?>(null)

    var currentBssid by mutableStateOf("--:--:--")
    var currentBand by mutableStateOf("---")
    var currentSignal by mutableIntStateOf(-100)
    var liveHistory by mutableStateOf(listOf<Int>())

    var scanningIndex by mutableStateOf<Int?>(null)
    var scanTimeLeft by mutableIntStateOf(0)

    fun updateSignal(wifiManager: WifiManager) {
        try {
            @Suppress("DEPRECATION")
            val info = wifiManager.connectionInfo
            if (info != null && info.networkId != -1) {
                currentSignal = info.rssi
                currentBssid = info.bssid ?: "Unknown"
                currentBand = if (info.frequency > 5000) "5GHz" else "2.4GHz"
                selectedNetwork = info.ssid.replace("\"", "")

                val newHistory = liveHistory.toMutableList()
                newHistory.add(info.rssi)
                if (newHistory.size > 60) newHistory.removeAt(0)
                liveHistory = newHistory
            }
        } catch (e: Exception) {}
    }

    fun startScanningTile(room: RoomData, index: Int) {
        if (scanningIndex != null) return
        scanningIndex = index
        scanTimeLeft = 5

        viewModelScope.launch {
            val signalSamples = mutableListOf<Int>()
            while (scanTimeLeft > 0) {
                signalSamples.add(currentSignal)
                delay(1000)
                scanTimeLeft -= 1
            }
            room.gridSignals[index] = signalSamples.average().toInt()
            scanningIndex = null
        }
    }

    fun saveCompleteSession() {
        if (liveHistory.isEmpty()) return
        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

        val snapshots = activeRooms.map { room ->
            RoomSnapshot(name = room.name, grid = room.gridSignals.toMap(), tags = room.gridTags.toMap())
        }

        savedSessions.add(0, InspectionSession(
            date = sdf.format(Date()),
            networkName = selectedNetwork,
            signalHistory = liveHistory.toList(),
            bssid = currentBssid,
            band = currentBand,
            rooms = snapshots
        ))
        activeRooms.clear()
    }
}

// --- MAIN ACTIVITY & NAVIGATION ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WiFiInspectorProTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onBackground) {
                    PermissionWrapper()
                }
            }
        }
    }
}

@Composable
fun PermissionWrapper() {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
        hasPermission = map[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    if (!hasPermission) {
        Box(modifier = Modifier.fillMaxSize()) {
            ThemeBackgroundDecorator()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(24.dp), 
                verticalArrangement = Arrangement.Center, 
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(24.dp))
                Text("Permissions Required", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "WiFi Inspector Pro needs fine location permission to scan nearby wireless access nodes.", 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(32.dp))
                KineticButton(
                    text = "Grant Access",
                    onClick = { launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    } else {
        AppNavigation()
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val vm: WifiViewModel = viewModel()

    NavHost(
        navController = navController, 
        startDestination = "home",
        enterTransition = { 
            slideInHorizontally(
                initialOffsetX = { it }, 
                animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy)
            ) + fadeIn(animationSpec = tween(250))
        },
        exitTransition = { 
            slideOutHorizontally(
                targetOffsetX = { -it }, 
                animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy)
            ) + fadeOut(animationSpec = tween(250))
        },
        popEnterTransition = { 
            slideInHorizontally(
                initialOffsetX = { -it }, 
                animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy)
            ) + fadeIn(animationSpec = tween(250))
        },
        popExitTransition = { 
            slideOutHorizontally(
                targetOffsetX = { it }, 
                animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy)
            ) + fadeOut(animationSpec = tween(250))
        }
    ) {
        composable("home") { HomeScreen(navController) }
        composable("select_network") { NetworkSelectScreen(navController, vm) }
        composable("room_dashboard") { RoomDashboardScreen(navController, vm) }
        composable("grid_mapper") { GridMapperScreen(navController, vm) }
        composable("history") { HistoryScreen(navController, vm) }
    }
}

// --- SCREENS ---
@Composable
fun HomeScreen(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize()) {
        ThemeBackgroundDecorator()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(28.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            
            // Central Radar View
            PlayfulRadarScreen()
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "PROJECT / WIFI INSPECTOR PRO", 
                    style = MaterialTheme.typography.labelMedium, 
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "WiFi Inspector", 
                    style = MaterialTheme.typography.displayMedium, 
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "High-fidelity signal diagnostics for local environments.", 
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
            
            Column(modifier = Modifier.fillMaxWidth()) {
                KineticButton(
                    text = "Start New Inspection", 
                    onClick = { navController.navigate("select_network") }, 
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                TextButton(
                    onClick = { navController.navigate("history") }, 
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(
                        text = "VIEW SCAN HISTORY", 
                        color = MaterialTheme.colorScheme.primary, 
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun NetworkSelectScreen(navController: NavHostController, vm: WifiViewModel) {
    val context = LocalContext.current
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    val availableNetworks = remember {
        @Suppress("DEPRECATION", "MissingPermission")
        val results = wifiManager.scanResults
        if (results.isEmpty()) wifiManager.startScan()
        results.filter { !it.SSID.isNullOrEmpty() }.distinctBy { it.SSID }.sortedByDescending { it.level }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ThemeBackgroundDecorator()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            Text("INFRASTRUCTURE / NODES", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text("Select Access Point", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))

            if (availableNetworks.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Scanning local environments...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(availableNetworks) { network ->
                        val level = network.level
                        val strengthLabel = when {
                            level >= -55 -> "Strong"
                            level >= -75 -> "Good"
                            else -> "Fair"
                        }
                        
                        NocturneGlassCard(modifier = Modifier.fillMaxWidth().bounceClick {
                            vm.selectedNetwork = network.SSID
                            vm.activeRooms.clear()
                            navController.navigate("room_dashboard")
                        }) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(network.SSID, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = strengthLabel.uppercase(), 
                                                color = MaterialTheme.colorScheme.primary, 
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 9.sp
                                            )
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Text("BSSID: ${network.BSSID}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Icon(Icons.Default.SignalWifi4Bar, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("BACK TO HOME", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RoomDashboardScreen(navController: NavHostController, vm: WifiViewModel) {
    var newRoomName by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        ThemeBackgroundDecorator()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            Text("LAYOUT / CONFIGURATION", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text("Add Diagnostics Rooms", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newRoomName, onValueChange = { newRoomName = it },
                    modifier = Modifier.weight(1f).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), RoundedCornerShape(16.dp)),
                    placeholder = { Text("e.g. Living Lounge", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), focusedIndicatorColor = MaterialTheme.colorScheme.primary
                    ),
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground)
                )
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = { if(newRoomName.isNotBlank()){ vm.activeRooms.add(RoomData(name = newRoomName.trim())); newRoomName = "" } },
                    modifier = Modifier.size(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            if (vm.activeRooms.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No rooms defined yet. Create one to map signals.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(vm.activeRooms) { room ->
                        NocturneGlassCard(modifier = Modifier.fillMaxWidth().bounceClick { vm.selectedRoomForMapping = room; navController.navigate("grid_mapper") }) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(room.name, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${room.gridSignals.size}/9 POINTS", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            KineticButton(
                text = "Finalize Inspection Report",
                onClick = { vm.saveCompleteSession(); navController.navigate("home") { popUpTo("home"){ inclusive = true } } },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridMapperScreen(navController: NavHostController, vm: WifiViewModel) {
    val context = LocalContext.current
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val room = vm.selectedRoomForMapping ?: return
    var expandedMenuIndex by remember { mutableStateOf<Int?>(null) }
    
    var animateEntry by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateEntry = true
    }

    LaunchedEffect(Unit) {
        while(true) { vm.updateSignal(wifiManager); delay(500) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ThemeBackgroundDecorator()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            Text("DIAGNOSTICS / ${room.name.uppercase()}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Real-time Signal", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(24.dp))

            NocturneGlassCard(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                SignalGraph(history = vm.liveHistory)
            }

            Spacer(Modifier.height(32.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3), modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), userScrollEnabled = false
            ) {
                items(9) { index ->
                    val signal = room.gridSignals[index]
                    val tag = room.gridTags[index] ?: TileTag.NONE
                    val isScanning = vm.scanningIndex == index

                    val entryAlpha by animateFloatAsState(
                        targetValue = if (animateEntry) 1f else 0f,
                        animationSpec = tween(durationMillis = 350, delayMillis = index * 40, easing = LinearEasing),
                        label = "item_entry_alpha"
                    )
                    val entryScale by animateFloatAsState(
                        targetValue = if (animateEntry) 1f else 0.82f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                        label = "item_entry_scale"
                    )

                    val scanScale by animateFloatAsState(
                        targetValue = if (isScanning) 1.08f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                        label = "item_scan_scale"
                    )
                    val borderAlpha by animateFloatAsState(
                        targetValue = if (isScanning) 0.6f else 0.08f,
                        animationSpec = tween(durationMillis = 300),
                        label = "item_border_alpha"
                    )

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .graphicsLayer {
                                alpha = entryAlpha
                                scaleX = entryScale * scanScale
                                scaleY = entryScale * scanScale
                            }
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isScanning) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                else if (signal != null) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isScanning) MaterialTheme.colorScheme.primary.copy(alpha = borderAlpha)
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = borderAlpha),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .combinedClickable(
                                onClick = { if (vm.scanningIndex == null) vm.startScanningTile(room, index) },
                                onLongClick = { expandedMenuIndex = index }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isScanning) {
                            val sonarTransition = rememberInfiniteTransition(label = "sonar")
                            val sonarRadius by sonarTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(durationMillis = 1500, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "sonar_radius"
                            )
                            val sonarAlpha by sonarTransition.animateFloat(
                                initialValue = 0.5f,
                                targetValue = 0f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(durationMillis = 1500, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "sonar_alpha"
                            )
                            
                            val sonarColor = MaterialTheme.colorScheme.primary
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = sonarColor.copy(alpha = sonarAlpha),
                                    radius = size.minDimension * 0.7f * sonarRadius,
                                    center = center
                                )
                            }
                        }

                        if (signal == null && !isScanning) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Add, 
                                    contentDescription = "Scan", 
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f), 
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "SCAN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 9.sp
                                )
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (tag != TileTag.NONE && tag.icon != null) {
                                    Icon(tag.icon, contentDescription = tag.label, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp).padding(bottom = 4.dp))
                                }
                                Text(
                                    text = if (isScanning) "${vm.scanTimeLeft}s" else signal?.toString() ?: "",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isScanning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = expandedMenuIndex == index, 
                            onDismissRequest = { expandedMenuIndex = null }, 
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            TileTag.values().forEach { tileTag ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (tileTag.icon != null) Icon(tileTag.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp).padding(end = 8.dp))
                                            Text(tileTag.label, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    },
                                    onClick = { room.gridTags[index] = tileTag; expandedMenuIndex = null }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("BACK TO ROOMS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HistoryScreen(navController: NavHostController, vm: WifiViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        ThemeBackgroundDecorator()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            Text("ARCHIVE / INSPECTION REPORTS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text("Saved Sessions", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))

            if (vm.savedSessions.isEmpty()) {
                Spacer(Modifier.height(40.dp))
                PlayfulEmptyState(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    items(vm.savedSessions) { session ->
                        NocturneGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text(session.networkName.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text("${session.date} • ${session.band}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 4.dp))
                            Spacer(Modifier.height(16.dp))

                            Box(modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))) {
                                SignalGraph(history = session.signalHistory, isCompact = true)
                            }

                            Spacer(Modifier.height(20.dp))
                            Text("ROOM HEATMAPS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)

                            session.rooms.forEach { room ->
                                Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(room.name, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    SmoothHeatmap(grid = room.grid, tags = room.tags)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            KineticButton(text = "Back to Home", onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth())
        }
    }
}

// --- UTILITIES ---
@Composable
fun SignalGraph(history: List<Int>, isCompact: Boolean = false) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    
    // Pulse Animation for the real-time active dot
    val infiniteTransition = rememberInfiniteTransition(label = "graph_pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 4.dp.value,
        targetValue = 9.dp.value,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "active_dot_radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "active_dot_alpha"
    )

    Canvas(
        modifier = Modifier.fillMaxSize().padding(
            top = if (isCompact) 10.dp else 40.dp, 
            bottom = if (isCompact) 10.dp else 20.dp,
            start = if (isCompact) 0.dp else 16.dp, 
            end = if (isCompact) 0.dp else 16.dp
        )
    ) {
        val width = size.width
        val height = size.height
        val minRssi = -100f
        val maxRssi = -30f
        val range = maxRssi - minRssi

        // 1. Draw Minimalist Grid Guidelines (only in full size)
        if (!isCompact) {
            val levels = listOf(
                -50f to "Excellent",
                -70f to "Good/Fair",
                -90f to "Poor"
            )
            
            levels.forEach { (rssi, label) ->
                val ratio = ((rssi - minRssi) / range).coerceIn(0f, 1f)
                val y = height - (ratio * height)
                
                // Draw thin solid grid line
                drawLine(
                    color = onSurfaceColor.copy(alpha = 0.05f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
                
                // Draw label using native canvas for maximum compatibility
                drawContext.canvas.nativeCanvas.drawText(
                    "$rssi dBm ($label)",
                    8.dp.toPx(),
                    y - 6.dp.toPx(),
                    Paint().apply {
                        color = onSurfaceColor.copy(alpha = 0.25f).toArgb()
                        textSize = 10.dp.toPx()
                        isAntiAlias = true
                    }
                )
            }
        }

        if (history.isEmpty()) return@Canvas

        // Determine step spacing
        val maxPoints = 60
        val stepX = width / (maxPoints - 1).toFloat()
        
        // 2. Build Bezier Path
        val path = Path()
        var lastX = 0f
        var lastY = 0f

        history.forEachIndexed { i, rssi ->
            val x = i * stepX
            val normalizedY = ((rssi.toFloat() - minRssi) / range).coerceIn(0f, 1f)
            val y = height - (normalizedY * height)

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                val prevX = (i - 1) * stepX
                val prevRssi = history[i - 1]
                val prevY = height - (((prevRssi.toFloat() - minRssi) / range).coerceIn(0f, 1f) * height)
                
                // Elegant Horizontal S-Curve (Cubic Bezier)
                path.cubicTo(
                    prevX + (x - prevX) / 2, prevY,
                    prevX + (x - prevX) / 2, y,
                    x, y
                )
            }
            if (i == history.lastIndex) {
                lastX = x
                lastY = y
            }
        }

        // 3. Draw underfill gradient
        if (history.size >= 2) {
            val fillPath = Path().apply {
                addPath(path)
                lineTo(lastX, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.12f), Color.Transparent)
                )
            )
            
            // 4. Draw primary graph line
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(
                    width = if (isCompact) 2.dp.toPx() else 3.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        } else if (history.size == 1) {
            // Draw a single starting dot if only one point exists
            val normalizedY = ((history[0].toFloat() - minRssi) / range).coerceIn(0f, 1f)
            val y = height - (normalizedY * height)
            drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = Offset(0f, y))
            lastX = 0f
            lastY = y
        }

        // 5. Draw Active Pulsing Probe Marker (only on full-size graph)
        if (!isCompact && history.isNotEmpty()) {
            // Draw outer pulsing ripple
            drawCircle(
                color = primaryColor.copy(alpha = pulseAlpha),
                radius = pulseRadius.dp.toPx(),
                center = Offset(lastX, lastY)
            )
            // Draw inner solid dot
            drawCircle(
                color = primaryColor,
                radius = 4.dp.toPx(),
                center = Offset(lastX, lastY)
            )
        }
    }
}

@Composable
fun SmoothHeatmap(grid: Map<Int, Int>, tags: Map<Int, TileTag>) {
    val isDark = isSystemInDarkTheme()
    val startColor = if (isDark) SlateBgDark else Color(0xFFE2E8F0)
    val endColor = if (isDark) IndigoPrimaryDark else Color(0xFF4F46E5)
    val gridBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val emptyCellColor = if (isDark) SurfaceLowestDark else SurfaceLowestLight

    fun getNocturneThermalColor(signal: Int): Color {
        val fraction = ((signal + 100f) / 60f).coerceIn(0f, 1f)
        fun blend(c1: Color, c2: Color, ratio: Float): Color {
            return Color(
                c1.red + ratio * (c2.red - c1.red),
                c1.green + ratio * (c2.green - c1.green),
                c1.blue + ratio * (c2.blue - c1.blue),
                c1.alpha + ratio * (c2.alpha - c1.alpha)
            )
        }
        return blend(startColor, endColor, fraction)
    }

    val bitmap = remember(grid, isDark) {
        val pixels = IntArray(9)
        for (i in 0..8) {
            val signal = grid[i]
            pixels[i] = if (signal != null) getNocturneThermalColor(signal).toArgb() else emptyCellColor.toArgb()
        }
        Bitmap.createBitmap(pixels, 3, 3, Bitmap.Config.ARGB_8888).asImageBitmap()
    }

    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, gridBorderColor, RoundedCornerShape(12.dp))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawImage(image = bitmap, dstSize = IntSize(size.width.toInt(), size.height.toInt()), filterQuality = FilterQuality.High)
            val stepX = size.width / 3
            val stepY = size.height / 3
            for (i in 1..2) {
                drawLine(gridBorderColor, Offset(stepX * i, 0f), Offset(stepX * i, size.height), 0.5f)
                drawLine(gridBorderColor, Offset(0f, stepY * i), Offset(size.width, stepY * i), 0.5f)
            }
        }
        Column(modifier = Modifier.matchParentSize()) {
            for (r in 0..2) {
                Row(modifier = Modifier.weight(1f)) {
                    for (c in 0..2) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                            val tag = tags[r * 3 + c]
                            if (tag != null && tag != TileTag.NONE && tag.icon != null) {
                                Icon(
                                    imageVector = tag.icon, 
                                    contentDescription = null, 
                                    tint = MaterialTheme.colorScheme.onSurface, 
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeBackgroundDecorator() {
    val isDark = isSystemInDarkTheme()
    val blobColor = if (isDark) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "bg_drift")
    val driftOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = size.minDimension * 0.6f
        val angleRad = Math.toRadians(driftOffset.toDouble())
        val offsetX = (Math.cos(angleRad) * 40.dp.toPx()).toFloat()
        val offsetY = (Math.sin(angleRad) * 40.dp.toPx()).toFloat()
        
        drawCircle(
            color = blobColor,
            radius = radius,
            center = Offset(size.width * 0.8f + offsetX, size.height * 0.15f + offsetY)
        )
    }
}

@Composable
fun PlayfulRadarScreen(modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Restart),
        label = "pulse1"
    )
    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2500, delayMillis = 800, easing = LinearEasing), RepeatMode.Restart),
        label = "pulse2"
    )
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "sweep"
    )
    
    Box(
        modifier = modifier
            .size(160.dp)
            .clip(RoundedCornerShape(80.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(80.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = this.center
            val maxRadius = size.minDimension * 0.45f
            
            drawCircle(color = primaryColor.copy(alpha = 0.03f), radius = maxRadius * 0.3f, center = center)
            drawCircle(color = primaryColor.copy(alpha = 0.03f), radius = maxRadius * 0.6f, center = center)
            drawCircle(color = primaryColor.copy(alpha = 0.03f), radius = maxRadius * 0.9f, center = center)
            
            val sweepRad = Math.toRadians(sweepAngle.toDouble())
            val sweepX = (center.x + Math.cos(sweepRad) * maxRadius).toFloat()
            val sweepY = (center.y + Math.sin(sweepRad) * maxRadius).toFloat()
            drawLine(
                color = primaryColor.copy(alpha = 0.4f),
                start = center,
                end = Offset(sweepX, sweepY),
                strokeWidth = 2.dp.toPx()
            )
            
            drawCircle(color = primaryColor.copy(alpha = (1f - pulse1) * 0.15f), radius = maxRadius * pulse1, center = center)
            drawCircle(color = primaryColor.copy(alpha = (1f - pulse2) * 0.15f), radius = maxRadius * pulse2, center = center)
            
            drawCircle(color = primaryColor.copy(alpha = 0.6f), radius = 3.dp.toPx(), center = Offset(center.x - maxRadius * 0.4f, center.y - maxRadius * 0.3f))
            drawCircle(color = primaryColor.copy(alpha = 0.4f), radius = 2.dp.toPx(), center = Offset(center.x + maxRadius * 0.5f, center.y + maxRadius * 0.2f))
        }
    }
}

@Composable
fun PlayfulEmptyState(modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(primaryColor.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SignalWifi4Bar,
                contentDescription = null,
                tint = primaryColor.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Vault Empty",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Initiate an active room inspection scan to populate historical logs.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}