/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.UseCase
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.exceptions.AlreadyExistingTournamentException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNameEmptyException
import io.reactivex.rxjava3.core.Single

/**
 * @property repository
 */
internal class SaveTournament(val repository: TournamentRepository) :
    UseCase<SaveTournament.RequestValues, SaveTournament.ResponseValue>() {
    override fun executeUseCase(requestValues: RequestValues): Single<ResponseValue> {
        return Single.defer {
            with(requestValues.tournament) {
                repository.getTournamentByName(name)
                    .flatMap {
                        Single.error<ResponseValue>(AlreadyExistingTournamentException())
                    }
                    .onErrorResumeNext {
                        if (name.isEmpty()) {
                            Single.error(TournamentNameEmptyException())
                        } else {
                            repository.insertTournament(this).map {
                                this.id = it
                                ResponseValue()
                            }
                        }
                    }
            }
        }
    }

    class ResponseValue : UseCase.ResponseValue
    /**
     * @property tournament
     */
    class RequestValues(val tournament: Tournament) : UseCase.RequestValues
}
