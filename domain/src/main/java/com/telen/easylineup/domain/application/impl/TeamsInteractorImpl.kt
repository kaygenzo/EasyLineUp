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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class TeamsInteractorImpl : TeamsInteractor, KoinComponent {
    private val teamsRepo: TeamRepository by inject()
    private val getTeam: GetTeam by inject()
    private val getAllTeamsUseCase: GetAllTeams by inject()
    private val saveCurrentTeam: SaveCurrentTeam by inject()
    private val deleteTeamUseCase: DeleteTeam by inject()
    private val saveTeamUseCase: SaveTeam by inject()
    private val checkTeamUseCase: CheckTeam by inject()
    private val errors: PublishSubject<DomainErrors.Teams> = PublishSubject.create()

    override fun getTeam(): Single<Team> {
        return UseCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .map { it.team }
            .doOnError { errors.onNext(DomainErrors.Teams.GET_TEAM_FAILED) }
    }

    override fun getAllTeams(): Single<List<Team>> {
        return UseCaseHandler.execute(getAllTeamsUseCase, GetAllTeams.RequestValues())
            .map { it.teams }
    }

    override fun getTeamsCount(): Single<Int> {
        return getAllTeams().map { it.size }
    }

    override fun insertTeam(team: Team): Single<Long> {
        return teamsRepo.insertTeam(team)
    }

    override fun updateCurrentTeam(currentTeam: Team): Completable {
        return UseCaseHandler.execute(saveCurrentTeam, SaveCurrentTeam.RequestValues(currentTeam))
            .ignoreElement()
    }

    override fun saveTeam(team: Team): Completable {
        return UseCaseHandler.execute(checkTeamUseCase, CheckTeam.RequestValues(team))
            .ignoreElement()
            .andThen(UseCaseHandler.execute(saveTeamUseCase, SaveTeam.RequestValues(team)))
            .map { it.team }
            .flatMap { UseCaseHandler.execute(saveCurrentTeam, SaveCurrentTeam.RequestValues(it)) }
            .ignoreElement()
    }

    override fun getTeamType(): Single<Int> {
        return UseCaseHandler.execute(getTeam, GetTeam.RequestValues())
            .map { it.team.type }
    }

    override fun deleteTeam(team: Team): Completable {
        return UseCaseHandler.execute(deleteTeamUseCase, DeleteTeam.RequestValues(team))
            .ignoreElement()
    }

    override fun observeTeams(): LiveData<List<Team>> {
        return teamsRepo.getTeams()
    }

    override fun observeErrors(): Subject<DomainErrors.Teams> {
        return errors
    }
}
