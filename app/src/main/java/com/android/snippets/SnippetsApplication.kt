package com.android.snippets

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.android.snippets.data.preferences.UserPreferencesRepository
import com.android.snippets.model.ThemePreference
import com.android.snippets.ui.theme.applyApplicationNightMode
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.imageLoader
import coil3.memory.MemoryCache
import coil3.request.allowHardware
import coil3.request.crossfade

@HiltAndroidApp
class SnippetsApplication : Application(), SingletonImageLoader.Factory {
    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initThemePreference()
    }

    private fun initThemePreference() {
        val storedPref = runBlocking {
            userPreferencesRepository.themePreferenceFlow.first()
        }
        val themePref = when (storedPref.uppercase()) {
            "LIGHT" -> ThemePreference.LIGHT
            "DARK" -> ThemePreference.DARK
            else -> ThemePreference.SYSTEM
        }
        userPreferencesRepository.cachedThemePreference = themePref
        applyApplicationNightMode(this, themePref)
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
