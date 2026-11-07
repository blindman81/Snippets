package com.android.snippets.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey
    val name: String,
    val icon: String? = null,
    val orderIndex: Int = 0,
    val isPinned: Boolean = false
)
