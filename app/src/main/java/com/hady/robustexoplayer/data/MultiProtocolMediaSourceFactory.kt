package com.hady.robustexoplayer.data

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.rtmp.RtmpDataSource
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.drm.DefaultDrmSessionManagerProvider
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.ima.ImaServerSideAdInsertionMediaSource
import androidx.media3.exoplayer.smoothstreaming.SsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.ui.PlayerView

@UnstableApi
class MultiProtocolMediaSourceFactory(
    private val context: Context
) {
    private val dataSourceFactory = DefaultHttpDataSource.Factory()

    private val drmSessionManagerProvider = DefaultDrmSessionManagerProvider().apply {
        setDrmHttpDataSourceFactory(DefaultHttpDataSource.Factory())
    }

    private var serverSideAdsLoader: ImaServerSideAdInsertionMediaSource.AdsLoader? = null
    private var serverSideAdsLoaderState: ImaServerSideAdInsertionMediaSource.AdsLoader.State? =
        null
    private val extractorsFactory = DefaultExtractorsFactory()

    fun getMediaSourceFactory(): MediaSource.Factory {
        return DefaultMediaSourceFactory(context)
            .setDataSourceFactory(dataSourceFactory)
            .setDrmSessionManagerProvider(drmSessionManagerProvider)
    }


    @OptIn(UnstableApi::class)
    fun createMediaSource(mediaItem: MediaItem): MediaSource {
        return when {
            mediaItem.localConfiguration?.uri.toString().endsWith(".m3u8") -> {
                // HLS Media Source
                HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            }

            mediaItem.localConfiguration?.uri.toString().endsWith(".mpd") -> {
                // DASH Media Source
                DashMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            }

            mediaItem.localConfiguration?.uri.toString()
                .endsWith(".ism") || mediaItem.localConfiguration?.uri.toString()
                .contains(".ism/Manifest") -> {
                // Smooth Streaming Media Source
                SsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            }

            mediaItem.localConfiguration?.uri.toString().startsWith("rtmp://") -> {
                // RTMP Media Source
                ProgressiveMediaSource.Factory(RtmpDataSource.Factory())
                    .createMediaSource(mediaItem)
            }

            else -> {
                // Default Fallback (MP4, MKV, etc.)
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun createMediaSourceWithDRM(mediaItem: MediaItem): MediaSource {
        return DefaultMediaSourceFactory(context)
            .setDataSourceFactory(dataSourceFactory)
            .setDrmSessionManagerProvider(drmSessionManagerProvider)
            .createMediaSource(mediaItem)
    }

    @OptIn(UnstableApi::class)
    fun createMediaSourceWithAds(mediaItem: MediaItem, playerView: PlayerView): MediaSource {
        if (serverSideAdsLoader == null) {
            val serverSideAdLoaderBuilder =
                ImaServerSideAdInsertionMediaSource.AdsLoader.Builder(context, playerView)
            if (serverSideAdsLoaderState != null) {
                serverSideAdLoaderBuilder.setAdsLoaderState(serverSideAdsLoaderState!!)
            }
            serverSideAdsLoader = serverSideAdLoaderBuilder.build()
        }

        return DefaultMediaSourceFactory(context)
            .setDataSourceFactory(dataSourceFactory)
            .setServerSideAdInsertionMediaSourceFactory(
                ImaServerSideAdInsertionMediaSource.Factory(
                    serverSideAdsLoader!!,
                    DefaultMediaSourceFactory(context)
                        .setDataSourceFactory(dataSourceFactory)
                )
            ).createMediaSource(mediaItem)
    }

    @OptIn(UnstableApi::class)
    fun createMediaItem(
        url: String,
        drmConfig: MediaItem.DrmConfiguration? = null,
        adsConfig: MediaItem.AdsConfiguration? = null
    ): MediaItem? {
        val uri = Uri.parse(url)

        // Build the MediaItem
        val mediaItemBuilder = MediaItem.Builder()
            .setUri(uri)
            .setMimeType(getMimeType(url)) // Auto-detect MIME type
            .apply {
                drmConfig?.let { setDrmConfiguration(it) }
                adsConfig?.let { setAdsConfiguration(it) }
            }

        val mediaItem = mediaItemBuilder.build()

        // Check if cleartext HTTP traffic is permitted
        if (!Util.checkCleartextTrafficPermitted(mediaItem)) {
            Log.e("MediaSourceFactory", "Cleartext HTTP not permitted: $url")
            return null // Reject media item if HTTP is blocked
        }

        return mediaItem
    }

    private fun getMimeType(url: String): String {
        return when {
            url.endsWith(".m3u8") -> MimeTypes.APPLICATION_M3U8
            url.endsWith(".mpd") -> MimeTypes.APPLICATION_MPD
            url.endsWith(".ism") -> MimeTypes.APPLICATION_SS
            url.startsWith("rtmp://") -> MimeTypes.VIDEO_MP4 // RTMP doesn't have a specific MIME type
            else -> MimeTypes.VIDEO_MP4 // Default to MP4
        }
    }


    fun releaseAdsLoader() {
        serverSideAdsLoaderState = serverSideAdsLoader?.release()
        serverSideAdsLoader = null
    }
}
