package io.github.ln.apnsettingshelper.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.ln.apnsettingshelper.ui.detail.PresetDetailScreen
import io.github.ln.apnsettingshelper.ui.list.PresetListScreen

@Composable
fun AppNavHost(
    currentLanguageTag: String?,
    onLanguageChange: (String?) -> Unit,
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            PresetListScreen(
                onPresetClick = { presetId -> navController.navigate(Routes.detail(presetId)) },
                currentLanguageTag = currentLanguageTag,
                onLanguageChange = onLanguageChange,
            )
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument(Routes.ARG_PRESET_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val presetId = backStackEntry.arguments?.getString(Routes.ARG_PRESET_ID).orEmpty()
            PresetDetailScreen(
                presetId = presetId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
