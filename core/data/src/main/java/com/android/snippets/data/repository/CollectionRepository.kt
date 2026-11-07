package com.android.snippets.data.repository

import com.android.snippets.data.CollectionDao
import com.android.snippets.data.CollectionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface CollectionRepository {
    val allCollectionsFlow: Flow<List<CollectionEntity>>
    suspend fun getAllCollections(): List<CollectionEntity>
    suspend fun getCollectionCount(): Int
    suspend fun getCollectionByName(name: String): CollectionEntity?
    suspend fun insertCollection(collection: CollectionEntity)
    suspend fun insertCollections(collections: List<CollectionEntity>)
    suspend fun updateCollection(collection: CollectionEntity)
    suspend fun deleteCollection(collection: CollectionEntity)
    suspend fun deleteCollectionByName(name: String)
    suspend fun deleteAllCollections()
}

@Singleton
class CollectionRepositoryImpl @Inject constructor(
    private val collectionDao: CollectionDao
) : CollectionRepository {
    override val allCollectionsFlow: Flow<List<CollectionEntity>> =
        collectionDao.getAllCollections()

    override suspend fun getAllCollections(): List<CollectionEntity> =
        collectionDao.getAllCollectionsSync()

    override suspend fun getCollectionCount(): Int =
        collectionDao.getCollectionCount()

    override suspend fun getCollectionByName(name: String): CollectionEntity? =
        collectionDao.getCollectionByName(name)

    override suspend fun insertCollection(collection: CollectionEntity) =
        collectionDao.insertCollection(collection)

    override suspend fun insertCollections(collections: List<CollectionEntity>) =
        collectionDao.insertCollections(collections)

    override suspend fun updateCollection(collection: CollectionEntity) =
        collectionDao.updateCollection(collection)

    override suspend fun deleteCollection(collection: CollectionEntity) =
        collectionDao.deleteCollection(collection)

    override suspend fun deleteCollectionByName(name: String) =
        collectionDao.deleteCollectionByName(name)

    override suspend fun deleteAllCollections() =
        collectionDao.deleteAllCollections()
}
