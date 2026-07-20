/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.repository

import com.telen.easylineup.domain.model.Tournament
import kotlinx.coroutines.flow.Flow

interface TournamentRepository {
    suspend fun getTournaments(): List<Tournament>
    fun observeTournaments(): Flow<List<Tournament>>
    suspend fun getTournamentByHash(hash: String): Tournament
    suspend fun getTournamentByName(name: String): Tournament
    suspend fun insertTournament(tournament: Tournament): Long
    suspend fun insertTournaments(tournaments: List<Tournament>)
    suspend fun updateTournament(tournament: Tournament)
    suspend fun updateTournamentsWithRowCount(tournaments: List<Tournament>): Int
    suspend fun deleteTournament(tournament: Tournament)
    suspend fun deleteTournaments(tournaments: List<Tournament>)
}
