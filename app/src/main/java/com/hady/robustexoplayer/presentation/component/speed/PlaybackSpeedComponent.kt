package com.hady.robustexoplayer.presentation.component.speed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hady.robustexoplayer.common.plusIcon
import com.hady.robustexoplayer.ui.theme.RobustExoPlayerTheme

import java.util.Locale


@Composable
fun PlaybackSpeedComponent(
    onSpeedChange: (Float) -> Unit,
    selectedSpeed: Float
) {
    val allSpeeds = listOf(0.25f, 1.0f, 1.25f, 1.5f, 2.0f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(start = 8.dp, end = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Display selected speed
        Text(
            text = if (selectedSpeed % 1 == 0f) {
                "${selectedSpeed.toInt()}x"  // Show integer if there's no decimal part
            } else {
                String.format(Locale.US, "%.2fx", selectedSpeed)  // Force US locale for decimal formatting
            },
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Black
        )

        //Spacer(modifier = Modifier.height(12.dp))

        // Minus Icon, Slider, and Plus Icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // Decrease Speed Button
            SpeedIconButton(
                icon = plusIcon,
                onClick = {
                    val newSpeed = (selectedSpeed - 0.05f).coerceIn(0.25f, 2.0f)
                    onSpeedChange(newSpeed)
                }
            )

            // Slider
            PlaybackSpeedSlider(
                value = selectedSpeed,
                onValueChange = {
                    onSpeedChange(it)
                },
                valueRange = 0.25f..2.0f,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, end = 16.dp)
            )

            // Increase Speed Button
            SpeedIconButton(
                icon = plusIcon,
                onClick = {
                    val newSpeed = (selectedSpeed + 0.05f).coerceIn(0.25f, 2.0f)
                    onSpeedChange(newSpeed)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Speed Presets Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            allSpeeds.forEach { speed ->
                SpeedOptionButton(
                    speed = speed,
                    isSelected = selectedSpeed == speed,
                    onSpeedChange = {
                        onSpeedChange(it)
                    }
                )
            }
        }
    }
}

@Composable
fun SpeedIconButton(icon: Int, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .background(Color.Gray.copy(alpha = 0.2f), shape = CircleShape)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun SpeedOptionButton(speed: Float, isSelected: Boolean, onSpeedChange: (Float) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(25.dp))
                .background(Color.Gray.copy(alpha = 0.2f))
                .clickable { onSpeedChange(speed) }
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
        ) {
            Text(
                text = speed.toString(),
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
        }
        if (speed == 1.0f) {
            Text(text = "Normal", fontSize = 12.sp, color = Color.Black)
        }
    }
}

@Composable
@Preview
internal fun PreviewPlaybackSpeedComponent() {
    RobustExoPlayerTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            PlaybackSpeedComponent(
                onSpeedChange = {},
                selectedSpeed = 1f
            )
        }

    }
}