package com.hady.robustexoplayer.domain.player

import androidx.media3.common.MediaItem

sealed class PlayerEvent {

    // Playback Control Events
    data class Play(val mediaItem: MediaItem) : PlayerEvent()
    object Pause : PlayerEvent()
    data class SeekTo(val positionMs: Long) : PlayerEvent()
    data class ChangeSpeed(val speed: Float) : PlayerEvent()
    object Next : PlayerEvent()
    object Previous : PlayerEvent()
    object Restart : PlayerEvent()
    object Stop : PlayerEvent()

    // Advanced Playback Events
    object EnablePiP : PlayerEvent()
    object ToggleFullscreen : PlayerEvent()
    object ToggleMute : PlayerEvent()
    object ToggleSubtitles : PlayerEvent()
    object ToggleCaptions : PlayerEvent()
    object ToggleLoop : PlayerEvent()
    object ToggleShuffle : PlayerEvent()
    data class SetSleepTimer(val minutes: Int) : PlayerEvent()
    object ToggleScreenLock : PlayerEvent()

    // Seek Control Events
    data class FastForward(val seconds: Int = 10) : PlayerEvent()
    data class Rewind(val seconds: Int = 10) : PlayerEvent()

    // UI Interaction Events
    object ToggleSettings : PlayerEvent() // Opens the Settings Menu
    object ToggleQualitySelection : PlayerEvent() // Open quality menu
    object ToggleComments : PlayerEvent() // Show/Hide comments section
}
