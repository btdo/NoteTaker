package com.noteaker.sample.ui.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * A simple gray overlay with a subtle sweeping shimmer.
 * Use as an overlay on top of content while loading (e.g. in a Box).
 */
@Composable
fun ShimmerOverlay(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )
    val overlayColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
    val highlightColor = Color.White.copy(alpha = 0.08f)
    val brush = Brush.linearGradient(
        colors = listOf(
            overlayColor,
            highlightColor,
            overlayColor
        ),
        start = Offset(offset * 2000f - 400f, 0f),
        end = Offset(offset * 2000f + 400f, 0f)
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush)
    )
}
