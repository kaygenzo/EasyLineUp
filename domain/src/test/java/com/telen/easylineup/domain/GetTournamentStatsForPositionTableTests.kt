/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.GetTournamentStatsForPositionTable
import com.telen.easylineup.domain.usecases.StringResourcesProvider
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetTournamentStatsForPositionTableTests {
    @Mock lateinit var lineupDao: LineupRepository
    @Mock lateinit var teamDao: TeamRepository
    @Mock lateinit var stringResourcesProvider: StringResourcesProvider
    lateinit var getTournamentStatsForPositionTable: GetTournamentStatsForPositionTable
    lateinit var team: Team
    lateinit var tournament: Tournament

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        team = Team(id = 1L, name = "toto", type = 0, main = true)
        tournament = Tournament(id = 5L, name = "Summer cup", createdAt = 1000L, startTime = 2000L, endTime = 3000L)
        getTournamentStatsForPositionTable =
            GetTournamentStatsForPositionTable(
                stringResourcesProvider, lineupDao, GetTeam(teamDao, testSchedulersProvider()),
                testSchedulersProvider()
            )

        Mockito.`when`(teamDao.getTeamsRx()).thenReturn(Single.just(listOf(team)))
    }

    @Test
    fun shouldPropagateRepositoryErrorWhenComputingPositionStats() {
        val error = RuntimeException("db error")
        Mockito.`when`(lineupDao.getAllPlayerPositionsForTournament(tournament.id, team.id))
            .thenReturn(Single.error(error))

        val observer = TestObserver<com.telen.easylineup.domain.model.TournamentStatsUiConfig>()
        getTournamentStatsForPositionTable(tournament, TeamStrategy.STANDARD)
            .subscribe(observer)
        observer.await()

        observer.assertError(error)
        Mockito.verify(lineupDao).getAllPlayerPositionsForTournament(tournament.id, team.id)
    }
}
