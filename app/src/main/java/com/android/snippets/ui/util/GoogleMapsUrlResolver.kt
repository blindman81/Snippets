package com.android.snippets.ui.util

import android.content.Context
import android.net.Uri
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder

object GoogleMapsUrlResolver {

    private const val DESKTOP_USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    fun extractPlaceNameFromLink(link: String?, context: Context? = null): String? {
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

            if (context != null) {
                val coords = parseCoordinatesFromLink(urlString)
                if (coords != null) {
                    val geocoded = ExifLocationHelper.reverseGeocodeSync(context, coords.first, coords.second)
                    if (!geocoded.isNullOrBlank()) return geocoded
                }
            }
        } else if (!trimmed.startsWith("http://", ignoreCase = true) && !trimmed.startsWith("https://", ignoreCase = true)) {
            val direct = parsePlaceNameFromSharedText(trimmed)
            if (direct != null) return direct

            if (context != null && isCoordinates(trimmed)) {
                val coords = parseCoordinatesFromString(trimmed)
                if (coords != null) {
                    val geocoded = ExifLocationHelper.reverseGeocodeSync(context, coords.first, coords.second)
                    if (!geocoded.isNullOrBlank()) return geocoded
                }
            }
        }

        if (surroundingText.isNotBlank()) {
            val textName = parsePlaceNameFromSharedText(surroundingText)
            if (textName != null) return textName
        }

