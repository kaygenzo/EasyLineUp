/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.adapters.impl

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.repository.dao.TeamDao
import com.telen.easylineup.repository.model.toDomain
import com.telen.easylineup.repository.model.toRoom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

internal class TeamRepositoryImpl(private val teamDao: TeamDao) : TeamRepository {
    init {
        Timber.i("TeamRepositoryImpl.init")
    }

    override suspend fun insertTeam(team: Team): Long {
        return teamDao.insertTeam(team.toRoom())
    }

    override suspend fun deleteTeam(team: Team) {
        teamDao.deleteTeam(team.toRoom())
    }

    override suspend fun deleteTeams(teams: List<Team>) {
        teamDao.deleteTeams(teams.map { it.toRoom() })
    }

    override suspend fun updateTeam(team: Team) {
        teamDao.updateTeam(team.toRoom())
    }

    override suspend fun updateTeams(teams: List<Team>) {
        teamDao.updateTeams(teams.map { it.toRoom() })
    }

    override suspend fun updateTeamsWithRowCount(teams: List<Team>): Int {
        return teamDao.updateTeamsWithRowCount(teams.map { it.toRoom() })
    }

    override suspend fun getTeamById(teamId: Long): Team {
        return teamDao.getTeamById(teamId).toDomain()
    }

    override suspend fun getTeamByHash(hash: String): Team {
        return teamDao.getTeamByHash(hash).toDomain()
    }

    override fun getTeams(): Flow<List<Team>> {
        return teamDao.getTeams().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getTeamsRx(): List<Team> {
        return teamDao.getTeamsRx().map { it.toDomain() }
    }
}
