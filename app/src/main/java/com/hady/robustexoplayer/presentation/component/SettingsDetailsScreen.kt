package com.hady.robustexoplayer.presentation.component

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.hady.robustexoplayer.data.model.SettingsFeature
import com.hady.robustexoplayer.data.model.VideoQualityOptions
import com.hady.robustexoplayer.domain.player.PlayerEvent
import com.hady.robustexoplayer.presentation.component.speed.PlaybackSpeedComponent
import com.hady.robustexoplayer.presentation.component.video_quality.AvailableVideoQualityComponent
import com.hady.robustexoplayer.presentation.component.video_quality.QualityFeaturesComponent
import com.hady.robustexoplayer.presentation.view_model.PlayerViewModel

//@OptIn(UnstableApi::class)
//@Composable
//internal fun SettingsDetailsScreen(
//    feature: SettingsFeature,
//    playerViewModel: PlayerViewModel,
//    selectedSpeed: Float
//) {
//
//    val listOfVideoQualityFeature by playerViewModel.videoQualityFeatureList.collectAsStateWithLifecycle()
//    val availableVideoResolution by playerViewModel.availableVideoResolutions.collectAsStateWithLifecycle()
//
//    when (feature) {
//        SettingsFeature.AdditionalSettings -> {}
//        SettingsFeature.Captions -> {}
//        SettingsFeature.LockScreen -> {}
//        SettingsFeature.PlaybackSpeed -> {
//            PlaybackSpeedComponent(
//                selectedSpeed = selectedSpeed,
//                onSpeedChange = { updatedSpeed ->
//                    playerViewModel.updatePlaybackSpeed(updatedSpeed)
//                }
//            )
//        }
//
//        SettingsFeature.VideoQualityFeatures.Main -> {
//            QualityFeaturesComponent(
//                videoQualityFeatures = listOfVideoQualityFeature,
//                onFeatureQualitySelected = { feat ->
//                    playerViewModel.onPlayerEvent(event = PlayerEvent.PlaybackQuality(quality = feat))
//                    when (feat) {
//                        VideoQualityOptions.Advanced -> {
//                            playerViewModel.settingsMenuSelection(SettingsFeature.VideoQualityFeatures.AvailableQuality)
//                        }
//
//                        else -> {
//                            //playerViewModel.onPlayerEvent(event = PlayerEvent.PlaybackQuality(quality = feat))
//                        }
//                    }
//                }
//            )
//        }
//
//        SettingsFeature.VideoQualityFeatures.AvailableQuality -> {
//            if (playerViewModel.willHaveContent()) {
//                AvailableVideoQualityComponent(
//                    availableVideoResolution = availableVideoResolution.map { it.first }
//                )
//            } else {
//                Text(
//                    text = "No quality options available",
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.Bold,
//                    modifier = Modifier.padding(16.dp)
//                )
//            }
//
//        }
//
//        SettingsFeature.SleepTimer -> {}
//    }
//}