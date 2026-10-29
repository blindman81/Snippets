package com.android.snippets.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.snippets.model.Photo

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey
    val id: String,
    val uriString: String,
    val date: Long,
    val dateAdded: Long = date,
    val snippets: List<String> = emptyList(),
    val isViewed: Boolean = false,
    val lastViewedTime: Long = 0L,
    val snippetsAddedTime: Long = 0L,
    val surfacedTime: Long = 0L,
    val isFavorite: Boolean = false,
    val isLibraryUpload: Boolean = true,
    val collections: List<String> = emptyList(),
    val widthPx: Int = 0,
    val heightPx: Int = 0,
    val isPublic: Boolean = false,
    val locationLink: String? = null,
    val locationName: String? = null,
    val rating: Int = 0,
    val name: String? = null,
    val title: String? = name,
    val tags: String = ""
)

fun PhotoEntity.toPhoto(): Photo = Photo(
    id = id,
    uriString = uriString,
    date = date,
    snippets = snippets,
    isViewed = isViewed,
    lastViewedTime = lastViewedTime,
    snippetsAddedTime = snippetsAddedTime,
    surfacedTime = surfacedTime,
    isFavorite = isFavorite,
    isLibraryUpload = isLibraryUpload,
    collections = collections,
    widthPx = widthPx,
    heightPx = heightPx,
    isPublic = isPublic,
    locationLink = locationLink,
    locationName = locationName,
    rating = rating,
    name = name ?: title
)

fun Photo.toEntity(): PhotoEntity = PhotoEntity(
    id = id,
    uriString = uriString,
    date = date,
    dateAdded = date,
    snippets = snippets,
    isViewed = isViewed,
    lastViewedTime = lastViewedTime,
    snippetsAddedTime = snippetsAddedTime,
    surfacedTime = surfacedTime,
    isFavorite = isFavorite,
    isLibraryUpload = isLibraryUpload,
    collections = collections,
    widthPx = widthPx,
    heightPx = heightPx,
    isPublic = isPublic,
    locationLink = locationLink,
    locationName = locationName,
    rating = rating,
    name = name,
    title = name,
    tags = collections.joinToString(" ")
)
