/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.usecases.DeleteTournamentLineups
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class DeleteTournamentLineupsTests {
    private val lineups: MutableList<Lineup> = mutableListOf()
    val observer: TestObserver<DeleteTournamentLineups.ResponseValue> = TestObserver()
    @Mock lateinit var lineupsDao: LineupRepository
    lateinit var deleteTournament: DeleteTournamentLineups
    lateinit var tournament: Tournament
    lateinit var team: Team

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        deleteTournament = DeleteTournamentLineups(lineupsDao)

        tournament = Tournament(id = 1L, name = "toto", createdAt = 1L, 2L, 3L, null)
        team = Team(id = 1L, name = "toto", main = true)
        lineups.addAll(listOf(
            Lineup(id = 1L, teamId = team.id, tournamentId = tournament.id, eventTimeInMillis = 1L,
                strategy = TeamStrategy.STANDARD.id, extraHitters = 0),
            Lineup(id = 2L, teamId = team.id, tournamentId = tournament.id, eventTimeInMillis = 1L,
                strategy = TeamStrategy.STANDARD.id, extraHitters = 0),
            Lineup(id = 3L, teamId = 2L, tournamentId = tournament.id, eventTimeInMillis = 1L,
                strategy = TeamStrategy.STANDARD.id, extraHitters = 0),
            Lineup(id = 4L, teamId = team.id, tournamentId = 2L, eventTimeInMillis = 1L,
                strategy = TeamStrategy.STANDARD.id, extraHitters = 0)
        ))

        Mockito.`when`(lineupsDao.getLineupsForTournamentRx(tournament.id, team.id)).thenReturn(Single.just(listOf(
            lineups[0], lineups[1]
        )))
        Mockito.`when`(lineupsDao.deleteLineups(any())).thenReturn(Completable.complete())
    }

    @Test
    fun shouldDeleteOnlyLineupOfSpecificTournamentAndTeam() {
        deleteTournament.executeUseCase(DeleteTournamentLineups.RequestValues(tournament, team))
            .subscribe(observer)
        observer.await()
        observer.assertComplete()

        verify(lineupsDao).deleteLineups(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(2, it.size)
            Assert.assertEquals(1L, it[0].id)
            Assert.assertEquals(2L, it[1].id)
        })
    }
}
