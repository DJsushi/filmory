package dev.gaens.filmory.feature.collection.data

import dev.gaens.filmory.core.data.toEpochMillis
import dev.gaens.filmory.core.data.toInstant
import dev.gaens.filmory.core.database.entity.CollectionEntity
import dev.gaens.filmory.core.database.entity.CollectionTitleEntity
import dev.gaens.filmory.feature.collection.domain.TitleCollection

fun toCollections(
    collections: List<CollectionEntity>,
    links: List<CollectionTitleEntity>,
): List<TitleCollection> {
    val titleIdsByCollection = links.groupBy({ it.collectionId }, { it.titleId })
    return collections.map { collection ->
        TitleCollection(
            id = collection.id,
            name = collection.name,
            titleIds = titleIdsByCollection[collection.id].orEmpty(),
            createdAt = collection.createdAtEpochMillis.toInstant(),
        )
    }
}

fun TitleCollection.toCollectionEntity(): CollectionEntity = CollectionEntity(
    id = id,
    name = name,
    createdAtEpochMillis = createdAt.toEpochMillis(),
)
