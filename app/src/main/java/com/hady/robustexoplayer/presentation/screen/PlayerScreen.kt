package com.hady.robustexoplayer.presentation.screen

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.hady.robustexoplayer.common.SeekOverlay
import com.hady.robustexoplayer.domain.player.PlayerEvent
import com.hady.robustexoplayer.presentation.component.AnimatedArrow
import com.hady.robustexoplayer.presentation.component.SeekOverlayEffect
import com.hady.robustexoplayer.presentation.component.SettingsBottomSheet
import com.hady.robustexoplayer.presentation.view_model.PlayerUiState
import com.hady.robustexoplayer.presentation.view_model.PlayerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreenRoute(
    playerViewModel: PlayerViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val player = playerViewModel.player
    val playerUiState by playerViewModel.playerUiState.collectAsStateWithLifecycle()
    val isFullscreen by playerViewModel.isFullscreen.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context.findActivity()

    /** ✅ Handle back press in fullscreen mode **/
    BackHandler(enabled = isFullscreen) {
        playerViewModel.toggleFullscreen()
    }

    /** ✅ Handle UI changes when fullscreen state changes **/
    LaunchedEffect(isFullscreen) {
        activity?.window?.let { window ->
            val controller = WindowInsetsControllerCompat(window, window.decorView)

            if (isFullscreen) {
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    PlayerScreen(
        player = player,
        playerUiState = playerUiState,
        playerViewModel = playerViewModel,
        modifier = modifier
    )
}


@OptIn(UnstableApi::class)
@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlayerScreen(
    player: ExoPlayer,
    playerUiState: PlayerUiState,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    val controlsVisible by playerViewModel.controlsVisible.collectAsStateWithLifecycle()


    val isFullscreen by playerViewModel.isFullscreen.collectAsStateWithLifecycle()
    val zoomedScale by playerViewModel.zoomScale.collectAsStateWithLifecycle()

    val selectedSpeed = playerViewModel.selectedPlaybackSpeed.collectAsStateWithLifecycle()
    var isSeekingTemporaryFastForward by remember { mutableStateOf(false) }

    val isSettingsVisible by playerViewModel.isSettingVisible.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()

    // FastForward and Rewind
    val seekOverlayDirection = remember { mutableStateOf<SeekOverlay>(SeekOverlay.IDLE) }
    val currentOverlayState by rememberUpdatedState(seekOverlayDirection.value)

    val ambientBackgroundAlpha by animateFloatAsState(
        targetValue = if (controlsVisible) 0.6f else 1f, // ✅ Dim background when controls are hidden
        animationSpec = tween(durationMillis = 500, easing = LinearEasing)
    )

    // Auto-hide seek overlay using LaunchedEffect
    LaunchedEffect(currentOverlayState) {
        if (currentOverlayState != SeekOverlay.IDLE) {
            delay(600) // Show overlay for 600ms before hiding
            seekOverlayDirection.value = SeekOverlay.IDLE
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isSettingsVisible) {
            SettingsBottomSheet(
                playerViewModel = playerViewModel,
                sheetState = sheetState,
                onDismiss = {
                    playerViewModel.onPlayerEvent(
                        event = PlayerEvent.ToggleSettings(
                            shouldOpen = false
                        )
                    )
                }
            )
        }
    }


    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                //.background(Color.Black.copy(alpha = ambientBackgroundAlpha))
                .fillMaxWidth()
                .then(if (isFullscreen) Modifier.fillMaxSize() else Modifier.aspectRatio(16f / 9f))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            playerViewModel.onPlayerTapped()
                        },
                        onDoubleTap = { offset ->
                            val screenWidth = size.width

                            seekOverlayDirection.value = if (offset.x < screenWidth / 2) {
                                playerViewModel.onPlayerEvent(event = PlayerEvent.Rewind(10))
                                SeekOverlay.BACKWARD
                            } else {
                                playerViewModel.onPlayerEvent(event = PlayerEvent.FastForward(10))
                                SeekOverlay.FORWARD
                            }
                        },
                        onLongPress = {
                            println("Long press started")
                            playerViewModel.onPlayerEvent(
                                event = PlayerEvent.PlaybackSpeed(
                                    speed = 2.0f,
                                    isTemporarySpeed = true
                                )
                            )
                            isSeekingTemporaryFastForward = true
                        }
                    )
                }
                .pointerInput(isSeekingTemporaryFastForward) {
                    if (isSeekingTemporaryFastForward) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Main)
                                val isUp = event.changes.all { it.pressed.not() }

                                if (isUp) {
                                    println("Long press ended")
                                    isSeekingTemporaryFastForward = false
                                    playerViewModel.onPlayerEvent(
                                        event = PlayerEvent.PlaybackSpeed(
                                            speed = selectedSpeed.value,
                                            isTemporarySpeed = false
                                        )
                                    )
                                    break // Exit the loop when the finger is lifted
                                }
                            }
                        }
                    }
                },
        ) {
            /** Video Player **/
            PlayerViewWrapper(
                player = player,
                isFullscreen = isFullscreen,
                zoomedScale = zoomedScale,
                playerViewModel = playerViewModel
            )


            /** ✅ Animated Visibility of Controls **/
            androidx.compose.animation.AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ControllerScreen(
                    playerViewModel = playerViewModel,
                    playerUiState = playerUiState,
                    isFullscreen = isFullscreen,
                    zoomedScale = zoomedScale,
                    seekOverlayDirection = seekOverlayDirection,
                    onTap = {
                       // playerViewModel.onPlayerTapped()
                    }
                )
            }

            /**
             * Show some views over the controller
             */
            /** ✅ Show Seek Overlay when Double Tap happens **/


            if (isSeekingTemporaryFastForward) {
                Box (
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.5f), RoundedCornerShape(25.dp)
                            )
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier,
                            text = "2x", color = Color.White
                        )
                        repeat(2) {
                            AnimatedArrow(direction = SeekOverlay.FORWARD.name)
                        }
                    }
                }
            }

            if (seekOverlayDirection.value != SeekOverlay.IDLE) {
                SeekOverlayEffect(seekOverlayDirection.value)
            }


        }

        /** ✅ Video Info **/
        Text(
            text = "Now Playing: ${playerUiState.currentUrl ?: "No Video Selected"}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )

        /** ✅ Video Selection Buttons **/
        VideoButton("Play HLS") { playerViewModel.onPlayerEvent(PlayerEvent.Play("https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/gear0/prog_index.m3u8")) }
        VideoButton("Play DASH") { playerViewModel.onPlayerEvent(PlayerEvent.Play("https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd")) }
        VideoButton("Play Smooth Streaming") { playerViewModel.onPlayerEvent(PlayerEvent.Play("https://playready.directtaps.net/smoothstreaming/SSWSS720H264/SuperSpeedway_720.ism/Manifest")) }
        VideoButton("Play RTMP") { playerViewModel.onPlayerEvent(PlayerEvent.Play("rtmp://live.example.com/stream")) }
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
internal fun PlayerViewWrapper(
    player: ExoPlayer,
    isFullscreen: Boolean,
    zoomedScale: Float,
    playerViewModel: PlayerViewModel
) {
    Log.d("CHECKED_ZOOMING", "PlayerViewWrapper: ${zoomedScale}")
    var scale by remember { mutableStateOf(zoomedScale) } // ✅ Local scale state for gestures
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var lastScale by remember { mutableStateOf(zoomedScale) }
    var showZoomStatus by remember { mutableStateOf(false) }

    val animatedScale by animateFloatAsState(
        targetValue = if (scale in 0.97f..1.03f) 1f else scale, // ✅ Auto-snap to 1.0x if close
        animationSpec = tween(200)
    )
    val animatedOffsetX by animateFloatAsState(targetValue = offsetX, animationSpec = tween(200))
    val animatedOffsetY by animateFloatAsState(targetValue = offsetY, animationSpec = tween(200))
// ✅ Sync local scale with ViewModel's zoomedScale
    LaunchedEffect(zoomedScale) {
        scale = zoomedScale
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isFullscreen) Modifier.fillMaxSize()
                else Modifier.aspectRatio(16f / 9f)
            )
            .background(Color.Black) // ✅ Prevent White Background
            .clipToBounds()
            .graphicsLayer(
                scaleX = animatedScale,
                scaleY = animatedScale,
                translationX = animatedOffsetX,
                translationY = animatedOffsetY
            )
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->

                    val newScale = (scale * zoom).coerceIn(0.5f, 2.5f) // ✅ Allow Zoom Out to 0.5x

                    if (newScale != lastScale) {
                        lastScale = newScale
                        playerViewModel.updateZoom(newScale) // ✅ Sync with ViewModel
                        showZoomStatus = true
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(1000)
                            showZoomStatus = false
                        }
                    }

                    scale = newScale

                    if (newScale > 1f) {
                        val maxOffsetX = ((newScale - 1) * size.width) / 2
                        val maxOffsetY = ((newScale - 1) * size.height) / 2

                        offsetX = (offsetX + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                        offsetY = (offsetY + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                    } else {
                        offsetX = 0f
                        offsetY = 0f
                    }
                }
            }
    ) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    this.player = player
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                playerView.player = player
            },
            modifier = Modifier.fillMaxSize()
        )

        // ✅ Show Zoom Status
        if (showZoomStatus) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    Text(
                        text = when {
                            animatedScale == 1f -> "Original" // ✅ Auto-snap displays "Original"
                            else -> "%.1fx".format(animatedScale)
                        },
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}