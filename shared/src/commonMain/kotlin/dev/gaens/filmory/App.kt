package dev.gaens.filmory

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.gaens.filmory.core.presentation.designsystem.FilmoryTheme
import dev.gaens.filmory.feature.home.presentation.HomePlaceholder
import dev.gaens.filmory.navigation.Route

@Composable
fun FilmoryApp() {
    FilmoryTheme {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = Route.Home) {
            composable<Route.Home> { HomePlaceholder() }
        }
    }
}
