package com.passmanager.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.passmanager.ui.screens.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object PasswordDetail : Screen("password/{id}") {
        fun createRoute(id: Long) = "password/$id"
    }
    object EditPassword : Screen("edit_password?id={id}&groupId={groupId}") {
        fun createRoute(id: Long? = null, groupId: Long? = null): String {
            val idPart = if (id != null) "id=$id" else "id=-1"
            val groupPart = if (groupId != null) "groupId=$groupId" else "groupId=-1"
            return "edit_password?$idPart&$groupPart"
        }
    }
    object Groups : Screen("groups")
    object EditGroup : Screen("edit_group?id={id}") {
        fun createRoute(id: Long? = null) = "edit_group?id=${id ?: -1}"
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(
            route = Screen.PasswordDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: return@composable
            PasswordDetailScreen(id = id, navController = navController)
        }
        composable(
            route = Screen.EditPassword.route,
            arguments = listOf(
                navArgument("id") { type = NavType.LongType; defaultValue = -1L },
                navArgument("groupId") { type = NavType.LongType; defaultValue = -1L }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id").takeIf { it != -1L }
            val groupId = backStackEntry.arguments?.getLong("groupId").takeIf { it != -1L }
            EditPasswordScreen(id = id, initialGroupId = groupId, navController = navController)
        }
        composable(Screen.Groups.route) {
            GroupsScreen(navController = navController)
        }
        composable(
            route = Screen.EditGroup.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id").takeIf { it != -1L }
            EditGroupScreen(id = id, navController = navController)
        }
    }
}
