package com.hady.robustexoplayer.presentation.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.hady.robustexoplayer.presentation.component.PlayerThinSlider
import com.hady.robustexoplayer.ui.theme.RobustExoPlayerTheme
import kotlinx.coroutines.delay

@Composable
fun ControllerScreen(

) {

    Column(
        modifier = Modifier.fillMaxSize(),//.background(Color.LightGray.copy(0.2f)),
        //contentAlignment = Alignment.BottomCenter
    ) {
        Row (
            modifier = Modifier.fillMaxWidth().height(45.dp).padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Absolute.Right
        ){
            Icon(
                imageVector = Icons.Outlined.Info,
                tint = MaterialTheme.colorScheme.surface,
                contentDescription = null
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Icon(
                imageVector = Icons.Outlined.Settings,
                tint = MaterialTheme.colorScheme.surface,
                contentDescription = null
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            //PlayPauseButton(isPlaying = isPlaying, onTogglePlay = { onTogglePlay() })
        }


        val progress = remember { mutableFloatStateOf(0f) }
        val currentPosition = remember { mutableStateOf("00:00") }
        val totalDuration = remember { mutableStateOf("00:00") }

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Box(
                    modifier = Modifier
                        .background(
                            Color.LightGray.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(25)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${currentPosition.value}/${totalDuration.value}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.surface
                    )
                }


                Box(
                    modifier = Modifier
                        .padding(end = 8.dp, bottom = 4.dp)
                        .size(36.dp)
                        .background(
                            Color.LightGray.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50)
                        ).padding(bottom = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        modifier = Modifier.size(28.dp),
                        onClick = {  }
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White)
                    }
                }

            }

            PlayerThinSlider(
                value = progress.floatValue,
                onValueChange = { newValue ->

                },
                modifier = Modifier
                    .padding(bottom = 5.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.error,
                inactiveTrackColor = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
@Preview
internal fun PreviewPlayerController() {
    RobustExoPlayerTheme {
        Column (
            modifier = Modifier.fillMaxSize()
                //.background(MaterialTheme.colorScheme.surface)
        ){
            ControllerScreen()
        }

    }
}