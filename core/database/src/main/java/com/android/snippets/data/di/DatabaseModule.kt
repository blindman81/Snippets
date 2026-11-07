package com.android.snippets.data.di

import android.content.Context
import com.android.snippets.data.CollectionDao
import com.android.snippets.data.PhotoDao
import com.android.snippets.data.SnippetsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSnippetsDatabase(
        @ApplicationContext context: Context
    ): SnippetsDatabase {
        return SnippetsDatabase.getInstance(context)
    }

    @Provides
    fun providePhotoDao(database: SnippetsDatabase): PhotoDao {
        return database.photoDao()
    }

    @Provides
    fun provideCollectionDao(database: SnippetsDatabase): CollectionDao {
        return database.collectionDao()
    }
}
