package com.hady.robustexoplayer.presentation.view_model

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.hady.robustexoplayer.di.ExoPlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
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
    val isPlaying: Boolean = false
)
