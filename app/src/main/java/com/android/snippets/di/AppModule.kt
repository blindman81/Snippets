package com.android.snippets.di

import android.content.Context
import com.android.snippets.data.CollectionDao
import com.android.snippets.data.PhotoDao
import com.android.snippets.data.SnippetsDatabase
import com.android.snippets.data.preferences.UserPreferencesRepository
import com.android.snippets.data.repository.CollectionRepository
import com.android.snippets.data.repository.CollectionRepositoryImpl
import com.android.snippets.data.repository.PhotoRepository
import com.android.snippets.data.repository.PhotoRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

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

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        @ApplicationContext context: Context
    ): UserPreferencesRepository {
        return UserPreferencesRepository.getInstance(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPhotoRepository(impl: PhotoRepositoryImpl): PhotoRepository

    @Binds
    @Singleton
    abstract fun bindCollectionRepository(impl: CollectionRepositoryImpl): CollectionRepository
}
