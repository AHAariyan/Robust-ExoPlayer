package com.hady.robustexoplayer.domain.player

import androidx.media3.common.MediaItem

sealed class PlayerEvent {

    // Playback Control Events
    data class Play(val mediaItem: MediaItem) : PlayerEvent()
    data object Pause : PlayerEvent()
    data class SeekTo(val positionMs: Long) : PlayerEvent()
    data class ChangeSpeed(val speed: Float) : PlayerEvent()
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
    data object ToggleSettings : PlayerEvent() // Opens the Settings Menu
    data object ToggleQualitySelection : PlayerEvent() // Open quality menu
    data object ToggleComments : PlayerEvent() // Show/Hide comments section
}
