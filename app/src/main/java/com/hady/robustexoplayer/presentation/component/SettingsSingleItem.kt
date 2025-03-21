package com.hady.robustexoplayer.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hady.robustexoplayer.common.additionalSettingsTitle
import com.hady.robustexoplayer.common.captionTitle
import com.hady.robustexoplayer.common.circularPlayIcon
import com.hady.robustexoplayer.common.lockScreenTitle
import com.hady.robustexoplayer.common.playbackSpeedTitle
import com.hady.robustexoplayer.common.qualityTitle
import com.hady.robustexoplayer.common.rightArrowIcon
import com.hady.robustexoplayer.common.sleepTimerTitle
import com.hady.robustexoplayer.ui.theme.RobustExoPlayerTheme
import java.util.Locale

/**
 * Expandable value should show only for the following features:
 *      1. Quality          Auto (360p)
 *      2. Playback speed   1x
 *      3. Sleep timer      Off/On
 */

@Composable
internal fun SettingsSingleItem(
    shouldExpandable: Boolean,
    shouldShowValue: Boolean,
    showSelectedValue: String ?= null,
    title: String,
    itemIcon: Painter,
    onItemClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .clickable{
                if (shouldExpandable) {
                    onItemClick()
                }
            }.padding(top = 8.dp, bottom = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = itemIcon,
            tint = Color.Black.copy(0.8f),
            contentDescription = null
        )
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = title,
            color = Color.Black.copy(0.8f),
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.weight(1f))
        if (shouldShowValue && showSelectedValue != null) {
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = showSelectedValue,
                color = Color.Black.copy(.5f),
                fontSize = 16.sp
            )
        }
        if (shouldExpandable) {
            Icon(
                modifier = Modifier.padding(start = 16.dp).size(24.dp),
                painter = painterResource(rightArrowIcon),
                tint = Color.Black.copy(0.5f),
                contentDescription = null
            )
        }
    }
}

@Composable
@Preview
internal fun PreviewSettingSingleItem() {
    val qualityTitle = stringResource(qualityTitle)
    val playbackSpeedTitle = stringResource(playbackSpeedTitle)
    val captionTitle = stringResource(captionTitle)
    val lockScreenTitle = stringResource(lockScreenTitle)
    val sleepTimerTitle = stringResource(sleepTimerTitle)
    val additionalSettingsTitle = stringResource(additionalSettingsTitle)
    val listOfItems = listOf(
        qualityTitle,
        playbackSpeedTitle,
        captionTitle,
        lockScreenTitle,
        sleepTimerTitle,
        additionalSettingsTitle
    )
    RobustExoPlayerTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            listOfItems.forEach { item ->
                SettingsSingleItem(
                    shouldExpandable = true,
                    shouldShowValue = true,
                    title = item,
                    itemIcon = painterResource(circularPlayIcon)
                )

                Spacer(Modifier.padding(top = 8.dp, bottom = 8.dp))
            }
        }

    }
}