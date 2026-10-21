package dev.gaens.filmory.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "seasons",
    foreignKeys = [
        ForeignKey(
            entity = TitleEntity::class,
            parentColumns = ["id"],
            childColumns = ["titleId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("titleId")],
)
data class SeasonEntity(
    @PrimaryKey val id: String,
    val titleId: String,
    val number: Int,
)
