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
import kotlinx.coroutines.CancellationException

class RestoreBackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val uri = inputData.getString(INPUT_URI)?.let(Uri::parse)
            ?: return Result.failure()

        return try {
            val database = SnippetsDatabase.getInstance(applicationContext)
            val preferences = UserPreferencesRepository.getInstance(applicationContext)
            SnippetsBackupManager(database, preferences).importBackup(applicationContext, uri)
            Toast.makeText(applicationContext, "Backup restored successfully!", Toast.LENGTH_SHORT).show()
            releaseUriPermission(uri)
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Backup restore failed", e)
            Toast.makeText(
                applicationContext,
                "Restore failed: ${e.message}",
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
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: SecurityException) {
            Log.w(TAG, "Could not release persisted backup URI permission", e)
        }
    }

    companion object {
        const val INPUT_URI = "backup_uri"
        const val UNIQUE_WORK_NAME = "restore_backup_work"
        private const val TAG = "RestoreBackupWorker"
    }
}
