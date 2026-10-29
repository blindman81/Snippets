package com.android.snippets.data.repository

import com.android.snippets.data.PhotoDao
import com.android.snippets.data.PhotoEntity
import com.android.snippets.data.toEntity
import com.android.snippets.data.toPhoto
import com.android.snippets.model.Photo
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface PhotoRepository {
    val allPhotosFlow: Flow<List<Photo>>
    suspend fun getAllPhotos(): List<Photo>
    suspend fun getPhotoCount(): Int
    suspend fun getPhotoById(id: String): Photo?
    suspend fun insertPhoto(photo: Photo)
    suspend fun insertPhotos(photos: List<Photo>)
    suspend fun updatePhoto(photo: Photo)
    suspend fun deletePhoto(photo: Photo)
    suspend fun deletePhotoById(id: String)
    suspend fun deletePhotos(photos: List<Photo>)
    suspend fun deleteAllPhotos()
    fun getPagingPhotos(): PagingSource<Int, PhotoEntity>
}

@Singleton
class PhotoRepositoryImpl @Inject constructor(
    private val photoDao: PhotoDao
) : PhotoRepository {
    override val allPhotosFlow: Flow<List<Photo>> =
        photoDao.getAllPhotos().map { entities -> entities.map { it.toPhoto() } }

    override suspend fun getAllPhotos(): List<Photo> =
        photoDao.getAllPhotosSync().map { it.toPhoto() }

    override suspend fun getPhotoCount(): Int =
        photoDao.getPhotoCount()

    override suspend fun getPhotoById(id: String): Photo? =
        photoDao.getPhotoById(id)?.toPhoto()

    override suspend fun insertPhoto(photo: Photo) =
        photoDao.insertPhoto(photo.toEntity())

    override suspend fun insertPhotos(photos: List<Photo>) =
        photoDao.insertPhotos(photos.map { it.toEntity() })

    override suspend fun updatePhoto(photo: Photo) =
        photoDao.updatePhoto(photo.toEntity())

    override suspend fun deletePhoto(photo: Photo) =
        photoDao.deletePhoto(photo.toEntity())

    override suspend fun deletePhotoById(id: String) =
        photoDao.deletePhotoById(id)

    override suspend fun deletePhotos(photos: List<Photo>) =
        photoDao.deletePhotos(photos.map { it.toEntity() })

    override suspend fun deleteAllPhotos() =
        photoDao.deleteAllPhotos()

    override fun getPagingPhotos(): PagingSource<Int, PhotoEntity> =
        photoDao.getPagingPhotos()
}
