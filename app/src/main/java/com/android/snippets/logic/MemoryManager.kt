package com.android.snippets.logic

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.android.snippets.model.Photo
import java.util.Calendar
import java.util.concurrent.TimeUnit

object MemoryManager {
    const val MEMORY_REMINDER_PREFIX = "memory_reminder_"
    const val MEMORIES_PER_CYCLE = 5
    const val HISTORY_MEMORIES_LIMIT = 10
    const val CYCLE_MORNING_HOUR = 9
    const val CYCLE_EVENING_HOUR = 21
    const val CYCLE_WINDOW_HOURS = 12L
    const val NEW_MEMORY_NOTIFICATION_DELAY_HOURS = 12L
    const val VIEWED_MEMORY_VISIBLE_HOURS = 12L
    const val RECENTLY_VIEWED_MEMORY_HOURS = 12L
    const val VIEWED_MEMORY_RESET_DAYS = 1L
    const val VIEWED_MEMORY_RESET_HOURS = 24L
    const val SURFACED_MEMORY_SPACING_HOURS = 3L
    const val TEST_NOTIFICATION_DELAY_SECONDS = 5L
    const val TEST_NOTIFICATION_PHOTO_ID = "test_notification"
    const val CYCLE_WORK_NAME = "memory_cycle_work"

    val CYCLE_WINDOW_MS: Long = TimeUnit.HOURS.toMillis(CYCLE_WINDOW_HOURS)
    val NEW_MEMORY_WAIT_MS: Long = TimeUnit.HOURS.toMillis(NEW_MEMORY_NOTIFICATION_DELAY_HOURS)
    val VIEWED_MEMORY_VISIBLE_MS: Long = TimeUnit.HOURS.toMillis(VIEWED_MEMORY_VISIBLE_HOURS)
    val RECENTLY_VIEWED_MEMORY_MS: Long = TimeUnit.HOURS.toMillis(RECENTLY_VIEWED_MEMORY_HOURS)
    val VIEWED_MEMORY_RESET_MS: Long = TimeUnit.HOURS.toMillis(VIEWED_MEMORY_RESET_HOURS)
    val SURFACED_MEMORY_SPACING_MS: Long = TimeUnit.HOURS.toMillis(SURFACED_MEMORY_SPACING_HOURS)

    fun getCurrentCycleStart(now: Long = System.currentTimeMillis()): Long {
        // Protect against background work triggering slightly early (within 2 minutes)
        val adjustedNow = now + TimeUnit.MINUTES.toMillis(2)
        val cal = Calendar.getInstance().apply { timeInMillis = adjustedNow }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return when {
            hour < CYCLE_MORNING_HOUR -> {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                cal.set(Calendar.HOUR_OF_DAY, CYCLE_EVENING_HOUR)
                cal.timeInMillis
            }
            hour < CYCLE_EVENING_HOUR -> {
                cal.set(Calendar.HOUR_OF_DAY, CYCLE_MORNING_HOUR)
                cal.timeInMillis
            }
            else -> {
                cal.set(Calendar.HOUR_OF_DAY, CYCLE_EVENING_HOUR)
                cal.timeInMillis
            }
        }
    }

