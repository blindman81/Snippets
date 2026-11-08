package com.ln.android.snippets.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
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
    }
}
