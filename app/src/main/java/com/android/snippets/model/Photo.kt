package com.android.snippets.model

import android.net.Uri

data class Photo(
    val id: String = java.util.UUID.randomUUID().toString(),
    val uriString: String,
    val date: Long = System.currentTimeMillis(),
    val snippets: List<String> = emptyList(),
    val isViewed: Boolean = false,
    val lastViewedTime: Long = 0L,
    val snippetsAddedTime: Long = 0L,
    val isFavorite: Boolean = false,
    val isLibraryUpload: Boolean = true,
    val collections: List<String> = emptyList(),
    val widthPx: Int = 0,
    val heightPx: Int = 0,
    val isPublic: Boolean = false,
    val locationLink: String? = null
) {
    val uri: Uri get() = if (uriString.isNullOrEmpty()) Uri.EMPTY else Uri.parse(uriString)
    val captureDate: Long get() = date
    val aspectRatio: Float? get() = if (widthPx > 0 && heightPx > 0) widthPx.toFloat() / heightPx.toFloat() else null
}
