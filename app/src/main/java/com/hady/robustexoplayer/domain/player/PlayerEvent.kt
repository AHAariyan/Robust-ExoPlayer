package com.hady.robustexoplayer.domain.player

import androidx.compose.ui.res.stringResource
import androidx.media3.common.MediaItem
import com.hady.robustexoplayer.common.advancedString
import com.hady.robustexoplayer.common.autoVideoQualityString
import com.hady.robustexoplayer.common.dataSaverString
import com.hady.robustexoplayer.common.highPictureQualityString

sealed class PlayerEvent {

    // Playback Control Events
    data class Play(val url: String) : PlayerEvent()
    data object Pause : PlayerEvent()
    data class SeekTo(val positionMs: Long) : PlayerEvent()
    data class PlaybackSpeed(val speed: Float) : PlayerEvent()
    data class PlaybackQuality(val quality: VideoQualityOptions) : PlayerEvent()
    data object Next : PlayerEvent()
    data object Previous : PlayerEvent()
    data object Restart : PlayerEvent()
    data object Stop : PlayerEvent()

    // Advanced Playback Events
    data object EnablePiP : PlayerEvent()
    data object ToggleFullscreen : PlayerEvent()
    data object ToggleMute : PlayerEvent()
    data object ToggleSubtitles : PlayerEvent()
    data object ToggleCaptions : PlayerEvent()
    data object ToggleLoop : PlayerEvent()
    data object ToggleShuffle : PlayerEvent()
    data class SetSleepTimer(val minutes: Int) : PlayerEvent()
    data object ToggleScreenLock : PlayerEvent()

    // Seek Control Events
    data class FastForward(val seconds: Int = 10) : PlayerEvent()
    data class Rewind(val seconds: Int = 10) : PlayerEvent()

    // UI Interaction Events
    data class ToggleSettings(val shouldOpen: Boolean) : PlayerEvent() // Opens the Settings Menu
    data object ToggleQualitySelection : PlayerEvent() // Open quality menu
    data object ToggleComments : PlayerEvent() // Show/Hide comments section
}

enum class VideoQualityOptions(
    val title: String,
    val subTitle: String
) {
    Auto(title = autoVideoQualityString, subTitle = "Adjusts to give you the best experience for your conditions"),
    HighQuality(
        title = highPictureQualityString,
        subTitle = "Uses more data for better quality"
    ),
    DataSaver(title = dataSaverString, subTitle = "Lower picture quality to save data"),
    Advanced(title = advancedString, subTitle = "Select a specific resolution") // Manually select resolution
}
