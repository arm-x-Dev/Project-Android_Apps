package com.example.wifiinspectorpro.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Custom spring-physics bouncy interaction modifier.
 * Animates a soft squish (scale down to 0.94f) on touch down, 
 * and springs back to normal scale on release.
 */
fun Modifier.bounceClick(onClick: () -> Unit) = composed {
    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()
    
    this
        .scale(scale.value)
        .pointerInput(onClick) {
            detectTapGestures(
                onPress = {
                    coroutineScope.launch {
                        scale.animateTo(
                            targetValue = 0.94f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                    try {
                        tryAwaitRelease()
                    } catch (e: Exception) {
                        // Handle cancellation
                    }
                    coroutineScope.launch {
                        scale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    }
                },
                onTap = {
                    onClick()
                }
            )
        }
}

/**
 * Level 2: Standard Cards
 * Dynamically adjusts to System Light and Dark themes.
 * Renders a crisp flat glass-like surface with 24.dp rounded corners
 * and a premium, ultra-thin border responsive to the background.
 */
@Composable
fun NocturneGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.65f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(24.dp),
        content = content
    )
}

/**
 * Kinetic Action Button
 * Engineered to use solid mono-accent brand containers.
 * Integrates bounceClick natively with high-fidelity spring feedback.
 */
@Composable
fun KineticButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .bounceClick(onClick = onClick)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}