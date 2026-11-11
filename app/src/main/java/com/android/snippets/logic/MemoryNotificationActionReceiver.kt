package com.android.snippets.logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MemoryNotificationActionReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_MARK_AS_VIEWED = "com.android.snippets.action.MARK_MEMORY_AS_VIEWED"
        const val EXTRA_PHOTO_ID = "photo_id"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        if (action == ACTION_MARK_AS_VIEWED || action?.startsWith(ACTION_MARK_AS_VIEWED) == true) {
            val photoId = intent.getStringExtra(EXTRA_PHOTO_ID) ?: return
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    MemoryWorker.markMemoryAsViewed(context, photoId)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
