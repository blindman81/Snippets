package com.android.snippets

import com.android.snippets.ui.components.calculateKeyboardOverlapOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class KeyboardOverlapTest {

    @Test
    fun testKeyboardClosed_returnsZero() {
        val offset = calculateKeyboardOverlapOffset(
            containerHeight = 2000,
            dialogHeight = 600,
            imeBottom = 0,
            statusBarTop = 100,
            marginPx = 48
        )
        assertEquals(0, offset)
    }

    @Test
    fun testKeyboardBelowDialog_returnsZero() {
        // containerHeight = 2000, dialogHeight = 600
        // dialogBottom = (2000 + 600) / 2 = 1300
        // margin = 48 -> threshold = 1348
        // keyboard height = 500 -> keyboardTop = 2000 - 500 = 1500
        // 1348 <= 1500, so keyboard does NOT overlap with the bottom of the dialog
        val offset = calculateKeyboardOverlapOffset(
            containerHeight = 2000,
            dialogHeight = 600,
            imeBottom = 500,
            statusBarTop = 100,
            marginPx = 48
        )
        assertEquals(0, offset)
    }

    @Test
    fun testKeyboardOverlapsDialogBottom_movesUpByOverlap() {
        // containerHeight = 2000, dialogHeight = 600
        // dialogBottom = 1300, margin = 48 -> target bottom = 1348
        // keyboard height = 900 -> keyboardTop = 2000 - 900 = 1100
        // overlap = 1348 - 1100 = 248
        val offset = calculateKeyboardOverlapOffset(
            containerHeight = 2000,
            dialogHeight = 600,
            imeBottom = 900,
            statusBarTop = 100,
            marginPx = 48
        )
        assertEquals(248, offset)
    }

    @Test
    fun testKeyboardVeryTall_clampedByStatusBar() {
        // containerHeight = 2000, dialogHeight = 600
        // dialogTop = (2000 - 600) / 2 = 700
        // statusBarTop = 100, margin = 48 -> min top = 148
        // maxMoveUp = 700 - 148 = 552
        // keyboard height = 1800 -> keyboardTop = 200
        // overlap = 1348 - 200 = 1148 (exceeds maxMoveUp)
        val offset = calculateKeyboardOverlapOffset(
            containerHeight = 2000,
            dialogHeight = 600,
            imeBottom = 1800,
            statusBarTop = 100,
            marginPx = 48
        )
        assertEquals(552, offset)
    }

    @Test
    fun testInvalidDimensions_returnsZero() {
        assertEquals(0, calculateKeyboardOverlapOffset(0, 600, 500, 100, 48))
        assertEquals(0, calculateKeyboardOverlapOffset(2000, 0, 500, 100, 48))
        assertEquals(0, calculateKeyboardOverlapOffset(2000, 600, -1, 100, 48))
    }
}
