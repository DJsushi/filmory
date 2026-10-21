package dev.gaens.filmory

import android.app.Application
import dev.gaens.filmory.core.data.network.TmdbConfig
import dev.gaens.filmory.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

class FilmoryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@FilmoryApplication)
            modules(module { single { TmdbConfig(apiKey = BuildConfig.TMDB_API_KEY) } })
        }
    }
}
