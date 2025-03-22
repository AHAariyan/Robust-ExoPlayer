package com.hady.robustexoplayer.presentation.component.video_quality

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.TrackSelectionOverride
import com.hady.robustexoplayer.common.circleIcon
import com.hady.robustexoplayer.common.tickMarkIcon
import com.hady.robustexoplayer.ui.theme.RobustExoPlayerTheme

@Composable
internal fun AvailableVideoQualityComponent(
    availableVideoTracks: List<Pair<Int, String>>,
    currentPlayingTrackIndex: Int?,
    onQualitySelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quality for current video",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )

            Icon(
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .size(8.dp),
                painter = painterResource(id = circleIcon),
                contentDescription = null,
                tint = Color.Gray
            )

            currentPlayingTrackIndex?.let { index ->
                availableVideoTracks.find { it.first == index }?.second?.let { resolution ->
                    Text(
                        text = resolution,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.padding(top = 16.dp))

        LazyColumn {
            items(availableVideoTracks.size) { index ->
                val (trackIndex, resolution) = availableVideoTracks[index]
                VideoQualitySingleItem(
                    title = resolution,
                    isSelected = currentPlayingTrackIndex == trackIndex,
                    onQualitySelected = { onQualitySelected(trackIndex) } // ✅ Pass track index
                )
            }
        }
    }
}

@Composable
internal fun VideoQualitySingleItem(
    title: String,
    isSelected: Boolean,
    onQualitySelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable {
                onQualitySelected()
            }
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (isSelected) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(tickMarkIcon),
                contentDescription = null,
                tint = Color.Black
            )
        } else {
            Spacer(modifier = Modifier.padding(start = 24.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

        }
    }
}


//@Composable
//@Preview
//internal fun PreviewAvailableVideoQualityComponent() {
//    RobustExoPlayerTheme {
//        Column {
//            AvailableVideoQualityComponent(
//                availableVideoResolution = emptyList(),
//                currentPlayingResolution = "",
//                onQualitySelected = {}
//            )
//        }
//    }
//}