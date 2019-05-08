package com.telen.easylineup.data

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface LineupDao {
    @Insert
    fun insertLineup(lineup: Lineup): Single<Long>

    @Insert
    fun insertLineup(lineups: List<Lineup>): Completable

    @Update
    fun updateLineup(lineup: Lineup)

    @Delete
    fun deleteLineup(lineup: Lineup)

    @Query("SELECT * FROM lineups ORDER BY id ASC")
    fun getAllLineup(): LiveData<List<Lineup>>

    @Query("SELECT * FROM lineups WHERE id = :lineupId")
    fun getLineupById(lineupId: Long): LiveData<Lineup>

    @Insert
    fun insertPlayerFieldPosition(fieldPositions: List<PlayerFieldPosition>): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlayerFieldPosition(fieldPositions: PlayerFieldPosition): Single<Long>

    @Query("SELECT * from playerFieldPosition")
    fun getAllPlayerFieldPositions(): LiveData<List<PlayerFieldPosition>>

    @Query("""
        SELECT playerFieldPosition.* FROM playerFieldPosition
        INNER JOIN players ON playerFieldPosition.playerID = players.id
        INNER JOIN lineups ON playerFieldPosition.lineupID = lineups.id
        WHERE playerFieldPosition.lineupID = :lineupId
    """)
    fun getAllPlayerFieldPositionsForLineup(lineupId: Long): LiveData<List<PlayerFieldPosition>>

    @Query("""
        SELECT playerFieldPosition.* FROM playerFieldPosition
        INNER JOIN players ON playerFieldPosition.playerID = players.id
        INNER JOIN lineups ON playerFieldPosition.lineupID = lineups.id
        WHERE playerFieldPosition.lineupID = :lineupID AND playerFieldPosition.playerID = :playerID
    """)
    fun getPlayerPositionFor(lineupID: Long, playerID: Long): Maybe<PlayerFieldPosition>

    @Query("""
        SELECT lineups.* FROM lineups
        INNER JOIN tournaments ON lineups.tournamentID = tournaments.id
        INNER JOIN teams ON lineups.teamID = teams.id
        WHERE lineups.tournamentID = :tournamentId
    """)
    fun getLineupsForTournament(tournamentId: Long): LiveData<List<Lineup>>

    @Query("SELECT * FROM lineups ORDER BY editedAt DESC LIMIT 1")
    fun getLastLineup(): LiveData<Lineup>
}