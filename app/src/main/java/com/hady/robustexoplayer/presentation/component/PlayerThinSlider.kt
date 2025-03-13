package com.hady.robustexoplayer.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun PlayerThinSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    thumbColor: Color = MaterialTheme.colorScheme.primary,
    activeTrackColor: Color = MaterialTheme.colorScheme.primary,
    inactiveTrackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f)
) {
    Box(modifier = modifier.height(24.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp) // Adjust the overall height to make room for custom track and thumb
                .align(Alignment.Center)
                .padding(horizontal = 8.dp)
        ) {
            val trackWidth = size.width
            val thumbPosition = trackWidth * value
            val thumbRadius = 8.dp.toPx() // Define the radius for the circular thumb

            // Draw inactive track
            drawLine(
                color = inactiveTrackColor,
                start = Offset(0f, center.y),
                end = Offset(trackWidth, center.y),
                strokeWidth = 3.dp.toPx()
            )

            // Draw active track
            drawLine(
                color = activeTrackColor,
                start = Offset(0f, center.y),
                end = Offset(thumbPosition, center.y),
                strokeWidth = 3.dp.toPx()
            )

            // Draw thumb
            drawCircle(
                color = thumbColor,
                radius = thumbRadius,
                center = Offset(thumbPosition, center.y)
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = null,
            modifier = Modifier.matchParentSize(),
            enabled = enabled,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = Color.Transparent, // Hide default thumb
                activeTrackColor = Color.Transparent, // Hide default active track
                inactiveTrackColor = Color.Transparent // Hide default inactive track
            ),
            interactionSource = remember { MutableInteractionSource() }
        )
    }
}

