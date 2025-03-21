package com.hady.robustexoplayer.presentation.component

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.hady.robustexoplayer.data.model.SettingsFeature
import com.hady.robustexoplayer.data.model.VideoQualityOptions
import com.hady.robustexoplayer.presentation.component.speed.PlaybackSpeedComponent
import com.hady.robustexoplayer.presentation.component.video_quality.AvailableVideoQualityComponent
import com.hady.robustexoplayer.presentation.component.video_quality.QualityFeaturesComponent
import com.hady.robustexoplayer.presentation.view_model.PlayerViewModel

@OptIn(UnstableApi::class)
@Composable
internal fun SettingsDetailsScreen(
    feature: SettingsFeature,
    playerViewModel: PlayerViewModel,
    selectedSpeed: Float
) {

    val listOfVideoQualityFeature by playerViewModel.videoQualityFeatureList.collectAsStateWithLifecycle()

    when (feature) {
        SettingsFeature.AdditionalSettings -> {}
        SettingsFeature.Captions -> {}
        SettingsFeature.LockScreen -> {}
        SettingsFeature.PlaybackSpeed -> {
            PlaybackSpeedComponent(
                selectedSpeed = selectedSpeed,
                onSpeedChange = { updatedSpeed ->
                    playerViewModel.updatePlaybackSpeed(updatedSpeed)
                }
            )
        }

        SettingsFeature.VideoQualityFeatures.Main -> {
            QualityFeaturesComponent(
                videoQualityFeatures = listOfVideoQualityFeature,
                onFeatureQualitySelected = { feature ->
                    when(feature) {
                        VideoQualityOptions.Advanced -> {
                            playerViewModel.settingsMenuSelection(SettingsFeature.VideoQualityFeatures.AvailableQuality)
                        }

                        VideoQualityOptions.Auto -> {}
                        VideoQualityOptions.HighQuality -> {}
                        VideoQualityOptions.DataSaver -> {}
                    }
                }
            )
        }
        SettingsFeature.VideoQualityFeatures.AvailableQuality -> {
            AvailableVideoQualityComponent()
        }
        SettingsFeature.SleepTimer -> {}
    }
}