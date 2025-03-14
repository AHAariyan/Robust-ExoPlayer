package com.hady.robustexoplayer.presentation.screen

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.hady.robustexoplayer.presentation.view_model.PlayerUiState
import com.hady.robustexoplayer.presentation.view_model.PlayerViewModel
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreenRoute(
    playerViewModel: PlayerViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val player = playerViewModel.player
    val playerUiState by playerViewModel.playerUiState.collectAsStateWithLifecycle()
    val isFullscreen by playerViewModel.isFullscreen.collectAsStateWithLifecycle()

    // Handle back press in fullscreen mode
    BackHandler(enabled = isFullscreen) {
        playerViewModel.toggleFullscreen()
    }

    PlayerScreen(
        player = player,
        playerUiState = playerUiState,
        playerViewModel = playerViewModel,
        modifier = modifier
    )
}

@OptIn(UnstableApi::class)
@Composable
internal fun PlayerScreen(
    player: ExoPlayer,
    playerUiState: PlayerUiState,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    var controlsVisible by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val isFullscreen by playerViewModel.isFullscreen.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context.findActivity()

    /** ✅ Show or Hide System UI based on Fullscreen State **/
    LaunchedEffect(isFullscreen) {
        activity?.window?.let { window ->
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            if (isFullscreen) {
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    /** ✅ Auto-hide controls after inactivity **/
    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            delay(3000) // Hide after 3s
            controlsVisible = false
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { controlsVisible = !controlsVisible })
                }
        ) {
            /** ✅ Video Player **/
            PlayerViewWrapper(player = player)

            /** ✅ Animated Visibility of Controls **/
            androidx.compose.animation.AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ControllerScreen(
                    isLandscape = isFullscreen,
                    progress = playerUiState.progress,
                    currentPosition = playerUiState.currentPosition,
                    totalDuration = playerUiState.totalDuration,
                    isPlaying = playerUiState.isPlaying,
                    isBuffering = playerUiState.isBuffering,
                    onPlayPause = {
                        controlsVisible = true
                        playerViewModel.togglePlayPause()
                    },
                    onSeekForward = {
                        controlsVisible = true
                        playerViewModel.seekForward()
                    },
                    onSeekBackward = {
                        controlsVisible = true
                        playerViewModel.seekBackward()
                    },
                    onSeekTo = {
                            progress -> playerViewModel.seekTo((progress * player.duration).toLong())
                    },
                    onToggleFullscreen = {
                        controlsVisible = true
                        playerViewModel.toggleFullscreen()
                    },
                    onSettingsClick = { controlsVisible = true },
                    onSubtitlesClick = { controlsVisible = true },
                    onCommentsClick = { controlsVisible = true }
                )
            }
        }

        /** ✅ Video Info **/
        Text(
            text = "Now Playing: ${playerUiState.currentUrl ?: "No Video Selected"}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )

        /** ✅ Video Selection Buttons **/
        VideoButton("Play HLS") { playerViewModel.playVideo("https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/gear0/prog_index.m3u8") }
        VideoButton("Play DASH") { playerViewModel.playVideo("https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd") }
        VideoButton("Play Smooth Streaming") { playerViewModel.playVideo("https://playready.directtaps.net/smoothstreaming/SSWSS720H264/SuperSpeedway_720.ism/Manifest") }
        VideoButton("Play RTMP") { playerViewModel.playVideo("rtmp://live.example.com/stream") }
    }
}

@Composable
fun VideoButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text)
    }
}

/** ✅ Helper function to find the current activity from context **/
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(UnstableApi::class)
@Composable
internal fun PlayerViewWrapper(player: ExoPlayer) {
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                this.player = player
                useController = false  // Show playback controls
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        },
        update = { playerView ->
            playerView.player = player
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f) // Maintain 16:9 aspect ratio
    )
}