package com.telen.easylineup.repository.adapters.impl

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.repository.dao.TeamDao
import com.telen.easylineup.repository.model.RoomTeam
import com.telen.easylineup.repository.model.init
import com.telen.easylineup.repository.model.toTeam
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import timber.log.Timber

internal class TeamRepositoryImpl(private val teamDao: TeamDao): TeamRepository {

    init {
        Timber.i("TeamRepositoryImpl.init")
    }

    override fun insertTeam(team: Team): Single<Long> {
        return teamDao.insertTeam(RoomTeam().init(team))
    }

    override fun deleteTeam(team: Team): Completable {
        return teamDao.deleteTeam(RoomTeam().init(team))
    }

    override fun deleteTeams(teams: List<Team>): Completable {
        return teamDao.deleteTeams(teams.map { RoomTeam().init(it) })
    }

    override fun updateTeam(team: Team): Completable {
        return teamDao.updateTeam(RoomTeam().init(team))
    }

    override fun updateTeams(teams: List<Team>): Completable {
        return teamDao.updateTeams(teams.map { RoomTeam().init(it) })
    }

    override fun updateTeamsWithRowCount(teams: List<Team>): Single<Int> {
        return teamDao.updateTeamsWithRowCount(teams.map { RoomTeam().init(it) })
    }

    override fun getTeamById(teamId: Long): Single<Team> {
        return teamDao.getTeamById(teamId).map { it.toTeam() }
    }

    override fun getTeamByHash(hash: String): Single<Team> {
        return teamDao.getTeamByHash(hash).map { it.toTeam() }
    }

    override fun getTeams(): LiveData<List<Team>> {
        return teamDao.getTeams().map {
            it.map { it.toTeam() }
        }
    }

    override fun getTeamsRx(): Single<List<Team>> {
        return teamDao.getTeamsRx().map { it.map { it.toTeam() } }
    }
}