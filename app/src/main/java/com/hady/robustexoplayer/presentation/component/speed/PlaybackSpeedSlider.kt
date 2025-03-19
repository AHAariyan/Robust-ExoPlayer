package com.hady.robustexoplayer.presentation.component.speed

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hady.robustexoplayer.ui.theme.RobustExoPlayerTheme

@Composable
internal fun PlaybackSpeedSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0.25f..2.0f, // Speed Range
    steps: Int = 0,
    thumbColor: Color = Color.White,
    activeTrackColor: Color = Color.Black,
    inactiveTrackColor: Color = Color.Gray.copy(alpha = 0.2f)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "Slider Animation"
    )

    Box(modifier = modifier.height(24.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .align(Alignment.Center)
        ) {
            val trackWidth = size.width
            val thumbPosition = trackWidth * animatedProgress
            val thumbRadius = 10.dp.toPx() // Define custom thumb size

            // 🔹 Draw Inactive Track
            drawLine(
                color = inactiveTrackColor,
                start = Offset(0f, center.y),
                end = Offset(trackWidth, center.y),
                strokeWidth = 3.dp.toPx()
            )

            // 🔹 Draw Active Track
            drawLine(
                color = activeTrackColor,
                start = Offset(0f, center.y),
                end = Offset(thumbPosition, center.y),
                strokeWidth = 3.dp.toPx()
            )

            // 🔹 Draw Custom Thumb
            drawCircle(
                color = thumbColor,
                radius = thumbRadius,
                center = Offset(thumbPosition, center.y)
            )
        }

        // 🔹 Invisible Slider for Interaction
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.matchParentSize(),
            enabled = enabled,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = Color.Transparent, // Hide default thumb
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            ),
            interactionSource = remember { MutableInteractionSource() }
        )
    }
}

@Composable
@Preview
internal fun PreviewPlaybackSpeedSlider() {
    RobustExoPlayerTheme {
        PlaybackSpeedSlider(
            value = 1.5f,
            onValueChange = {},
            modifier = Modifier.height(48.dp).padding(16.dp)
        )
    }
}