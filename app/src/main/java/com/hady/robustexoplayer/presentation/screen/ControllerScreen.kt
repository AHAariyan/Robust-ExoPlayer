package com.hady.robustexoplayer.presentation.screen

import androidx.annotation.OptIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import com.hady.robustexoplayer.common.commentsIcon
import com.hady.robustexoplayer.common.fullScreenIcon
import com.hady.robustexoplayer.common.pauseIcon
import com.hady.robustexoplayer.common.playIcon
import com.hady.robustexoplayer.common.settingIcon
import com.hady.robustexoplayer.common.subtitlesIcon
import com.hady.robustexoplayer.domain.player.PlayerEvent
import com.hady.robustexoplayer.presentation.component.player.PlayerThinSlider
import com.hady.robustexoplayer.presentation.view_model.PlayerUiState
import com.hady.robustexoplayer.presentation.view_model.PlayerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun ControllerScreen(
    playerViewModel: PlayerViewModel,
    playerUiState: PlayerUiState,
    isFullscreen: Boolean,
    zoomedScale: Float
) {

    val playPauseIcon = if (playerUiState.isPlaying) painterResource(pauseIcon) else painterResource(playIcon)
    Box(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.7f))
            .fillMaxSize()
    ) {

        /** ✅ Double-Tap Gesture for Seek (Like YouTube) **/
        DoubleTapSeekGesture(
            onDoubleTapLeft = { playerViewModel.onPlayerEvent(PlayerEvent.Rewind(10)) },
            onDoubleTapRight = { playerViewModel.onPlayerEvent(PlayerEvent.FastForward(10)) }
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

                if (zoomedScale > 1f || zoomedScale < 1f) { // ✅ Always check non-1.0 scale
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(24.dp) // ✅ Fixed size for a perfect circle
                            .clip(CircleShape) // ✅ Ensures circular shape
                            .background(Color.Black.copy(alpha = 0.7f)) // ✅ Background inside the circle
                            .border(1.dp, color = Color.White, CircleShape) // ✅ Proper circular border
                            .clickable {
                                playerViewModel.resetZoom() // ✅ Reset zoom via ViewModel
                            },
                        contentAlignment = Alignment.Center // ✅ Ensures text is centered
                    ) {
                        Text(
                            text = "%.1fx".format(zoomedScale),
                            color = Color.White,
                            fontSize = 8.sp, // ✅ Slightly increased for better visibility
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }


                }


                if (isFullscreen) {
                    IconButton(onClick = {playerViewModel.onPlayerEvent(PlayerEvent.ToggleComments)}) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(commentsIcon),
                            contentDescription = "Comments",
                            tint = Color.White
                        )
                    }
                }
                IconButton(onClick = { playerViewModel.onPlayerEvent(PlayerEvent.ToggleSubtitles) }) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(subtitlesIcon),
                        contentDescription = "Subtitles",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { playerViewModel.onPlayerEvent(PlayerEvent.ToggleSettings(shouldOpen = true)) }) {
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
                modifier = Modifier.fillMaxWidth().padding(bottom = if (isFullscreen) 16.dp else 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${playerUiState.currentPosition} / ${playerUiState.totalDuration}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )

                    IconButton(onClick = { playerViewModel.onPlayerEvent(PlayerEvent.ToggleFullscreen) }) {
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
                    value = playerUiState.progress,
                    onValueChange = {newValue ->
                        playerViewModel.onPlayerEvent(PlayerEvent.SeekTo((newValue * playerViewModel.player.duration).toLong()))
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
        if (playerUiState.isBuffering) {
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
                onClick = { playerViewModel.onPlayerEvent(PlayerEvent.Pause) }
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


//@Composable
//@Preview(showBackground = true, name = "Player Controller Preview")
//internal fun PreviewPlayerController() {
//    RobustExoPlayerTheme {
//        Box (
//            modifier = Modifier
//                .fillMaxSize()
//               // .background(Color.White)
//        ) {
//            Image(modifier = Modifier.fillMaxWidth(), painter = painterResource(id = image), contentDescription = null)
//            ControllerScreen(
//                isLandscape = false, // Portrait mode preview
//                progress = 0.3f, // 30% progress
//                currentPosition = "01:30",
//                totalDuration = "05:00",
//                onPlayPause = {},
//                onSeekForward = {},
//                onSeekBackward = {},
//                onSeekTo = {},
//                onToggleFullscreen = {},
//                onSettingsClick = {},
//                onSubtitlesClick = {},
//                onCommentsClick = {},
//                isPlaying = false,
//                isBuffering = false// This won't be visible in portrait mode
//            )
//        }
//    }
//}
