package com.example.wifiinspectorpro.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Level 2: Standard Cards
 * Uses the "Glass & Gradient" recipe: 60% opacity surface
 */
@Composable
fun NocturneGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp)) // XL roundedness
            .background(SurfaceLow.copy(alpha = 0.6f))
            .padding(24.dp), // spacing-6
        content = content
    )
}

/**
 * Kinetic Action Button
 * Uses a linear gradient from primary_container to secondary_container
 */
@Composable
fun KineticButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp), // Editorial scale
        shape = RoundedCornerShape(24.dp), // XL roundedness
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(GradientStart, GradientEnd)))
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Label-md: All-caps with 10% letter spacing
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White
            )
        }
    }
}