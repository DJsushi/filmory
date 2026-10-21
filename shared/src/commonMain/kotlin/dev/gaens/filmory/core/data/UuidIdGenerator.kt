package dev.gaens.filmory.core.data

import dev.gaens.filmory.core.domain.IdGenerator
import kotlin.uuid.Uuid

class UuidIdGenerator : IdGenerator {
    override fun newId(): String = Uuid.random().toString()
}
