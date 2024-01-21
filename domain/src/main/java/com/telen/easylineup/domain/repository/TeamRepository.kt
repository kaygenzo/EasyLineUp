/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.repository

import androidx.lifecycle.LiveData
import com.telen.easylineup.domain.model.Team
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface TeamRepository {
    fun insertTeam(team: Team): Single<Long>
    fun deleteTeam(team: Team): Completable
    fun deleteTeams(teams: List<Team>): Completable
    fun updateTeam(team: Team): Completable
    fun updateTeams(teams: List<Team>): Completable
    fun updateTeamsWithRowCount(teams: List<Team>): Single<Int>
    fun getTeamById(teamId: Long): Single<Team>
    fun getTeamByHash(hash: String): Single<Team>
    fun getTeams(): LiveData<List<Team>>
    fun getTeamsRx(): Single<List<Team>>
}
