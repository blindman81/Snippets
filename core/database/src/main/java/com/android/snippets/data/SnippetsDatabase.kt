package com.android.snippets.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PhotoEntity::class, CollectionEntity::class, PhotoFtsEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SnippetsDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun collectionDao(): CollectionDao

    companion object {
        @Volatile
        private var INSTANCE: SnippetsDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_photos_date` ON `photos` (`date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_photos_isFavorite_date` ON `photos` (`isFavorite`, `date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_photos_dateAdded` ON `photos` (`dateAdded`)")
            }
        }

        fun getInstance(context: Context): SnippetsDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SnippetsDatabase::class.java,
                    "snippets_database.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
