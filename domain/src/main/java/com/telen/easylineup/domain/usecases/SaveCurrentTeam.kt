package com.telen.easylineup.domain.usecases

import android.annotation.SuppressLint
import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

internal class SaveCurrentTeam(val dao: TeamRepository): UseCase<SaveCurrentTeam.RequestValues, SaveCurrentTeam.ResponseValue>() {

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