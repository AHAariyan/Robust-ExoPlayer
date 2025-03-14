package com.hady.robustexoplayer.presentation.screen

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hady.robustexoplayer.common.commentsIcon
import com.hady.robustexoplayer.common.fullScreenIcon
import com.hady.robustexoplayer.common.image
import com.hady.robustexoplayer.common.pauseIcon
import com.hady.robustexoplayer.common.playIcon
import com.hady.robustexoplayer.common.settingIcon
import com.hady.robustexoplayer.common.subtitlesIcon
import com.hady.robustexoplayer.presentation.component.PlayerThinSlider
import com.hady.robustexoplayer.ui.theme.RobustExoPlayerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ControllerScreen(
    isLandscape: Boolean, // Determines portrait or landscape mode
    progress: Float,
    currentPosition: String,
    totalDuration: String,
    onPlayPause: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onSeekTo: (Float) -> Unit,
    onToggleFullscreen: () -> Unit,
    onSettingsClick: () -> Unit,
    onSubtitlesClick: () -> Unit,
    onCommentsClick: () -> Unit, // Only in landscape mode
    isPlaying: Boolean,
    isBuffering: Boolean
) {

    val playPauseIcon = if (isPlaying) painterResource(pauseIcon) else painterResource(playIcon)

    Box(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.7f))
            .fillMaxSize()
    ) {

        /** ✅ Double-Tap Gesture for Seek (Like YouTube) **/
        DoubleTapSeekGesture(
            onDoubleTapLeft = onSeekBackward,
            onDoubleTapRight = onSeekForward
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            /** ✅ Top Row (Settings, Subtitles & Comments) **/
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLandscape) {
                    IconButton(onClick = onCommentsClick) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(commentsIcon),
                            contentDescription = "Comments",
                            tint = Color.White
                        )
                    }
                }
                IconButton(onClick = onSubtitlesClick) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(subtitlesIcon),
                        contentDescription = "Subtitles",
                        tint = Color.White
                    )
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(settingIcon),
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            }

            /** ✅ Bottom Controls (Timer, Fullscreen & SeekBar) **/
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$currentPosition / $totalDuration",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )

                    IconButton(onClick = onToggleFullscreen) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(fullScreenIcon),
                            contentDescription = "Fullscreen",
                            tint = Color.White
                        )
                    }
                }

                // SeekBar
                PlayerThinSlider(
                    value = progress,
                    onValueChange = {newValue ->
                        onSeekTo(newValue)
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.error,
                    inactiveTrackColor = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        /** ✅ Show Buffering Indicator **/
        if (isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }

        /** ✅ Play Button (Centered) **/
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            IconButton(
                onClick = onPlayPause
            ) {
                Icon(
                    painter = playPauseIcon,
                    contentDescription = "Play/Pause",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

/** ✅ Double-Tap Gesture (YouTube-Style Seek Forward & Rewind) **/
@Composable
fun DoubleTapSeekGesture(
    onDoubleTapLeft: () -> Unit,
    onDoubleTapRight: () -> Unit
) {
    val isVisibleLeft = remember { mutableStateOf(false) }
    val isVisibleRight = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Side (Rewind)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            isVisibleLeft.value = true
                            scope.launch {
                                delay(500) // Delay before fading out
                                isVisibleLeft.value = false
                            }
                            onDoubleTapLeft()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility (
                visible = isVisibleLeft.value,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Rewind 10s",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        // Right Side (Fast Forward)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            isVisibleRight.value = true
                            scope.launch {
                                delay(500) // Delay before fading out
                                isVisibleRight.value = false
                            }
                            onDoubleTapRight()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility (
                visible = isVisibleRight.value,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Fast Forward 10s",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}


@Composable
@Preview(showBackground = true, name = "Player Controller Preview")
internal fun PreviewPlayerController() {
    RobustExoPlayerTheme {
        Box (
            modifier = Modifier
                .fillMaxSize()
               // .background(Color.White)
        ) {
            Image(modifier = Modifier.fillMaxWidth(), painter = painterResource(id = image), contentDescription = null)
            ControllerScreen(
                isLandscape = false, // Portrait mode preview
                progress = 0.3f, // 30% progress
                currentPosition = "01:30",
                totalDuration = "05:00",
                onPlayPause = {},
                onSeekForward = {},
                onSeekBackward = {},
                onSeekTo = {},
                onToggleFullscreen = {},
                onSettingsClick = {},
                onSubtitlesClick = {},
                onCommentsClick = {},
                isPlaying = false,
                isBuffering = false// This won't be visible in portrait mode
            )
        }
    }
}
