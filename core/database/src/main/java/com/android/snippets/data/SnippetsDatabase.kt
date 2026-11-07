package com.android.snippets.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [PhotoEntity::class, CollectionEntity::class, PhotoFtsEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SnippetsDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun collectionDao(): CollectionDao

    companion object {
        @Volatile
        private var INSTANCE: SnippetsDatabase? = null

        fun getInstance(context: Context): SnippetsDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SnippetsDatabase::class.java,
                    "snippets_database.db"
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
