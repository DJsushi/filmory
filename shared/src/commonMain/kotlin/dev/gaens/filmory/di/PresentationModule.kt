package dev.gaens.filmory.di

import dev.gaens.filmory.feature.collection.presentation.CollectionsViewModel
import dev.gaens.filmory.feature.home.presentation.HomeViewModel
import dev.gaens.filmory.feature.library.presentation.LibraryPreset
import dev.gaens.filmory.feature.library.presentation.LibraryViewModel
import dev.gaens.filmory.feature.title.presentation.TitleDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::HomeViewModel)
    viewModel { (preset: LibraryPreset, collectionId: String?) ->
        LibraryViewModel(preset, collectionId, get(), get(), get(), get(), get())
    }
    viewModelOf(::CollectionsViewModel)
    viewModel { (titleId: String) ->
        TitleDetailViewModel(
            titleId, get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
        )
    }
}
