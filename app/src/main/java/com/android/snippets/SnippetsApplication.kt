package com.android.snippets

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.imageLoader
import coil.memory.MemoryCache
import coil.util.DebugLogger

@HiltAndroidApp
class SnippetsApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    @Suppress("DEPRECATION")
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        try {
            if (level >= TRIM_MEMORY_BACKGROUND || level >= TRIM_MEMORY_UI_HIDDEN || level >= TRIM_MEMORY_RUNNING_LOW) {
                imageLoader.memoryCache?.clear()
            }
        } catch (_: Exception) {
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        try {
            imageLoader.memoryCache?.clear()
        } catch (_: Exception) {
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.08)
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .allowHardware(true)
            .crossfade(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "New memory"
            val descriptionText = "Notifications when a new memory is ready"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("new_memory_channel", name, importance).apply {
                description = descriptionText
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
