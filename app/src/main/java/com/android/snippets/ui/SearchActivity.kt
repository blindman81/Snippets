package com.android.snippets.ui

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.android.snippets.MainActivity

class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val query = intent?.getStringExtra("search_query")
            ?: intent?.getStringExtra(SearchManager.QUERY)
            ?: intent?.data?.getQueryParameter("q")

        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (!query.isNullOrBlank()) {
                putExtra("search_query", query)
            }
        }
        startActivity(mainIntent)
        finish()
    }
}
