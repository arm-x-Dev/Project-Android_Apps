package com.example.canetrack

import android.content.Context
import android.os.Bundle
import com.example.canetrack.ui.theme.CaneTrackTheme
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.unit.TextUnit
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.glance.appwidget.updateAll
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.style.TextAlign
import kotlin.math.roundToInt

// 1. DATA MODELS
data class Worker(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val baseWeight: Double,
    var trips: List<Double> = emptyList()
)

data class DaySummary(
    val id: String = UUID.randomUUID().toString(),
    val date: String,
    val totalWeight: Double,
    val totalAmount: Double,
    val workers: List<Worker>
)

// 2. STATE CONTROLLER (Fixed saveSession logic)
class MainViewModel : ViewModel() {
    var screen by mutableStateOf("date_picker")
    var selectedDate by mutableStateOf(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()))
    var pricePerKg by mutableStateOf(0.0)
    var activeWorkers by mutableStateOf<List<Worker>>(emptyList())
    var historyLog by mutableStateOf<List<DaySummary>>(emptyList())

    fun addWorker(name: String, weight: Double) {
        activeWorkers = activeWorkers + Worker(name = name, baseWeight = weight)
    }

    fun addTrip(workerId: String, scaleReading: Double) {
        activeWorkers = activeWorkers.map { if (it.id == workerId) it.copy(trips = it.trips + scaleReading) else it }
    }

    // FIXED: Corrected parameter mismatch and added 'workers' parameter
    fun saveSession(context: Context) {
        val totalW = activeWorkers.sumOf { w -> w.trips.sumOf { it - w.baseWeight } }
        val newEntry = DaySummary(
            date = selectedDate,
            totalWeight = totalW,
            totalAmount = totalW * pricePerKg,
            workers = activeWorkers
        )
        val updatedHistory = listOf(newEntry) + historyLog
        persistHistory(context, updatedHistory)
        activeWorkers = emptyList()
        screen = "history"
    }

    fun deleteHistoryEntry(context: Context, id: String) {
        val updatedHistory = historyLog.filter { it.id != id }
        persistHistory(context, updatedHistory)
    }

    private fun persistHistory(context: Context, list: List<DaySummary>) {
        val prefs = context.getSharedPreferences("CanePrefs", Context.MODE_PRIVATE)
        val totalWeightSum = list.sumOf { it.totalWeight }
        val totalAmountSum = list.sumOf { it.totalAmount }
        val latestDate = list.firstOrNull()?.date ?: "No active session"

        prefs.edit()
            .putString("history_json", Gson().toJson(list))
            .putFloat("widget_total_weight", totalWeightSum.toFloat())
            .putFloat("widget_total_amount", totalAmountSum.toFloat())
            .putString("widget_date", latestDate)
            .apply()

        historyLog = list

        // Instantly update the Glance App Widget in the background
        viewModelScope.launch {
            try {
                CaneTrackWidget().updateAll(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadHistory(context: Context) {
        val prefs = context.getSharedPreferences("CanePrefs", Context.MODE_PRIVATE)
        val json = prefs.getString("history_json", null)
        if (json != null) {
            val type = object : TypeToken<List<DaySummary>>() {}.type
            historyLog = Gson().fromJson(json, type)
        }
    }
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadHistory(this)
        setContent {
            CaneTrackTheme {
                val accent = MaterialTheme.colorScheme.primary
                var selectedTab by remember { mutableIntStateOf(0) }

                // BACK GESTURE HANDLING
                BackHandler(enabled = viewModel.screen != "date_picker") {
                    when(viewModel.screen) {
                        "setup" -> viewModel.screen = "date_picker"
                        "home" -> viewModel.screen = "date_picker"
                        "active" -> viewModel.screen = "home"
                        "history" -> viewModel.screen = "home"
                    }
                }

                Scaffold(
                    bottomBar = {
                        AnimatedVisibility(visible = viewModel.screen != "date_picker" && viewModel.screen != "setup") {
                            NavigationBar {
                                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0; viewModel.screen = "home" }, label = { Text("HOME") }, icon = { Icon(Icons.Default.Home, null) })
                                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1; viewModel.screen = "active" }, label = { Text("TRACK") }, icon = { Icon(Icons.Default.PlayArrow, null) })
                                NavigationBarItem(selected = selectedTab == 2, onClick = { selectedTab = 2; viewModel.screen = "history" }, label = { Text("HISTORY") }, icon = { Icon(Icons.Default.List, null) })
                            }
                        }
                    }
                ) { pad ->
                    Box(Modifier.padding(pad)) {
                        val screenIndices = remember {
                            mapOf(
                                "date_picker" to 0,
                                "setup" to 1,
                                "home" to 2,
                                "active" to 3,
                                "history" to 4
                            )
                        }

                        AnimatedContent(
                            targetState = viewModel.screen,
                            transitionSpec = {
                                val initialIdx = screenIndices[initialState] ?: 0
                                val targetIdx = screenIndices[targetState] ?: 0
                                if (targetIdx > initialIdx) {
                                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                                            slideOutHorizontally { width -> -width } + fadeOut()
                                } else {
                                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                            slideOutHorizontally { width -> width } + fadeOut()
                                }
                            },
                            label = "screenTransition"
                        ) { targetScreen ->
                            when (targetScreen) {
                                "date_picker" -> DateEntryScreen(viewModel, accent)
                                "home" -> HomeView(viewModel, accent)
                                "setup" -> SetupView(viewModel, accent)
                                "active" -> ActiveView(viewModel, accent)
                                "history" -> HistoryView(viewModel, accent)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Custom Bouncy Click Modifier using Spring Physics
fun Modifier.bounceClick(onClick: (() -> Unit)? = null) = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounceAnimation"
    )

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
    .pointerInput(onClick) {
        detectTapGestures(
            onPress = {
                isPressed = true
                try {
                    tryAwaitRelease()
                } finally {
                    isPressed = false
                }
            },
            onTap = {
                onClick?.invoke()
            }
        )
    }
}

// 3. UI SCREENS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateEntryScreen(vm: MainViewModel, accent: Color) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState()

    Column(Modifier.fillMaxSize().padding(32.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("CANE TRACKER", fontSize = 42.sp, fontWeight = FontWeight.Black, color = accent)
        Spacer(Modifier.height(48.dp))

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .bounceClick { showDatePicker = true }
        ) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, null, tint = accent)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Session Date", fontSize = 12.sp, color = Color.Gray)
                    Text(vm.selectedDate, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(onClick = { vm.screen = "setup" }, Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(accent)) {
            Text("START SESSION", fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = { vm.screen = "home" }, Modifier.fillMaxWidth().height(60.dp)) {
            Text("OPEN DIARY", fontWeight = FontWeight.Bold)
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        vm.selectedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = dateState) }
    }
}

@Composable
fun SetupView(vm: MainViewModel, accent: Color) {
    var name by remember { mutableStateOf("") }
    var baseW by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    val isDark = isSystemInDarkTheme()
    val borderColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)

    Column(Modifier.padding(24.dp).fillMaxSize()) {
        Text("CREW SETUP", fontSize = 24.sp, fontWeight = FontWeight.Black, color = accent)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = priceInput,
            onValueChange = { priceInput = it; vm.pricePerKg = it.toDoubleOrNull() ?: 0.0 },
            label = { Text("Price per KG") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { if (it.all { char -> char.isLetter() || char.isWhitespace() }) name = it },
            label = { Text("Worker Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = baseW,
            onValueChange = { baseW = it },
            label = { Text("Base Tare Weight (kg)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { if(name.isNotBlank()){ vm.addWorker(name, baseW.toDoubleOrNull() ?: 0.0); name = ""; baseW = "" } }, 
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(accent)
        ) {
            Text("ADD WORKER TO CREW", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))
        Text("ACTIVE CREW", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        
        LazyColumn(
            modifier = Modifier.weight(1f).padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(vm.activeWorkers) { worker -> 
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    ),
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👤", fontSize = 18.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(worker.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Base Tare: ${worker.baseWeight} kg", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { vm.screen = "active" }, 
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(accent)
        ) {
            Text("GO TO FIELD", fontWeight = FontWeight.Black)
        }
    }
}
@Composable
fun SlideToConfirm(
    text: String,
    onConfirm: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    var thumbOffset by remember { mutableStateOf(0f) }
    var isConfirmed by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()
    val trackColor = if (isDark) Color(0xFF0E1422) else Color(0xFFE2E8F0)
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(trackColor, RoundedCornerShape(32.dp))
            .border(1.dp, if (isDark) Color(0xFF1E293B) else Color(0xFFCBD5E1), RoundedCornerShape(32.dp))
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val widthPx = constraints.maxWidth.toFloat() - with(density) { 64.dp.toPx() } // max thumb offset
        
        // Centered instruction text
        Text(
            text = if (isConfirmed) "SAVING SESSION..." else text,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = if (isConfirmed) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )

        // The draggable thumb
        val animatedOffset by animateFloatAsState(
            targetValue = thumbOffset,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "thumbOffsetAnimation"
        )

        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .width(56.dp)
                .fillMaxHeight()
                .background(
                    Brush.linearGradient(listOf(accentColor, MaterialTheme.colorScheme.secondary)),
                    RoundedCornerShape(28.dp)
                )
                .pointerInput(isConfirmed) {
                    if (isConfirmed) return@pointerInput
                    detectDragGestures(
                        onDragEnd = {
                            if (thumbOffset >= widthPx * 0.85f) {
                                thumbOffset = widthPx
                                isConfirmed = true
                                onConfirm()
                            } else {
                                thumbOffset = 0f
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            thumbOffset = (thumbOffset + dragAmount.x).coerceIn(0f, widthPx)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Swipe Arrow",
                tint = Color.White
            )
        }
    }
}

@Composable
fun AnimatedWeightText(
    weight: Double,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    fontSize: TextUnit = 42.sp,
    fontWeight: FontWeight = FontWeight.Black
) {
    val formattedString = "%.2f".format(weight)
    
    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        formattedString.forEachIndexed { index, char ->
            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInVertically { it } + fadeIn() togetherWith
                                slideOutVertically { -it } + fadeOut()
                    } else {
                        slideInVertically { -it } + fadeIn() togetherWith
                                slideOutVertically { it } + fadeOut()
                    }
                },
                label = "digitTicker_$index"
            ) { digit ->
                Text(
                    text = digit.toString(),
                    color = textColor,
                    fontSize = fontSize,
                    fontWeight = fontWeight
                )
            }
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = "kg",
            color = textColor.copy(alpha = 0.8f),
            fontSize = fontSize * 0.45f,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
    }
}

@Composable
fun ActiveView(vm: MainViewModel, accent: Color) {
    val context = LocalContext.current
    val totalNet = vm.activeWorkers.sumOf { w -> w.trips.sumOf { it - w.baseWeight } }
    var showDialog by remember { mutableStateOf(false) }
    var activeId by remember { mutableStateOf("") }
    var reading by remember { mutableStateOf("") }
    val isDark = isSystemInDarkTheme()
    val borderColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)

    Column(Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth(), 
            shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp), 
            colors = CardDefaults.cardColors(accent),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(accent, MaterialTheme.colorScheme.secondary)
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .fillMaxWidth()
            ) {
                Text("RUNNING TOTAL NET", color = Color.White.copy(0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                AnimatedWeightText(weight = totalNet)
                Spacer(Modifier.height(4.dp))
                Text("₹${"%.0f".format(totalNet * vm.pricePerKg)}", color = Color.White.copy(0.9f), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Text(
            text = "CREW WEIGHT LOGS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(start = 24.dp, top = 20.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(vm.activeWorkers) { worker ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick { activeId = worker.id; showDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp), 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(worker.name, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            val net = worker.trips.sumOf { it - worker.baseWeight }
                            Text(
                                text = "${worker.trips.size} trips | Net: ${"%.1f".format(net)} kg", 
                                color = accent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        IconButton(
                            onClick = { activeId = worker.id; showDialog = true }, 
                            modifier = Modifier
                                .background(accent, RoundedCornerShape(12.dp))
                                .width(40.dp)
                                .height(40.dp)
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.White)
                        }
                    }
                }
            }
        }
        
        SlideToConfirm(
            text = "SLIDE TO COMPLETE DAY",
            onConfirm = { vm.saveSession(context) },
            accentColor = accent,
            modifier = Modifier.padding(24.dp)
        )
        
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Log Scale Weight") },
                text = {
                    OutlinedTextField(
                        value = reading,
                        onValueChange = { reading = it },
                        label = { Text("Gross Scale Reading (kg)") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                },
                confirmButton = { 
                    Button(
                        onClick = { vm.addTrip(activeId, reading.toDoubleOrNull() ?: 0.0); reading = ""; showDialog = false },
                        shape = RoundedCornerShape(8.dp)
                    ) { 
                        Text("SAVE") 
                    } 
                }
            )
        }
    }
}

@Composable
fun CaneLiftedGauge(
    totalWeight: Double,
    targetWeight: Double = 5000.0,
    accentColor: Color
) {
    val progress = if (targetWeight > 0) (totalWeight / targetWeight).coerceIn(0.0, 1.0).toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1500),
        label = "gaugeProgress"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(vertical = 16.dp)
            .width(200.dp)
            .height(200.dp)
    ) {
        val neutralGray = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        val glowColor = MaterialTheme.colorScheme.secondary // LimeGlow
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            
            // Draw background track arc
            drawArc(
                color = neutralGray,
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // Draw progress arc with gradient brush
            if (animatedProgress > 0f) {
                drawArc(
                    brush = Brush.horizontalGradient(
                        colors = listOf(accentColor, glowColor)
                    ),
                    startAngle = 140f,
                    sweepAngle = animatedProgress * 260f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
        
        // Inner texts
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${"%.1f".format(totalWeight)} kg",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Target: ${"%.0f".format(targetWeight)} kg",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${"%.0f".format(progress * 100)}% Lifted",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

@Composable
fun HomeView(vm: MainViewModel, accent: Color) {
    val totalCane = vm.historyLog.sumOf { it.totalWeight }
    val isDark = isSystemInDarkTheme()
    val borderColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)

    Column(Modifier.padding(24.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text("DASHBOARD", fontSize = 26.sp, fontWeight = FontWeight.Black, color = accent)
        }
        
        Spacer(Modifier.height(16.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TOTAL CANE LIFTED",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                
                CaneLiftedGauge(totalWeight = totalCane, accentColor = accent)
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Button(
            onClick = { vm.screen = "setup" },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(accent)
        ) {
            Text("NEW CREW MEMBER", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun HistoryView(vm: MainViewModel, accent: Color) {
    val context = LocalContext.current
    var expandedId by remember { mutableStateOf<String?>(null) }
    val isDark = isSystemInDarkTheme()
    val borderColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)

    Column(Modifier.padding(24.dp).fillMaxSize()) {
        Text("HISTORY DIARY", fontSize = 26.sp, fontWeight = FontWeight.Black, color = accent)
        
        LazyColumn(
            modifier = Modifier.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(vm.historyLog) { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick { expandedId = if (expandedId == log.id) null else log.id },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                    ) {
                        Row(
                            Modifier.fillMaxWidth(), 
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(log.date, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(2.dp))
                                Text("${"%.1f".format(log.totalWeight)} kg", fontSize = 22.sp, fontWeight = FontWeight.Black)
                                Text("₹${"%.0f".format(log.totalAmount)} earned", color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = { vm.deleteHistoryEntry(context, log.id) },
                                modifier = Modifier
                                    .background(Color.Red.copy(0.08f), RoundedCornerShape(12.dp))
                                    .width(36.dp)
                                    .height(36.dp)
                            ) {
                                Icon(Icons.Default.Delete, null, tint = Color.Red.copy(0.8f))
                            }
                        }
                        AnimatedVisibility(visible = expandedId == log.id) {
                            Column(
                                Modifier
                                    .padding(top = 16.dp)
                                    .background(
                                        color = if (isDark) Color(0xFF0F1524) else Color(0xFFF1F5F9),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Text("CREW DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Spacer(Modifier.height(8.dp))
                                log.workers.forEach { w ->
                                    val net = w.trips.sumOf { it - w.baseWeight }
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp), 
                                        Arrangement.SpaceBetween
                                    ) {
                                        Text(w.name, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                        Text("${"%.1f".format(net)} kg", fontWeight = FontWeight.Bold, color = accent)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}