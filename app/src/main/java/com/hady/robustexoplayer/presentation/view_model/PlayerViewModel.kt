package com.hady.robustexoplayer.presentation.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.hady.robustexoplayer.data.model.SettingsFeature
import com.hady.robustexoplayer.data.model.TrackInfo
import com.hady.robustexoplayer.data.model.VideoQualityOptions
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

    // Player Instantiating
    val player: ExoPlayer = exoPlayerManager.getPlayer()

    // Holding core information about the player
    private val _playerUiState = MutableStateFlow(PlayerUiState())
    val playerUiState: StateFlow<PlayerUiState> = _playerUiState.asStateFlow()

    // Track Fullscreen Mode
    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

    // Track Zoom Scale
    private val _zoomScale = MutableStateFlow(1f)
    val zoomScale: StateFlow<Float> = _zoomScale.asStateFlow()

    // Available tracks
    private val _availableTracks = MutableStateFlow<Map<Int, List<Tracks.Group>>>(emptyMap())
    val availableTracks: StateFlow<Map<Int, List<Tracks.Group>>> = _availableTracks.asStateFlow()

    private val _trackSelectionParameters = MutableStateFlow(player.trackSelectionParameters)
    private val trackSelectionParameters: StateFlow<TrackSelectionParameters> = _trackSelectionParameters.asStateFlow()

    // Track Disabled State & Overrides
    private val _disabledTrackTypes = MutableStateFlow(mutableSetOf<Int>())
    val disabledTrackTypes: StateFlow<Set<Int>> = _disabledTrackTypes.asStateFlow()

    // To open-up bottom sheet
    private val _isSettingVisible = MutableStateFlow(false)
    val isSettingVisible: StateFlow<Boolean> = _isSettingVisible.asStateFlow()

    // Audio play speed -> Current & Manually selected
    private val _selectedPlaybackSpeed = MutableStateFlow(1.0f)
    val selectedPlaybackSpeed: StateFlow<Float> = _selectedPlaybackSpeed.asStateFlow()

    // Menus available in bottom sheet - Selected by the user
    private val _selectedSettingsMenu = MutableStateFlow<SettingsFeature?>(null)
    val selectedSettingMenu: StateFlow<SettingsFeature?> = _selectedSettingsMenu.asStateFlow()

    // Solely for video selection as it has sub category
    private val _selectedQualityOption = MutableStateFlow<VideoQualityOptions>(VideoQualityOptions.Auto)
    val selectedQualityOption: StateFlow<VideoQualityOptions> = _selectedQualityOption.asStateFlow()

    // Holds manually selectable video resolutions
    private val _availableVideoResolutions = MutableStateFlow<List<Pair<String, TrackSelectionOverride>>>(emptyList())
    val availableVideoResolutions: StateFlow<List<Pair<String, TrackSelectionOverride>>> = _availableVideoResolutions.asStateFlow()

    private val _availableVideoTracks = MutableStateFlow<List<Pair<Int, String>>>(emptyList())
    val availableVideoTracks: StateFlow<List<Pair<Int, String>>> = _availableVideoTracks.asStateFlow()


    // Running video resolution
    private val _currentPlayingResolution = MutableStateFlow<String?>(null)
    val currentPlayingResolution: StateFlow<String?> = _currentPlayingResolution.asStateFlow()

    // 🔹 Holds the track index of the currently playing resolution
    private val _currentPlayingTrackIndex = MutableStateFlow<Int?>(null)
    val currentPlayingTrackIndex: StateFlow<Int?> = _currentPlayingTrackIndex.asStateFlow()

    // Total menus available for video quality selection
    private val _videoQualityFeatureList = MutableStateFlow<List<Pair<VideoQualityOptions, Boolean>>>(emptyList())
    val videoQualityFeatureList: StateFlow<List<Pair<VideoQualityOptions, Boolean>>> = _videoQualityFeatureList.asStateFlow()

    // Track which feature is selected
    private val _selectedVideoQualityFeatures = MutableStateFlow<VideoQualityOptions>(VideoQualityOptions.Auto)
    val selectedVideoQualityFeatures: StateFlow<VideoQualityOptions> = _selectedVideoQualityFeatures.asStateFlow()

    // Track override by user selection
    private val overrides: StateFlow<Map<TrackGroup, TrackSelectionOverride>> =
        trackSelectionParameters.map { it.overrides }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())


    /** One-Time UI Events (e.g., Open Settings, Open Comments, etc.) **/
    private val _uiEvent = MutableSharedFlow<PlayerEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        // Start tracking and update the progress of the tracks
        startTrackingProgress()

        // Default Events of the player - ExoPlayer
        observePlayerEvents()

        // Available menus of Video quality selection feature - Bottom Sheet (Always static)
        updateVideoQualityFeatures(selectedVideoQualityFeature = selectedVideoQualityFeatures.value)
    }

    /** Handle Player Events **/
    fun onPlayerEvent(event: PlayerEvent) {
        when (event) {
            is PlayerEvent.Play -> playVideo(url = event.url)
            is PlayerEvent.Pause -> togglePlayPause()
            is PlayerEvent.SeekTo -> seekTo(event.positionMs)
            is PlayerEvent.PlaybackSpeed -> updatePlaybackSpeed(event.speed)
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
            is PlayerEvent.ToggleSettings -> openSettings(shouldOpen = event.shouldOpen)
            is PlayerEvent.ToggleQualitySelection -> openQualitySelection()
            is PlayerEvent.ToggleComments -> openComments()
            is PlayerEvent.PlaybackQuality -> updateVideoQuality(videoQualityOptions = event.quality)
        }
    }

    /**
     * Responsible for selecting menus when setting's bottom sheet popped-up
     */
    fun settingsMenuSelection(feature: SettingsFeature?) {
        _selectedSettingsMenu.value = feature
    }

    /**
     * ////////////////////////////////////////////////////////// AUDIO PART /////////////////////////////////////////////////////////////////
     */
    //Update Playback Speed (0.25x, 0.5x, 1x, 1.5x, 2x)
    fun updatePlaybackSpeed(speed: Float) {
        _selectedPlaybackSpeed.value = speed // store user preferences
        player.playbackParameters = PlaybackParameters(_selectedPlaybackSpeed.value)
    }

    /**
     * ////////////////////////////////////////////////////////// VIDEO PART /////////////////////////////////////////////////////////////////
     */

    // ✅ Extracts all available tracks and stores video resolutions separately
