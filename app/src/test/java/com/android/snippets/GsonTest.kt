package com.android.snippets

import com.android.snippets.model.Photo
import com.google.gson.Gson
import org.junit.Test
import org.junit.Assert.*

class GsonTest {
    @Test
    fun testPhotoSerialization() {
        val gson = Gson()
        val photo = Photo(
            uriString = "content://...",
            isPublic = true,
            locationLink = "http://maps.google.com"
        )
        val json = gson.toJson(photo)
        println(json)
        val deserialized = gson.fromJson(json, Photo::class.java)
        println(deserialized)
        assertTrue(deserialized.isPublic)
        assertEquals("http://maps.google.com", deserialized.locationLink)
    }
}
