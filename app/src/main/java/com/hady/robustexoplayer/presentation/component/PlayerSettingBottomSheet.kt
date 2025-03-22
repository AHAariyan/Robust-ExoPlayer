package com.hady.robustexoplayer.presentation.component

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.hady.robustexoplayer.common.circularPlayIcon
import com.hady.robustexoplayer.common.lockIcon
import com.hady.robustexoplayer.common.playbackSpeedIcon
import com.hady.robustexoplayer.common.settingIcon
import com.hady.robustexoplayer.common.sleepTimerIcon
import com.hady.robustexoplayer.common.subtitlesIcon
import com.hady.robustexoplayer.common.topBarIcon
import com.hady.robustexoplayer.common.videoQualityIcon
import com.hady.robustexoplayer.data.model.SettingsFeature
import com.hady.robustexoplayer.data.model.VideoQualityOptions
import com.hady.robustexoplayer.data.model.settingFeatures
import com.hady.robustexoplayer.domain.player.PlayerEvent
import com.hady.robustexoplayer.presentation.component.speed.PlaybackSpeedComponent
import com.hady.robustexoplayer.presentation.component.video_quality.AvailableVideoQualityComponent
import com.hady.robustexoplayer.presentation.component.video_quality.QualityFeaturesComponent
import com.hady.robustexoplayer.presentation.view_model.PlayerViewModel
import java.util.Locale


@OptIn(UnstableApi::class)
@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    playerViewModel: PlayerViewModel,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    val selectedFeature by playerViewModel.selectedSettingMenu.collectAsStateWithLifecycle()
    val selectedSpeed = playerViewModel.selectedPlaybackSpeed.collectAsStateWithLifecycle()

    val currentPlayingResolution by playerViewModel.currentPlayingResolution.collectAsStateWithLifecycle()
    val currentTrackIndex by playerViewModel.currentPlayingTrackIndex.collectAsStateWithLifecycle()

    val listOfVideoQualityFeature by playerViewModel.videoQualityFeatureList.collectAsStateWithLifecycle()
    val availableVideoResolution by playerViewModel.availableVideoResolutions.collectAsStateWithLifecycle()
    val availableVideoTracks by playerViewModel.availableVideoTracks.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = {
            playerViewModel.settingsMenuSelection(null)
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {

        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                painter = painterResource(topBarIcon),
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.padding(top = 8.dp))

            when (selectedFeature) {
                SettingsFeature.AdditionalSettings -> {}
                SettingsFeature.Captions -> {}
                SettingsFeature.LockScreen -> {}

                SettingsFeature.PlaybackSpeed -> {
                    PlaybackSpeedComponent(
                        selectedSpeed = selectedSpeed.value,
                        onSpeedChange = { updatedSpeed ->
                            playerViewModel.updatePlaybackSpeed(updatedSpeed)
                        }
                    )
                }

                SettingsFeature.VideoQualityFeatures.Main -> {
                    QualityFeaturesComponent(
                        videoQualityFeatures = listOfVideoQualityFeature,
                        currentPlayingResolution = currentPlayingResolution,
                        onFeatureQualitySelected = { feat ->
                            playerViewModel.onPlayerEvent(event = PlayerEvent.PlaybackQuality(quality = feat))
                            when (feat) {
                                VideoQualityOptions.Advanced -> {
                                    playerViewModel.settingsMenuSelection(SettingsFeature.VideoQualityFeatures.AvailableQuality)
                                }

                                else -> {
                                    //playerViewModel.onPlayerEvent(event = PlayerEvent.PlaybackQuality(quality = feat))
                                }
                            }
                        }
                    )
                }
                is SettingsFeature.VideoQualityFeatures.AvailableQuality -> {
                    if (playerViewModel.willHaveContent()) {
                        Log.d("QUALITY_LIST", "SettingsBottomSheet: ${availableVideoResolution.size}")
                        AvailableVideoQualityComponent(
                            availableVideoTracks = availableVideoTracks,
                            currentPlayingTrackIndex = currentTrackIndex,
                            onQualitySelected = { trackIndex ->
                                playerViewModel.updateVideoQuality(trackIndex) // ✅ Apply user selection
                            }
                        )
                    } else {
                        Text(
                            text = "No quality options available",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }


                SettingsFeature.SleepTimer -> {}
                null -> {
                    settingFeatures.forEach { feature ->
                        val selectedValue = if (feature == SettingsFeature.PlaybackSpeed) {
                            if (selectedSpeed.value % 1 == 0f) {
                                "${selectedSpeed.value.toInt()}x"  // Show integer if there's no decimal part
                            } else {
                                String.format(
                                    Locale.US,
                                    "%.2fx",
                                    selectedSpeed.value
                                )  // Force US locale for decimal formatting
                            }
                        } else if (feature == SettingsFeature.VideoQualityFeatures.Main) {
                            currentPlayingResolution
                        } else {
                            "Off"
                        }
                        SettingsSingleItem(
                            shouldExpandable = feature is SettingsFeature.VideoQualityFeatures.Main || feature is SettingsFeature.PlaybackSpeed || feature is SettingsFeature.SleepTimer,
                            shouldShowValue = feature is SettingsFeature.VideoQualityFeatures.Main || feature is SettingsFeature.PlaybackSpeed || feature is SettingsFeature.SleepTimer,
                            title = stringResource(feature.titleResId),
                            itemIcon = painterResource(id = getFeatureIcon(feature)),
                            showSelectedValue = selectedValue,
                            onItemClick = {
                                playerViewModel.settingsMenuSelection(feature)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun getFeatureIcon(feature: SettingsFeature): Int {
    return when (feature) {
        is SettingsFeature.VideoQualityFeatures.Main -> videoQualityIcon
        is SettingsFeature.PlaybackSpeed -> playbackSpeedIcon
        is SettingsFeature.Captions -> subtitlesIcon
        is SettingsFeature.LockScreen -> lockIcon
        is SettingsFeature.SleepTimer -> sleepTimerIcon
        is SettingsFeature.AdditionalSettings -> settingIcon
        SettingsFeature.VideoQualityFeatures.AvailableQuality -> circularPlayIcon
    }
}
