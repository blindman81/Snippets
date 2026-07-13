package com.android.snippets.ui.components

import android.os.Build
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider

/**
 * Applies hardware-accelerated background blur to the bottom sheet's dialog window.
 * Uses both FLAG_BLUR_BEHIND and setBackgroundBlurRadius for maximum compatibility.
 * Only active on Android 12+ (API 31+); no-ops on older versions.
 */
@Composable
fun BottomSheetWindowBlur(blurRadius: Int = 30) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

    val view = LocalView.current

    DisposableEffect(Unit) {
        val applyBlur = Runnable {
            val window = (view.parent as? DialogWindowProvider)?.window ?: return@Runnable
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.attributes = window.attributes.apply {
                blurBehindRadius = blurRadius
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window.setBackgroundBlurRadius(blurRadius)
            }
        }

        // Try immediately (parent may already be set)
        applyBlur.run()
        // Post to run after the view hierarchy is fully attached
        view.post(applyBlur)

        onDispose {
            view.removeCallbacks(applyBlur)
        }
    }
}
