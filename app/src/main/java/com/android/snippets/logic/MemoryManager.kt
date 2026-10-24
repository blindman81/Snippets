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

    fun shouldResurfaceMemory(photo: Photo, now: Long = System.currentTimeMillis()): Boolean {
        if (!photo.isLibraryUpload || photo.snippets.isEmpty() || photo.snippetsAddedTime == 0L) {
            return false
        }
        if (photo.surfacedTime != 0L && photo.surfacedTime > now) {
            return false
        }
        if (photo.isViewed) {
            return now - photo.lastViewedTime >= VIEWED_MEMORY_RESET_MS
        }
        return photo.surfacedTime != 0L &&
                now - photo.surfacedTime >= VIEWED_MEMORY_RESET_MS + VIEWED_MEMORY_VISIBLE_MS
    }

    fun reconcileSurfacedMemories(
        photos: List<Photo>,
        now: Long = System.currentTimeMillis()
    ): Pair<List<Photo>, List<Pair<String, Long>>>? {
        var changed = false
        val scheduledNotifications = mutableListOf<Pair<String, Long>>()
        val currentCycleStart = getCurrentCycleStart(now)

        val updatedPhotos = photos.map { photo ->
            if (shouldResurfaceMemory(photo, now)) {
                changed = true
                photo.copy(isViewed = false, lastViewedTime = 0L, surfacedTime = 0L)
            } else photo
        }.toMutableList()

        // Count memories active in the current cycle
        val activeCountInCycle = updatedPhotos.count { photo ->
            photo.isLibraryUpload &&
                    photo.snippets.isNotEmpty() &&
                    photo.surfacedTime != 0L &&
                    photo.surfacedTime <= now &&
                    (now - photo.surfacedTime < CYCLE_WINDOW_MS)
        }

        val limit = MEMORIES_PER_CYCLE
        if (activeCountInCycle < limit) {
            val needed = limit - activeCountInCycle

            // 1. Fresh unviewed photos (newly taken / added photos)
            val freshCandidates = updatedPhotos.filter { photo ->
                photo.isLibraryUpload &&
                        photo.snippets.isNotEmpty() &&
                        photo.snippetsAddedTime != 0L &&
                        !photo.isViewed &&
                        photo.surfacedTime == 0L
            }.sortedByDescending { it.snippetsAddedTime }

            val chosenFresh = freshCandidates.take(needed)
            for (candidate in chosenFresh) {
                val index = updatedPhotos.indexOfFirst { it.id == candidate.id }
                if (index != -1) {
                    updatedPhotos[index] = updatedPhotos[index].copy(surfacedTime = currentCycleStart)
                    scheduledNotifications.add(candidate.id to currentCycleStart)
                    changed = true
                }
            }

            val remainingNeeded = needed - chosenFresh.size
            if (remainingNeeded > 0) {
                // 2. Photos with snippets updated after being viewed
                val updatedSnippetCandidates = updatedPhotos.filter { photo ->
                    photo.isLibraryUpload &&
                            photo.snippets.isNotEmpty() &&
                            photo.snippetsAddedTime != 0L &&
                            photo.snippetsAddedTime > photo.lastViewedTime &&
                            photo.surfacedTime == 0L
                }.sortedByDescending { it.snippetsAddedTime }

                val chosenUpdated = updatedSnippetCandidates.take(remainingNeeded)
                for (candidate in chosenUpdated) {
                    val index = updatedPhotos.indexOfFirst { it.id == candidate.id }
                    if (index != -1) {
                        updatedPhotos[index] = updatedPhotos[index].copy(isViewed = false, surfacedTime = currentCycleStart)
                        scheduledNotifications.add(candidate.id to currentCycleStart)
                        changed = true
                    }
                }

                val stillNeeded = remainingNeeded - chosenUpdated.size
                if (stillNeeded > 0) {
                    // 3. Recycle expired memories to fill remaining slots
                    val expiredCandidates = updatedPhotos.filter { photo ->
                        photo.isLibraryUpload &&
                                photo.snippets.isNotEmpty() &&
                                photo.snippetsAddedTime != 0L &&
                                photo.surfacedTime != 0L &&
                                photo.surfacedTime <= now &&
                                (now - photo.surfacedTime >= CYCLE_WINDOW_MS) &&
                                (!photo.isViewed || (now - photo.lastViewedTime >= CYCLE_WINDOW_MS))
                    }.sortedBy { if (it.isViewed) it.lastViewedTime else it.surfacedTime }
                        .take(stillNeeded)

                    for (expired in expiredCandidates) {
                        val index = updatedPhotos.indexOfFirst { it.id == expired.id }
                        if (index != -1) {
                            updatedPhotos[index] = updatedPhotos[index].copy(isViewed = false, lastViewedTime = 0L, surfacedTime = currentCycleStart)
                            scheduledNotifications.add(expired.id to currentCycleStart)
                            changed = true
                        }
                    }
                }
            }
        }

        // Queue remaining unscheduled candidates into future cycles (9 am / 9 pm)
        val remainingQueued = updatedPhotos.filter { photo ->
            photo.isLibraryUpload &&
                    photo.snippets.isNotEmpty() &&
                    photo.snippetsAddedTime != 0L &&
                    (!photo.isViewed || photo.snippetsAddedTime > photo.lastViewedTime) &&
                    photo.surfacedTime == 0L
        }.sortedByDescending { it.snippetsAddedTime }

        if (remainingQueued.isNotEmpty()) {
            var nextCycle = getNextCycleStart(now)
            var countInNextCycle = updatedPhotos.count { it.surfacedTime == nextCycle }
            for (candidate in remainingQueued) {
                if (countInNextCycle >= limit) {
                    nextCycle = getNextCycleStart(nextCycle)
                    countInNextCycle = updatedPhotos.count { it.surfacedTime == nextCycle }
                }
                val index = updatedPhotos.indexOfFirst { it.id == candidate.id }
                if (index != -1) {
                    updatedPhotos[index] = updatedPhotos[index].copy(surfacedTime = nextCycle)
                    scheduledNotifications.add(candidate.id to nextCycle)
                    countInNextCycle++
                    changed = true
                }
            }
        }

        return if (changed) Pair(updatedPhotos, scheduledNotifications) else null
    }

    fun getCuratedMemories(photos: List<Photo>, now: Long = System.currentTimeMillis()): List<Photo> {
        return photos.filter { photo ->
            photo.isLibraryUpload &&
                    photo.snippets.isNotEmpty() &&
                    photo.snippetsAddedTime != 0L &&
                    photo.surfacedTime != 0L &&
                    photo.surfacedTime <= now &&
                    (now - photo.surfacedTime < CYCLE_WINDOW_MS) &&
                    (!photo.isViewed || photo.snippetsAddedTime > photo.lastViewedTime || (now - photo.lastViewedTime < CYCLE_WINDOW_MS))
        }.sortedWith(
            compareByDescending<Photo> {
                !it.isViewed
            }.thenByDescending {
                it.isViewed && (now - it.lastViewedTime < CYCLE_WINDOW_MS)
            }.thenByDescending {
                if (it.isViewed) it.lastViewedTime else it.surfacedTime
            }
        ).take(MEMORIES_PER_CYCLE)
            .sortedByDescending { it.date }
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

    fun scheduleNewMemoryNotification(context: Context, photoId: String) {
        scheduleMemoryNotification(
            context = context,
            photoId = photoId,
            delay = NEW_MEMORY_NOTIFICATION_DELAY_HOURS,
            delayUnit = TimeUnit.HOURS,
            notificationType = MemoryWorker.TYPE_NEW
        )
    }

    fun scheduleUpdatedMemoryNotification(context: Context, photoId: String) {
        scheduleMemoryNotification(
            context = context,
            photoId = photoId,
            delay = NEW_MEMORY_NOTIFICATION_DELAY_HOURS,
            delayUnit = TimeUnit.HOURS,
            notificationType = MemoryWorker.TYPE_UPDATED
        )
    }

    fun cancelMemoryNotification(context: Context, photoId: String) {
        if (photoId.isBlank()) return
        val reminderName = memoryReminderName(photoId)
        WorkManager.getInstance(context).cancelUniqueWork(reminderName)
        WorkManager.getInstance(context).cancelAllWorkByTag(reminderName)
        MemoryWorker.cancelPostedNotification(context, photoId)
    }

    fun reconcileMemoryNotifications(
        context: Context,
        photos: List<Photo>
    ) {
        val now = System.currentTimeMillis()
        val unpostedPhotos = photos
            .filter { !it.isViewed || it.snippetsAddedTime > it.lastViewedTime }
            .filterNot { MemoryWorker.wasNotificationPosted(context, it.id) }
            .filter { it.surfacedTime != 0L }
            .sortedBy { it.surfacedTime }

        unpostedPhotos.forEach { photo ->
            val delayMs = maxOf(0L, photo.surfacedTime - now)
            val notificationType = if (photo.isViewed) {
                MemoryWorker.TYPE_UPDATED
            } else if (photo.snippetsAddedTime > photo.lastViewedTime && photo.lastViewedTime != 0L) {
                MemoryWorker.TYPE_RESURFACED
            } else {
                MemoryWorker.TYPE_NEW
            }
            scheduleMemoryNotification(
                context = context,
                photoId = photo.id,
                delay = delayMs,
                delayUnit = TimeUnit.MILLISECONDS,
                notificationType = notificationType,
                policy = ExistingWorkPolicy.REPLACE,
                resetPostedState = false
            )
        }
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