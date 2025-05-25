package com.example.stopprogressif.di

import android.content.Context
import com.example.stopprogressif.data.DataStoreManager
import com.example.stopprogressif.NotificationHelper
import com.example.stopprogressif.timer.TimerController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager {
        return DataStoreManager(context)
    }

    @Singleton
    @Provides
    fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper {
        return NotificationHelper(context)
    }

    @Singleton
    @Provides
    fun provideTimerController(@ApplicationContext context: Context): TimerController {
        // Hilt fournira le Context à votre TimerController si besoin
        return TimerController(context)
    }

    // Si vous avez d'autres dépendances à fournir, ajoutez-les ici.
}