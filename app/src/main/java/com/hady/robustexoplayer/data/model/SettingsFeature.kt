package com.hady.robustexoplayer.data.model

import com.hady.robustexoplayer.common.additionalSettingsTitle
import com.hady.robustexoplayer.common.advancedString
import com.hady.robustexoplayer.common.advancedTitle
import com.hady.robustexoplayer.common.autoVideoQualityString
import com.hady.robustexoplayer.common.captionTitle
import com.hady.robustexoplayer.common.dataSaverString
import com.hady.robustexoplayer.common.highPictureQualityString
import com.hady.robustexoplayer.common.lockScreenTitle
import com.hady.robustexoplayer.common.playbackSpeedTitle
import com.hady.robustexoplayer.common.qualityTitle
import com.hady.robustexoplayer.common.sleepTimerTitle

sealed class SettingsFeature(val titleResId: Int) {
    object PlaybackSpeed : SettingsFeature(playbackSpeedTitle)
    object Captions : SettingsFeature(captionTitle)
    object LockScreen : SettingsFeature(lockScreenTitle)
    object SleepTimer : SettingsFeature(sleepTimerTitle)
    object AdditionalSettings : SettingsFeature(additionalSettingsTitle)

    sealed class VideoQualityFeatures(titleResId: Int) : SettingsFeature(titleResId) {
        data object Main: VideoQualityFeatures(titleResId = qualityTitle) // Main Feature
        data object AvailableQuality : VideoQualityFeatures(titleResId = qualityTitle)
    }
}

val settingFeatures = listOf(
    SettingsFeature.VideoQualityFeatures.Main,
    SettingsFeature.PlaybackSpeed,
    SettingsFeature.Captions,
    SettingsFeature.LockScreen,
    SettingsFeature.SleepTimer,
    SettingsFeature.AdditionalSettings
)


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

