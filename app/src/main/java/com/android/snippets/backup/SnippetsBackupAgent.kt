package com.android.snippets.backup

import android.app.backup.BackupAgent
import android.app.backup.FullBackupDataOutput
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.File

class SnippetsBackupAgent : BackupAgent() {
    private companion object {
        const val TAG = "SnippetsBackupAgent"
    }

    override fun onFullBackup(data: FullBackupDataOutput) {
        // Check if the current operation is a Device-to-Device (D2D) transfer.
        // Google Drive cloud backups are disabled. We only allow device-to-device transfers.
        val isD2d = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            (data.transportFlags and FLAG_DEVICE_TO_DEVICE_TRANSFER) != 0
        } else {
            false
        }
        Log.d(TAG, "onFullBackup. Is D2D transfer: $isD2d")
        
        if (!isD2d) {
            Log.d(TAG, "Skipping backup. Cloud backup is disabled, only device-to-device transfer is supported.")
            return
        }

        // 1. Back up metadata JSON files in filesDir (contains snippets, collections, star ratings, locations)
        val filesToBackup = listOf(
            "photos_v2.json",
            "collections_v2.json",
            "collection_icons_v2.json",
            "snippet_colors_v2.json",
            "snippet_styles_v2.json",
            "snippet_first_seen_v1.json"
        )
        for (fileName in filesToBackup) {
            val file = File(filesDir, fileName)
            if (file.exists()) {
                Log.d(TAG, "D2D transfer: Backing up file: ${file.absolutePath}")
                try {
                    fullBackupFile(file, data)
                } catch (e: Exception) {
                    Log.e(TAG, "Error backing up file $fileName", e)
                }
            }
        }

        // 2. Back up photo images in filesDir/photos in original full quality (D2D limit is 2GB)
        val photosDir = File(filesDir, "photos")
        if (photosDir.exists() && photosDir.isDirectory) {
            photosDir.listFiles()?.forEach { photoFile ->
                if (photoFile.isFile) {
                    Log.d(TAG, "D2D transfer: Backing up original photo: ${photoFile.name}")
                    try {
                        fullBackupFile(photoFile, data)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error backing up photo ${photoFile.name}", e)
                    }
                }
            }
        }

        // 3. Back up shared preferences
        val sharedPrefsFile = File(dataDir, "shared_prefs/snippets_prefs.xml")
        if (sharedPrefsFile.exists()) {
            Log.d(TAG, "D2D transfer: Backing up shared preferences: ${sharedPrefsFile.absolutePath}")
            try {
                fullBackupFile(sharedPrefsFile, data)
            } catch (e: Exception) {
                Log.e(TAG, "Error backing up shared preferences", e)
            }
        }
    }

    override fun onBackup(
        oldState: ParcelFileDescriptor?,
        data: android.app.backup.BackupDataOutput?,
        newState: ParcelFileDescriptor?
    ) {
        // No-op for fullBackupOnly
    }

    override fun onRestore(
        data: android.app.backup.BackupDataInput?,
        appVersionCode: Int,
        newState: ParcelFileDescriptor?
    ) {
        // No-op for fullBackupOnly
    }
}
