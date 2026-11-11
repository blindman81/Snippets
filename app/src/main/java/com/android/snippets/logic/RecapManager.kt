package com.android.snippets.logic

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.android.snippets.data.preferences.UserPreferencesRepository
import com.android.snippets.model.Photo
import com.android.snippets.ui.MonthlyRecapActivity
import com.android.snippets.ui.YearlyRecapActivity
import com.ln.android.snippets.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.flow.first

object RecapManager {
    const val CHANNEL_ID = "recap_channel"
    const val NOTIFICATION_ID_MONTHLY = 2001
    const val NOTIFICATION_ID_YEARLY = 2002

    fun canPostNotifications(context: Context): Boolean {
        val hasRuntimePermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        if (!hasRuntimePermission || !NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            var channel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (channel == null) {
                val recapChannel = NotificationChannel(
                    CHANNEL_ID,
                    "Recaps",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for monthly and yearly photo recaps"
                    enableVibration(true)
                    setShowBadge(true)
                }
                notificationManager.createNotificationChannel(recapChannel)
                channel = notificationManager.getNotificationChannel(CHANNEL_ID)
            }
            if (channel?.importance == NotificationManager.IMPORTANCE_NONE) return false
        }

        return true
    }

    suspend fun checkAndPostRecapNotification(context: Context, photos: List<Photo>) {
        if (!canPostNotifications(context)) return
        if (photos.isEmpty()) return

        val repo = UserPreferencesRepository.getInstance(context)
        val prefs = repo.userPreferencesFlow.first()

        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)
        val currentMonth = now.get(Calendar.MONTH) // 0..11

        // 1. Check Yearly Recap: when entering a new year (e.g. in January)
        val prevYear = currentYear - 1
        if (currentMonth == Calendar.JANUARY && prevYear >= 2020) {
            if (prefs.lastSeenYearlyRecapYear != prevYear && prefs.lastNotifiedYearlyRecapYear != prevYear) {
                val yearlyPhotos = getRecapPhotos(photos, month = null, year = prevYear)
                if (yearlyPhotos.isNotEmpty()) {
                    repo.updateLastNotifiedYearlyRecapYear(prevYear)
                    sendYearlyRecapNotification(context, prevYear, yearlyPhotos)
                    return
                } else {
                    repo.updateLastNotifiedYearlyRecapYear(prevYear)
                }
            }
        }

        // 2. Check Monthly Recap: for the completed previous month
        val prevCal = (now.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
        val targetMonth = prevCal.get(Calendar.MONTH)
        val targetYear = prevCal.get(Calendar.YEAR)
        val recapKey = "${targetYear}_${targetMonth}"

        if (prefs.lastSeenMonthlyRecapPeriod != recapKey && prefs.lastNotifiedMonthlyRecapPeriod != recapKey) {
            val monthlyPhotos = getRecapPhotos(photos, month = targetMonth, year = targetYear)
            if (monthlyPhotos.isNotEmpty()) {
                repo.updateLastNotifiedMonthlyRecapPeriod(recapKey)
                sendMonthlyRecapNotification(context, targetMonth, targetYear, monthlyPhotos)
            } else {
                repo.updateLastNotifiedMonthlyRecapPeriod(recapKey)
            }
        }
    }

    fun getRecapPhotos(photos: List<Photo>, month: Int?, year: Int): List<Photo> {
        val matched = photos.filter { photo ->
            val c = Calendar.getInstance().apply { timeInMillis = photo.date }
            val matchesYear = c.get(Calendar.YEAR) == year
            val matchesMonth = month == null || c.get(Calendar.MONTH) == month
            matchesYear && matchesMonth
        }

        val filtered = if (month == null) {
            val highRated = matched.filter { it.isFavorite || it.rating >= 3 }
            if (highRated.isNotEmpty()) highRated else matched
        } else {
            matched
        }

        return filtered.sortedWith(
            compareByDescending<Photo> { it.isFavorite }
                .thenByDescending { it.rating }
                .thenByDescending { it.snippets.size }
                .thenByDescending { it.date }
        )
    }

    suspend fun sendMonthlyRecapNotification(
        context: Context,
        month: Int,
        year: Int,
        photos: List<Photo>
    ) {
        val monthCal = Calendar.getInstance().apply {
            set(Calendar.MONTH, month)
            set(Calendar.YEAR, year)
        }
        val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(monthCal.time)
        val title = "Your $monthName recap"
        val count = photos.size
        val text = if (count == 1) {
            "Look back at your photo from $monthName."
        } else {
            "Look back at your $count photos from $monthName."
        }

        val intent = Intent(context, MonthlyRecapActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("month", month)
            putExtra("year", year)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_MONTHLY,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val topPhoto = photos.firstOrNull()
        val rawBitmap = topPhoto?.let { loadPhotoBitmap(context, it) }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (rawBitmap != null) {
            builder.setLargeIcon(rawBitmap)
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(rawBitmap)
                    .bigLargeIcon(null as Bitmap?)
                    .setBigContentTitle(title)
                    .setSummaryText(text)
            )
        } else {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(text))
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_MONTHLY, builder.build())
    }

    suspend fun sendYearlyRecapNotification(
        context: Context,
        year: Int,
        photos: List<Photo>
    ) {
        val title = "Your $year recap"
        val count = photos.size
        val text = if (count == 1) {
            "Look back at your photo from $year."
        } else {
            "Look back at your $count photos from $year."
        }

        val intent = Intent(context, YearlyRecapActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("year", year)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_YEARLY,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val topPhoto = photos.firstOrNull()
        val rawBitmap = topPhoto?.let { loadPhotoBitmap(context, it) }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (rawBitmap != null) {
            builder.setLargeIcon(rawBitmap)
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(rawBitmap)
                    .bigLargeIcon(null as Bitmap?)
                    .setBigContentTitle(title)
                    .setSummaryText(text)
            )
        } else {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(text))
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_YEARLY, builder.build())
    }

    suspend fun testRecapNotification(context: Context, photos: List<Photo>, isYearly: Boolean = false) {
        val now = Calendar.getInstance()
        if (isYearly) {
            val targetYear = now.get(Calendar.YEAR)
            val matched = getRecapPhotos(photos, null, targetYear).ifEmpty {
                getRecapPhotos(photos, null, targetYear - 1).ifEmpty { photos }
            }
            sendYearlyRecapNotification(context, targetYear, matched)
        } else {
            val targetMonth = now.get(Calendar.MONTH)
            val targetYear = now.get(Calendar.YEAR)
            val matched = getRecapPhotos(photos, targetMonth, targetYear).ifEmpty {
                val prevCal = (now.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                getRecapPhotos(photos, prevCal.get(Calendar.MONTH), prevCal.get(Calendar.YEAR)).ifEmpty { photos }
            }
            sendMonthlyRecapNotification(context, targetMonth, targetYear, matched)
        }
    }

    private suspend fun loadPhotoBitmap(context: Context, photo: Photo): Bitmap? {
        return try {
            val uri = photo.uri
            if (uri == android.net.Uri.EMPTY) return null
            val request = ImageRequest.Builder(context)
                .data(uri)
                .allowHardware(false)
                .size(1024, 1024)
                .build()
            val result = context.imageLoader.execute(request)
            result.image?.toBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
