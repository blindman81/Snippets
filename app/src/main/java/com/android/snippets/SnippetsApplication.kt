package com.android.snippets

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.imageLoader
import coil3.memory.MemoryCache
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.util.DebugLogger

@HiltAndroidApp
class SnippetsApplication : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    @Suppress("DEPRECATION")
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        try {
            // Never clear cache on TRIM_MEMORY_UI_HIDDEN or TRIM_MEMORY_BACKGROUND,
            // as opening a file picker or briefly switching apps would wipe all decoded photos.
            if (level >= TRIM_MEMORY_COMPLETE || level >= TRIM_MEMORY_RUNNING_CRITICAL) {
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

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(250L * 1024 * 1024)
                    .build()
            }
            .allowHardware(true)
            .crossfade(false)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val memoryChannel = NotificationChannel("new_memory_channel", "New memory", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifications when a new memory is ready"
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(memoryChannel)

            val recapChannel = NotificationChannel(
                com.android.snippets.logic.RecapManager.CHANNEL_ID,
                "Recaps",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for monthly and yearly photo recaps"
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(recapChannel)
        }
    }
}
