package com.hady.robustexoplayer.data.model

import com.hady.robustexoplayer.common.additionalSettingsTitle
import com.hady.robustexoplayer.common.captionTitle
import com.hady.robustexoplayer.common.lockScreenTitle
import com.hady.robustexoplayer.common.playbackSpeedTitle
import com.hady.robustexoplayer.common.qualityTitle
import com.hady.robustexoplayer.common.sleepTimerTitle

sealed class SettingsFeature(val titleResId: Int) {
    object Quality : SettingsFeature(qualityTitle)
    object PlaybackSpeed : SettingsFeature(playbackSpeedTitle)
    object Captions : SettingsFeature(captionTitle)
    object LockScreen : SettingsFeature(lockScreenTitle)
    object SleepTimer : SettingsFeature(sleepTimerTitle)
    object AdditionalSettings : SettingsFeature(additionalSettingsTitle)

    companion object {
        val allFeatures = listOf(Quality, PlaybackSpeed, Captions, LockScreen, SleepTimer, AdditionalSettings)
    }
}

val settingFeatures = listOf(
    SettingsFeature.Quality,
    SettingsFeature.PlaybackSpeed,
    SettingsFeature.Captions,
    SettingsFeature.LockScreen,
    SettingsFeature.SleepTimer,
    SettingsFeature.AdditionalSettings
)

