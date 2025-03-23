package com.hady.robustexoplayer.presentation.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hady.robustexoplayer.common.SeekOverlay
import com.hady.robustexoplayer.ui.theme.RobustExoPlayerTheme

@Composable
fun SeekOverlayEffect(direction: SeekOverlay) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp // ✅ Full screen width

    val alphaAnim by animateFloatAsState(
        targetValue = if (direction == SeekOverlay.IDLE) 0f else 0.8f,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing)
    )

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(screenWidth * 0.5f) // ✅ Half of the screen width
            .offset(
                x = if (direction == SeekOverlay.FORWARD) screenWidth * 0.5f else 0.dp // ✅ Align correctly
            )
            .alpha(alphaAnim),
        contentAlignment = if (direction == SeekOverlay.FORWARD) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = Color.Red.copy(alpha = 0.5f), // ✅ Semi-transparent black
                    startAngle = if (direction == SeekOverlay.FORWARD) 90f else 270f, // ✅ Arc starts correctly
                    sweepAngle = 180f, // ✅ Half-moon shape
                    useCenter = true,
                    size = Size(size.width * 2, size.height * 2f), // ✅ Cover full side
                    //topLeft = if (direction == SeekOverlay.FORWARD) Offset(0f, 0f) else Offset(-size.width, 0f)
                    topLeft = if (direction == SeekOverlay.FORWARD)
                        Offset(0f, -size.height * 0.5f)
                    else
                        Offset(-size.width, -size.height * 0.5f) // ✅ Shift upwards for better shape

                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Row {
                    repeat(3) { AnimatedArrow(direction.name) } // ✅ Animated arrows
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (direction == SeekOverlay.FORWARD) "+10 seconds" else "-10 seconds",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
fun AnimatedArrow(direction: String) {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .size(18.dp) // Adjusted size for better compactness
            .alpha(alpha)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val arrowWidth = size.width * 0.7f // Adjusted width for a more compact shape
            val arrowHeight = size.height * 0.8f

            val isForward = direction == SeekOverlay.FORWARD.name

            // Draw the arrowhead (triangle)
            val arrowPath = Path().apply {
                moveTo(if (isForward) size.width else 0f, size.height / 2) // Arrow tip
                lineTo(if (isForward) size.width - arrowWidth else arrowWidth, size.height / 2 - arrowHeight / 2)
                lineTo(if (isForward) size.width - arrowWidth else arrowWidth, size.height / 2 + arrowHeight / 2)
                close()
            }

            drawPath(
                path = arrowPath,
                color = Color.White,
                style = Fill
            )
        }
    }
}



@Composable
@Preview
internal fun PreviewAnimateArrow(){
    RobustExoPlayerTheme {
        AnimatedArrow(direction = SeekOverlay.FORWARD.name)
    }
}