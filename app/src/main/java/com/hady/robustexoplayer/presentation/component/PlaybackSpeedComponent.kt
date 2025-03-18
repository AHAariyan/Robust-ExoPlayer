package com.hady.robustexoplayer.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hady.robustexoplayer.common.plusIcon
import com.hady.robustexoplayer.ui.theme.RobustExoPlayerTheme

@Composable
internal fun PlaybackSpeedComponent(
    onSpeedChange: (Float) -> Unit
) {
    val allSpeed = listOf(0.25f, 0.5f, 1f, 1.5f, 2f)
    var selectedSpeed by remember { mutableStateOf(1f) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = "${selectedSpeed}x", fontSize = 16.sp, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(25.dp)
                    ),
                painter = painterResource(plusIcon),
                contentDescription = null,
                tint = Color.White
            )
            PlayerThinSlider(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, end = 16.dp)
                    .height(24.dp),
                value = 0.5f,
                onValueChange = { newSpeed ->
                    selectedSpeed = newSpeed
                    onSpeedChange(newSpeed)
                },
                thumbColor = Color.Black.copy(alpha = 0.7f),
                activeTrackColor = Color.Black.copy(alpha = 0.7f),
                inactiveTrackColor = Color.Black.copy(alpha = 0.1f)
            )

            Icon(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(25.dp)
                    ).clickable {
                        val newSpeed = allSpeed.getOrElse(allSpeed.indexOf(selectedSpeed) + 1) { selectedSpeed }
                        selectedSpeed = newSpeed
                        onSpeedChange(newSpeed)
                    },
                painter = painterResource(plusIcon),
                contentDescription = null,
                tint = Color.White
            )
        }

        Row(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            allSpeed.forEach { speed ->
                SpeedTextBackground(
                    title = "${speed}x",
                    isSelected = speed == selectedSpeed,
                    onClick = {
                        selectedSpeed = speed
                        onSpeedChange(speed)
                    }
                )
            }
        }
    }
}

@Composable
internal fun SpeedTextBackground(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column {
        Text(
            modifier = Modifier
                .width(56.dp)
                .clickable{
                    onClick()
                }
                .background(
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(25.dp)
                )
                .padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 2.dp),
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        if (title == "1f")
            Text(text = "Normal", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                onSpeedChange = {}
            )
        }

    }
}