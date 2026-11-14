package com.android.snippets.logic

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.android.snippets.data.SnippetsDatabase
import com.android.snippets.data.backup.SnippetsBackupManager
import com.android.snippets.data.preferences.UserPreferencesRepository
import com.android.snippets.data.repository.CollectionRepositoryImpl
import com.android.snippets.data.repository.PhotoRepositoryImpl
import kotlinx.coroutines.CancellationException

class ExportBackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val uri = inputData.getString(OUTPUT_URI)?.let(Uri::parse)
            ?: return Result.failure()

        return try {
            val database = SnippetsDatabase.getInstance(applicationContext)
            val photos = PhotoRepositoryImpl(database.photoDao()).getAllPhotos()
            val collections = CollectionRepositoryImpl(database.collectionDao()).getAllCollections()
            val backupManager = SnippetsBackupManager(
                database,
                UserPreferencesRepository.getInstance(applicationContext)
            )

            backupManager.exportBackup(
                context = applicationContext,
                uri = uri,
                photos = photos,
                userCollections = collections.map { it.name },
                collectionIcons = collections.associate { it.name to (it.icon ?: "") },
                snippetFirstSeenTimes = SnippetManager.rebuildSnippetFirstSeenTimes(photos)
            )
            Toast.makeText(
                applicationContext,
                "Backup file exported successfully!",
                Toast.LENGTH_SHORT
            ).show()
            releaseUriPermission(uri)
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Backup export failed", e)
            Toast.makeText(
                applicationContext,
                "Export failed: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            releaseUriPermission(uri)
            Result.failure()
        }
    }

    private fun releaseUriPermission(uri: Uri) {
        try {
            applicationContext.contentResolver.releasePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (e: SecurityException) {
            Log.w(TAG, "Could not release persisted backup URI permission", e)
        }
    }

    companion object {
        const val OUTPUT_URI = "backup_output_uri"
        const val UNIQUE_WORK_NAME = "export_backup_work"
        private const val TAG = "ExportBackupWorker"
    }
}
