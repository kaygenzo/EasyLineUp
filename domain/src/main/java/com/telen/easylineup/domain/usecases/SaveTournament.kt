/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.usecases

import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.exceptions.AlreadyExistingTournamentException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNameEmptyException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.withContext

class SaveTournament(
    private val repository: TournamentRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend operator fun invoke(tournament: Tournament): Result<Unit> = runCatchingCancellable {
        withContext(dispatcherProvider.io()) {
            if (tournament.name.isEmpty()) {
                throw TournamentNameEmptyException()
            }

            val alreadyExists = try {
                repository.getTournamentByName(tournament.name).await()
                true
            } catch (c: CancellationException) {
                throw c
            } catch (e: Exception) {
                false
            }

            if (alreadyExists) {
                throw AlreadyExistingTournamentException()
            }

            tournament.id = repository.insertTournament(tournament).await()
        }
    }
}
