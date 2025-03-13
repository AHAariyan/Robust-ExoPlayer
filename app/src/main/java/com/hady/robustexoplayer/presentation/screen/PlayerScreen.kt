package com.hady.robustexoplayer.presentation.screen

import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.hady.robustexoplayer.presentation.view_model.PlayerUiState
import com.hady.robustexoplayer.presentation.view_model.PlayerViewModel

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreenRoute(
    playerViewModel: PlayerViewModel = hiltViewModel(),
    modifier: Modifier
) {

    val player = playerViewModel.player

    val playerUiState by playerViewModel.playerUiState.collectAsStateWithLifecycle()

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
    modifier: Modifier
) {
    Column (
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        // VideoPlayerUi:
        PlayerViewWrapper(player = player)

        Text(
            text = "Now Playing: ${playerUiState.currentUrl ?: "No Video Selected"}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )

        VideoButton("Play HLS") { playerViewModel.playVideo("https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/gear0/prog_index.m3u8") }
        VideoButton("Play DASH") { playerViewModel.playVideo("https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd") }
        VideoButton("Play Smooth Streaming") { playerViewModel.playVideo("https://playready.directtaps.net/smoothstreaming/SSWSS720H264/SuperSpeedway_720.ism/Manifest") }
        VideoButton("Play RTMP") { playerViewModel.playVideo("rtmp://live.example.com/stream") }

        Row(modifier = Modifier.padding(16.dp)) {
            IconButton(onClick = { playerViewModel.togglePlayPause() }) {
                Icon(
                    imageVector = if (playerUiState.isPlaying) Icons.Default.Refresh else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause"
                )
            }
        }
    }
}

@Composable
fun VideoButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Text(text)
    }
}

@OptIn(UnstableApi::class)
@Composable
internal fun PlayerViewWrapper(player: ExoPlayer) {
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                this.player = player
                useController = true  // Show playback controls
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