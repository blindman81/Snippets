package com.android.snippets.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for Snippets.
 */
sealed interface NavigationRoute

@Serializable
data object LibraryRoute : NavigationRoute

@Serializable
data class MemoryRoute(
    val photoId: String? = null,
    val initialIndex: Int = 0
) : NavigationRoute

@Serializable
data object SettingsRoute : NavigationRoute

@Serializable
data object AboutRoute : NavigationRoute

@Serializable
data class EmojiPickerRoute(
    val collection: String? = null
) : NavigationRoute

@Serializable
data object StatsRoute : NavigationRoute

@Serializable
data class MonthlyRecapRoute(
    val month: Int = 0,
    val year: Int = 0
) : NavigationRoute

@Serializable
data class YearlyRecapRoute(
    val year: Int = 0
) : NavigationRoute
