/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.Tournament
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.GetAllTournamentsWithLineupsUseCase
import com.telen.easylineup.domain.usecases.GetTeam
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
internal class GetAllTournamentsWithLineupsUseCaseTests {
    @Mock lateinit var lineupDao: LineupRepository
    @Mock lateinit var teamDao: TeamRepository
    lateinit var getAllTournamentsWithLineups: GetAllTournamentsWithLineupsUseCase
    lateinit var team: Team

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        team = Team(id = 1L, name = "toto", main = true)
        getAllTournamentsWithLineups = GetAllTournamentsWithLineupsUseCase(
            lineupDao,
            GetTeam(teamDao, testSchedulersProvider()),
            testSchedulersProvider()
        )

        Mockito.`when`(teamDao.getTeamsRx()).thenReturn(Single.just(listOf(team)))
    }

    @Test
    fun shouldReturnEmptyListWhenNoTournamentsForCurrentTeam() {
        Mockito.`when`(lineupDao.getAllTournamentsWithLineups("summer", team.id))
            .thenReturn(Single.just(emptyList()))

        val observer = TestObserver<List<Pair<Tournament, List<Lineup>>>>()
        getAllTournamentsWithLineups("summer")
            .subscribe(observer)
        observer.await()

        observer.assertComplete()
        Assert.assertTrue(observer.values().first().isEmpty())
        Mockito.verify(lineupDao).getAllTournamentsWithLineups("summer", team.id)
    }
}
