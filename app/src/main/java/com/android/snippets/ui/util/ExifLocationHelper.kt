package com.android.snippets.ui.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import com.android.snippets.model.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

object ExifLocationHelper {

    fun getLocationFromExif(context: Context? = null, photo: Photo): String? {
        if (!photo.locationName.isNullOrBlank()) {
            return LocationNameCleaner.cleanLocationName(photo.locationName)
        }
        if (!photo.locationLink.isNullOrBlank()) {
            val nameFromLink = GoogleMapsUrlResolver.extractPlaceNameFromLink(photo.locationLink, context)
            if (nameFromLink != null) return LocationNameCleaner.cleanLocationName(nameFromLink)
            if (!photo.locationLink.startsWith("http://", ignoreCase = true) && !photo.locationLink.startsWith("https://", ignoreCase = true)) {
                return LocationNameCleaner.cleanLocationName(photo.locationLink)
            }
        }
        if (context != null) {
            val coords = extractCoordinates(context, photo)
            if (coords != null) {
                val geocoded = reverseGeocodeSync(context, coords.first, coords.second)
                if (!geocoded.isNullOrBlank()) {
                    return LocationNameCleaner.cleanLocationName(geocoded)
                }
            }
        }
        return null
    }

    fun extractCoordinates(context: Context, photo: Photo): Pair<Double, Double>? {
        photo.locationLink?.let { link ->
            val parsed = GoogleMapsUrlResolver.parseCoordinatesFromLink(link)
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

    suspend fun reverseGeocode(context: Context, latitude: Double, longitude: Double): String? = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) return@withContext null
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            if (continuation.isActive) {
                                continuation.resume(addresses)
                            }
                        }
                        override fun onError(errorMessage: String?) {
                            if (continuation.isActive) {
                                continuation.resume(emptyList())
                            }
                        }
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)
            }
            formatAddress(addresses?.firstOrNull())
        } catch (_: Exception) {
            null
        }
    }

    fun reverseGeocodeSync(context: Context, latitude: Double, longitude: Double): String? {
        if (!Geocoder.isPresent()) return null
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val latch = CountDownLatch(1)
                var result: List<Address>? = null
                geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        result = addresses
                        latch.countDown()
                    }
                    override fun onError(errorMessage: String?) {
                        latch.countDown()
                    }
                })
                latch.await(3, TimeUnit.SECONDS)
                result
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)
            }
            formatAddress(addresses?.firstOrNull())
        } catch (_: Exception) {
            null
        }
    }

    suspend fun geocodeAddress(context: Context, addressName: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent() || addressName.isBlank()) return@withContext null
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocationName(addressName, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            if (continuation.isActive) {
                                continuation.resume(addresses)
                            }
                        }
                        override fun onError(errorMessage: String?) {
                            if (continuation.isActive) {
                                continuation.resume(emptyList())
                            }
                        }
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(addressName, 1)
            }
            val first = addresses?.firstOrNull() ?: return@withContext null
            Pair(first.latitude, first.longitude)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun resolvePlaceNameFromAddress(context: Context, addressName: String): String? = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent() || addressName.isBlank()) return@withContext null
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocationName(addressName, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            if (continuation.isActive) {
                                continuation.resume(addresses)
                            }
                        }
                        override fun onError(errorMessage: String?) {
                            if (continuation.isActive) {
                                continuation.resume(emptyList())
                            }
                        }
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(addressName, 1)
            }
            formatAddress(addresses?.firstOrNull())
        } catch (_: Exception) {
            null
        }
    }

    fun formatAddress(address: Address?): String? {
        if (address == null) return null

        val feature = address.featureName?.trim()
        val isStreetNumber = feature != null && (
            feature.matches(Regex("""^\d+[\w\s-]*$""")) ||
            feature.equals(address.subThoroughfare, ignoreCase = true) ||
            feature.equals(address.thoroughfare, ignoreCase = true)
        )

        val placeName = if (!feature.isNullOrBlank() && !isStreetNumber && GoogleMapsUrlResolver.isValidPlaceName(feature)) {
            feature
        } else null

        val subLocality = address.subLocality?.trim()?.takeIf { GoogleMapsUrlResolver.isValidPlaceName(it) }
        val locality = address.locality?.trim()?.takeIf { GoogleMapsUrlResolver.isValidPlaceName(it) }
        val adminArea = address.adminArea?.trim()?.takeIf { GoogleMapsUrlResolver.isValidPlaceName(it) }
        val country = address.countryName?.trim()?.takeIf { GoogleMapsUrlResolver.isValidPlaceName(it) }

        val rawResult = when {
            placeName != null && locality != null && !placeName.equals(locality, ignoreCase = true) -> {
                "$placeName, $locality"
            }
            placeName != null -> placeName
            subLocality != null && locality != null && !subLocality.equals(locality, ignoreCase = true) -> {
                "$subLocality, $locality"
            }
            subLocality != null -> subLocality
            locality != null -> locality
            adminArea != null -> adminArea
            country != null -> country
            address.maxAddressLineIndex >= 0 -> address.getAddressLine(0)
            else -> null
        }

        return rawResult?.let { LocationNameCleaner.cleanLocationName(it) }
    }
}
