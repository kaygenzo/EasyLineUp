/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.ports.repository

import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.ShirtNumberEntry
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    suspend fun insertPlayer(player: Player): Long
    suspend fun insertPlayers(players: List<Player>)
    suspend fun deletePlayer(player: Player)
    suspend fun updatePlayer(player: Player)
    suspend fun updatePlayersWithRowCount(players: List<Player>): Int
    suspend fun getPlayerByHash(hash: String): Player?
    fun observePlayerById(playerId: Long): Flow<Player>
    suspend fun getPlayerById(playerId: Long): Player
    suspend fun getPlayersByTeamId(teamId: Long): List<Player>
    suspend fun getPlayers(): List<Player>
    fun observePlayers(teamId: Long): Flow<List<Player>>
    fun getTeamPlayersAndMaybePositions(lineupId: Long): Flow<List<PlayerWithPosition>>
    suspend fun getShirtNumberFromPlayers(teamId: Long, number: Int): List<ShirtNumberEntry>
}
