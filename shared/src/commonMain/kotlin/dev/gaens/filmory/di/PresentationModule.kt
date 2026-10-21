package dev.gaens.filmory.di

import dev.gaens.filmory.feature.collection.presentation.CollectionsViewModel
import dev.gaens.filmory.feature.home.presentation.HomeViewModel
import dev.gaens.filmory.feature.library.presentation.LibraryPreset
import dev.gaens.filmory.feature.library.presentation.LibraryViewModel
import dev.gaens.filmory.feature.title.presentation.TitleDetailViewModel
import dev.gaens.filmory.feature.note.presentation.detail.NoteDetailViewModel
import dev.gaens.filmory.feature.note.presentation.editor.NoteEditorViewModel
import dev.gaens.filmory.feature.note.presentation.list.NotesViewModel
import dev.gaens.filmory.feature.watched.presentation.WatchedViewModel
import dev.gaens.filmory.feature.calendar.presentation.CalendarViewModel
import dev.gaens.filmory.feature.settings.presentation.AppViewModel
import dev.gaens.filmory.feature.settings.presentation.SettingsViewModel
import dev.gaens.filmory.feature.search.presentation.SearchViewModel
import dev.gaens.filmory.feature.title.presentation.form.TitleFormViewModel
import dev.gaens.filmory.feature.backup.presentation.BackupViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::HomeViewModel)
    viewModel { (preset: LibraryPreset, collectionId: String?) ->
        LibraryViewModel(preset, collectionId, get(), get(), get(), get(), get())
    }
    viewModelOf(::CollectionsViewModel)
    viewModelOf(::WatchedViewModel)
    viewModelOf(::CalendarViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::AppViewModel)
    viewModelOf(::BackupViewModel)
    viewModelOf(::SearchViewModel)
    viewModel { (titleId: String?) -> TitleFormViewModel(titleId, get(), get(), get()) }
    viewModel { NotesViewModel(get(), get(), get(), get()) }
    viewModel { (titleId: String?, episodeId: String?, noteId: String?) ->
        NoteEditorViewModel(titleId, episodeId, noteId, get(), get(), get(), get(), get(), get(), get())
    }
    viewModel { (noteId: String) -> NoteDetailViewModel(noteId, get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { (titleId: String) ->
        TitleDetailViewModel(
            titleId, get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
        )
    }
}
