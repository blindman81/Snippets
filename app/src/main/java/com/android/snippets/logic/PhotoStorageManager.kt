package com.android.snippets.logic

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

object PhotoStorageManager {

    fun extractCaptureDate(context: Context, uri: Uri): Long {
        var fallbackDate = 0L

        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val dateModifiedIndex = cursor.getColumnIndex(android.provider.MediaStore.MediaColumns.DATE_MODIFIED)
                    if (dateModifiedIndex != -1) {
                        val dateSeconds = cursor.getLong(dateModifiedIndex)
                        if (dateSeconds > 0) fallbackDate = dateSeconds * 1000L
                    }
                    if (fallbackDate == 0L) {
                        val docLastModIndex = cursor.getColumnIndex(android.provider.DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                        if (docLastModIndex != -1) {
                            val dateMs = cursor.getLong(docLastModIndex)
                            if (dateMs > 0) fallbackDate = dateMs
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        if (fallbackDate == 0L) fallbackDate = System.currentTimeMillis()

        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val exif = ExifInterface(input)
                val dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                    ?: exif.getAttribute(ExifInterface.TAG_DATETIME)

                if (dateTime != null) {
                    val format = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US)
                    format.parse(dateTime)?.time ?: fallbackDate
                } else {
                    fallbackDate
                }
            } ?: fallbackDate
        } catch (e: Exception) {
            e.printStackTrace()
            fallbackDate
        }
    }

    fun saveToInternalStorage(context: Context, uri: Uri): String? {
        val photosDir = File(context.filesDir, "photos")
        if (!photosDir.exists()) photosDir.mkdirs()

        val fileName = "photo_${System.currentTimeMillis()}.jpg"
        val localFile = File(photosDir, fileName)

        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(localFile).use { output ->
                    input.copyTo(output)
                }
            }
            localFile.absolutePath.let { if (it.startsWith("/")) "file://$it" else it }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deletePhotoFile(uriString: String) {
        try {
            if (uriString.startsWith("file://") || uriString.contains("/files/photos/")) {
                val path = if (uriString.startsWith("file://")) {
                    uriString.substring(7)
                } else {
                    uriString
                }
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            }
        } catch (_: Exception) {}
    }

    fun extractImageDimensions(context: Context, uri: Uri): Pair<Int, Int> {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            val path = uri.path
            var orientation = ExifInterface.ORIENTATION_NORMAL
            if (path != null && !uri.toString().startsWith("content:")) {
                val file = File(path)
                if (file.exists()) {
                    java.io.FileInputStream(file).use { input ->
                        BitmapFactory.decodeStream(input, null, options)
                    }
                    orientation = ExifInterface(file.absolutePath).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                } else {
                    val resolver = context.contentResolver
                    resolver.openInputStream(uri)?.use { input -> BitmapFactory.decodeStream(input, null, options) }
                    resolver.openInputStream(uri)?.use { input ->
                        orientation = ExifInterface(input).getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                    }
                }
            } else {
                val resolver = context.contentResolver
                resolver.openInputStream(uri)?.use { input -> BitmapFactory.decodeStream(input, null, options) }
                resolver.openInputStream(uri)?.use { input ->
                    orientation = ExifInterface(input).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                }
            }
            var width = options.outWidth.takeIf { it > 0 } ?: 0
            var height = options.outHeight.takeIf { it > 0 } ?: 0
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90 ||
                orientation == ExifInterface.ORIENTATION_ROTATE_270 ||
                orientation == ExifInterface.ORIENTATION_TRANSPOSE ||
                orientation == ExifInterface.ORIENTATION_TRANSVERSE
            ) {
                val temp = width
                width = height
                height = temp
            }
            width to height
        } catch (_: Exception) {
            0 to 0
        }
    }

    fun healFileUris(context: Context, photos: List<com.android.snippets.model.Photo>): Pair<List<com.android.snippets.model.Photo>, Boolean> {
        val photosDir = File(context.filesDir, "photos")
        var healingNeeded = false
        val healed = photos.map { p ->
            var effUri = p.uriString
            if (effUri.startsWith("file://") || effUri.startsWith("/")) {
                val rawPath = if (effUri.startsWith("file://")) effUri.substring(7) else effUri
                val f = File(rawPath)
                if (!f.exists()) {
                    val candidate = File(photosDir, f.name)
                    if (candidate.exists()) {
                        effUri = "file://${candidate.absolutePath}"
                        healingNeeded = true
                    }
                }
            }
            if (effUri != p.uriString) p.copy(uriString = effUri) else p
        }
        return healed to healingNeeded
    }

    fun healContentUris(context: Context, photos: List<com.android.snippets.model.Photo>): Pair<List<com.android.snippets.model.Photo>, Boolean> {
        val brokenPhotos = photos.filter { it.uriString.startsWith("content://") }
        if (brokenPhotos.isEmpty()) return photos to false

        var changed = false
        val updatedList = photos.toMutableList()
        brokenPhotos.forEach { p ->
            val healedUri = saveToInternalStorage(context, Uri.parse(p.uriString))
            if (healedUri != null) {
                val (newW, newH) = extractImageDimensions(context, Uri.parse(healedUri))
                val index = updatedList.indexOfFirst { it.id == p.id }
                if (index != -1) {
                    updatedList[index] = updatedList[index].copy(uriString = healedUri, widthPx = newW, heightPx = newH)
                    changed = true
                }
            }
        }
        return updatedList to changed
    }

    fun backfillMissingDimensions(context: Context, photos: List<com.android.snippets.model.Photo>): Pair<List<com.android.snippets.model.Photo>, Boolean> {
        val missing = photos.filter { it.widthPx <= 0 || it.heightPx <= 0 }
        if (missing.isEmpty()) return photos to false

        val updated = photos.toMutableList()
        var changed = false
        missing.forEach { item ->
            val (w, h) = extractImageDimensions(context, Uri.parse(item.uriString))
            if (w > 0 && h > 0) {
                val idx = updated.indexOfFirst { it.id == item.id }
                if (idx != -1) {
                    updated[idx] = updated[idx].copy(widthPx = w, heightPx = h)
                    changed = true
                }
            }
        }
        return updated to changed
    }
}
