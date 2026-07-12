package com.android.snippets.ui.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.android.snippets.model.Photo
import java.util.Locale
import android.os.Build
import android.provider.MediaStore

object LocationUtils {

    fun getLocationFromExif(context: Context, photo: Photo): String? {
        if (!photo.locationName.isNullOrBlank()) {
            return cleanLocationName(photo.locationName)
        }
        if (!photo.locationLink.isNullOrBlank()) {
            val nameFromLink = extractPlaceNameFromLink(photo.locationLink)
            if (nameFromLink != null) return cleanLocationName(nameFromLink)
            if (!photo.locationLink.startsWith("http")) {
                return cleanLocationName(photo.locationLink)
            }
        }
        return null
    }

    /**
     * Cleans a location name to show only the main/primary name.
     * Strips descriptive suffixes commonly found in Google Maps names like:
     * - "XYZ Best Gym in XYZ Colony" -> "XYZ Best Gym"
     * - "ABC Restaurant - Fine Dining" -> "ABC Restaurant"
     * - "DEF Cafe, MG Road" -> "DEF Cafe"
     * - "GHI Store | Branch 2" -> "GHI Store"
     */
    fun cleanLocationName(name: String): String {
        var cleaned = name.trim()

        // Split on common delimiters and take the first part
        val delimiters = listOf(" - ", " | ", " · ", " — ", " – ")
        for (delimiter in delimiters) {
            val idx = cleaned.indexOf(delimiter)
            if (idx > 0) {
                cleaned = cleaned.substring(0, idx).trim()
            }
        }

        // Remove trailing descriptive phrases starting with common prepositions
        val prepositionPatterns = listOf(
            """\s+in\s+.+$""",
            """\s+at\s+.+$""",
            """\s+near\s+.+$""",
            """\s+on\s+.+$""",
            """\s+opp\.?\s+.+$""",
            """\s+opposite\s+.+$""",
            """\s+beside\s+.+$""",
            """\s+next\s+to\s+.+$"""
        )
        for (pattern in prepositionPatterns) {
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            val match = regex.find(cleaned)
            if (match != null && match.range.first > 2) {
                cleaned = cleaned.substring(0, match.range.first).trim()
            }
        }

        // Remove trailing comma-separated address portions (e.g., ", MG Road, Bangalore")
        val commaIdx = cleaned.indexOf(',')
        if (commaIdx > 2) {
            cleaned = cleaned.substring(0, commaIdx).trim()
        }

        return cleaned.ifBlank { name.trim() }
    }

    /**
     * Extracts a place name from a Google Maps URL.
     * Handles URLs like:
     * - https://www.google.com/maps/place/XYZ+Best+Gym/...
     * - https://maps.app.goo.gl/...
     * - https://goo.gl/maps/...
     */
    fun extractPlaceNameFromLink(link: String): String? {
        try {
            val uri = Uri.parse(link)
            // Handle standard Google Maps place URLs
            val path = uri.path ?: return null
            val placePrefix = "/maps/place/"
            val placeIdx = path.indexOf(placePrefix)
            if (placeIdx >= 0) {
                val afterPlace = path.substring(placeIdx + placePrefix.length)
                val nameEncoded = afterPlace.split("/").firstOrNull()?.takeIf { it.isNotBlank() } ?: return null
                // Decode URL-encoded name: "XYZ+Best+Gym" -> "XYZ Best Gym"
                val decoded = java.net.URLDecoder.decode(nameEncoded, "UTF-8")
                return decoded.ifBlank { null }
            }
        } catch (e: Exception) {
            // ignore parsing errors
        }
        return null
    }

    fun extractCoordinates(context: Context, photo: Photo): Pair<Double, Double>? {
        photo.locationLink?.let { link ->
            val parsed = parseCoordinatesFromLink(link)
            if (parsed != null) return parsed
        }
        
        try {
            if (!photo.uriString.isNullOrBlank()) {
                val uri = photo.uri
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val exif = ExifInterface(stream)
                    val latLong = exif.latLong
                    if (latLong != null && latLong.size >= 2) {
                        val lat = latLong[0]
                        val lng = latLong[1]
                        if (lat != 0.0 || lng != 0.0) {
                            return Pair(lat, lng)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return null
    }

    private fun parseCoordinatesFromLink(link: String): Pair<Double, Double>? {
        val regex = """(-?\d+\.\d+)\s*,\s*(-?\d+\.\d+)""".toRegex()
        val matchResult = regex.find(link)
        if (matchResult != null) {
            try {
                val lat = matchResult.groupValues[1].toDoubleOrNull()
                val lng = matchResult.groupValues[2].toDoubleOrNull()
                if (lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0) {
                    return Pair(lat, lng)
                }
            } catch (e: Exception) {
                // ignore
            }
        }
        return null
    }
}
