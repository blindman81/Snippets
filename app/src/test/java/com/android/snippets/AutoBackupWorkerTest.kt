package com.android.snippets

import com.android.snippets.logic.AutoBackupWorker
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class AutoBackupWorkerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testIdenticalPayloadsExceptExportedAtReturnsFalse() {
        val existing = JsonObject().apply {
            addProperty("version", 2)
            addProperty("exportedAt", 1000L)
            add("photos", JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("id", "p1")
                    addProperty("rating", 4)
                })
            })
            add("settings", JsonObject().apply {
                addProperty("theme_preference", "DARK")
            })
        }

        val current = JsonObject().apply {
            addProperty("version", 2)
            addProperty("exportedAt", 5000L) // Different timestamp
            add("photos", JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("id", "p1")
                    addProperty("rating", 4)
                })
            })
            add("settings", JsonObject().apply {
                addProperty("theme_preference", "DARK")
            })
        }

        assertFalse(AutoBackupWorker.hasNewChanges(existing, current))
    }

    @Test
    fun testPhotoListChangeReturnsTrue() {
        val existing = JsonObject().apply {
            addProperty("version", 2)
            addProperty("exportedAt", 1000L)
            add("photos", JsonArray().apply {
                add(JsonObject().apply { addProperty("id", "p1") })
            })
        }

        val current = JsonObject().apply {
            addProperty("version", 2)
            add("photos", JsonArray().apply {
                add(JsonObject().apply { addProperty("id", "p1") })
                add(JsonObject().apply { addProperty("id", "p2") })
            })
        }

        assertTrue(AutoBackupWorker.hasNewChanges(existing, current))
    }

    @Test
    fun testPhotoFieldChangeReturnsTrue() {
        val existing = JsonObject().apply {
            addProperty("version", 2)
            add("photos", JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("id", "p1")
                    addProperty("rating", 2)
                })
            })
        }

        val current = JsonObject().apply {
            addProperty("version", 2)
            add("photos", JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("id", "p1")
                    addProperty("rating", 5)
                })
            })
        }

        assertTrue(AutoBackupWorker.hasNewChanges(existing, current))
    }

    @Test
    fun testCollectionChangeReturnsTrue() {
        val existing = JsonObject().apply {
            addProperty("version", 2)
            add("collections", JsonArray().apply { add("Vacation") })
        }

        val current = JsonObject().apply {
            addProperty("version", 2)
            add("collections", JsonArray().apply {
                add("Vacation")
                add("Work")
            })
        }

        assertTrue(AutoBackupWorker.hasNewChanges(existing, current))
    }

    @Test
    fun testSettingsChangeReturnsTrue() {
        val existing = JsonObject().apply {
            addProperty("version", 2)
            add("settings", JsonObject().apply {
                addProperty("theme_preference", "DARK")
            })
        }

        val current = JsonObject().apply {
            addProperty("version", 2)
            add("settings", JsonObject().apply {
                addProperty("theme_preference", "LIGHT")
            })
        }

        assertTrue(AutoBackupWorker.hasNewChanges(existing, current))
    }

    @Test
    fun testMissingKeyInExistingReturnsTrue() {
        val existing = JsonObject().apply {
            addProperty("version", 2)
        }

        val current = JsonObject().apply {
            addProperty("version", 2)
            add("photos", JsonArray())
        }

        assertTrue(AutoBackupWorker.hasNewChanges(existing, current))
    }

    @Test
    fun testMissingKeyInCurrentReturnsTrue() {
        val existing = JsonObject().apply {
            addProperty("version", 2)
            add("photos", JsonArray())
        }

        val current = JsonObject().apply {
            addProperty("version", 2)
        }

        assertTrue(AutoBackupWorker.hasNewChanges(existing, current))
    }

    @Test
    fun testVersionChangeReturnsTrue() {
        val existing = JsonObject().apply {
            addProperty("version", 1)
        }

        val current = JsonObject().apply {
            addProperty("version", 2)
        }

        assertTrue(AutoBackupWorker.hasNewChanges(existing, current))
    }

    @Test
    fun testGetReferenceBackupFileReturnsTargetSuffixWhenExists() {
        val folder = tempFolder.newFolder("SnippetsBackups")
        val dailyFile = File(folder, "snippets_backup_auto_daily.json").apply { writeText("{}") }
        val weeklyFile = File(folder, "snippets_backup_auto_weekly.json").apply { writeText("{}") }

        val ref = AutoBackupWorker.getReferenceBackupFile(folder, "daily")
        assertEquals(dailyFile.absolutePath, ref?.absolutePath)
    }

    @Test
    fun testGetReferenceBackupFileFallsBackToLatestAutoBackupWhenSuffixMissing() {
        val folder = tempFolder.newFolder("SnippetsBackups")
        val dailyFile = File(folder, "snippets_backup_auto_daily.json").apply {
            writeText("{}")
            setLastModified(1000L)
        }
        val monthlyFile = File(folder, "snippets_backup_auto_monthly.json").apply {
            writeText("{}")
            setLastModified(5000L)
        }

        val ref = AutoBackupWorker.getReferenceBackupFile(folder, "weekly")
        assertEquals(monthlyFile.absolutePath, ref?.absolutePath)
    }

    @Test
    fun testGetReferenceBackupFileReturnsNullWhenNoAutoBackups() {
        val folder = tempFolder.newFolder("SnippetsBackups")
        File(folder, "other_file.json").writeText("{}")

        val ref = AutoBackupWorker.getReferenceBackupFile(folder, "daily")
        assertNull(ref)
    }
}
