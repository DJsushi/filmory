package dev.gaens.filmory.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import dev.gaens.filmory.core.database.entity.CollectionEntity
import dev.gaens.filmory.core.database.entity.CollectionTitleEntity
import dev.gaens.filmory.core.database.entity.DatabaseSnapshot
import dev.gaens.filmory.core.database.entity.EpisodeCheckEntity
import dev.gaens.filmory.core.database.entity.EpisodeEntity
import dev.gaens.filmory.core.database.entity.NoteCommentEntity
import dev.gaens.filmory.core.database.entity.NoteEntity
import dev.gaens.filmory.core.database.entity.SeasonEntity
import dev.gaens.filmory.core.database.entity.TitleEntity
import dev.gaens.filmory.core.database.entity.WatchingEntity

@Dao
abstract class BackupDao {
    @Query("SELECT * FROM titles") abstract suspend fun titles(): List<TitleEntity>
    @Query("SELECT * FROM seasons") abstract suspend fun seasons(): List<SeasonEntity>
    @Query("SELECT * FROM episodes") abstract suspend fun episodes(): List<EpisodeEntity>
    @Query("SELECT * FROM watchings") abstract suspend fun watchings(): List<WatchingEntity>
    @Query("SELECT * FROM episode_checks") abstract suspend fun episodeChecks(): List<EpisodeCheckEntity>
    @Query("SELECT * FROM notes") abstract suspend fun notes(): List<NoteEntity>
    @Query("SELECT * FROM note_comments") abstract suspend fun noteComments(): List<NoteCommentEntity>
    @Query("SELECT * FROM collections") abstract suspend fun collections(): List<CollectionEntity>
    @Query("SELECT * FROM collection_titles") abstract suspend fun collectionTitles(): List<CollectionTitleEntity>

    // Deleting titles and collections cascades to every other table.
    @Query("DELETE FROM titles") abstract suspend fun deleteTitles()
    @Query("DELETE FROM collections") abstract suspend fun deleteCollections()

    @Insert abstract suspend fun insertTitles(rows: List<TitleEntity>)
    @Insert abstract suspend fun insertSeasons(rows: List<SeasonEntity>)
    @Insert abstract suspend fun insertEpisodes(rows: List<EpisodeEntity>)
    @Insert abstract suspend fun insertWatchings(rows: List<WatchingEntity>)
    @Insert abstract suspend fun insertEpisodeChecks(rows: List<EpisodeCheckEntity>)
    @Insert abstract suspend fun insertNotes(rows: List<NoteEntity>)
    @Insert abstract suspend fun insertNoteComments(rows: List<NoteCommentEntity>)
    @Insert abstract suspend fun insertCollections(rows: List<CollectionEntity>)
    @Insert abstract suspend fun insertCollectionTitles(rows: List<CollectionTitleEntity>)

    @Transaction
    open suspend fun snapshot() = DatabaseSnapshot(
        titles(), seasons(), episodes(), watchings(), episodeChecks(), notes(), noteComments(), collections(), collectionTitles(),
    )

    /** Replaces everything in one transaction, in foreign key order. A failure leaves the old data. */
    @Transaction
    open suspend fun replaceAll(snapshot: DatabaseSnapshot) {
        deleteTitles()
        deleteCollections()
        insertTitles(snapshot.titles)
        insertSeasons(snapshot.seasons)
        insertEpisodes(snapshot.episodes)
        insertWatchings(snapshot.watchings)
        insertEpisodeChecks(snapshot.episodeChecks)
        insertNotes(snapshot.notes)
        insertNoteComments(snapshot.noteComments)
        insertCollections(snapshot.collections)
        insertCollectionTitles(snapshot.collectionTitles)
    }
}
