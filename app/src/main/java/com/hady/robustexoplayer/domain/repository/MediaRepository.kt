package com.hady.robustexoplayer.domain.repository

import android.net.Uri
import androidx.media3.common.MediaItem

interface MediaRepository {

    suspend fun getMediaItem(): MediaItem
    suspend fun downloadMedia(uri: Uri): Boolean
}