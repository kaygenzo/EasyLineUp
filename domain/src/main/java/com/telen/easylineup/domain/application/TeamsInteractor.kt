package com.telen.easylineup.domain.application

import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.model.DomainErrors
import com.telen.easylineup.domain.model.StepConfiguration
import com.telen.easylineup.domain.model.Team
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

interface TeamsInteractor {
    fun getTeam(): Single<Team>
    fun getAllTeams(): Single<List<Team>>
    fun getTeamsCount(): Single<Int>

    /** @deprecated **/
    fun insertTeam(team: Team): Single<Long>
    fun updateCurrentTeam(currentTeam: Team): Completable
    fun saveTeam(team: Team): Completable
    fun getTeamType(): Single<Int>
    fun deleteTeam(team: Team): Completable
    fun getTeamCreationNextStep(currentStep: Int, team: Team): Single<StepConfiguration>
    fun getTeamCreationPreviousStep(currentStep: Int, team: Team): Single<StepConfiguration>
    fun observeTeams(): LiveData<List<Team>>
    fun observeErrors(): Subject<DomainErrors.Teams>
}