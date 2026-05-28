package com.example.canetrack

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

import androidx.compose.ui.graphics.Color
import androidx.glance.material3.ColorProviders

private val WidgetDarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = Color(0xFF10B981), // SugarcaneMint
    secondary = Color(0xFF22C55E), // LimeGlow
    background = Color(0xFF070A13), // ObsidianDarkBg
    surface = Color(0xFF0E1422), // SlateSurfaceDark
    onPrimary = Color.White,
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    onSurfaceVariant = Color.Gray
)

private val WidgetLightColorScheme = androidx.compose.material3.lightColorScheme(
    primary = Color(0xFF059669), // SugarcaneMintDark
    secondary = Color(0xFF10B981), // SugarcaneMint
    background = Color(0xFFF6F8F5), // CreamLightBg
    surface = Color(0xFFFFFFFF), // WhiteSurfaceLight
    onPrimary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color.Gray
)

private val ObsidianWidgetColors = ColorProviders(
    light = WidgetLightColorScheme,
    dark = WidgetDarkColorScheme
)

class CaneTrackWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme(colors = ObsidianWidgetColors) {
                WidgetContent(context)
            }
        }
    }

    @Composable
    private fun WidgetContent(context: Context) {
        val prefs = context.getSharedPreferences("CanePrefs", Context.MODE_PRIVATE)
        
        // Retrieve values saved by the app. Float coordinates to avoid parsing latency
        val totalWeight = prefs.getFloat("widget_total_weight", 0f).toDouble()
        val totalAmount = prefs.getFloat("widget_total_amount", 0f).toDouble()
        val dateText = prefs.getString("widget_date", "No active session") ?: "No active session"
        
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CANE TRACKER",
                    style = TextStyle(
                        color = GlanceTheme.colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = dateText,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                )
            }
            
            Spacer(modifier = GlanceModifier.height(10.dp))
            
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "TOTAL HARVESTED",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Text(
                        text = "${"%.1f".format(totalWeight)} kg",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                
                Spacer(modifier = GlanceModifier.width(12.dp))
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "EARNINGS",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Text(
                        text = "₹${"%.0f".format(totalAmount)}",
                        style = TextStyle(
                            color = GlanceTheme.colors.primary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            
            Spacer(modifier = GlanceModifier.height(12.dp))
            
            Button(
                text = "LOG NEW TRIP",
                onClick = actionStartActivity<MainActivity>(),
                modifier = GlanceModifier.fillMaxWidth()
            )
        }
    }
}
