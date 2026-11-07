package com.android.snippets.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos ORDER BY date DESC")
    fun getAllPhotos(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos ORDER BY dateAdded DESC")
    fun getPagingPhotos(): PagingSource<Int, PhotoEntity>

    @Query("SELECT photos.* FROM photos JOIN photos_fts ON photos.rowid = photos_fts.docid WHERE photos_fts MATCH :searchQuery")
    fun searchPhotos(searchQuery: String): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos ORDER BY date DESC")
    suspend fun getAllPhotosSync(): List<PhotoEntity>

    @Query("SELECT * FROM photos WHERE id = :id LIMIT 1")
    suspend fun getPhotoById(id: String): PhotoEntity?

    @Query("SELECT * FROM photos WHERE isFavorite = 1 ORDER BY date DESC")
    fun getFavoritePhotos(): Flow<List<PhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoEntity>)

    @Update
    suspend fun updatePhoto(photo: PhotoEntity)

    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)

    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun deletePhotoById(id: String)

    @Delete
    suspend fun deletePhotos(photos: List<PhotoEntity>)

    @Query("DELETE FROM photos")
    suspend fun deleteAllPhotos()

    @Query("SELECT COUNT(*) FROM photos")
    suspend fun getPhotoCount(): Int
}
