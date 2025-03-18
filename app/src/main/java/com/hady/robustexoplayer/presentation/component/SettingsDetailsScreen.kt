package com.hady.robustexoplayer.presentation.component

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.media3.common.util.UnstableApi
import com.hady.robustexoplayer.data.model.SettingsFeature
import com.hady.robustexoplayer.domain.player.PlayerEvent
import com.hady.robustexoplayer.presentation.view_model.PlayerViewModel

@OptIn(UnstableApi::class)
@Composable
internal fun SettingsDetailsScreen(
    feature: SettingsFeature,
    playerViewModel: PlayerViewModel
) {
    when (feature) {
        SettingsFeature.AdditionalSettings -> {}
        SettingsFeature.Captions -> {}
        SettingsFeature.LockScreen -> {}
        SettingsFeature.PlaybackSpeed -> {
            PlaybackSpeedComponent(
                onSpeedChange = { updatedSpeed ->
                    Log.d("PLAYBACK_SPEED", "SettingsDetailsScreen: ${updatedSpeed}")
                    //playerViewModel.onPlayerEvent(event = PlayerEvent.PlaybackSpeed(updatedSpeed))
                    playerViewModel.updatePlaybackSpeed(updatedSpeed)
                }
            )
        }

        SettingsFeature.Quality -> {}
        SettingsFeature.SleepTimer -> {}
    }
}