package com.ln.android.snippets.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates Baseline Profiles for [com.ln.android.snippets].
 *
 * Run this generator using:
 * `./gradlew :app:generateBaselineProfile`
 *
 * Running this test requires an Android test device or emulator (API 33+ or rooted).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() = baselineProfileRule.collect(
        packageName = "com.ln.android.snippets",
        includeInStartupProfile = true
    ) {
        // Cold app startup journey
        pressHome()
        startActivityAndWait()

        // Wait for feed content to load
        device.waitForIdle()
        device.wait(Until.hasObject(By.pkg("com.ln.android.snippets").depth(0)), 5000)

        // Scroll the photo feed to trace grid measure, layout, and Coil image decode
        val feed = device.findObject(By.scrollable(true))
        if (feed != null) {
            feed.setGestureMargin(device.displayWidth / 5)
            feed.fling(Direction.DOWN)
            device.waitForIdle()
            feed.fling(Direction.UP)
            device.waitForIdle()
        }

        // Tap the Favorites tab to compile tab transition and filtered feed rendering
        val favoritesTab = device.findObject(By.text("Favorites"))
        favoritesTab?.click()
        device.waitForIdle()

        // Return to Library tab
        val libraryTab = device.findObject(By.text("Library"))
        libraryTab?.click()
        device.waitForIdle()
    }
}
