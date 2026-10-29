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
    const val VIEWED_MEMORY_RESET_DAYS = 3L
    const val SURFACED_MEMORY_SPACING_HOURS = 3L
    const val TEST_NOTIFICATION_DELAY_SECONDS = 5L
    const val TEST_NOTIFICATION_PHOTO_ID = "test_notification"
    const val CYCLE_WORK_NAME = "memory_cycle_work"

    val CYCLE_WINDOW_MS: Long = TimeUnit.HOURS.toMillis(CYCLE_WINDOW_HOURS)
    val NEW_MEMORY_WAIT_MS: Long = TimeUnit.HOURS.toMillis(NEW_MEMORY_NOTIFICATION_DELAY_HOURS)
    val VIEWED_MEMORY_VISIBLE_MS: Long = TimeUnit.HOURS.toMillis(VIEWED_MEMORY_VISIBLE_HOURS)
    val RECENTLY_VIEWED_MEMORY_MS: Long = TimeUnit.HOURS.toMillis(RECENTLY_VIEWED_MEMORY_HOURS)
    val VIEWED_MEMORY_RESET_MS: Long = TimeUnit.DAYS.toMillis(VIEWED_MEMORY_RESET_DAYS)
    val SURFACED_MEMORY_SPACING_MS: Long = TimeUnit.HOURS.toMillis(SURFACED_MEMORY_SPACING_HOURS)

    fun getCurrentCycleStart(now: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = now }
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
        return false
    }

    fun reconcileSurfacedMemories(
        photos: List<Photo>,
        now: Long = System.currentTimeMillis()
    ): Pair<List<Photo>, List<Pair<String, Long>>>? {
        var changed = false
        val scheduledNotifications = mutableListOf<Pair<String, Long>>()
        val currentCycleStart = getCurrentCycleStart(now)
        val previousCycleStart = getPreviousCycleStart(now)

        val updatedPhotos = photos.map { photo ->
            if (shouldResurfaceMemory(photo, now) || (photo.isViewed && photo.snippetsAddedTime > photo.lastViewedTime)) {
                changed = true
                photo.copy(isViewed = false, lastViewedTime = 0L, surfacedTime = 0L)
            } else photo
        }.toMutableList()

        // 1. Current cycle allocation: ensure up to 5 memories are active
        val currentCycleCount = updatedPhotos.count { photo ->
            photo.isLibraryUpload &&
                    photo.snippets.isNotEmpty() &&
                    photo.surfacedTime == currentCycleStart
        }
        if (currentCycleCount < MEMORIES_PER_CYCLE) {
            val neededCurrent = MEMORIES_PER_CYCLE - currentCycleCount
            val freshCurrent = updatedPhotos.filter { photo ->
                photo.isLibraryUpload &&
                        photo.snippets.isNotEmpty() &&
                        photo.snippetsAddedTime != 0L &&
                        !photo.isViewed &&
                        photo.surfacedTime == 0L
            }.sortedByDescending { it.snippetsAddedTime }.take(neededCurrent)

            for (candidate in freshCurrent) {
                val index = updatedPhotos.indexOfFirst { it.id == candidate.id }
                if (index != -1) {
                    updatedPhotos[index] = updatedPhotos[index].copy(surfacedTime = currentCycleStart)
                    if (currentCycleStart > now) {
                        scheduledNotifications.add(candidate.id to currentCycleStart)
                    }
                    changed = true
                }
            }
        }

        // 2. Previous cycle allocation: ensure previous cycle has up to 5 memories so the sheet has up to 10
        val previousCycleCount = updatedPhotos.count { photo ->
            photo.isLibraryUpload &&
                    photo.snippets.isNotEmpty() &&
                    photo.surfacedTime == previousCycleStart
        }
        if (previousCycleCount < MEMORIES_PER_CYCLE) {
            val neededPrevious = MEMORIES_PER_CYCLE - previousCycleCount
            val freshPrevious = updatedPhotos.filter { photo ->
                photo.isLibraryUpload &&
                        photo.snippets.isNotEmpty() &&
                        photo.snippetsAddedTime != 0L &&
                        !photo.isViewed &&
                        photo.surfacedTime == 0L
            }.sortedByDescending { it.snippetsAddedTime }.take(neededPrevious)

            for (candidate in freshPrevious) {
                val index = updatedPhotos.indexOfFirst { it.id == candidate.id }
                if (index != -1) {
                    updatedPhotos[index] = updatedPhotos[index].copy(surfacedTime = previousCycleStart)
                    changed = true
                }
            }
        }

        // 3. Future queue: all remaining unallocated photos queue for upcoming cycles (9 AM / 9 PM)
        val remainingQueued = updatedPhotos.filter { photo ->
            photo.isLibraryUpload &&
                    photo.snippets.isNotEmpty() &&
                    photo.snippetsAddedTime != 0L &&
                    photo.surfacedTime == 0L
        }.sortedByDescending { it.snippetsAddedTime }

        if (remainingQueued.isNotEmpty()) {
            var nextCycle = getNextCycleStart(now)
            var countInNextCycle = updatedPhotos.count { it.surfacedTime == nextCycle }
            for (candidate in remainingQueued) {
                if (countInNextCycle >= MEMORIES_PER_CYCLE) {
                    nextCycle = getNextCycleStart(nextCycle)
                    countInNextCycle = updatedPhotos.count { it.surfacedTime == nextCycle }
                }
                val index = updatedPhotos.indexOfFirst { it.id == candidate.id }
                if (index != -1) {
                    updatedPhotos[index] = updatedPhotos[index].copy(surfacedTime = nextCycle)
                    if (nextCycle > now) {
                        scheduledNotifications.add(candidate.id to nextCycle)
                    }
                    countInNextCycle++
                    changed = true
                }
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
                    photo.surfacedTime <= now &&
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
        val nextCycle = getNextCycleStart(now)
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