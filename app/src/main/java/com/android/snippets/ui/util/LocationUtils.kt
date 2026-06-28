package com.android.snippets.ui.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.android.snippets.model.Photo
import java.util.Locale

object LocationUtils {

    fun getLocationFromExif(context: Context, photo: Photo): String? {
        if (!photo.locationName.isNullOrBlank()) {
            return photo.locationName
        }
        if (!photo.locationLink.isNullOrBlank() && !photo.locationLink.startsWith("http")) {
            return photo.locationLink
        }
        if (photo.uriString.isBlank()) return null
        
        return extractLocationFromUri(context, photo.uri)
    }

    fun extractLocationFromUri(context: Context, uri: Uri): String? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            inputStream.use { stream ->
                val exif = ExifInterface(stream)
                val latLong = exif.latLong
                if (latLong != null && latLong.size >= 2) {
                    val lat = latLong[0]
                    val lng = latLong[1]
                    if (lat != 0.0 || lng != 0.0) {
                        if (Geocoder.isPresent()) {
                            try {
                                val geocoder = Geocoder(context, Locale.getDefault())
                                @Suppress("DEPRECATION")
                                val addresses: List<Address>? = geocoder.getFromLocation(lat, lng, 1)
                                if (!addresses.isNullOrEmpty()) {
                                    val address = addresses[0]
                                    val city = address.locality ?: address.subAdminArea ?: address.adminArea
                                    val country = address.countryName
                                    val formatted = when {
                                        !city.isNullOrBlank() && !country.isNullOrBlank() -> "$city, $country"
                                        !city.isNullOrBlank() -> city
                                        !country.isNullOrBlank() -> country
                                        else -> null
                                    }
                                    if (formatted != null) return formatted
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        return formatCoordinates(lat, lng)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun formatCoordinates(lat: Double, lng: Double): String {
        val latDir = if (lat >= 0) "N" else "S"
        val lngDir = if (lng >= 0) "E" else "W"
        return String.format(Locale.US, "%.2f°%s, %.2f°%s", Math.abs(lat), latDir, Math.abs(lng), lngDir)
    }
}
