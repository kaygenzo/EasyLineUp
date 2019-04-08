package com.telen.easylineup.data

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Completable

@Dao
interface LineupDao {
    @Insert
    fun insertLineup(lineup: Lineup): Completable

    @Insert
    fun insertLineup(lineups: List<Lineup>): Completable

    @Update
    fun updateLineup(lineup: Lineup)

    @Delete
    fun deleteLineup(lineup: Lineup)

    @Query("SELECT * FROM lineups ORDER BY id ASC")
    fun getAllLineup(): LiveData<List<Lineup>>

    @Query("SELECT * FROM lineups WHERE id = :lineupId")
    fun getLineupById(lineupId: Int): LiveData<Lineup>

    @Insert
    fun insertPlayerFieldPosition(fieldPositions: List<PlayerFieldPosition>): Completable

    @Query("SELECT * from playerFieldPosition")
    fun getAllPlayerFieldPositions(): LiveData<List<PlayerFieldPosition>>

    @Query("""
        SELECT playerFieldPosition.* FROM playerFieldPosition
        INNER JOIN players ON playerFieldPosition.playerID = players.id
        INNER JOIN lineups ON playerFieldPosition.lineupID = lineups.id
        WHERE playerFieldPosition.lineupID = :lineupId
    """)
    fun getAllPlayerFieldPositionsForLineup(lineupId: Int): LiveData<List<PlayerFieldPosition>>
}