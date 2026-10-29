package com.android.snippets.ui.util

import android.content.Context
import android.location.Address
import com.android.snippets.model.Photo

object LocationUtils {

    fun getLocationFromExif(context: Context? = null, photo: Photo): String? =
        ExifLocationHelper.getLocationFromExif(context, photo)

    fun cleanLocationName(name: String): String =
        LocationNameCleaner.cleanLocationName(name)

    fun extractPlaceNameFromLink(link: String?, context: Context? = null): String? =
        GoogleMapsUrlResolver.extractPlaceNameFromLink(link, context)

    fun parsePlaceNameFromUrl(urlStr: String): String? =
        GoogleMapsUrlResolver.parsePlaceNameFromUrl(urlStr)

    fun isValidPlaceName(name: String): Boolean =
        GoogleMapsUrlResolver.isValidPlaceName(name)

    fun isCoordinates(text: String): Boolean =
        GoogleMapsUrlResolver.isCoordinates(text)

    fun decodeUrlComponent(str: String): String =
        GoogleMapsUrlResolver.decodeUrlComponent(str)

    fun resolveUrlLocation(urlString: String, context: Context? = null): String? =
        GoogleMapsUrlResolver.resolveUrlLocation(urlString, context)

    fun extractTitleFromHtml(html: String): String? =
        GoogleMapsUrlResolver.extractTitleFromHtml(html)

    fun extractCoordinates(context: Context, photo: Photo): Pair<Double, Double>? =
        ExifLocationHelper.extractCoordinates(context, photo)

    fun parseCoordinatesFromLink(link: String): Pair<Double, Double>? =
        GoogleMapsUrlResolver.parseCoordinatesFromLink(link)

    fun parseCoordinatesFromString(str: String): Pair<Double, Double>? =
        GoogleMapsUrlResolver.parseCoordinatesFromString(str)

    suspend fun reverseGeocode(context: Context, latitude: Double, longitude: Double): String? =
        ExifLocationHelper.reverseGeocode(context, latitude, longitude)

    fun reverseGeocodeSync(context: Context, latitude: Double, longitude: Double): String? =
        ExifLocationHelper.reverseGeocodeSync(context, latitude, longitude)

    suspend fun geocodeAddress(context: Context, addressName: String): Pair<Double, Double>? =
        ExifLocationHelper.geocodeAddress(context, addressName)

    suspend fun resolvePlaceNameFromAddress(context: Context, addressName: String): String? =
        ExifLocationHelper.resolvePlaceNameFromAddress(context, addressName)

    fun formatAddress(address: Address?): String? =
        ExifLocationHelper.formatAddress(address)
}
