package com.hady.robustexoplayer.presentation.component.video_quality

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.hady.robustexoplayer.common.circleIcon
import com.hady.robustexoplayer.common.tickMarkIcon
import com.hady.robustexoplayer.domain.player.VideoQualityOptions
import com.hady.robustexoplayer.ui.theme.RobustExoPlayerTheme


@Composable
internal fun QualityFeaturesComponent(
    videoQualityFeatures: List<Pair<VideoQualityOptions, Boolean>>,
    onFeatureQualitySelected: (VideoQualityOptions) -> Unit
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
                modifier = Modifier.padding(start = 4.dp, end = 4.dp).size(8.dp),
                painter = painterResource(id = circleIcon),
                contentDescription = null,
                tint = Color.Gray
            )


            Text(
                text = "360p",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.padding(top = 16.dp))
        videoQualityFeatures.forEach { (feature, isSelected) ->
            VideoQualityFeatureSingleItem(
                feature = feature,
                isSelected = isSelected,
                onFeatureQualitySelected = onFeatureQualitySelected
            )
        }
    }
}


@Composable
internal fun VideoQualityFeatureSingleItem(
    feature: VideoQualityOptions,
    isSelected: Boolean,
    onFeatureQualitySelected: (VideoQualityOptions) -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onFeatureQualitySelected(feature) }
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
                text = feature.title,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                text = feature.subTitle,
                color = Color.Black.copy(alpha = 0.5f),
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
@Preview
internal fun PreviewQualityFeatureComponent() {

    val list = VideoQualityOptions.entries.map { it to (it == VideoQualityOptions.Auto) }
    RobustExoPlayerTheme {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            QualityFeaturesComponent(videoQualityFeatures = list, onFeatureQualitySelected = {})
        }
    }
}