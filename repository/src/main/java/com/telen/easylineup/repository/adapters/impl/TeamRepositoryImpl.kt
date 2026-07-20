/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.repository.adapters.impl

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.repository.dao.TeamDao
import com.telen.easylineup.repository.model.RoomTeam
import com.telen.easylineup.repository.model.init
import com.telen.easylineup.repository.model.toTeam
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

internal class TeamRepositoryImpl(private val teamDao: TeamDao) : TeamRepository {
    init {
        Timber.i("TeamRepositoryImpl.init")
    }

    override suspend fun insertTeam(team: Team): Long {
        return teamDao.insertTeam(RoomTeam().init(team))
    }

    override suspend fun deleteTeam(team: Team) {
        teamDao.deleteTeam(RoomTeam().init(team))
    }

    override suspend fun deleteTeams(teams: List<Team>) {
        teamDao.deleteTeams(teams.map { RoomTeam().init(it) })
    }

    override suspend fun updateTeam(team: Team) {
        teamDao.updateTeam(RoomTeam().init(team))
    }

    override suspend fun updateTeams(teams: List<Team>) {
        teamDao.updateTeams(teams.map { RoomTeam().init(it) })
    }

    override suspend fun updateTeamsWithRowCount(teams: List<Team>): Int {
        return teamDao.updateTeamsWithRowCount(teams.map { RoomTeam().init(it) })
    }

    override suspend fun getTeamById(teamId: Long): Team {
        return teamDao.getTeamById(teamId).toTeam()
    }

    override suspend fun getTeamByHash(hash: String): Team {
        return teamDao.getTeamByHash(hash).toTeam()
    }

    override fun getTeams(): Flow<List<Team>> {
        return teamDao.getTeams().map { list -> list.map { it.toTeam() } }
    }

    override suspend fun getTeamsRx(): List<Team> {
        return teamDao.getTeamsRx().map { it.toTeam() }
    }
}
