package dev.gaens.filmory.di

import org.koin.core.module.Module

/** Bindings that need platform APIs, such as the database file location. */
expect val platformModule: Module
