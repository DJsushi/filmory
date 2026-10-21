package dev.gaens.filmory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.gaens.filmory.core.presentation.designsystem.FilmoryTheme
import dev.gaens.filmory.core.presentation.designsystem.components.BottomBarItem
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryBottomBar
import dev.gaens.filmory.feature.collection.presentation.CollectionsScreenRoot
import dev.gaens.filmory.feature.home.presentation.HomeScreenRoot
import dev.gaens.filmory.feature.library.presentation.LibraryPreset
import dev.gaens.filmory.feature.library.presentation.LibraryScreenRoot
import dev.gaens.filmory.feature.note.presentation.detail.NoteDetailScreenRoot
import dev.gaens.filmory.feature.note.presentation.editor.NoteEditorScreenRoot
import dev.gaens.filmory.feature.note.presentation.list.NotesScreenRoot
import dev.gaens.filmory.feature.title.presentation.TitleDetailScreenRoot
import dev.gaens.filmory.navigation.Route
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.ic_home
import dev.gaens.filmory.resources.ic_notes
import dev.gaens.filmory.resources.ic_profile
import dev.gaens.filmory.resources.ic_watched
import dev.gaens.filmory.resources.tab_home
import dev.gaens.filmory.resources.tab_notes
import dev.gaens.filmory.resources.tab_profile
import dev.gaens.filmory.resources.tab_watched
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private enum class TopLevelTab(val route: Route, val label: StringResource, val icon: DrawableResource) {
    HOME(Route.Home, Res.string.tab_home, Res.drawable.ic_home),
    NOTES(Route.Notes, Res.string.tab_notes, Res.drawable.ic_notes),
    WATCHED(Route.Watched, Res.string.tab_watched, Res.drawable.ic_watched),
    PROFILE(Route.Profile, Res.string.tab_profile, Res.drawable.ic_profile),
}

@Composable
fun FilmoryApp() {
    FilmoryTheme {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val selectedTab = TopLevelTab.entries.indexOfFirst { tab ->
            backStackEntry?.destination?.hasRoute(tab.route::class) == true
        }

        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            FilmoryNavHost(navController)
            if (selectedTab != -1) {
                FilmoryBottomBar(
                    items = TopLevelTab.entries.map { BottomBarItem(stringResource(it.label), it.icon) },
                    selectedIndex = selectedTab,
                    onSelect = { index -> navController.navigateToTab(TopLevelTab.entries[index].route) },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}

private fun NavHostController.navigateToTab(route: Route) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun FilmoryNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Route.Home) {
        composable<Route.Home> {
            HomeScreenRoot(
                onTitleClick = { navController.navigate(Route.TitleDetail(it)) },
                onLibraryClick = { preset, collectionId -> navController.navigate(Route.Library(preset, collectionId)) },
                onCalendarClick = { navController.navigate(Route.Calendar) },
                onCollectionsClick = { navController.navigate(Route.Collections) },
            )
        }
        // Screens below land one by one in milestone 3.
        composable<Route.Notes> {
            NotesScreenRoot(
                onNoteClick = { navController.navigate(Route.NoteDetail(it)) },
                onTitleClick = { navController.navigate(Route.TitleDetail(it)) },
                onAddNote = { navController.navigate(Route.AddNote(it)) },
            )
        }
        composable<Route.AddNote> { entry ->
            val route = entry.toRoute<Route.AddNote>()
            NoteEditorScreenRoot(
                titleId = route.titleId,
                episodeId = route.episodeId,
                noteId = null,
                onClose = navController::navigateUp,
            )
        }
        composable<Route.EditNote> { entry ->
            NoteEditorScreenRoot(
                titleId = null,
                episodeId = null,
                noteId = entry.toRoute<Route.EditNote>().noteId,
                onClose = navController::navigateUp,
            )
        }
        composable<Route.NoteDetail> { entry ->
            val noteId = entry.toRoute<Route.NoteDetail>().noteId
            NoteDetailScreenRoot(
                noteId = noteId,
                onBackClick = navController::navigateUp,
                onEditClick = { navController.navigate(Route.EditNote(noteId)) },
                onTitleClick = { navController.navigate(Route.TitleDetail(it)) },
            )
        }
        composable<Route.Watched> {}
        composable<Route.Profile> {}
        composable<Route.Library> { entry ->
            val route = entry.toRoute<Route.Library>()
            LibraryScreenRoot(
                preset = route.preset,
                collectionId = route.collectionId,
                onBackClick = navController::navigateUp,
                onTitleClick = { navController.navigate(Route.TitleDetail(it)) },
                onAddTitleClick = {},
            )
        }
        composable<Route.Collections> {
            CollectionsScreenRoot(
                onBackClick = navController::navigateUp,
                onCollectionClick = { navController.navigate(Route.Library(LibraryPreset.COLLECTION, it)) },
            )
        }
        composable<Route.TitleDetail> { entry ->
            val titleId = entry.toRoute<Route.TitleDetail>().titleId
            TitleDetailScreenRoot(
                titleId = titleId,
                onBackClick = navController::navigateUp,
                onNoteClick = { navController.navigate(Route.NoteDetail(it)) },
                onAddNoteClick = { episodeId -> navController.navigate(Route.AddNote(titleId, episodeId)) },
                onEditTitleClick = {},
            )
        }
        composable<Route.Calendar> {}
    }
}
