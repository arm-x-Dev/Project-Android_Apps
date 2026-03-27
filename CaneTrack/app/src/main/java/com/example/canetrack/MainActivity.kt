package com.example.canetrack

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

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
        prefs.edit().putString("history_json", Gson().toJson(list)).apply()
        historyLog = list
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
            val isDark = isSystemInDarkTheme()
            val emerald = if (isDark) Color(0xFF10B981) else Color(0xFF059669)

            MaterialTheme(colorScheme = if (isDark) darkColorScheme(background = Color(0xFF020617)) else lightColorScheme(background = Color(0xFFF9FAFB))) {
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
                        Crossfade(targetState = viewModel.screen) { targetScreen ->
                            when (targetScreen) {
                                "date_picker" -> DateEntryScreen(viewModel, emerald)
                                "home" -> HomeView(viewModel, emerald)
                                "setup" -> SetupView(viewModel, emerald)
                                "active" -> ActiveView(viewModel, emerald)
                                "history" -> HistoryView(viewModel, emerald)
                            }
                        }
                    }
                }
            }
        }
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

        OutlinedCard(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
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
    Column(Modifier.padding(24.dp).fillMaxSize()) {
        Text("CREW SETUP", fontSize = 24.sp, fontWeight = FontWeight.Black, color = accent)

        OutlinedTextField(
            value = priceInput,
            onValueChange = { priceInput = it; vm.pricePerKg = it.toDoubleOrNull() ?: 0.0 },
            label = { Text("Price per KG") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { if (it.all { char -> char.isLetter() || char.isWhitespace() }) name = it },
            label = { Text("Name (Letters Only)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = baseW,
            onValueChange = { baseW = it },
            label = { Text("Base Weight") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Button(onClick = { if(name.isNotBlank()){ vm.addWorker(name, baseW.toDoubleOrNull() ?: 0.0); name = ""; baseW = "" } }, Modifier.fillMaxWidth().padding(top = 12.dp), colors = ButtonDefaults.buttonColors(accent)) {
            Text("ADD WORKER")
        }
        LazyColumn(Modifier.weight(1f).padding(top = 16.dp)) {
            items(vm.activeWorkers) { worker -> Text("👤 ${worker.name}", modifier = Modifier.padding(4.dp)) }
        }
        Button(onClick = { vm.screen = "active" }, Modifier.fillMaxWidth().height(64.dp), colors = ButtonDefaults.buttonColors(accent)) {
            Text("GO TO FIELD", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun ActiveView(vm: MainViewModel, accent: Color) {
    val context = LocalContext.current
    val totalNet = vm.activeWorkers.sumOf { w -> w.trips.sumOf { it - w.baseWeight } }
    var showDialog by remember { mutableStateOf(false) }
    var activeId by remember { mutableStateOf("") }
    var reading by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp), colors = CardDefaults.cardColors(accent)) {
            Column(Modifier.padding(32.dp)) {
                Text("${"%.2f".format(totalNet)} kg", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Black)
                Text("₹${"%.0f".format(totalNet * vm.pricePerKg)}", color = Color.White.copy(0.9f), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
        LazyColumn(Modifier.weight(1f).padding(16.dp)) {
            items(vm.activeWorkers) { worker ->
                ElevatedCard(Modifier.padding(vertical = 6.dp).fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(worker.name, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            Text("${worker.trips.size} trips logged", color = Color.Gray)
                        }
                        IconButton(onClick = { activeId = worker.id; showDialog = true }, Modifier.background(accent, RoundedCornerShape(12.dp))) {
                            Icon(Icons.Default.Add, null, tint = Color.White)
                        }
                    }
                }
            }
        }
        Button(onClick = { vm.saveSession(context) }, Modifier.fillMaxWidth().padding(24.dp).height(64.dp), colors = ButtonDefaults.buttonColors(accent)) {
            Text("DONE FOR DAY", fontWeight = FontWeight.Black)
        }
        if (showDialog) {
            AlertDialog(onDismissRequest = { showDialog = false },
                title = { Text("Log Weight") },
                text = {
                    OutlinedTextField(
                        value = reading,
                        onValueChange = { reading = it },
                        label = { Text("Scale Reading") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                },
                confirmButton = { Button(onClick = { vm.addTrip(activeId, reading.toDoubleOrNull() ?: 0.0); reading = ""; showDialog = false }) { Text("SAVE") } }
            )
        }
    }
}

@Composable
fun HomeView(vm: MainViewModel, accent: Color) {
    Column(Modifier.padding(24.dp).fillMaxSize()) {
        Text("DASHBOARD", fontSize = 28.sp, fontWeight = FontWeight.Black, color = accent)
        ElevatedCard(Modifier.fillMaxWidth().padding(vertical = 16.dp), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(24.dp)) {
                Text("TOTAL CANE LIFTED", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("${"%.1f".format(vm.historyLog.sumOf { it.totalWeight })} kg", fontSize = 40.sp, fontWeight = FontWeight.Black)
            }
        }
        Button(onClick = { vm.screen = "setup" }, Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(accent)) {
            Text("NEW CREW MEMBER", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun HistoryView(vm: MainViewModel, accent: Color) {
    val context = LocalContext.current
    var expandedId by remember { mutableStateOf<String?>(null) }
    Column(Modifier.padding(24.dp).fillMaxSize()) {
        Text("HISTORY", fontSize = 28.sp, fontWeight = FontWeight.Black, color = accent)
        LazyColumn(Modifier.padding(top = 16.dp)) {
            items(vm.historyLog) { log ->
                ElevatedCard(Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { expandedId = if (expandedId == log.id) null else log.id }) {
                    Column(Modifier.padding(20.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(log.date, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("${"%.1f".format(log.totalWeight)} kg", fontSize = 22.sp, fontWeight = FontWeight.Black)
                            }
                            IconButton(onClick = { vm.deleteHistoryEntry(context, log.id) }) {
                                Icon(Icons.Default.Delete, null, tint = Color.Red.copy(0.6f))
                            }
                        }
                        AnimatedVisibility(visible = expandedId == log.id) {
                            Column(Modifier.padding(top = 12.dp)) {
                                log.workers.forEach { w ->
                                    val net = w.trips.sumOf { it - w.baseWeight }
                                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                        Text(w.name, color = Color.Gray)
                                        Text("${"%.1f".format(net)} kg", fontWeight = FontWeight.Bold)
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