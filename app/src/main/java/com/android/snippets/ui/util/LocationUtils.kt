package com.android.snippets.ui.util

import android.content.Context
import com.android.snippets.model.Photo

object LocationUtils {

    fun getLocationFromExif(context: Context? = null, photo: Photo): String? =
        ExifLocationHelper.getLocationFromExif(context, photo)

    fun cleanLocationName(name: String): String =
        LocationNameCleaner.cleanLocationName(name)

    fun extractPlaceNameFromLink(link: String?, context: Context? = null): String? =
        GoogleMapsUrlResolver.extractPlaceNameFromLink(link, context)

    fun isValidPlaceName(name: String): Boolean =
        GoogleMapsUrlResolver.isValidPlaceName(name)

    fun resolveUrlLocation(urlString: String, context: Context? = null): String? =
        GoogleMapsUrlResolver.resolveUrlLocation(urlString, context)

    fun extractCoordinates(context: Context, photo: Photo): Pair<Double, Double>? =
        ExifLocationHelper.extractCoordinates(context, photo)

    fun buildLocationNameMap(photos: List<Photo>): Map<String, String> {
        val map = mutableMapOf<String, String>()
        photos.forEach { p ->
            val link = p.locationLink?.trim()?.takeIf { it.isNotBlank() }
            val name = p.locationName?.trim()?.takeIf { it.isNotBlank() }
                ?: link?.let { extractPlaceNameFromLink(it) }
            if (link != null && name != null) {
                val clean = cleanLocationName(name)
                if (clean.isNotBlank()) {
                    map[link] = clean
                }
            }
        }
        return map
    }

    fun buildUniqueLocations(photos: List<Photo>): List<String> {
        val nameToLink = mutableMapOf<String, String>()
        photos.forEach { p ->
            val link = p.locationLink?.trim()?.takeIf { it.isNotBlank() }
            val name = p.locationName?.trim()?.takeIf { it.isNotBlank() }
                ?: link?.let { extractPlaceNameFromLink(it) }
            if (link != null && name != null) {
                val clean = cleanLocationName(name)
                if (clean.isNotBlank()) {
                    nameToLink.putIfAbsent(clean.lowercase(), link)
                }
            }
        }

        val counts = mutableMapOf<String, Int>()
        photos.forEach { p ->
            val link = p.locationLink?.trim()?.takeIf { it.isNotBlank() }
            val rawName = p.locationName?.trim()?.takeIf { it.isNotBlank() }
            val cleanName = rawName?.let { cleanLocationName(it) }

            val key = when {
                link != null -> link
                cleanName != null -> nameToLink[cleanName.lowercase()] ?: cleanName
                else -> null
            }

            if (!key.isNullOrBlank()) {
                counts[key] = (counts[key] ?: 0) + 1
            }
        }

        photos.forEach { p ->
            val rawName = p.locationName?.trim()?.takeIf { it.isNotBlank() }
            if (rawName != null) {
                val clean = cleanLocationName(rawName)
                val key = nameToLink[clean.lowercase()] ?: clean
                if (key.isNotBlank() && !counts.containsKey(key)) {
                    counts[key] = 1
                }
            }
        }

        return counts.entries
            .sortedWith(
                compareByDescending<Map.Entry<String, Int>> { it.value }
                    .thenBy { it.key.lowercase() }
            )
            .map { it.key }
    }
}
