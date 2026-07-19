/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.ports.SchedulersProvider
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.exceptions.AlreadyExistingTournamentException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNameEmptyException
import io.reactivex.rxjava3.core.Completable

class SaveTournament(
    private val repository: TournamentRepository,
    private val schedulersProvider: SchedulersProvider
) {
    operator fun invoke(tournament: Tournament): Completable {
        return Completable.defer {
            with(tournament) {
                if (name.isEmpty()) {
                    return@defer Completable.error(TournamentNameEmptyException())
                }
                repository.getTournamentByName(name)
                    .flatMapCompletable {
                        Completable.error(AlreadyExistingTournamentException())
                    }
                    .onErrorResumeNext { error ->
                        if (error is AlreadyExistingTournamentException) {
                            Completable.error(error)
                        } else {
                            repository.insertTournament(this).flatMapCompletable {
                                this.id = it
                                Completable.complete()
                            }
                        }
                    }
            }
        }.subscribeOn(schedulersProvider.io())
    }
}
