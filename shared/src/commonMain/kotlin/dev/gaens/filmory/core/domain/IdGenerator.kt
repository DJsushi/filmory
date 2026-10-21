package dev.gaens.filmory.core.domain

/** Every record gets a stable, globally unique id so sync can be added later. */
interface IdGenerator {
    fun newId(): String
}
