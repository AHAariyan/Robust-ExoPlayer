package com.hady.robustexoplayer.presentation.view_model

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.hady.robustexoplayer.data.model.TrackInfo
import com.hady.robustexoplayer.di.ExoPlayerManager
import com.hady.robustexoplayer.domain.player.PlayerEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    /** 🔹 Track Zoom Scale **/
    private val _zoomScale = MutableStateFlow(1f)
    val zoomScale: StateFlow<Float> = _zoomScale.asStateFlow()

    private val _availableTracks = MutableStateFlow<Map<Int, List<Tracks.Group>>>(emptyMap())
    val availableTracks: StateFlow<Map<Int, List<Tracks.Group>>> = _availableTracks.asStateFlow()

    private val _trackSelectionParameters =
        MutableStateFlow(player.trackSelectionParameters)
    val trackSelectionParameters: StateFlow<TrackSelectionParameters> =
        _trackSelectionParameters.asStateFlow()

    /** 🔹 Track Disabled State & Overrides */
    private val _disabledTrackTypes = MutableStateFlow(mutableSetOf<Int>())
    val disabledTrackTypes: StateFlow<Set<Int>> = _disabledTrackTypes.asStateFlow()

    val overrides: StateFlow<Map<TrackGroup, TrackSelectionOverride>> =
        trackSelectionParameters.map { it.overrides }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())



    /** One-Time UI Events (e.g., Open Settings, Open Comments, etc.) **/
    private val _uiEvent = MutableSharedFlow<PlayerEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        startTrackingProgress()
        observePlayerEvents()


    }

    /** 🔹 Handle Player Events **/
    fun onPlayerEvent(event: PlayerEvent) {
        when (event) {
            is PlayerEvent.Play -> playVideo(url = event.url)
            is PlayerEvent.Pause -> togglePlayPause()
            is PlayerEvent.SeekTo -> seekTo(event.positionMs)
            is PlayerEvent.ChangeSpeed -> changeSpeed(event.speed)
            is PlayerEvent.FastForward -> seekForward()
            is PlayerEvent.Rewind -> seekBackward()
            is PlayerEvent.Next -> playNext()
            is PlayerEvent.Previous -> playPrevious()
            is PlayerEvent.Restart -> restartVideo()
            is PlayerEvent.Stop -> stopPlayer()
            is PlayerEvent.EnablePiP -> enablePictureInPicture()
            is PlayerEvent.ToggleFullscreen -> toggleFullscreen()
            is PlayerEvent.ToggleMute -> toggleMute()
            is PlayerEvent.ToggleSubtitles -> toggleSubtitles()
            is PlayerEvent.ToggleCaptions -> toggleCaptions()
            is PlayerEvent.ToggleLoop -> toggleLoop()
            is PlayerEvent.ToggleShuffle -> toggleShuffle()
            is PlayerEvent.SetSleepTimer -> setSleepTimer(event.minutes)
            is PlayerEvent.ToggleScreenLock -> toggleScreenLock()
            is PlayerEvent.ToggleSettings -> openSettings()
            is PlayerEvent.ToggleQualitySelection -> openQualitySelection()
            is PlayerEvent.ToggleComments -> openComments()
            is PlayerEvent.ChangeQuality -> changeVideoQuality(trackIndex = event.trackIndex)
        }
    }

    /** ✅ Extract track qualities when initialized */
    fun extractAvailableTracks() {
        val tracks = player.currentTracks
        val extractedTracks = mutableMapOf<Int, List<Tracks.Group>>()

        SUPPORTED_TRACK_TYPES.forEach { trackType ->
            val trackGroups = tracks.groups.filter { it.type == trackType }
            if (trackGroups.isNotEmpty()) {
                extractedTracks[trackType] = trackGroups
            }
        }

        _availableTracks.value = extractedTracks
    }

    /** ✅ Disable/Enable Track Type */
    fun toggleTrackType(trackType: Int) {
        _trackSelectionParameters.update { params ->
            params.buildUpon()
                .setTrackTypeDisabled(trackType, !params.disabledTrackTypes.contains(trackType))
                .build()
        }
        player.trackSelectionParameters = _trackSelectionParameters.value
    }

    /** ✅ Apply Selection (Fully follows Ideal Codebase) */
    private fun applyTrackSelection() {
        val builder = _trackSelectionParameters.value.buildUpon().apply {
            SUPPORTED_TRACK_TYPES.forEach { trackType ->
                setTrackTypeDisabled(trackType, willDisableTrackType(trackType))
                clearOverridesOfType(trackType)
            }
            overrides.value.forEach { (_, override) ->
                addOverride(override)
            }
        }

        _trackSelectionParameters.value = builder.build()
        player.trackSelectionParameters = _trackSelectionParameters.value
    }

    private fun willDisableTrackType(trackType: Int): Boolean {
        return trackSelectionParameters.value.disabledTrackTypes.contains(trackType)
    }

    fun changeVideoQuality(trackIndex: Int) {
        val trackGroups = player.currentTracks.groups.filter { it.type == C.TRACK_TYPE_VIDEO }
        if (trackGroups.isEmpty()) return // No video tracks available

        val selectedGroup = trackGroups.first()
        val override = TrackSelectionOverride(selectedGroup.mediaTrackGroup, trackIndex)

        if (_trackSelectionParameters.value.overrides[selectedGroup.mediaTrackGroup] == override) return // ✅ Avoid unnecessary update

        val newParams = _trackSelectionParameters.value.buildUpon()
            .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
            .addOverride(override)
            .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, false)
            .build()

        _trackSelectionParameters.value = newParams
        player.trackSelectionParameters = newParams // ✅ Apply to ExoPlayer
    }



    /** 🔄 Emit One-Time UI Events **/
    private fun sendUiEvent(event: PlayerEvent) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
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
                delay(500) // ✅ Update every 500ms
            }
        }
    }


