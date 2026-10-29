package com.android.snippets.ui.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.widget.Toast
import com.android.snippets.model.Photo
import java.util.ArrayList

object PhotoCardExporter {

    suspend fun downloadPhotoCard(
        context: Context,
        photo: Photo,
        pureSnippets: List<String>,
        isDark: Boolean,
        bgColor: Int
    ): Boolean {
        val locationText = LocationUtils.getLocationFromExif(context, photo)
        val success = MediaSaver.saveSnippetToGallery(
            context = context,
            photo = photo,
            snippets = pureSnippets,
            isDark = isDark,
            bgColor = bgColor,
            showTime = true,
            locationText = locationText
        )
        if (success) {
            Toast.makeText(context, "Downloaded to Gallery", Toast.LENGTH_SHORT).show()
        }
        return success
    }

    suspend fun sharePhotoCard(
        context: Context,
        photo: Photo,
        pureSnippets: List<String>,
        isDark: Boolean,
        bgColor: Int
    ) {
        val locationText = LocationUtils.getLocationFromExif(context, photo)
        val uri = MediaSaver.getShareableUri(
            context = context,
            photo = photo,
            snippets = pureSnippets,
            isDark = isDark,
            bgColor = bgColor,
            showTime = true,
            locationText = locationText
        )
        if (uri != null) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newRawUri("", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Snippets"))
        }
    }

    suspend fun shareMultipleCards(
        context: Context,
        photosToShare: List<Pair<Photo, List<String>>>,
        isDark: Boolean
    ): Boolean {
        if (photosToShare.isEmpty()) return false

        Toast.makeText(context, "Preparing shared snippets...", Toast.LENGTH_SHORT).show()
        val uris = mutableListOf<Uri>()

        for ((photo, pureSnippets) in photosToShare) {
            val locationText = LocationUtils.getLocationFromExif(context, photo)
            val uri = MediaSaver.getShareableUri(
                context = context,
                photo = photo,
                snippets = pureSnippets,
                isDark = isDark,
                bgColor = if (isDark) Color.BLACK else Color.WHITE,
                showTime = true,
                locationText = locationText
            )
            if (uri != null) {
                uris.add(uri)
            }
        }

        if (uris.isNotEmpty()) {
            val intent = if (uris.size == 1) {
                Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, uris.first())
                    clipData = ClipData.newRawUri("", uris.first())
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } else {
                Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "image/jpeg"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                    val clipDataItems = ClipData.newRawUri("", uris.first())
                    for (i in 1 until uris.size) {
                        clipDataItems.addItem(ClipData.Item(uris[i]))
                    }
                    clipData = clipDataItems
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
            context.startActivity(Intent.createChooser(intent, "Share Snippets"))
            return true
        } else {
            Toast.makeText(context, "Failed to prepare share images", Toast.LENGTH_SHORT).show()
            return false
        }
    }
}
