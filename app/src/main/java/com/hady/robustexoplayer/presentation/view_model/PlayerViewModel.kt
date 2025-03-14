package com.hady.robustexoplayer.presentation.view_model

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.hady.robustexoplayer.di.ExoPlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@UnstableApi
class PlayerViewModel
@Inject constructor(
    private val exoPlayerManager: ExoPlayerManager
) : ViewModel() {

    private val _playerUiState = MutableStateFlow(PlayerUiState())
    val playerUiState: StateFlow<PlayerUiState> = _playerUiState.asStateFlow()

    val player: ExoPlayer = exoPlayerManager.getPlayer()

    /** Track Fullscreen Mode **/
    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

    init {
        startTrackingProgress()
        observePlayerEvents()
    }

    private fun startTrackingProgress() {
        viewModelScope.launch {
            while (true) {
                _playerUiState.update { state ->
                    state.copy(
                        currentPosition = formatTime(player.currentPosition),
                        totalDuration = formatTime(player.duration),
                        progress = if (player.duration > 0) player.currentPosition / player.duration.toFloat() else 0f
                    )
                }
                delay(1000) // ✅ Update every second
            }
        }
    }

    fun playVideo(
        url: String,
        drmConfig: MediaItem.DrmConfiguration? = null,
        adsConfig: MediaItem.AdsConfiguration? = null
    ) {
        exoPlayerManager.preparePlayer(url, drmConfig, adsConfig)
        _playerUiState.update { it.copy(currentUrl = url, isPlaying = true) }
    }

    fun togglePlayPause() {
        player.playWhenReady = !player.isPlaying
        _playerUiState.update { it.copy(isPlaying = player.isPlaying) }
    }

    /** Toggle Fullscreen **/
    fun toggleFullscreen() {
        _isFullscreen.update { !it }
    }

    /** 🔄 Seek to Position **/
    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    /** ⏩ Seek Forward (10s) **/
    fun seekForward() {
        seekTo(player.currentPosition + 10_000)
    }

    /** ⏪ Seek Backward (10s) **/
    fun seekBackward() {
        seekTo((player.currentPosition - 10_000).coerceAtLeast(0))
    }

    /** 🔄 Track Player Events **/
    private fun observePlayerEvents() {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                _playerUiState.update {
                    it.copy(
                        isBuffering = playbackState == Player.STATE_BUFFERING, // ✅ Update buffering state
                        progress = player.currentPosition.toFloat() / player.duration.toFloat(),
                        currentPosition = formatTime(player.currentPosition),
                        totalDuration = formatTime(player.duration)
                    )
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerUiState.update { it.copy(isPlaying = isPlaying) }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                _playerUiState.update {
                    it.copy(currentPosition = formatTime(newPosition.positionMs))
                }
            }
        })
    }

    /** ⏳ Update UI with Current Progress **/
    fun updateProgress() {
        viewModelScope.launch {
            while (player.isPlaying) {
                _playerUiState.update {
                    it.copy(
                        progress = player.currentPosition.toFloat() / player.duration,
                        currentPosition = formatTime(player.currentPosition),
                        totalDuration = formatTime(player.duration)
                    )
                }
                delay(500) // Update every 500ms
            }
        }
    }

    /** ⏱ Format Time **/
    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    fun releasePlayer() {
        exoPlayerManager.releasePlayer()
        exoPlayerManager.releaseAdsLoaders()
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}

data class PlayerUiState(
    val currentUrl: String? = null,
    val isPlaying: Boolean = false,
    val playbackState: Int = Player.STATE_IDLE,
    val isBuffering: Boolean = false,
    val progress: Float = 0f,
    val currentPosition: String = "00:00",
    val totalDuration: String = "00:00"
)
