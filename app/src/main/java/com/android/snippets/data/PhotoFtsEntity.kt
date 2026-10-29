package com.android.snippets.data

import androidx.room.Entity
import androidx.room.Fts4

@Entity(tableName = "photos_fts")
@Fts4(contentEntity = PhotoEntity::class)
data class PhotoFtsEntity(
    val title: String?,
    val snippets: String?,
    val locationName: String?,
    val tags: String?
)
