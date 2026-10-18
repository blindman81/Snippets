package com.android.snippets

import android.content.Context
import com.android.snippets.logic.MemoryWorker
import com.android.snippets.model.Photo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MemoryNotificationTest {

    @Test
    fun testNotificationDateFormatProducesExpectedFormat() {
        // Create a fixed date: Wednesday, 18 Oct 2026
        val calendar = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 18, 12, 0, 0)
        }
        val formatted = SimpleDateFormat(MemoryWorker.NOTIFICATION_DATE_FORMAT, Locale.US)
            .format(calendar.time)

        assertEquals("Sun, 18 Oct", formatted)

        // Specifically test a Wednesday date (e.g. Oct 21, 2026)
        val wedCalendar = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 21, 12, 0, 0)
        }
        val wedFormatted = SimpleDateFormat(MemoryWorker.NOTIFICATION_DATE_FORMAT, Locale.US)
            .format(wedCalendar.time)

        assertEquals("Wed, 21 Oct", wedFormatted)
    }

    @Test
    fun testPhotoNameInTitleWithLocation() {
        val calendar = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 18, 12, 0, 0)
        }
        val photo = Photo(
            uriString = "",
            date = calendar.timeInMillis,
            name = "Picnic in the Park",
            locationName = "Central Park, New York"
        )

        val (title, text) = MemoryWorker.buildNotificationMetadata(null, photo, MemoryWorker.TYPE_NEW)

        assertEquals("Picnic in the Park • Central Park", title)
        assertTrue(text.contains("Oct"))
    }

    @Test
    fun testFallbackToDayAndDateWhenNoPhotoNameWithLocation() {
        val calendar = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 18, 12, 0, 0)
        }
        val expectedDate = SimpleDateFormat(MemoryWorker.NOTIFICATION_DATE_FORMAT, Locale.getDefault())
            .format(calendar.time)

        val photo = Photo(
            uriString = "",
            date = calendar.timeInMillis,
            name = null,
            locationName = "Golden Gate Bridge"
        )

        val (title, _) = MemoryWorker.buildNotificationMetadata(null, photo, MemoryWorker.TYPE_NEW)

        assertEquals("$expectedDate • Golden Gate Bridge", title)
    }

    @Test
    fun testPhotoNameWithoutLocation() {
        val photo = Photo(
            uriString = "",
            name = "Graduation Day",
            locationName = null
        )

        val (title, _) = MemoryWorker.buildNotificationMetadata(null, photo, MemoryWorker.TYPE_NEW)

        assertEquals("Graduation Day", title)
    }

    @Test
    fun testFallbackToDayAndDateWithoutLocation() {
        val calendar = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 18, 12, 0, 0)
        }
        val expectedDate = SimpleDateFormat(MemoryWorker.NOTIFICATION_DATE_FORMAT, Locale.getDefault())
            .format(calendar.time)

        val photo = Photo(
            uriString = "",
            date = calendar.timeInMillis,
            name = "",
            locationName = null
        )

        val (title, _) = MemoryWorker.buildNotificationMetadata(null, photo, MemoryWorker.TYPE_NEW)

        assertEquals(expectedDate, title)
    }

    @Test
    fun testNullPhotoFallback() {
        val (title, text) = MemoryWorker.buildNotificationMetadata(null, null, MemoryWorker.TYPE_NEW)
        assertEquals("New memory", title)
        assertEquals("Relive this memory", text)
    }

    @Test
    fun testPhotoWithSnippetsInBody() {
        val calendar = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 18, 12, 0, 0)
        }
        val expectedDate = SimpleDateFormat(MemoryWorker.NOTIFICATION_DATE_FORMAT, Locale.getDefault())
            .format(calendar.time)

        val photo = Photo(
            uriString = "",
            date = calendar.timeInMillis,
            name = null,
            locationName = "Central Park",
            snippets = listOf("Picnic with friends", "Sunny afternoon")
        )

        val (title, text) = MemoryWorker.buildNotificationMetadata(null, photo, MemoryWorker.TYPE_NEW)

        assertEquals("$expectedDate • Central Park", title)
        assertEquals("Picnic with friends • Sunny afternoon", text)
    }

    @Test
    fun testPhotoWithSnippetsAndCustomName() {
        val calendar = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 18, 12, 0, 0)
        }
        val expectedDate = SimpleDateFormat(MemoryWorker.NOTIFICATION_DATE_FORMAT, Locale.getDefault())
            .format(calendar.time)

        val photo = Photo(
            uriString = "",
            date = calendar.timeInMillis,
            name = "Summer Trip",
            locationName = "Miami Beach",
            snippets = listOf("Swimming in the ocean")
        )

        val (title, text) = MemoryWorker.buildNotificationMetadata(null, photo, MemoryWorker.TYPE_NEW)

        assertEquals("Summer Trip • Miami Beach", title)
        assertEquals("$expectedDate • Swimming in the ocean", text)
    }

    @Test
    fun testNoSnippetsNoNameFallbackTextDoesNotContainOldGenericString() {
        val calendar = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 18, 12, 0, 0)
        }
        val photo = Photo(
            uriString = "",
            date = calendar.timeInMillis,
            name = null,
            locationName = null,
            snippets = emptyList(),
            isFavorite = false,
            rating = 0
        )

        val (_, text) = MemoryWorker.buildNotificationMetadata(null, photo, MemoryWorker.TYPE_NEW)
        assertEquals("Relive this memory", text)
    }

    @Test
    fun testGroupKeyAndSummaryIdConstants() {
        assertEquals("com.android.snippets.MEMORIES", MemoryWorker.GROUP_KEY)
        assertEquals(0, MemoryWorker.SUMMARY_NOTIFICATION_ID)
    }
}
