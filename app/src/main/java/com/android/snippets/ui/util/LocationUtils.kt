package com.android.snippets.ui.util

import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.android.snippets.model.Photo
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder

object LocationUtils {

    fun getLocationFromExif(context: Context? = null, photo: Photo): String? {
        if (!photo.locationName.isNullOrBlank()) {
            return cleanLocationName(photo.locationName)
        }
        if (!photo.locationLink.isNullOrBlank()) {
            val nameFromLink = extractPlaceNameFromLink(photo.locationLink)
            if (nameFromLink != null) return cleanLocationName(nameFromLink)
            if (!photo.locationLink.startsWith("http://", ignoreCase = true) && !photo.locationLink.startsWith("https://", ignoreCase = true)) {
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
     * - "Central Park - Google Maps" -> "Central Park"
     */
    fun cleanLocationName(name: String): String {
        var cleaned = name.trim()

        // Strip Google Maps branding suffixes
        val mapSuffixes = listOf(
            Regex("""\s*[-–—·|]\s*Google\s+Maps.*$""", RegexOption.IGNORE_CASE),
            Regex("""\s*[-–—·|]\s*Maps.*$""", RegexOption.IGNORE_CASE)
        )
        for (pattern in mapSuffixes) {
            cleaned = cleaned.replace(pattern, "").trim()
        }

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
     * Extracts a place name from a Google Maps or other location URL, or shared text.
     * Robustly handles:
     * - Standard place URLs: /maps/place/<Place+Name>/@... or /place/<Place+Name>
     * - Search URLs: /maps/search/<Place+Name> and /maps/search/?api=1&query=<Place+Name>
     * - Directions URLs: /maps/dir/.../<Destination> and /maps/dir/?destination=<Destination>
     * - Query parameter URLs: ?q=<Place+Name>, ?query=..., ?daddr=..., ?place=..., etc.
     * - Labeled coordinates: ?q=40.7128,-74.0060(Place+Name)
     * - Shared snippet text: "Blue Bottle Coffee\nhttps://maps.app.goo.gl/..."
     * - Rejects pure coordinates, @lat,lng, data= flags, and system paths.
     */
    fun extractPlaceNameFromLink(link: String?): String? {
        if (link.isNullOrBlank()) return null
        val trimmed = link.trim()

        val urlRegex = Regex("""https?://\S+""", RegexOption.IGNORE_CASE)
        val urlMatch = urlRegex.find(trimmed)
        val urlString = urlMatch?.value
        val surroundingText = if (urlMatch != null) {
            trimmed.replace(urlMatch.value, "").trim()
        } else {
            ""
        }

        if (urlString != null) {
            val nameFromUrl = parsePlaceNameFromUrl(urlString)
            if (nameFromUrl != null) return nameFromUrl
        } else if (!trimmed.startsWith("http://", ignoreCase = true) && !trimmed.startsWith("https://", ignoreCase = true)) {
            val direct = parsePlaceNameFromSharedText(trimmed)
            if (direct != null) return direct
        }

        if (surroundingText.isNotBlank()) {
            val textName = parsePlaceNameFromSharedText(surroundingText)
            if (textName != null) return textName
        }

        return null
    }

    private fun parsePlaceNameFromUrl(urlStr: String): String? {
        try {
            val path = getPath(urlStr)

            // 1. Google Maps Place path: /maps/place/<PlaceName>/... or /place/<PlaceName>/...
            val placePatterns = listOf("/maps/place/", "/place/")
            for (prefix in placePatterns) {
                val idx = path.indexOf(prefix)
                if (idx >= 0) {
                    val afterPlace = path.substring(idx + prefix.length)
                    val rawSegment = afterPlace.substringBefore('/').substringBefore('@').substringBefore('?').substringBefore('#')
                    val candidate = decodeUrlComponent(rawSegment)
                    if (isValidPlaceName(candidate)) {
                        return candidate
                    }
                }
            }

            // 2. Google Maps Search path: /maps/search/<PlaceName>/... or /search/<PlaceName>/...
            val searchPatterns = listOf("/maps/search/", "/search/")
            for (prefix in searchPatterns) {
                val idx = path.indexOf(prefix)
                if (idx >= 0) {
                    val afterSearch = path.substring(idx + prefix.length)
                    val rawSegment = afterSearch.substringBefore('/').substringBefore('@').substringBefore('?').substringBefore('#')
                    val candidate = decodeUrlComponent(rawSegment)
                    if (isValidPlaceName(candidate)) {
                        return candidate
                    }
                }
            }

            // 3. Google Maps Directions path: /maps/dir/.../<Destination>
            val dirPrefix = "/maps/dir/"
            val dirIdx = path.indexOf(dirPrefix)
            if (dirIdx >= 0) {
                val afterDir = path.substring(dirIdx + dirPrefix.length)
                val segments = afterDir.split('/')
                    .map { decodeUrlComponent(it.substringBefore('@').substringBefore('?').substringBefore('#')) }
                    .filter { isValidPlaceName(it) }
                if (segments.isNotEmpty()) {
                    return segments.last()
                }
            }

            // 4. Query parameters: query, q, destination, daddr, place, address, location
            val queryParamKeys = listOf("query", "q", "destination", "daddr", "place", "address", "location")
            for (key in queryParamKeys) {
                val rawParam = getQueryParam(urlStr, key)
                if (!rawParam.isNullOrBlank()) {
                    val decoded = decodeUrlComponent(rawParam)

                    // Check for labeled coordinates: e.g. "40.7128,-74.0060(Place Name)" or "loc:40.7128,-74.0060 (Place Name)"
                    val labelMatch = Regex("""\(([^)]+)\)""").find(decoded)
                    if (labelMatch != null) {
                        val label = labelMatch.groupValues[1].trim()
                        if (isValidPlaceName(label)) {
                            return label
                        }
                    }

                    if (isValidPlaceName(decoded)) {
                        return decoded
                    }
                }
            }
        } catch (e: Exception) {
            // ignore
        }
        return null
    }

    private fun parsePlaceNameFromSharedText(text: String): String? {
        var cleaned = text.trim()
        val prefixesToRemove = listOf(
            Regex("""^Check\s+out\s+""", RegexOption.IGNORE_CASE),
            Regex("""^Take\s+a\s+look\s+at\s+""", RegexOption.IGNORE_CASE),
            Regex("""^Here\s+is\s+""", RegexOption.IGNORE_CASE),
            Regex("""^Found\s+""", RegexOption.IGNORE_CASE)
        )
        for (pattern in prefixesToRemove) {
            cleaned = cleaned.replace(pattern, "").trim()
        }

        val suffixesToRemove = listOf(
            Regex("""\s+on\s+Google\s+Maps.*$""", RegexOption.IGNORE_CASE),
            Regex("""\s+in\s+Google\s+Maps.*$""", RegexOption.IGNORE_CASE)
        )
        for (pattern in suffixesToRemove) {
            cleaned = cleaned.replace(pattern, "").trim()
        }

        if (cleaned.isNotBlank() && isValidPlaceName(cleaned)) {
            return cleanLocationName(cleaned)
        }
        return null
    }

    fun isValidPlaceName(name: String): Boolean {
        if (name.isBlank()) return false
        val trimmed = name.trim()

        if (trimmed.startsWith("@") || trimmed.startsWith("data=", ignoreCase = true) || trimmed.startsWith("!")) {
            return false
        }

        val systemTokens = setOf("maps", "place", "search", "dir", "preview", "api", "u", "b")
        if (systemTokens.contains(trimmed.lowercase())) {
            return false
        }

        if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            return false
        }

        if (isCoordinates(trimmed)) {
            return false
        }

        return true
    }

    fun isCoordinates(text: String): Boolean {
        val trimmed = text.trim().removePrefix("@").removePrefix("loc:").trim()
        // Decimal degrees e.g. "40.7128,-74.0060" or "40.7128, -74.0060" or "40.7128,-74.0060,17z"
        if (trimmed.matches(Regex("""^[+-]?\d+(\.\d+)?\s*,\s*[+-]?\d+(\.\d+)?(\s*,\s*\d+(\.\d+)?z?)?$"""))) {
            return true
        }
        // Degree-minute-second (DMS) e.g. 40°42'46.1"N 74°00'21.7"W or 40°42'46.1"N+74°00'21.7"W
        if (trimmed.contains("°") && (trimmed.contains("N", ignoreCase = true) || trimmed.contains("S", ignoreCase = true))
            && (trimmed.contains("E", ignoreCase = true) || trimmed.contains("W", ignoreCase = true))) {
            return true
        }
        return false
    }

    fun decodeUrlComponent(str: String): String {
        return try {
            URLDecoder.decode(str.replace('+', ' '), "UTF-8").trim()
        } catch (e: Exception) {
            str.replace('+', ' ').trim()
        }
    }

    private fun getPath(url: String): String {
        val schemeEnd = url.indexOf("://")
        val pathStart = if (schemeEnd >= 0) {
            url.indexOf('/', schemeEnd + 3).takeIf { it >= 0 } ?: return ""
        } else {
            url.indexOf('/').takeIf { it >= 0 } ?: return ""
        }
        val pathEnd = url.indexOfAny(charArrayOf('?', '#'), pathStart).takeIf { it >= 0 } ?: url.length
        return url.substring(pathStart, pathEnd)
    }

    private fun getQueryParam(url: String, key: String): String? {
        val queryStart = url.indexOf('?').takeIf { it >= 0 } ?: return null
        val queryEnd = url.indexOf('#').takeIf { it >= 0 } ?: url.length
        if (queryEnd <= queryStart) return null
        val queryString = url.substring(queryStart + 1, queryEnd)

        val pairs = queryString.split('&')
        for (pair in pairs) {
            val eq = pair.indexOf('=')
            val k = if (eq >= 0) pair.substring(0, eq) else pair
            if (k.equals(key, ignoreCase = true)) {
                return if (eq >= 0) pair.substring(eq + 1) else ""
            }
        }
        return null
    }

    /**
     * Resolves redirects for short URLs (e.g. maps.app.goo.gl, goo.gl/maps)
     * and extracts the place name either from the resolved URL or page title.
     */
    fun resolveUrlLocation(urlString: String): String? {
        var connection: HttpURLConnection? = null
        try {
            var currentUrl = urlString
            var redirects = 0
            while (redirects < 6) {
                val url = URL(currentUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = false
                    connectTimeout = 3500
                    readTimeout = 3500
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36")
                    setRequestProperty("Accept-Language", "en-US,en;q=0.9")
                }
                val responseCode = connection.responseCode
                if (responseCode in 300..399) {
                    val locationHeader = connection.getHeaderField("Location")
                    if (!locationHeader.isNullOrBlank()) {
                        currentUrl = URL(url, locationHeader).toString()
                        redirects++
                        connection.disconnect()
                        val name = extractPlaceNameFromLink(currentUrl)
                        if (name != null) return name
                    } else {
                        break
                    }
                } else if (responseCode == 200) {
                    val title = extractTitleFromHtmlStream(connection.inputStream)
                    connection.disconnect()
                    if (!title.isNullOrBlank()) {
                        val cleanedTitle = cleanLocationName(title)
                        if (isValidPlaceName(cleanedTitle)) {
                            return cleanedTitle
                        }
                    }
                    break
                } else {
                    break
                }
            }
            return extractPlaceNameFromLink(currentUrl)
        } catch (e: Exception) {
            return null
        } finally {
            connection?.disconnect()
        }
    }

    private fun extractTitleFromHtmlStream(stream: InputStream): String? {
        return try {
            val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
            val buffer = CharArray(4096)
            val read = reader.read(buffer, 0, buffer.size)
            if (read > 0) {
                val html = String(buffer, 0, read)
                val ogMatch = Regex("""<meta\s+property=["']og:title["']\s+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(html)
                    ?: Regex("""<meta\s+content=["']([^"']+)["']\s+property=["']og:title["']""", RegexOption.IGNORE_CASE).find(html)
                if (ogMatch != null) {
                    return ogMatch.groupValues[1].trim()
                }
                val titleMatch = Regex("""<title>([^<]+)</title>""", RegexOption.IGNORE_CASE).find(html)
                if (titleMatch != null) {
                    return titleMatch.groupValues[1].trim()
                }
            }
            null
        } catch (e: Exception) {
            null
        }
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