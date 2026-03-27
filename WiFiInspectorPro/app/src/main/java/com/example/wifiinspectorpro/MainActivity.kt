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
import com.example.wifiinspectorpro.ui.theme.GradientStart
import com.example.wifiinspectorpro.ui.theme.KineticButton
import com.example.wifiinspectorpro.ui.theme.KineticLavender
import com.example.wifiinspectorpro.ui.theme.NocturneGlassCard
import com.example.wifiinspectorpro.ui.theme.ObsidianBg
import com.example.wifiinspectorpro.ui.theme.PulseCoral
import com.example.wifiinspectorpro.ui.theme.ShadowLavender
import com.example.wifiinspectorpro.ui.theme.SurfaceBright
import com.example.wifiinspectorpro.ui.theme.SurfaceLow
import com.example.wifiinspectorpro.ui.theme.SurfaceLowest
import com.example.wifiinspectorpro.ui.theme.WiFiInspectorProTheme
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
                Surface(modifier = Modifier.fillMaxSize(), color = ObsidianBg, contentColor = Color.White) {
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
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(80.dp), tint = PulseCoral)
            Spacer(Modifier.height(24.dp))
            Text("Permissions Required", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Spacer(Modifier.height(32.dp))
            KineticButton(
                text = "Grant Access",
                onClick = { launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    } else {
        AppNavigation()
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val vm: WifiViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {
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
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("PROJECT / NEON NOCTURNE", style = MaterialTheme.typography.labelMedium, color = PulseCoral)
        Spacer(Modifier.height(8.dp))
        Text("WiFi\nInspector", style = MaterialTheme.typography.displayLarge, color = Color.White, lineHeight = 56.sp)
        Text("High-fidelity signal diagnostics for liquid-crystal environments.", color = Color.LightGray.copy(alpha = 0.7f), modifier = Modifier.padding(top = 16.dp, bottom = 48.dp))

        KineticButton(text = "Start New Inspection", onClick = { navController.navigate("select_network") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = { navController.navigate("history") }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("VIEW HISTORY", color = KineticLavender, style = MaterialTheme.typography.labelMedium)
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

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("INFRASTRUCTURE / AVAILABLE", style = MaterialTheme.typography.labelMedium, color = PulseCoral)
        Text("Select Node", style = MaterialTheme.typography.headlineMedium, color = Color.White, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(availableNetworks) { network ->
                NocturneGlassCard(modifier = Modifier.fillMaxWidth().clickable {
                    vm.selectedNetwork = network.SSID
                    vm.activeRooms.clear()
                    navController.navigate("room_dashboard")
                }) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(network.SSID, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("BSSID: ${network.BSSID}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                        Icon(Icons.Default.SignalWifi4Bar, contentDescription = null, tint = KineticLavender)
                    }
                }
            }
        }
    }
}

@Composable
fun RoomDashboardScreen(navController: NavHostController, vm: WifiViewModel) {
    var newRoomName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("LAYOUT / CONFIGURATION", style = MaterialTheme.typography.labelMedium, color = PulseCoral)
        Text("Add Rooms", style = MaterialTheme.typography.headlineMedium, color = Color.White, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newRoomName, onValueChange = { newRoomName = it },
                modifier = Modifier.weight(1f).background(SurfaceLowest, RoundedCornerShape(12.dp)),
                placeholder = { Text("e.g. Master Suite", color = Color.Gray) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.15f), focusedIndicatorColor = KineticLavender
                ),
                textStyle = TextStyle(color = Color.White)
            )
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = { if(newRoomName.isNotBlank()){ vm.activeRooms.add(RoomData(name = newRoomName.trim())); newRoomName = "" } },
                modifier = Modifier.height(56.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = KineticLavender)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
            }
        }

        Spacer(Modifier.height(24.dp))

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(vm.activeRooms) { room ->
                NocturneGlassCard(modifier = Modifier.fillMaxWidth().clickable { vm.selectedRoomForMapping = room; navController.navigate("grid_mapper") }) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(room.name, color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Text("${room.gridSignals.size}/9 POINTS", color = KineticLavender, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        KineticButton(
            text = "Finalize Report",
            onClick = { vm.saveCompleteSession(); navController.navigate("home") { popUpTo("home"){ inclusive = true } } },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridMapperScreen(navController: NavHostController, vm: WifiViewModel) {
    val context = LocalContext.current
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val room = vm.selectedRoomForMapping ?: return
    var expandedMenuIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        while(true) { vm.updateSignal(wifiManager); delay(500) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("DIAGNOSTICS / ${room.name.uppercase()}", style = MaterialTheme.typography.labelMedium, color = PulseCoral)
        Spacer(Modifier.height(8.dp))
        Text("Real-time Signal", style = MaterialTheme.typography.headlineMedium, color = Color.White)
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

                Box(
                    modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(16.dp))
                        .background(if (isScanning) KineticLavender.copy(alpha = 0.2f) else if (signal != null) SurfaceBright else SurfaceLow)
                        .combinedClickable(
                            onClick = { if (vm.scanningIndex == null) vm.startScanningTile(room, index) },
                            onLongClick = { expandedMenuIndex = index }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (tag != TileTag.NONE && tag.icon != null) {
                            Icon(tag.icon, contentDescription = tag.label, tint = Color.White, modifier = Modifier.size(24.dp).padding(bottom = 4.dp))
                        }
                        Text(
                            text = if (isScanning) "${vm.scanTimeLeft}s" else signal?.toString() ?: "TAP",
                            style = MaterialTheme.typography.labelMedium, color = if (isScanning) KineticLavender else Color.White
                        )
                    }

                    DropdownMenu(expanded = expandedMenuIndex == index, onDismissRequest = { expandedMenuIndex = null }, modifier = Modifier.background(SurfaceBright.copy(alpha = 0.95f))) {
                        TileTag.values().forEach { tileTag ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (tileTag.icon != null) Icon(tileTag.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp).padding(end = 8.dp))
                                        Text(tileTag.label, color = Color.White)
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
            Text("BACK TO ROOMS", style = MaterialTheme.typography.labelMedium, color = KineticLavender)
        }
    }
}

@Composable
fun HistoryScreen(navController: NavHostController, vm: WifiViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("ARCHIVE / INSPECTION REPORTS", style = MaterialTheme.typography.labelMedium, color = PulseCoral)
        Text("Saved Sessions", style = MaterialTheme.typography.headlineMedium, color = Color.White, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))

        if (vm.savedSessions.isEmpty()) {
            Text("No reports found. Initiate a scan to generate data.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            items(vm.savedSessions) { session ->
                NocturneGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(session.networkName.uppercase(), style = MaterialTheme.typography.labelMedium, color = KineticLavender)
                    Text("${session.date} • ${session.band}", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                    Spacer(Modifier.height(16.dp))

                    Box(modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(12.dp)).background(SurfaceLowest)) {
                        SignalGraph(history = session.signalHistory, isCompact = true)
                    }

                    Spacer(Modifier.height(20.dp))
                    Text("ROOM HEATMAPS", style = MaterialTheme.typography.labelMedium, color = Color.White)

                    session.rooms.forEach { room ->
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(room.name, color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
                            SmoothHeatmap(grid = room.grid, tags = room.tags)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        KineticButton(text = "Back to Home", onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth())
    }
}

// --- UTILITIES ---
@Composable
fun SignalGraph(history: List<Int>, isCompact: Boolean = false) {
    Canvas(
        modifier = Modifier.fillMaxSize().padding(
            top = if (isCompact) 10.dp else 40.dp, bottom = if (isCompact) 10.dp else 0.dp,
            start = if (isCompact) 0.dp else 10.dp, end = if (isCompact) 0.dp else 10.dp
        )
    ) {
        val width = size.width
        val height = size.height
        val minRssi = -100f
        val maxRssi = -30f
        val range = maxRssi - minRssi

        if (history.size < 2) return@Canvas

        val stepX = width / 59f
        val path = Path()

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
                path.quadraticTo(prevX, prevY, (x + prevX) / 2, (y + prevY) / 2)
            }
        }

        val fillPath = Path().apply { addPath(path); lineTo(width, height); lineTo(0f, height); close() }
        drawPath(path = fillPath, brush = Brush.verticalGradient(colors = listOf(KineticLavender.copy(alpha = 0.3f), Color.Transparent)))
        drawPath(path = path, brush = Brush.horizontalGradient(listOf(GradientStart, PulseCoral)), style = Stroke(width = if (isCompact) 3f else 6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun SmoothHeatmap(grid: Map<Int, Int>, tags: Map<Int, TileTag>) {
    fun getNocturneThermalColor(signal: Int): Color {
        val fraction = ((signal + 100f) / 60f).coerceIn(0f, 1f)
        fun blend(c1: Color, c2: Color, ratio: Float): Color {
            return Color(c1.red + ratio * (c2.red - c1.red), c1.green + ratio * (c2.green - c1.green), c1.blue + ratio * (c2.blue - c1.blue))
        }
        return if (fraction < 0.5f) blend(ObsidianBg, ShadowLavender, fraction * 2f) else blend(ShadowLavender, KineticLavender, (fraction - 0.5f) * 2f)
    }

    val bitmap = remember(grid) {
        val pixels = IntArray(9)
        for (i in 0..8) {
            val signal = grid[i]
            pixels[i] = if (signal != null) getNocturneThermalColor(signal).toArgb() else SurfaceLowest.toArgb()
        }
        Bitmap.createBitmap(pixels, 3, 3, Bitmap.Config.ARGB_8888).asImageBitmap()
    }

    Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawImage(image = bitmap, dstSize = IntSize(size.width.toInt(), size.height.toInt()), filterQuality = FilterQuality.High)
            val stepX = size.width / 3
            val stepY = size.height / 3
            for (i in 1..2) {
                drawLine(Color.White.copy(alpha = 0.1f), Offset(stepX * i, 0f), Offset(stepX * i, size.height), 0.5f)
                drawLine(Color.White.copy(alpha = 0.1f), Offset(0f, stepY * i), Offset(size.width, stepY * i), 0.5f)
            }
        }
        Column(modifier = Modifier.matchParentSize()) {
            for (r in 0..2) {
                Row(modifier = Modifier.weight(1f)) {
                    for (c in 0..2) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                            val tag = tags[r * 3 + c]
                            if (tag != null && tag != TileTag.NONE && tag.icon != null) {
                                Icon(imageVector = tag.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}