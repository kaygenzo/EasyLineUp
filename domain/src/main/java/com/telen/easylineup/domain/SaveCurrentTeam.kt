package com.telen.easylineup.domain

import android.annotation.SuppressLint
import com.telen.easylineup.repository.data.TeamDao
import com.telen.easylineup.repository.model.Team
import io.reactivex.Observable
import io.reactivex.Single

class SaveCurrentTeam(val dao: TeamDao): UseCase<SaveCurrentTeam.RequestValues, SaveCurrentTeam.ResponseValue>() {

    @SuppressLint("ApplySharedPref")
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return dao.getTeamsRx()
                .flatMapObservable { Observable.fromIterable(it) }
                .map {
                    it.main = it.id == requestValues.team.id
                    it
                }
                .toList()
                .flatMapCompletable { dao.updateTeams(it) }
                .andThen(Single.just(ResponseValue()))
    }

    class ResponseValue: UseCase.ResponseValue
    class RequestValues(val team: Team): UseCase.RequestValues
}