package dev.gaens.filmory.core.database.entity

import kotlinx.serialization.Serializable

/** Every row of the database, for backup and restore. */
@Serializable
data class DatabaseSnapshot(
    val titles: List<TitleEntity>,
    val seasons: List<SeasonEntity>,
    val episodes: List<EpisodeEntity>,
    val watchings: List<WatchingEntity>,
    val episodeChecks: List<EpisodeCheckEntity>,
    val notes: List<NoteEntity>,
    val noteComments: List<NoteCommentEntity>,
    val collections: List<CollectionEntity>,
    val collectionTitles: List<CollectionTitleEntity>,
)
