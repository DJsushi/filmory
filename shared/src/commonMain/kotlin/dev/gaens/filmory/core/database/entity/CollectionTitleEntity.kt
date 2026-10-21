package dev.gaens.filmory.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "collection_titles",
    primaryKeys = ["collectionId", "titleId"],
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TitleEntity::class,
            parentColumns = ["id"],
            childColumns = ["titleId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("titleId")],
)
data class CollectionTitleEntity(
    val collectionId: String,
    val titleId: String,
)
