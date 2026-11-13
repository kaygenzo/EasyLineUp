/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.telen.easylineup.repository.model.RoomLineup
import com.telen.easylineup.repository.model.RoomPlayerInLineup
import com.telen.easylineup.repository.model.RoomTournamentWithLineup
import kotlinx.coroutines.flow.Flow

@Dao
internal interface LineupDao {
    @Query("DELETE FROM lineups")
    suspend fun deleteAll()

    @Insert
    suspend fun insertLineup(lineup: RoomLineup): Long

    @Update
    suspend fun updateLineup(lineup: RoomLineup)

    @Update
    suspend fun updateLineupsWithRowCount(lineups: List<RoomLineup>): Int

    @Delete
    suspend fun deleteLineup(lineup: RoomLineup)

    @Delete
    suspend fun deleteLineups(lineups: List<RoomLineup>)

    @Query("SELECT * FROM lineups")
    suspend fun getLineups(): List<RoomLineup>

    @Query("SELECT * FROM lineups WHERE id = :lineupId")
    fun observeLineupById(lineupId: Long): Flow<List<RoomLineup>>

    @Query("SELECT * FROM lineups WHERE hash = :hash")
    suspend fun getLineupByHash(hash: String): RoomLineup?

    @Query("SELECT * FROM lineups WHERE id = :lineupId")
    suspend fun getLineupById(lineupId: Long): RoomLineup

    @Query(
        """
        SELECT * FROM lineups
        WHERE lineups.tournamentID = :tournamentId AND lineups.teamID = :teamId
    """
    )
    suspend fun getLineupsForTournament(tournamentId: Long, teamId: Long): List<RoomLineup>

    @Query("SELECT EXISTS(SELECT 1 FROM lineups WHERE tournamentID = :tournamentId)")
    suspend fun hasLineupsForTournament(tournamentId: Long): Boolean

    @Query("SELECT * FROM lineups WHERE teamID = :teamId ORDER BY editedAt DESC LIMIT 1")
    suspend fun getLastLineup(teamId: Long): RoomLineup?

    @Query(
        """
        SELECT tournaments.id as tournamentID,
        tournaments.name as tournamentName,
        tournaments.createdAt as tournamentCreatedAt,
        tournaments.startTime as tournamentStartTime,
        tournaments.endTime as tournamentEndTime,
        tournaments.address as tournamentAddress,
        playerFieldPosition.id as fieldPositionID,
        lineups.name as lineupName,
        lineups.id as lineupID,
        lineups.teamID as teamID,
        lineups.eventTime as lineupEventTime,
        lineups.createdAt as lineupCreatedTime,
        lineups.roaster as roster,
        lineups.mode as lineupMode,
        lineups.strategy as lineupStrategy,
        lineups.extraHitters as lineupExtraHittersSize,
        x, y, position
        FROM tournaments
        LEFT JOIN lineups ON tournaments.id = lineups.tournamentID AND lineups.teamID = :teamId
        LEFT JOIN playerFieldPosition ON playerFieldPosition.lineupID = lineups.id
        WHERE (lineups.id IS NOT NULL
                OR NOT EXISTS (SELECT 1 FROM lineups AS any_team
                    WHERE any_team.tournamentID = tournaments.id))
            AND (tournamentName LIKE '%' || :filter || '%' ESCAPE '\'
                OR lineupName LIKE '%' || :filter || '%' ESCAPE '\')
        ORDER BY tournaments.createdAt DESC
    """
    )
    /** [filter] is matched literally once escaped with [escapeForLike]: `%` and `_` included. */
    fun observeAllTournamentsWithLineups(
        filter: String,
        teamId: Long
    ): Flow<List<RoomTournamentWithLineup>>

    @Query(
        """
        SELECT
        lineups.name as lineupName,
        lineups.id as lineupID,
        position,
        players.name as playerName,
        players.id as playerID
        FROM lineups
        LEFT JOIN playerFieldPosition ON playerFieldPosition.lineupID = lineups.id
        LEFT JOIN players ON playerFieldPosition.playerID = players.id
        WHERE lineups.teamID = :teamId AND lineups.tournamentID = :tournamentId
    """
    )
    suspend fun getAllPlayerPositionsForTournament(
        tournamentId: Long,
        teamId: Long
    ): List<RoomPlayerInLineup>
}
