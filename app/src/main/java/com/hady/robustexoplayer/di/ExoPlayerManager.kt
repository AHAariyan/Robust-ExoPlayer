package com.hady.robustexoplayer.di

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.ima.ImaAdsLoader
import androidx.media3.exoplayer.ima.ImaServerSideAdInsertionMediaSource
import androidx.media3.exoplayer.source.ads.AdsLoader
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.util.EventLogger
import com.hady.robustexoplayer.data.MultiProtocolMediaSourceFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton


@UnstableApi
@Singleton
class ExoPlayerManager (
    @ApplicationContext private val context: Context
){
    private var exoPlayer: ExoPlayer? = null
    private val mediaSourceFactory = MultiProtocolMediaSourceFactory(context)

    // Ads and DRM management
    private var clientSideAdsLoader: ImaAdsLoader? = null
    private var serverSideAdsLoader: ImaServerSideAdInsertionMediaSource.AdsLoader? = null
    private var serverSideAdsLoaderState: ImaServerSideAdInsertionMediaSource.AdsLoader.State? = null

    @OptIn(UnstableApi::class)
    fun getPlayer(): ExoPlayer {
        if (exoPlayer == null) {
            val playerBuilder = ExoPlayer.Builder(context)
                .setTrackSelector(DefaultTrackSelector(context))
                .setLoadControl(DefaultLoadControl())
                .setMediaSourceFactory(mediaSourceFactory.getMediaSourceFactory())
                .setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus= */ true)

            // ✅ Set custom RenderersFactory
            setRenderersFactory(playerBuilder, preferExtensionDecoders = false)

            exoPlayer = playerBuilder.build()

            // Add analytics listener
            exoPlayer?.addAnalyticsListener(EventLogger())

            // Set DRM and AdsLoader
            configurePlayerWithServerSideAdsLoader()
        }
        return exoPlayer!!
    }

    @OptIn(UnstableApi::class)
    fun preparePlayer(
        url: String,
        drmConfig: MediaItem.DrmConfiguration? = null,
        adsConfig: MediaItem.AdsConfiguration? = null
    ) {
        val player = getPlayer()

        // Create MediaItem
        val mediaItem = mediaSourceFactory.createMediaItem(url, drmConfig, adsConfig)
        if (mediaItem == null) {
            Log.e("ExoPlayerManager", "Failed to create MediaItem for URL: $url")
            return
        }

        // Create MediaSource
        val mediaSource = mediaSourceFactory.createMediaSource(mediaItem)

        player.setMediaSource(mediaSource)
        player.prepare()
        player.playWhenReady = true
    }

    private fun setRenderersFactory(playerBuilder: ExoPlayer.Builder, preferExtensionDecoders: Boolean) {
        val renderersFactory = buildRenderersFactory(context, preferExtensionDecoders)
        playerBuilder.setRenderersFactory(renderersFactory)
    }

    private fun buildRenderersFactory(context: Context, preferExtensionDecoders: Boolean): RenderersFactory {
        return DefaultRenderersFactory(context)
            .setExtensionRendererMode(
                if (preferExtensionDecoders) DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                else DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF
            )
    }

    private fun configurePlayerWithServerSideAdsLoader() {
        serverSideAdsLoader?.setPlayer(getPlayer())
    }

    // Handle DRM & Ad Management
    private fun getClientSideAdsLoader(adsConfiguration: MediaItem.AdsConfiguration): AdsLoader {
        if (clientSideAdsLoader == null) {
            clientSideAdsLoader = ImaAdsLoader.Builder(context).build()
        }
        clientSideAdsLoader?.setPlayer(getPlayer())
        return clientSideAdsLoader!!
    }

    fun releasePlayer() {
        exoPlayer?.run {
            stop()  // Stops playback to avoid crashes
            clearMediaItems()  // Clears the playlist
            release()  // Releases ExoPlayer resources
        }
        exoPlayer = null // Ensure no memory leaks
    }

    fun releaseAdsLoaders() {
        clientSideAdsLoader?.release()
        clientSideAdsLoader = null
        serverSideAdsLoaderState = serverSideAdsLoader?.release()
        serverSideAdsLoader = null
    }
}
