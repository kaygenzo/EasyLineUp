/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.application.impl

import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.UseCaseHandler
import com.telen.easylineup.domain.application.TeamsInteractor
import com.telen.easylineup.domain.model.DomainErrors
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.CheckTeam
import com.telen.easylineup.domain.usecases.DeleteTeam
import com.telen.easylineup.domain.usecases.GetAllTeams
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.SaveCurrentTeam
import com.telen.easylineup.domain.usecases.SaveTeam
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

internal class TeamsInteractorImpl(
    private val teamsRepo: TeamRepository,
    private val getTeam: GetTeam,
    private val getAllTeamsUseCase: GetAllTeams,
    private val saveCurrentTeam: SaveCurrentTeam,
    private val deleteTeamUseCase: DeleteTeam,
    private val saveTeamUseCase: SaveTeam,
    private val checkTeamUseCase: CheckTeam,
    private val useCaseHandler: UseCaseHandler
) : TeamsInteractor {

    private val errors: PublishSubject<DomainErrors.Teams> = PublishSubject.create()

    override fun getTeam(): Single<Team> {
        return useCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .map { it.team }
            .doOnError { errors.onNext(DomainErrors.Teams.GET_TEAM_FAILED) }
    }

    override fun getAllTeams(): Single<List<Team>> {
        return useCaseHandler.execute(getAllTeamsUseCase, GetAllTeams.RequestValues())
            .map { it.teams }
    }

    override fun getTeamsCount(): Single<Int> {
        return getAllTeams().map { it.size }
    }

    override fun insertTeam(team: Team): Single<Long> {
        return teamsRepo.insertTeam(team)
    }

    override fun updateCurrentTeam(currentTeam: Team): Completable {
        return useCaseHandler.execute(saveCurrentTeam, SaveCurrentTeam.RequestValues(currentTeam))
            .ignoreElement()
    }

    override fun saveTeam(team: Team): Completable {
        return useCaseHandler.execute(checkTeamUseCase, CheckTeam.RequestValues(team))
            .ignoreElement()
            .andThen(useCaseHandler.execute(saveTeamUseCase, SaveTeam.RequestValues(team)))
            .map { it.team }
            .flatMap { useCaseHandler.execute(saveCurrentTeam, SaveCurrentTeam.RequestValues(it)) }
            .ignoreElement()
    }

    override fun getTeamType(): Single<Int> {
        return useCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .map { it.team.type }
    }

    override fun deleteTeam(team: Team): Completable {
        return useCaseHandler.execute(deleteTeamUseCase, DeleteTeam.RequestValues(team))
            .ignoreElement()
    }

    override fun observeTeams(): LiveData<List<Team>> {
        return teamsRepo.getTeams()
    }

    override fun observeErrors(): Subject<DomainErrors.Teams> {
        return errors
    }
}
