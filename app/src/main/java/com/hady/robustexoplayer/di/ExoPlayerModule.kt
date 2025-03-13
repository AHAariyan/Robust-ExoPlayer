package com.hady.robustexoplayer.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExoPlayerModule {
    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideExoPlayerManager(@ApplicationContext context: Context): ExoPlayerManager {
        return ExoPlayerManager(context)
    }
}
