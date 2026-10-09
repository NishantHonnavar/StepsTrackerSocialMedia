package com.example

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

/**
 * HashRouter-style routes for navigation inside Scroll Tax.
 */
sealed class AppRoute(val routeKey: String) {
    object Dashboard : AppRoute("dashboard")
    object LevelsRoadmap : AppRoute("levels_roadmap")
    data class LevelDetail(val rank: UserRank) : AppRoute("level_${rank.level}")
    object Settings : AppRoute("settings")
    object ProfileSelection : AppRoute("profiles")
    object BlockedApps : AppRoute("blocked_apps")
    object Trends : AppRoute("trends")
    object Achievements : AppRoute("achievements")
    object WebCompanion : AppRoute("web_companion")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AppRoute) return false
        return this.routeKey == other.routeKey
    }

    override fun hashCode(): Int {
        return routeKey.hashCode()
    }
}

/**
 * HashRouter-style Navigation Controller managing an explicit backstack.
 * Guarantees that the Android Back gesture/button navigates backward through the stack
 * to the Main Dashboard, never exiting the app accidentally.
 */
class AppNavController(initialRoute: AppRoute = AppRoute.Dashboard) {
    val backStack = mutableStateListOf<AppRoute>(initialRoute)

    val currentRoute: AppRoute
        get() = backStack.lastOrNull() ?: AppRoute.Dashboard

    fun navigateTo(route: AppRoute) {
        if (currentRoute == route) return
        backStack.add(route)
    }

    fun popBack(): Boolean {
        return if (backStack.size > 1) {
            backStack.removeAt(backStack.size - 1)
            true
        } else {
            false
        }
    }

    fun popToDashboard() {
        while (backStack.size > 1) {
            backStack.removeAt(backStack.size - 1)
        }
    }

    fun isAtRoot(): Boolean = backStack.size <= 1
}

@Composable
fun rememberAppNavController(initialRoute: AppRoute = AppRoute.Dashboard): AppNavController {
    return remember { AppNavController(initialRoute) }
}
