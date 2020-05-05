package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.TournamentRepository
import com.telen.easylineup.domain.usecases.DeleteTournament
import io.reactivex.Completable
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.util.*


@RunWith(MockitoJUnitRunner::class)
internal class DeleteTournamentTests {

    @Mock lateinit var tournamentDao: TournamentRepository
    lateinit var mDeleteTournament: DeleteTournament
    lateinit var tournament: Tournament

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mDeleteTournament = DeleteTournament(tournamentDao)

        tournament = Tournament(id = 1L, name = "toto", createdAt = Calendar.getInstance().timeInMillis)

        Mockito.`when`(tournamentDao.deleteTournament(tournament)).thenReturn(Completable.complete())
    }

    @Test
    fun shouldDeleteTournament() {
        val observer = TestObserver<DeleteTournament.ResponseValue>()
        mDeleteTournament.executeUseCase(DeleteTournament.RequestValues(tournament))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
    }
}