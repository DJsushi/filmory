package dev.gaens.filmory

import androidx.compose.ui.window.ComposeUIViewController
import dev.gaens.filmory.core.data.network.TmdbConfig
import dev.gaens.filmory.di.initKoin
import org.koin.dsl.module

fun MainViewController() = ComposeUIViewController(
    configure = { initKoin { modules(module { single { TmdbConfig(apiKey = "") } }) } },
) {
    FilmoryApp()
}
