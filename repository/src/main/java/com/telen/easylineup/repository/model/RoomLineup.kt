/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.telen.easylineup.domain.model.Lineup

/**
 * @property id
 * @property name
 * @property teamId
 * @property tournamentId
 * @property mode
 * @property strategy
 * @property extraHitters
 * @property eventTimeInMillis
 * @property createdTimeInMillis
 * @property editedTimeInMillis
 * @property roster
 * @property hash
 */
@Entity(
    tableName = "lineups",
    indices = [
        Index(value = ["name", "teamID", "tournamentID"]),
        Index(value = ["teamID"]),
        Index(value = ["tournamentID"]),
        Index(value = ["hash"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = RoomTeam::class, parentColumns = ["id"], childColumns = ["teamID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RoomTournament::class, parentColumns = ["id"], childColumns = ["tournamentID"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
internal data class RoomLineup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String = "",
    @ColumnInfo(name = "teamID") val teamId: Long = 0,
    @ColumnInfo(name = "tournamentID") val tournamentId: Long = 0,
    @ColumnInfo(name = "mode") val mode: Int = 0,
    @ColumnInfo(name = "strategy") val strategy: Int = 0,
    @ColumnInfo(name = "extraHitters") val extraHitters: Int = 0,
    @ColumnInfo(name = "eventTime") val eventTimeInMillis: Long = 0,
    @ColumnInfo(name = "createdAt")
    val createdTimeInMillis: Long = 0L,
    @ColumnInfo(name = "editedAt")
    val editedTimeInMillis: Long = 0L,
    @ColumnInfo(name = "roaster") val roster: String? = null,
    @ColumnInfo(name = "hash") val hash: String? = null
)

internal fun Lineup.toRoom(): RoomLineup {
    return RoomLineup(
        id = id,
        name = name,
        teamId = teamId,
        tournamentId = tournamentId,
        mode = mode,
        eventTimeInMillis = eventTimeInMillis,
        createdTimeInMillis = createdTimeInMillis,
        editedTimeInMillis = editedTimeInMillis,
        roster = roster,
        hash = hash,
        strategy = strategy,
        extraHitters = extraHitters
    )
}

internal fun RoomLineup.toDomain(): Lineup {
    return Lineup(
        id = id,
        name = name,
        teamId = teamId,
        tournamentId = tournamentId,
        mode = mode,
        strategy = strategy,
        extraHitters = extraHitters,
        eventTimeInMillis = eventTimeInMillis,
        createdTimeInMillis = createdTimeInMillis,
        editedTimeInMillis = editedTimeInMillis,
        roster = roster,
        hash = hash
    )
}
