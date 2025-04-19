package com.example.speechmaster.ui.components.practice

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import kotlin.math.pow

@Composable
fun AudioVisualizer(
    normalizedAmplitude: Float, // Expecting value between 0.0f and 1.0f
    modifier: Modifier = Modifier,
    barCount: Int = 30, // Number of bars on each side of the center
    barColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    minBarHeight: Float = 2f, // Minimum height for bars in dp
    maxBarHeightMultiplier: Float = 0.8f // Max height relative to Canvas height
) {
    // Animate the amplitude smoothly
    val animatedAmplitude by animateFloatAsState(
        targetValue = normalizedAmplitude,
        animationSpec = tween(durationMillis = 100), // Adjust duration for smoothness
        label = "Amplitude Animation"
    )

    val totalBars = remember(barCount) { barCount * 2 + 1 }

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barWidth = (canvasWidth / totalBars) * 0.7f // Bar width with some spacing
        val spacing = (canvasWidth / totalBars) * 0.3f
        val maxPossibleBarHeight = canvasHeight * maxBarHeightMultiplier
        val centerIndex = barCount

        for (i in 0..totalBars) {
            val distanceFromCenter = kotlin.math.abs(i - centerIndex)
            val distanceFactor = distanceFromCenter.toFloat() / barCount

            val heightMultiplier = (1f - distanceFactor.pow(1.5f)).coerceAtLeast(0f)

            val targetBarHeight = (minBarHeight + (maxPossibleBarHeight - minBarHeight) * animatedAmplitude * heightMultiplier).coerceAtLeast(minBarHeight)

            val startX = (i * (barWidth + spacing)) + (spacing / 2f)
            val startY = (canvasHeight - targetBarHeight) / 2f
            val endY = startY + targetBarHeight

            drawLine(
                color = barColor,
                start = Offset(x = startX, y = startY),
                end = Offset(x = startX, y = endY),
                strokeWidth = barWidth,
                cap = StrokeCap.Round // Rounded ends for the bars
            )
        }
    }
}