//    private fun extractAvailableTracks() {
//        val tracks = player.currentTracks
//        val extractedTracks = mutableMapOf<Int, List<Tracks.Group>>()
//        val resolutionList = mutableListOf<Pair<String, TrackSelectionOverride>>()
//
//        SUPPORTED_TRACK_TYPES.forEach { trackType ->
//            val trackGroups = tracks.groups.filter { it.type == trackType }
//            if (trackGroups.isNotEmpty()) {
//                extractedTracks[trackType] = trackGroups
//            }
//
//            // ✅ Extract video resolutions for manual selection
//            if (trackType == C.TRACK_TYPE_VIDEO) {
//                trackGroups.forEach { trackGroup ->
//                    val mediaTrackGroup = trackGroup.mediaTrackGroup
//                    for (i in 0 until mediaTrackGroup.length) {
//                        val format = mediaTrackGroup.getFormat(i)
//                        val resolution = "${format.height}p" // Example: "360p", "720p"
//
//                        val override = TrackSelectionOverride(mediaTrackGroup, i) // Manual selection override
//                        resolutionList.add(resolution to override)
//                    }
//                }
//            }
//        }
//
//        _availableTracks.value = extractedTracks
//        _availableVideoResolutions.value = resolutionList.distinctBy { it.first } // Remove duplicate resolutions
//    }

    private fun extractAvailableTracks() {
        val tracks = player.currentTracks
        val extractedTracks = mutableMapOf<Int, List<Tracks.Group>>()
        val resolutionList = mutableListOf<Pair<Int, String>>() // ✅ Store (trackIndex, resolution)

        SUPPORTED_TRACK_TYPES.forEach { trackType ->
            val trackGroups = tracks.groups.filter { it.type == trackType }
            if (trackGroups.isNotEmpty()) {
                extractedTracks[trackType] = trackGroups
            }

            // ✅ Extract video resolutions for manual selection
            if (trackType == C.TRACK_TYPE_VIDEO) {
                trackGroups.forEach { trackGroup ->
                    val mediaTrackGroup = trackGroup.mediaTrackGroup
                    for (i in 0 until mediaTrackGroup.length) {
                        val format = mediaTrackGroup.getFormat(i)
                        val resolution = "${format.height}p" // Example: "360p", "720p"

                        resolutionList.add(i to resolution) // ✅ Store trackIndex with resolution
                    }
                }
            }
        }

        _availableTracks.value = extractedTracks
        _availableVideoTracks.value = resolutionList.distinctBy { it.second } // ✅ Remove duplicate resolutions
    }

    // When use change the quality - Entry point of interacting with the quality selection
    private fun updateVideoQuality(
        videoQualityOptions: VideoQualityOptions
    ) {
        // Update the video quality feature selection:
        updateVideoQualityFeatures(selectedVideoQualityFeature = videoQualityOptions)
        Log.d("ON_PLAY_EVENT", "updateVideoQuality: CALLED")
        applyQualitySelection(videoQualityOptions = videoQualityOptions)
    }

    // Apply the quality based on the different quality types
    private fun applyQualitySelection(videoQualityOptions: VideoQualityOptions) {
        when (videoQualityOptions) {
            VideoQualityOptions.Auto -> enableAdaptiveStreaming()
            VideoQualityOptions.HighQuality -> setHighBitrate()
            VideoQualityOptions.DataSaver -> setLowBitrate()
            VideoQualityOptions.Advanced -> openAdvancedQualitySelection()
        }
    }

    // Automatic streaming based on the user's network including Dash/HLS and so on
    private fun enableAdaptiveStreaming() {
        _trackSelectionParameters.update { params ->
            params.buildUpon()
                .clearOverridesOfType(C.TRACK_TYPE_VIDEO) // clear manual selection
                .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, false) // enable auto-adaptive
                .build()
        }

        player.trackSelectionParameters = _trackSelectionParameters.value
    }

    // Set the highest bitrate available
    private fun setHighBitrate() {
        val trackGroup = player.currentTracks.groups.filter { track ->
            track.type == C.TRACK_TYPE_VIDEO
        }

        if (trackGroup.isEmpty()) return

        val highestQualityTrackGroup = trackGroup.maxByOrNull { track ->
            track.mediaTrackGroup.length
        } ?: return

        val highestQualityIndex =
            highestQualityTrackGroup.length - 1 // Last track is usually highest

        val override =
            TrackSelectionOverride(highestQualityTrackGroup.mediaTrackGroup, highestQualityIndex)

        _trackSelectionParameters.update { params ->
            params.buildUpon()
                .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                .addOverride(override) // Apply highes quality
                .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, false)
                .build()
        }

        player.trackSelectionParameters = _trackSelectionParameters.value
    }

    // Set the lowest bitrate available
    private fun setLowBitrate() {
        val trackGroup = player.currentTracks.groups.filter { track ->
            track.type == C.TRACK_TYPE_VIDEO
        }

        if (trackGroup.isEmpty()) return

        val lowestQualityTrackGroup = trackGroup.minByOrNull { track ->
            track.mediaTrackGroup.length
        } ?: return

        val lowestQualityIndex = 0 // First track is usually the lowest

        val override =
            TrackSelectionOverride(lowestQualityTrackGroup.mediaTrackGroup, lowestQualityIndex)

        _trackSelectionParameters.update { params ->
            params.buildUpon()
                .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                .addOverride(override) // Apply lowest quality
                .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, false)
                .build()
        }

        player.trackSelectionParameters = _trackSelectionParameters.value
    }

    // Trigger to open advance quality selection -> 360p, 480p, 720p
    private fun openAdvancedQualitySelection() {
        onPlayerEvent(event = PlayerEvent.ToggleQualitySelection)
    }

    // When user select a specific quality
    fun updateVideoQuality(trackIndex: Int) {
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

    /**
     * ////////////////////////////////////////////////////////// [START] Player Controller event updates /////////////////////////////////////////////////////////////////
     */

    // Progress of tracks -> Primarily using for seekbar updates
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

    private fun playVideo(
        url: String,
        drmConfig: MediaItem.DrmConfiguration? = null,
        adsConfig: MediaItem.AdsConfiguration? = null
    ) {
        exoPlayerManager.preparePlayer(url, drmConfig, adsConfig)
        _playerUiState.update { it.copy(currentUrl = url, isPlaying = true) }
    }

    private fun togglePlayPause() {
        player.playWhenReady = !player.isPlaying
        _playerUiState.update { it.copy(isPlaying = player.isPlaying) }
    }

    // Toggle Fullscreen
    fun toggleFullscreen() {
        _isFullscreen.update { !it }
    }

    // Seek to Position
    private fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    // Seek Forward (10s)
    private fun seekForward() {
        seekTo(player.currentPosition + 10_000)
    }

    // Seek Backward (10s)
    private fun seekBackward() {
        seekTo((player.currentPosition - 10_000).coerceAtLeast(0))
    }

    /**
     * ////////////////////////////////////////////////////////// [END] Player Controller event updates /////////////////////////////////////////////////////////////////
     */

    /**
     * ////////////////////////////////////////////////////////// [START] Player surface interaction /////////////////////////////////////////////////////////////////
     */
    fun updateZoom(scale: Float) {
        _zoomScale.value = scale
    }

    fun resetZoom() {
        _zoomScale.value = 1f
    }
    /**
     * ////////////////////////////////////////////////////////// [END] Player surface interaction /////////////////////////////////////////////////////////////////
     */

    /**
     * ////////////////////////////////////////////////////////// [START] GC /////////////////////////////////////////////////////////////////
     */
    private fun releasePlayer() {
        exoPlayerManager.releasePlayer()
        exoPlayerManager.releaseAdsLoaders()
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
    /**
     * ////////////////////////////////////////////////////////// [END] GC /////////////////////////////////////////////////////////////////
     */

    /**
     * ////////////////////////////////////////////////////////// [START] UTILITY /////////////////////////////////////////////////////////////////
     */
    // Format Time
    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    // List of Supported Track Types (Video, Audio, Subtitles, Images)
    private val SUPPORTED_TRACK_TYPES = listOf(
        C.TRACK_TYPE_VIDEO, C.TRACK_TYPE_AUDIO, C.TRACK_TYPE_TEXT, C.TRACK_TYPE_IMAGE
    )

    /**
     * ////////////////////////////////////////////////////////// [END] UTILITY /////////////////////////////////////////////////////////////////
     */

    /**
     * ////////////////////////////////////////////////////////// [START] ExoPlayer Events - Default One /////////////////////////////////////////////////////////////////
     */

    // Track Player Events

    private fun observePlayerEvents() {
        player.addListener(object : Player.Listener {

            override fun onEvents(player: Player, events: Player.Events) {
                Log.d("PlayerEvent", "🔄 Player Events Triggered: $events")

                if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)) {
                    handlePlaybackStateChanged(player)
                }
                if (events.contains(Player.EVENT_IS_PLAYING_CHANGED)) {
                    _playerUiState.update { it.copy(isPlaying = player.isPlaying) }
                }
                if (events.contains(Player.EVENT_TRACKS_CHANGED)) {
                    handleTracksChanged(player.currentTracks)
                }
                if (events.contains(Player.EVENT_MEDIA_METADATA_CHANGED)) {
                    handleMetadataChanged(player.mediaMetadata)
                }
                if (events.contains(Player.EVENT_VIDEO_SIZE_CHANGED)) {
                    detectCurrentPlayingResolution()
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                handlePositionDiscontinuity(player, oldPosition, newPosition, reason)
            }

            override fun onTracksChanged(tracks: Tracks) {
                handleTracksChanged(tracks)
            }

            override fun onRenderedFirstFrame() {
                viewModelScope.launch {
                    delay(1000) // ✅ Allow time for the resolution to stabilize
                    detectCurrentPlayingResolution()
                }
            }

            override fun onSurfaceSizeChanged(width: Int, height: Int) {
                detectCurrentPlayingResolution()
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                detectCurrentPlayingResolution()
            }
        })
    }

    private fun handlePositionDiscontinuity(
        player: Player,
        oldPosition: Player.PositionInfo? = null,
        newPosition: Player.PositionInfo? = null,
        reason: Int
    ) {
        val reasonMessage = when (reason) {
            Player.DISCONTINUITY_REASON_AUTO_TRANSITION -> "Auto-transition to next media item."
            Player.DISCONTINUITY_REASON_SEEK -> "User performed seek."
            Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT -> "Seek position adjusted."
            Player.DISCONTINUITY_REASON_SKIP -> "Skipped media item."
            Player.DISCONTINUITY_REASON_REMOVE -> "Media item removed from playlist."
            Player.DISCONTINUITY_REASON_INTERNAL -> "Internal player transition."
            Player.DISCONTINUITY_REASON_SILENCE_SKIP -> "Silence skipped."
            else -> "Unknown reason."
        }

        Log.d("PlayerEvent", "🔄 Position Discontinuity: $reasonMessage")

        _playerUiState.update {
            it.copy(
                currentPosition = formatTime(player.currentPosition),
                totalDuration = formatTime(player.duration),
                progress = if (player.duration > 0) player.currentPosition.toFloat() / player.duration else 0f
            )
        }
    }

    private fun detectCurrentPlayingResolution() {
        val trackResolution = player.videoFormat?.height ?: 0 // Metadata resolution
        val renderedResolution = player.videoSize.height // Rendered resolution
        val decoderResolution = getRealTimeDecoderResolution()?.second ?: 0 // Decoder resolution

        val resolutions = listOf(trackResolution, renderedResolution, decoderResolution)
            .filter { it > 0 } // ✅ Remove invalid values
            .distinct() // ✅ Avoid duplicates
            .sortedDescending() // ✅ Sort in descending order (highest first)

        val finalResolution = resolutions.firstOrNull() ?: 0 // ✅ Get highest available resolution

        val resolutionString = "${finalResolution}p" // ✅ Convert to standard format

        if (_currentPlayingResolution.value != resolutionString) {
            _currentPlayingResolution.value = resolutionString
            Log.d("TrackDebug", "📢 Real-Time Playing Resolution Updated: $resolutionString")
        }

        // ✅ Debugging logs
        Log.d("TrackDebug", "🧐 Available Resolutions -> Track: $trackResolution, Rendered: $renderedResolution, Decoder: $decoderResolution")
    }


    private fun getRealTimeDecoderResolution(): Pair<Int, Int>? {
        val decoderCounters = player.videoDecoderCounters ?: return null

        val width = decoderCounters.renderedOutputBufferCount
        val height = player.videoSize.height // Usually contains the decoded height

        return if (width > 0 && height > 0) Pair(width, height) else null
    }
    private fun handlePlaybackStateChanged(player: Player) {
        val playbackState = player.playbackState
        val playbackStateMessage = when (playbackState) {
            Player.STATE_READY -> "Player is ready to play."
            Player.STATE_BUFFERING -> "Buffering..."
            Player.STATE_ENDED -> "Playback ended."
            Player.STATE_IDLE -> "Player is idle."
            else -> "Unknown playback state."
        }

        Log.d("PlayerEvent", "🎬 Playback State: $playbackStateMessage")

        _playerUiState.update {
            it.copy(
                isBuffering = playbackState == Player.STATE_BUFFERING,
                progress = if (player.duration > 0) player.currentPosition.toFloat() / player.duration else 0f,
                currentPosition = formatTime(player.currentPosition),
                totalDuration = formatTime(player.duration)
            )
        }
    }

    private fun handleTracksChanged(tracks: Tracks) {
        extractAvailableTracks()

        val activeTrack = tracks.groups.firstOrNull { track ->
            track.isSelected && track.type == C.TRACK_TYPE_VIDEO
        }

        activeTrack?.let { track ->
            val format = track.getTrackFormat(0)
            val resolution = "${format.height}p" // Directly use height
            val trackIndex = track.mediaTrackGroup.indexOf(format)

            if (_currentPlayingResolution.value != resolution || _currentPlayingTrackIndex.value != trackIndex) {
                _currentPlayingResolution.value = resolution
                _currentPlayingTrackIndex.value = trackIndex
                Log.d("TrackDebug", "✅ New Resolution Selected: $resolution")
            }
        }
    }


    private fun handleMetadataChanged(mediaMetadata: MediaMetadata) {
        Log.d("PlayerEvent", "🎵 Media Metadata Changed: ${mediaMetadata.title}")
    }

//    private fun observePlayerEvents() {
//        player.addListener(object : Player.Listener {
//            override fun onPlaybackStateChanged(playbackState: Int) {
//                _playerUiState.update {
//                    it.copy(
//                        isBuffering = playbackState == Player.STATE_BUFFERING, // ✅ Update buffering state
//                        progress = player.currentPosition.toFloat() / player.duration.toFloat(),
//                        currentPosition = formatTime(player.currentPosition),
//                        totalDuration = formatTime(player.duration)
//                    )
//                }
//            }
//
//            override fun onIsPlayingChanged(isPlaying: Boolean) {
//                _playerUiState.update { it.copy(isPlaying = isPlaying) }
//            }
//
//            override fun onPositionDiscontinuity(
//                oldPosition: Player.PositionInfo,
//                newPosition: Player.PositionInfo,
//                reason: Int
//            ) {
//                _playerUiState.update {
//                    it.copy(currentPosition = formatTime(newPosition.positionMs))
//                }
//            }
//
//            override fun onTracksChanged(tracks: Tracks) {
//                extractAvailableTracks()
//
//                // Detect currently playing resolution
//                val activeTrack = tracks.groups.firstOrNull { track ->
//                    track.isSelected && track.type == C.TRACK_TYPE_VIDEO
//                }
//
//                activeTrack?.let { t ->
//                    val format = t.getTrackFormat(0) // get the active track format
//                    val resolution = "${format.height}p"
//                    _currentPlayingResolution.value = resolution
//
//                    Log.d("TrackDebug", "Current Playing Resolution: $resolution")
//                }
//            }
//        })
//    }

    /**
     * ////////////////////////////////////////////////////////// [END] ExoPlayer Events - Default One /////////////////////////////////////////////////////////////////
     */


    /**
     * ////////////////////////////////////////////////////////// [START] Custom Events /////////////////////////////////////////////////////////////////
     */
    // Restart Video
    private fun restartVideo() {
        seekTo(0L)
        player.playWhenReady = true
    }

    private fun openComments() {

    }

    private fun openQualitySelection() {

    }

    private fun openSettings(shouldOpen: Boolean) {
        _isSettingVisible.update { shouldOpen }
    }

    private fun toggleScreenLock() {

    }

    private fun setSleepTimer(minutes: Int) {

    }

    private fun toggleShuffle() {

    }

    private fun toggleLoop() {

    }

    private fun toggleCaptions() {

    }

    private fun toggleSubtitles() {

    }

    private fun toggleMute() {

    }

    private fun enablePictureInPicture() {

    }

    private fun stopPlayer() {

    }

    private fun playPrevious() {

    }

    private fun playNext() {

    }
    /**
     * ////////////////////////////////////////////////////////// [END] Custom Events /////////////////////////////////////////////////////////////////
     */

    /** Extract track qualities when initialized */
    // Checks if the current player has selectable tracks
    fun willHaveContent(): Boolean {
        return willHaveContent(player.currentTracks)
    }

    // Checks if any supported track types exist in the given track list
    fun willHaveContent(tracks: Tracks): Boolean {
        return tracks.groups.any { trackGroup ->
            SUPPORTED_TRACK_TYPES.contains(trackGroup.type)
        }
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

        // ✅ Apply track selection
        _trackSelectionParameters.value = builder.build()
        player.trackSelectionParameters = _trackSelectionParameters.value

        // ✅ Apply playback speed separately (since it’s not part of track selection)
        if (selectedPlaybackSpeed.value != _selectedPlaybackSpeed.value)
            player.playbackParameters = PlaybackParameters(_selectedPlaybackSpeed.value)
    }

    private fun willDisableTrackType(trackType: Int): Boolean {
        return trackSelectionParameters.value.disabledTrackTypes.contains(trackType)
    }

    private fun updateVideoQualityFeatures(selectedVideoQualityFeature: VideoQualityOptions) {
        _selectedVideoQualityFeatures.value = selectedVideoQualityFeature
        _videoQualityFeatureList.value =
            VideoQualityOptions.entries.map { it to (it == selectedVideoQualityFeature) }
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
