package dev.gaens.filmory.feature.collection.domain

import dev.gaens.filmory.core.domain.IdGenerator
import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.core.domain.util.map
import kotlinx.coroutines.flow.first
import kotlin.time.Clock

/** Adds an empty collection after all the others and returns its id. */
class CreateCollection(
    private val collectionRepository: CollectionRepository,
    private val idGenerator: IdGenerator,
    private val clock: Clock,
) {
    suspend operator fun invoke(name: String): Result<String, DataError.Local> {
        val lastOrder = collectionRepository.observeCollections().first().maxOfOrNull { it.sortOrder } ?: 0
        val collection = TitleCollection(
            id = idGenerator.newId(),
            name = name,
            titleIds = emptyList(),
            createdAt = clock.now(),
            sortOrder = lastOrder + 1,
        )
        return collectionRepository.upsertCollection(collection).map { collection.id }
    }
}
