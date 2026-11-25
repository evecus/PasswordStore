package com.mobile.passwordmanager.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mobile.passwordmanager.CryptoVault
import com.mobile.passwordmanager.ui.screens.EditScreen
import com.mobile.passwordmanager.ui.screens.GroupScreen
import com.mobile.passwordmanager.ui.screens.HomeScreen
import com.mobile.passwordmanager.ui.screens.UnlockScreen
import com.mobile.passwordmanager.ui.screens.ViewScreen

/**
 * 应用导航根。数据变更后通过 [AppState.bump] 触发界面重组，
 * 因为 CryptoVault 使用普通内存集合而非 State。
 */
class AppState {
    // 用可见 state 触发依赖数据的可组合项重组
    var revision by androidx.compose.runtime.mutableIntStateOf(0)
        private set

    fun bump() {
        revision++
    }
}

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val appState = remember { AppState() }

    // 未解锁时强制进入解锁页；已解锁则进入主页
    val start = if (CryptoVault.unlocked) Dest.HOME else Dest.UNLOCK

    NavHost(navController = navController, startDestination = start) {
        composable(Dest.UNLOCK) {
            UnlockScreen(
                onUnlocked = {
                    appState.bump()
                    navController.navigate(Dest.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Dest.HOME) {
            HomeScreen(
                revision = appState.revision,
                onChanged = { appState.bump() },
                onOpenGroup = { id -> navController.navigate(Dest.group(id)) },
                onViewEntry = { id -> navController.navigate(Dest.view(id, null)) },
                onEditEntry = { id -> navController.navigate(Dest.edit(id, null)) },
                onLocked = {
                    navController.navigate(Dest.UNLOCK) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            Dest.GROUP_ROUTE,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { entry ->
            val groupId = entry.arguments?.getString("groupId").orEmpty()
            GroupScreen(
                groupId = groupId,
                revision = appState.revision,
                onChanged = { appState.bump() },
                onBack = { navController.popBackStack() },
                onViewEntry = { id -> navController.navigate(Dest.view(id, groupId)) },
                onEditEntry = { id -> navController.navigate(Dest.edit(id, groupId)) }
            )
        }

        composable(
            Dest.VIEW_ROUTE,
            arguments = listOf(
                navArgument("entryId") { type = NavType.StringType },
                navArgument("groupId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            val entryId = entry.arguments?.getString("entryId").orEmpty()
            ViewScreen(
                entryId = entryId,
                revision = appState.revision,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Dest.edit(id, null)) }
            )
        }

        composable(
            Dest.EDIT_ROUTE,
            arguments = listOf(
                navArgument("entryId") { type = NavType.StringType },
                navArgument("groupId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            val entryId = entry.arguments?.getString("entryId")
            val groupId = entry.arguments?.getString("groupId")
            EditScreen(
                entryId = entryId?.takeIf { it != Dest.NEW },
                groupId = groupId,
                revision = appState.revision,
                onChanged = { appState.bump() },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

object Dest {
    const val UNLOCK = "unlock"
    const val HOME = "home"
    const val NEW = "new"
    const val GROUP_ROUTE = "group/{groupId}"
    const val VIEW_ROUTE = "view/{entryId}?groupId={groupId}"
    const val EDIT_ROUTE = "edit/{entryId}?groupId={groupId}"

    fun group(id: String) = "group/$id"
    fun view(id: String, groupId: String? = null): String {
        return if (groupId != null) "view/$id?groupId=$groupId" else "view/$id"
    }
    fun edit(id: String?, groupId: String? = null): String {
        val eid = id ?: NEW
        return if (groupId != null) "edit/$eid?groupId=$groupId" else "edit/$eid"
    }
}