    fun getNextCycleStart(time: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = time }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return if (hour < CYCLE_MORNING_HOUR || (hour == CYCLE_MORNING_HOUR && minute == 0)) {
            cal.set(Calendar.HOUR_OF_DAY, CYCLE_MORNING_HOUR)
            if (cal.timeInMillis <= time) {
                cal.set(Calendar.HOUR_OF_DAY, CYCLE_EVENING_HOUR)
            }
            cal.timeInMillis
        } else if (hour < CYCLE_EVENING_HOUR || (hour == CYCLE_EVENING_HOUR && minute == 0)) {
            cal.set(Calendar.HOUR_OF_DAY, CYCLE_EVENING_HOUR)
            if (cal.timeInMillis <= time) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, CYCLE_MORNING_HOUR)
            }
            cal.timeInMillis
        } else {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            cal.set(Calendar.HOUR_OF_DAY, CYCLE_MORNING_HOUR)
            cal.timeInMillis
        }
    }

    fun getPreviousCycleStart(now: Long = System.currentTimeMillis()): Long {
        val current = getCurrentCycleStart(now)
        val cal = Calendar.getInstance().apply { timeInMillis = current }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        return if (hour == CYCLE_MORNING_HOUR) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
            cal.set(Calendar.HOUR_OF_DAY, CYCLE_EVENING_HOUR)
            cal.timeInMillis
        } else {
            cal.set(Calendar.HOUR_OF_DAY, CYCLE_MORNING_HOUR)
            cal.timeInMillis
        }
    }

    fun shouldResurfaceMemory(photo: Photo, now: Long = System.currentTimeMillis()): Boolean {
        if (!photo.isLibraryUpload || photo.snippets.isEmpty() || photo.snippetsAddedTime == 0L) {
            return false
        }
        if (photo.surfacedTime != 0L && photo.surfacedTime > now) {
            return false
        }
        if (photo.isViewed && photo.lastViewedTime > 0L) {
            return now - photo.lastViewedTime >= VIEWED_MEMORY_RESET_MS
        }
        if (!photo.isViewed && photo.surfacedTime != 0L) {
            return now - photo.surfacedTime >= VIEWED_MEMORY_RESET_MS
        }
        return false
    }

    fun reconcileSurfacedMemories(
        photos: List<Photo>,
        now: Long = System.currentTimeMillis()
    ): Pair<List<Photo>, List<Pair<String, Long>>>? {
        var changed = false
        val scheduledNotifications = mutableListOf<Pair<String, Long>>()
        val currentCycleStart = getCurrentCycleStart(now)

        val updatedPhotos = photos.map { photo ->
            if (shouldResurfaceMemory(photo, now) || (photo.isViewed && photo.lastViewedTime > 0L && photo.snippetsAddedTime > photo.lastViewedTime)) {
                changed = true
                photo.copy(isViewed = false, surfacedTime = 0L)
            } else photo
        }.toMutableList()

        // Upcoming queue candidates:
        // - Newly added snippets (surfacedTime == 0L)
        // - Already queued future memories (surfacedTime > now)
        // - Past unviewed memories whose cycle has expired (surfacedTime < currentCycleStart && !isViewed)
        // Fresh/unviewed snippets take top priority; previously viewed (reset) memories only fill
        // slots when the queue has free room.
        val queueCandidates = updatedPhotos.filter { photo ->
            photo.isLibraryUpload &&
                    photo.snippets.isNotEmpty() &&
                    photo.snippetsAddedTime != 0L &&
                    !photo.isViewed &&
                    (photo.surfacedTime == 0L || photo.surfacedTime > now || photo.surfacedTime < currentCycleStart)
        }.sortedWith(
            compareBy<Photo> { it.lastViewedTime > 0L } // Fresh memories first, reset memories only when queue is free
                .thenByDescending { it.snippetsAddedTime } // Newest snippets first
                .thenByDescending { it.date }
        )

        if (queueCandidates.isNotEmpty()) {
            var targetCycle = getNextCycleStart(now)
            // Assign candidates in batches of MEMORIES_PER_CYCLE (5) to upcoming cycles.
            // When newer snippets enter the queue, older ones are pushed into subsequent cycle slots.
            for (chunk in queueCandidates.chunked(MEMORIES_PER_CYCLE)) {
                for (candidate in chunk) {
                    val index = updatedPhotos.indexOfFirst { it.id == candidate.id }
                    if (index != -1 && updatedPhotos[index].surfacedTime != targetCycle) {
                        updatedPhotos[index] = updatedPhotos[index].copy(surfacedTime = targetCycle)
                        if (targetCycle > now) {
                            scheduledNotifications.add(candidate.id to targetCycle)
                        }
                        changed = true
                    }
                }
                targetCycle = getNextCycleStart(targetCycle)
            }
        }

        return if (changed) Pair(updatedPhotos, scheduledNotifications) else null
    }

    fun getCuratedMemories(photos: List<Photo>, now: Long = System.currentTimeMillis()): List<Photo> {
        val currentCycleStart = getCurrentCycleStart(now)
        return photos.filter { photo ->
            photo.isLibraryUpload &&
                    photo.snippets.isNotEmpty() &&
                    photo.snippetsAddedTime != 0L &&
                    photo.surfacedTime != 0L &&
                    (photo.surfacedTime <= now || photo.surfacedTime == currentCycleStart) &&
                    (photo.surfacedTime == currentCycleStart || (now - photo.surfacedTime < CYCLE_WINDOW_MS))
        }.sortedByDescending { it.date }
            .take(MEMORIES_PER_CYCLE)
    }

    fun getHistoryMemories(
        photos: List<Photo>,
        now: Long = System.currentTimeMillis(),
        limit: Int = HISTORY_MEMORIES_LIMIT
    ): List<Photo> {
        return photos.filter { photo ->
            photo.isLibraryUpload &&
                    photo.snippets.isNotEmpty() &&
                    photo.snippetsAddedTime != 0L &&
                    photo.surfacedTime != 0L &&
                    photo.surfacedTime <= now
        }.sortedWith(
            compareByDescending<Photo> { it.surfacedTime }
                .thenByDescending { it.date }
        ).take(limit)
    }

    fun hasUnviewedMemories(curated: List<Photo>): Boolean {
        return curated.any { !it.isViewed }
    }

    fun memoryReminderName(photoId: String) = "$MEMORY_REMINDER_PREFIX$photoId"

    fun scheduleMemoryNotification(
        context: Context,
        photoId: String,
        delay: Long,
        delayUnit: TimeUnit = TimeUnit.HOURS,
        notificationType: String = MemoryWorker.TYPE_NEW,
        policy: ExistingWorkPolicy = ExistingWorkPolicy.REPLACE,
        resetPostedState: Boolean = true
    ) {
        if (photoId.isBlank()) return

        if (resetPostedState) {
            MemoryWorker.clearPostedNotificationState(context, photoId)
        }
        val reminderName = memoryReminderName(photoId)
        val workRequestBuilder = OneTimeWorkRequestBuilder<MemoryWorker>()
            .setInitialDelay(delay, delayUnit)
            .setBackoffCriteria(
                androidx.work.BackoffPolicy.LINEAR,
                30,
                TimeUnit.MINUTES
            )
            .setInputData(
                workDataOf(
                    MemoryWorker.INPUT_PHOTO_ID to photoId,
                    MemoryWorker.INPUT_NOTIFICATION_TYPE to notificationType
                )
            )
            .addTag(reminderName)

        val workRequest = workRequestBuilder.build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            reminderName,
            policy,
            workRequest
        )
    }

    fun cancelMemoryNotification(context: Context, photoId: String) {
        if (photoId.isBlank()) return
        val reminderName = memoryReminderName(photoId)
        WorkManager.getInstance(context).cancelUniqueWork(reminderName)
        WorkManager.getInstance(context).cancelAllWorkByTag(reminderName)
        MemoryWorker.cancelPostedNotification(context, photoId)
    }

    fun scheduleNextCycleWork(context: Context) {
        val now = System.currentTimeMillis()
        val nextCycle = getNextCycleStart(maxOf(now, getCurrentCycleStart(now) + TimeUnit.MINUTES.toMillis(5)))
        val delay = maxOf(0L, nextCycle - now)

        // Cancel any legacy per-photo work requests to prevent arbitrary fire times
        try {
            WorkManager.getInstance(context).cancelAllWorkByTag(MEMORY_REMINDER_PREFIX)
            WorkManager.getInstance(context).cancelUniqueWork("daily_reminder_work")
            val allPhotos = MemoryWorker.loadAllPhotos(context)
            for (p in allPhotos) {
                val reminderName = memoryReminderName(p.id)
                WorkManager.getInstance(context).cancelUniqueWork(reminderName)
                WorkManager.getInstance(context).cancelAllWorkByTag(reminderName)
            }
            WorkManager.getInstance(context).pruneWork()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val workRequest = OneTimeWorkRequestBuilder<MemoryWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(MemoryWorker.INPUT_IS_CYCLE_TRIGGER to true))
            .addTag(CYCLE_WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            CYCLE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun reconcileMemoryNotifications(
        context: Context,
        photos: List<Photo>
    ) {
        scheduleNextCycleWork(context)
    }

    fun testNotification(context: Context, photos: List<Photo>) {
        val photosWithSnippets = photos.filter { it.snippets.isNotEmpty() }
        val testId = photosWithSnippets.firstOrNull()?.id ?: TEST_NOTIFICATION_PHOTO_ID

        val workRequest = OneTimeWorkRequestBuilder<MemoryWorker>()
            .setInitialDelay(TEST_NOTIFICATION_DELAY_SECONDS, TimeUnit.SECONDS)
            .setInputData(
                workDataOf(
                    MemoryWorker.INPUT_PHOTO_ID to testId,
                    MemoryWorker.INPUT_NOTIFICATION_TYPE to MemoryWorker.TYPE_NEW
                )
            )
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}