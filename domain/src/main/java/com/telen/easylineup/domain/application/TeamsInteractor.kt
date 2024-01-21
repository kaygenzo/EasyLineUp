/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.application

import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.model.DomainErrors
import com.telen.easylineup.domain.model.Team
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.Subject

interface TeamsInteractor {
    fun getTeam(): Single<Team>
    fun getAllTeams(): Single<List<Team>>
    fun getTeamsCount(): Single<Int>

    /** @deprecated
     * @return **/
    fun insertTeam(team: Team): Single<Long>
    fun updateCurrentTeam(currentTeam: Team): Completable
    fun saveTeam(team: Team): Completable
    fun getTeamType(): Single<Int>
    fun deleteTeam(team: Team): Completable
    fun observeTeams(): LiveData<List<Team>>
    fun observeErrors(): Subject<DomainErrors.Teams>
}
