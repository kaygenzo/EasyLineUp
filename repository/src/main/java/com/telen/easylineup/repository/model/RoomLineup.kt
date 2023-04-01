package com.telen.easylineup.repository.model

import androidx.room.*
import com.telen.easylineup.domain.model.Lineup
import java.util.*

@Entity(
    tableName = "lineups",
    indices = [Index(value = ["name", "teamID", "tournamentID"])],
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
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = "name") var name: String = "",
    @ColumnInfo(name = "teamID") var teamId: Long = 0,
    @ColumnInfo(name = "tournamentID") var tournamentId: Long = 0,
    @ColumnInfo(name = "mode") var mode: Int = 0,
    @ColumnInfo(name = "strategy") var strategy: Int = 0,
    @ColumnInfo(name = "extraHitters") var extraHitters: Int = 0,
    @ColumnInfo(name = "eventTime") var eventTimeInMillis: Long = 0,
    @ColumnInfo(name = "createdAt")
    var createdTimeInMillis: Long = Calendar.getInstance().timeInMillis,
    @ColumnInfo(name = "editedAt")
    var editedTimeInMillis: Long = Calendar.getInstance().timeInMillis,
    @ColumnInfo(name = "roaster") var roster: String? = null,
    @ColumnInfo(name = "hash") var hash: String? = UUID.randomUUID().toString()
)

internal fun RoomLineup.init(lineup: Lineup): RoomLineup {
    id = lineup.id
    name = lineup.name
    teamId = lineup.teamId
    tournamentId = lineup.tournamentId
    mode = lineup.mode
    eventTimeInMillis = lineup.eventTimeInMillis
    createdTimeInMillis = lineup.createdTimeInMillis
    editedTimeInMillis = lineup.editedTimeInMillis
    roster = lineup.roster
    hash = lineup.hash
    strategy = lineup.strategy
    extraHitters = lineup.extraHitters
    return this
}

internal fun RoomLineup.toLineup(): Lineup {
    return Lineup(
        id,
        name,
        teamId,
        tournamentId,
        mode,
        strategy,
        extraHitters,
        eventTimeInMillis,
        createdTimeInMillis,
        editedTimeInMillis,
        roster,
        hash
    )
}