package com.hady.robustexoplayer.presentation.component

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.media3.common.util.UnstableApi
import com.hady.robustexoplayer.data.model.SettingsFeature
import com.hady.robustexoplayer.presentation.component.speed.PlaybackSpeedComponent
import com.hady.robustexoplayer.presentation.view_model.PlayerViewModel

@OptIn(UnstableApi::class)
@Composable
internal fun SettingsDetailsScreen(
    feature: SettingsFeature,
    playerViewModel: PlayerViewModel,
    selectedSpeed: Float
) {

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

        SettingsFeature.Quality -> {}
        SettingsFeature.SleepTimer -> {}
    }
}