//    private fun startTrackingProgress() {
//        viewModelScope.launch {
//            flow {
//                while (true) {
//                    emit(Unit)
//                    delay(500) // Update every 500ms
//                }
//            }.collect {
//                _playerUiState.update { state ->
//                    state.copy(
//                        currentPosition = formatTime(player.currentPosition),
//                        totalDuration = formatTime(player.duration),
//                        progress = if (player.duration > 0) player.currentPosition / player.duration.toFloat() else 0f
//                    )
//                }
//            }
//        }
//    }

    fun playVideo(
        url: String,
        drmConfig: MediaItem.DrmConfiguration? = null,
        adsConfig: MediaItem.AdsConfiguration? = null
    ) {
        exoPlayerManager.preparePlayer(url, drmConfig, adsConfig)
        _playerUiState.update { it.copy(currentUrl = url, isPlaying = true) }

        extractAvailableTracks()
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

    fun updateZoom(scale: Float) {
        _zoomScale.value = scale
    }

    fun resetZoom() {
        _zoomScale.value = 1f
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

    private fun openComments() {
        TODO("Not yet implemented")
    }

    private fun openQualitySelection() {
        TODO("Not yet implemented")
    }

    private fun openSettings() {
        TODO("Not yet implemented")
    }

    private fun toggleScreenLock() {
        TODO("Not yet implemented")
    }

    private fun setSleepTimer(minutes: Int) {
        TODO("Not yet implemented")
    }

    private fun toggleShuffle() {
        TODO("Not yet implemented")
    }

    private fun toggleLoop() {
        TODO("Not yet implemented")
    }

    private fun toggleCaptions() {
        TODO("Not yet implemented")
    }

    private fun toggleSubtitles() {
        TODO("Not yet implemented")
    }

    private fun toggleMute() {
        TODO("Not yet implemented")
    }

    private fun enablePictureInPicture() {
        TODO("Not yet implemented")
    }

    private fun stopPlayer() {
        TODO("Not yet implemented")
    }

    private fun playPrevious() {
        TODO("Not yet implemented")
    }

    private fun playNext() {
        TODO("Not yet implemented")
    }

    private fun changeSpeed(speed: Float) {
        TODO("Not yet implemented")
    }

    /** ✅ Checks if the current player has selectable tracks */
    fun willHaveContent(): Boolean {
        return willHaveContent(player.currentTracks)
    }

    /** ✅ Checks if any supported track types exist in the given track list */
    fun willHaveContent(tracks: Tracks): Boolean {
        return tracks.groups.any { trackGroup ->
            SUPPORTED_TRACK_TYPES.contains(trackGroup.type)
        }
    }

    /** ✅ List of Supported Track Types (Video, Audio, Subtitles, Images) */
    private val SUPPORTED_TRACK_TYPES = listOf(
        C.TRACK_TYPE_VIDEO, C.TRACK_TYPE_AUDIO, C.TRACK_TYPE_TEXT, C.TRACK_TYPE_IMAGE
    )


    /** 🔹 Restart Video **/
    private fun restartVideo() {
        seekTo(0L)
        player.playWhenReady = true
    }
}

data class PlayerUiState(
    val currentUrl: String? = null,
    val isPlaying: Boolean = false,
    val playbackState: Int = Player.STATE_IDLE,
    val isBuffering: Boolean = false,
    val progress: Float = 0f,
    val currentPosition: String = "00:00",
    val totalDuration: String = "00:00",
    //
    val playbackSpeed: Float = 1.0f,
    val availableQualities: List<TrackInfo> = emptyList(),
    val selectedQualityIndex: Int? = null
)
