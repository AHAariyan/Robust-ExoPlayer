package com.hady.robustexoplayer.presentation.component

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.hady.robustexoplayer.common.circularPlayIcon
import com.hady.robustexoplayer.common.topBarIcon
import com.hady.robustexoplayer.data.model.SettingsFeature
import com.hady.robustexoplayer.data.model.settingFeatures
import com.hady.robustexoplayer.presentation.view_model.PlayerViewModel


@OptIn(UnstableApi::class)
@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    playerViewModel: PlayerViewModel,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    val selectedFeature by playerViewModel.selectedFeature.collectAsStateWithLifecycle()
    val selectedSpeed = playerViewModel.selectedPlaybackSpeed.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = {
            playerViewModel.selectFeature(null)
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

            if (selectedFeature == null) {
                settingFeatures.forEach { feature ->
                    SettingsSingleItem(
                        shouldExpandable = feature is SettingsFeature.Quality || feature is SettingsFeature.PlaybackSpeed,
                        shouldShowValue = feature is SettingsFeature.Quality || feature is SettingsFeature.PlaybackSpeed,
                        title = stringResource(feature.titleResId),
                        itemIcon = painterResource(circularPlayIcon),
                        showSelectedSpeed = selectedSpeed.value,
                        onItemClick = {
                            playerViewModel.selectFeature(feature)
                        }
                    )
                }
            } else { // Show the secondary screen with values for the selected feature
                SettingsDetailsScreen(
                    feature = selectedFeature!!,
                    playerViewModel = playerViewModel,
                    selectedSpeed = selectedSpeed.value
                )
            }
        }
    }
}
