package com.telen.easylineup.repository.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.telen.easylineup.repository.model.*
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface LineupDao {
    @Insert
    fun insertLineup(lineup: Lineup): Single<Long>

    @Insert
    fun insertLineup(lineups: List<Lineup>): Completable

    @Update
    fun updateLineup(lineup: Lineup): Completable

    @Delete
    fun deleteLineup(lineup: Lineup): Completable

    @Query("SELECT * FROM lineups ORDER BY id ASC")
    fun getAllLineup(): LiveData<List<Lineup>>

    @Query("SELECT * FROM lineups WHERE id = :lineupId")
    fun getLineupById(lineupId: Long): LiveData<Lineup>

    @Query("SELECT * FROM lineups WHERE id = :lineupId")
    fun getLineupByIdSingle(lineupId: Long): Single<Lineup>

    @Query("""
        SELECT lineups.* FROM lineups
        INNER JOIN tournaments ON lineups.tournamentID = tournaments.id
        INNER JOIN teams ON lineups.teamID = teams.id
        WHERE lineups.tournamentID = :tournamentId AND lineups.teamID = :teamID
    """)
    fun getLineupsForTournament(tournamentId: Long, teamID: Long): LiveData<List<Lineup>>

    @Query("SELECT * FROM lineups ORDER BY editedAt DESC LIMIT 1")
    fun getLastLineup(): Single<Lineup>

    @Query("""
        SELECT tournaments.id as tournamentID,
        tournaments.name as tournamentName,
        tournaments.createdAt as tournamentCreatedAt,
        playerFieldPosition.id as fieldPositionID,
        lineups.name as lineupName,
        lineups.id as lineupID,
        lineups.teamID as teamID,
        x, y, position
        FROM tournaments
        LEFT JOIN lineups ON tournaments.id = lineups.tournamentID
        LEFT JOIN playerFieldPosition ON playerFieldPosition.lineupID = lineups.id
        WHERE teamID = :teamID AND (tournamentName LIKE '%' || :filter || '%' OR lineupName LIKE '%' || :filter || '%')
        ORDER BY tournaments.createdAt DESC
    """)
    fun getAllTournamentsWithLineups(filter: String, teamID: Long): Single<List<TournamentWithLineup>>
}