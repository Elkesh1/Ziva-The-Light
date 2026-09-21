package com.example.zivaministries.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    shimmerColors: ShimmerColors = rememberShimmerColors(),
    content: @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "shimmer")

    val shimmerProgress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_progress"
    )

    val brush = shimmerColors.brush(
        progress = (shimmerProgress.value % 100) / 100f
    )

    Box(
        modifier = modifier
            .shimmerContent(brush, shape)
    ) {
        content()
    }
}


@Composable
fun Modifier.shimmerContent(
    brush: Brush,
    shape: Shape
): Modifier = this.then(
    Modifier.drawWithCache {
        val shimmerBrush = brush

        onDrawWithContent {
            // Draw the original content effect
            drawContent()

            // Overlay the shimmer effect
            val rect = size.toRect()
            val radius = shape.let {
                // Get corner radius for rounded corners
                if (shape is RoundedCornerShape) {
                    shape.topStart
                } else {
                    Dp.Unspecified
                }
            }

            // Draw the shimmer overlay
            drawRect(
                brush = shimmerBrush,
                size = size,
                style = androidx.compose.ui.graphics.drawscope.Fill
            )
        }
    }
)

/*
* Data class holding the colors used in the shimmer animation.
 */

data class ShimmerColors(
    val baseColor: Color,
    val highlightColor: Color
) {
    @Composable
    fun brush(progress: Float): Brush {
        // Create a gradient that moves across the screen
        val width = 1f
        val startX = -width + (progress * 2 * width)
        val endX = startX + width

        // Gradient from base to highlight and back
        val colors = listOf(
            baseColor,
            highlightColor,
            baseColor
        )

        return Brush.horizontalGradient(
            colors = colors,
            startX = startX * 1000f,
            endX = endX * 1000f
        )
    }
}

/**
 * Remember the shimmer colors with defaults.
 */

@Composable
fun rememberShimmerColors(
    baseColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    highlightColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
): ShimmerColors {
    return remember(baseColor, highlightColor) {
        ShimmerColors(baseColor, highlightColor)
    }
}