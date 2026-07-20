/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.repository

import com.telen.easylineup.domain.model.Team
import kotlinx.coroutines.flow.Flow

interface TeamRepository {
    suspend fun insertTeam(team: Team): Long
    suspend fun deleteTeam(team: Team)
    suspend fun deleteTeams(teams: List<Team>)
    suspend fun updateTeam(team: Team)
    suspend fun updateTeams(teams: List<Team>)
    suspend fun updateTeamsWithRowCount(teams: List<Team>): Int
    suspend fun getTeamById(teamId: Long): Team
    suspend fun getTeamByHash(hash: String): Team
    fun getTeams(): Flow<List<Team>>
    suspend fun getTeamsRx(): List<Team>
}