        return null
    }

    fun parsePlaceNameFromUrl(urlStr: String): String? {
        try {
            val segments = try {
                Uri.parse(urlStr).pathSegments ?: emptyList()
            } catch (e: Exception) {
                try {
                    val path = java.net.URI(urlStr).path
                    path?.split('/')?.filter { it.isNotEmpty() } ?: emptyList()
                } catch (_: Exception) {
                    emptyList()
                }
            }

            if (segments.isNotEmpty()) {
                val placeIdx = segments.indexOf("place")
                if (placeIdx != -1 && placeIdx + 1 < segments.size) {
                    val candidate = decodeUrlComponent(segments[placeIdx + 1].substringBefore('@'))
                    if (isValidPlaceName(candidate)) {
                        return candidate
                    }
                }

                val searchIdx = segments.indexOf("search")
                if (searchIdx != -1 && searchIdx + 1 < segments.size) {
                    val candidate = decodeUrlComponent(segments[searchIdx + 1].substringBefore('@'))
                    if (isValidPlaceName(candidate)) {
                        return candidate
                    }
                }

                val dirIdx = segments.indexOf("dir")
                if (dirIdx != -1 && dirIdx + 1 < segments.size) {
                    val dirCandidates = segments.subList(dirIdx + 1, segments.size)
                        .map { decodeUrlComponent(it.substringBefore('@')) }
                        .filter { isValidPlaceName(it) }
                    if (dirCandidates.isNotEmpty()) {
                        return dirCandidates.last()
                    }
                }
            }

            val queryParamKeys = listOf("query", "q", "destination", "daddr", "place", "address", "location")
            for (key in queryParamKeys) {
                val param = try {
                    Uri.parse(urlStr).getQueryParameter(key)
                } catch (e: Exception) {
                    try {
                        val query = java.net.URI(urlStr).query
                        query?.split('&')?.mapNotNull {
                            val parts = it.split('=', limit = 2)
                            if (parts.size == 2 && parts[0] == key) parts[1] else null
                        }?.firstOrNull()
                    } catch (_: Exception) {
                        null
                    }
                }
                if (!param.isNullOrBlank()) {
                    val decoded = decodeUrlComponent(param)
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
        } catch (_: Exception) {
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
            return LocationNameCleaner.cleanLocationName(cleaned)
        }
        return null
    }

    fun isValidPlaceName(name: String): Boolean {
        if (name.isBlank()) return false
        val trimmed = name.trim()

        if (trimmed.startsWith("@") || trimmed.startsWith("data=", ignoreCase = true) || trimmed.startsWith("!")) {
            return false
        }

        val systemTokens = setOf(
            "maps", "place", "search", "dir", "preview", "api", "u", "b",
            "google maps", "google map", "maps google"
        )
        if (systemTokens.contains(trimmed.lowercase())) {
            return false
        }

        if (trimmed.contains("Find local businesses", ignoreCase = true) ||
            trimmed.contains("view maps and get driving directions", ignoreCase = true) ||
            trimmed.contains("Google Search", ignoreCase = true)) {
            return false
        }

        if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            return false
        }

        if (isCoordinates(trimmed)) {
            return false
        }

        // Reject shortener hashes or machine ID tokens like "98TXioATzcA56XYe6"
        if (!trimmed.contains(" ") && trimmed.length >= 8 &&
            trimmed.matches(Regex("""^[A-Za-z0-9_-]+$""")) &&
            trimmed.any { it.isDigit() } &&
            trimmed.any { it.isUpperCase() } &&
            trimmed.any { it.isLowerCase() }) {
            return false
        }

        return true
    }

    fun isCoordinates(text: String): Boolean {
        val trimmed = text.trim().removePrefix("@").removePrefix("loc:").trim()
        if (trimmed.matches(Regex("""^[+-]?\d+(\.\d+)?\s*,\s*[+-]?\d+(\.\d+)?(\s*,\s*\d+(\.\d+)?z?)?$"""))) {
            return true
        }
        if (trimmed.contains("°") && (trimmed.contains("N", ignoreCase = true) || trimmed.contains("S", ignoreCase = true))
            && (trimmed.contains("E", ignoreCase = true) || trimmed.contains("W", ignoreCase = true))) {
            return true
        }
        return false
    }

    fun decodeUrlComponent(str: String): String {
        return try {
            URLDecoder.decode(str.replace('+', ' '), "UTF-8").trim()
        } catch (_: Exception) {
            str.replace('+', ' ').trim()
        }
    }

    fun resolveUrlLocation(urlString: String, context: Context? = null): String? {
        val directName = extractPlaceNameFromLink(urlString, context)
        if (directName != null) return directName

        if (context != null) {
            val coords = parseCoordinatesFromLink(urlString)
            if (coords != null) {
                val geocoded = ExifLocationHelper.reverseGeocodeSync(context, coords.first, coords.second)
                if (!geocoded.isNullOrBlank()) return LocationNameCleaner.cleanLocationName(geocoded)
            }
        }

        var connection: HttpURLConnection? = null
        try {
            var currentUrl = urlString
            var redirects = 0
            while (redirects < 6) {
                val uri = try { Uri.parse(currentUrl) } catch (_: Exception) { null }
                val host = uri?.host?.lowercase().orEmpty()
                val isShortener = host.contains("goo.gl") || host.contains("maps.app") ||
                        host.contains("bit.ly") || host.contains("t.co") || host.contains("tinyurl")
                if (!isShortener && redirects > 0) {
                    val name = extractPlaceNameFromLink(currentUrl, context)
                    if (name != null) return name
                }

                val url = URL(currentUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = false
                    connectTimeout = 4000
                    readTimeout = 4000
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", DESKTOP_USER_AGENT)
                    setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    setRequestProperty("Accept-Language", "en-US,en;q=0.9")
                }

                val responseCode = try {
                    connection.responseCode
                } catch (_: Exception) {
                    break
                }

                if (responseCode in 300..399) {
                    val locationHeader = connection.getHeaderField("Location")
                    connection.disconnect()
                    if (!locationHeader.isNullOrBlank()) {
                        if (locationHeader.startsWith("intent://", ignoreCase = true)) {
                            val intentPath = locationHeader.removePrefix("intent://")
                            val queryIdx = intentPath.indexOf('?')
                            if (queryIdx != -1) {
                                val queryStr = intentPath.substring(queryIdx + 1).substringBefore('#')
                                val fakeUrl = "https://maps.google.com/?$queryStr"
                                val nameFromIntent = extractPlaceNameFromLink(fakeUrl, context)
                                if (nameFromIntent != null) return nameFromIntent
                            }
                            break
                        }

                        currentUrl = try {
                            URL(url, locationHeader).toString()
                        } catch (_: Exception) {
                            locationHeader
                        }
                        val nestedUrl = extractNestedUrl(currentUrl)
                        if (nestedUrl != null) {
                            val nestedName = extractPlaceNameFromLink(nestedUrl, context)
                            if (nestedName != null) return nestedName
                            currentUrl = nestedUrl
                        }
                        redirects++
                        val name = extractPlaceNameFromLink(currentUrl, context)
                        if (name != null) return name
                    } else {
                        break
                    }
                } else if (responseCode == 200) {
                    val name = extractPlaceNameFromLink(currentUrl, context)
                    if (name != null) {
                        connection.disconnect()
                        return name
                    }

                    val stream = connection.inputStream
                    val buffer = CharArray(49152)
                    val readCount = stream.bufferedReader().read(buffer, 0, buffer.size)
                    connection.disconnect()
                    if (readCount > 0) {
                        val htmlChunk = String(buffer, 0, readCount)
                        val htmlTitle = extractTitleFromHtml(htmlChunk)
                        if (htmlTitle != null) {
                            return htmlTitle
                        }
                    }
                    break
                } else {
                    connection.disconnect()
                    break
                }
            }

            val finalName = extractPlaceNameFromLink(currentUrl, context)
            if (finalName != null) return finalName

            if (context != null) {
                val coords = parseCoordinatesFromLink(currentUrl)
                if (coords != null) {
                    val geocoded = ExifLocationHelper.reverseGeocodeSync(context, coords.first, coords.second)
                    if (!geocoded.isNullOrBlank()) return LocationNameCleaner.cleanLocationName(geocoded)
                }
            }

            return null
        } catch (_: Exception) {
            return null
        } finally {
            connection?.disconnect()
        }
    }

    fun extractNestedUrl(urlString: String): String? {
        try {
            val uri = Uri.parse(urlString)
            val nestedParams = listOf("continue", "url", "q", "target", "next", "destination", "dest")
            for (paramKey in nestedParams) {
                val paramValue = uri.getQueryParameter(paramKey)
                if (!paramValue.isNullOrBlank() && (paramValue.startsWith("http://", ignoreCase = true) || paramValue.startsWith("https://", ignoreCase = true))) {
                    return paramValue
                }
            }
        } catch (_: Exception) {}
        return null
    }

    fun extractTitleFromHtml(html: String): String? {
        val ogRegex = Regex("""<meta\s+[^>]*property=["']og:title["'][^>]*content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        val ogMatch = ogRegex.find(html) ?: Regex("""<meta\s+[^>]*content=["']([^"']+)["'][^>]*property=["']og:title["']""", RegexOption.IGNORE_CASE).find(html)
        if (ogMatch != null) {
            val title = LocationNameCleaner.unescapeHtml(ogMatch.groupValues[1])
            val cleaned = LocationNameCleaner.cleanLocationName(title)
            if (isValidPlaceName(cleaned)) return cleaned
        }

        val twitterRegex = Regex("""<meta\s+[^>]*name=["']twitter:title["'][^>]*content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        val twitterMatch = twitterRegex.find(html)
        if (twitterMatch != null) {
            val title = LocationNameCleaner.unescapeHtml(twitterMatch.groupValues[1])
            val cleaned = LocationNameCleaner.cleanLocationName(title)
            if (isValidPlaceName(cleaned)) return cleaned
        }

        val itempropRegex = Regex("""<meta\s+[^>]*itemprop=["']name["'][^>]*content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        val itempropMatch = itempropRegex.find(html)
        if (itempropMatch != null) {
            val title = LocationNameCleaner.unescapeHtml(itempropMatch.groupValues[1])
            val cleaned = LocationNameCleaner.cleanLocationName(title)
            if (isValidPlaceName(cleaned)) return cleaned
        }

        val titleRegex = Regex("""<title[^>]*>([^<]+)</title>""", RegexOption.IGNORE_CASE)
        val titleMatch = titleRegex.find(html)
        if (titleMatch != null) {
            val title = LocationNameCleaner.unescapeHtml(titleMatch.groupValues[1])
            val cleaned = LocationNameCleaner.cleanLocationName(title)
            if (isValidPlaceName(cleaned)) return cleaned
        }

        return null
    }

    fun parseCoordinatesFromLink(link: String): Pair<Double, Double>? {
        try {
            val coordinateKeys = listOf("q", "query", "ll", "sll", "center", "loc")
            for (key in coordinateKeys) {
                val param = try {
                    Uri.parse(link).getQueryParameter(key)
                } catch (_: Exception) {
                    try {
                        val query = java.net.URI(link).query
                        query?.split('&')?.mapNotNull {
                            val parts = it.split('=', limit = 2)
                            if (parts.size == 2 && parts[0] == key) parts[1] else null
                        }?.firstOrNull()
                    } catch (_: Exception) {
                        null
                    }
                }
                if (!param.isNullOrBlank()) {
                    val coords = parseCoordinatesFromString(param)
                    if (coords != null) return coords
                }
            }
            val segments = try {
                Uri.parse(link).pathSegments ?: emptyList()
            } catch (_: Exception) {
                try {
                    val path = java.net.URI(link).path
                    path?.split('/')?.filter { it.isNotEmpty() } ?: emptyList()
                } catch (_: Exception) {
                    emptyList()
                }
            }
            for (segment in segments) {
                if (segment.startsWith("@")) {
                    val coords = parseCoordinatesFromString(segment)
                    if (coords != null) return coords
                }
            }
        } catch (_: Exception) {
            // fallback
        }
        return parseCoordinatesFromString(link)
    }

    fun parseCoordinatesFromString(str: String): Pair<Double, Double>? {
        val trimmed = str.trim().removePrefix("@").removePrefix("loc:").trim()
        val regex = """(-?\d+\.\d+)\s*,\s*(-?\d+\.\d+)""".toRegex()
        val matchResult = regex.find(trimmed)
        if (matchResult != null) {
            try {
                val lat = matchResult.groupValues[1].toDoubleOrNull()
                val lng = matchResult.groupValues[2].toDoubleOrNull()
                if (lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0) {
                    return Pair(lat, lng)
                }
            } catch (_: Exception) {
                // ignore
            }
        }
        return null
    }
}
