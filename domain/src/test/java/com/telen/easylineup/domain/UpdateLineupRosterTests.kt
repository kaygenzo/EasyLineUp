/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.usecases.UpdateLineupRoster
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class UpdateLineupRosterTests {
    val observer: TestObserver<Void> = TestObserver()
    @Mock lateinit var lineupRepository: LineupRepository
    lateinit var updateLineupRoster: UpdateLineupRoster
    lateinit var lineup: Lineup

    private fun player(id: Long) = Player(id = id, teamId = 1L, name = "p$id", shirtNumber = id.toInt(), licenseNumber = id)

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        updateLineupRoster = UpdateLineupRoster(lineupRepository, testSchedulersProvider())
        lineup = Lineup(id = 1L, name = "test", teamId = 1L, tournamentId = 1L)
        Mockito.`when`(lineupRepository.getLineupByIdSingle(1L)).thenReturn(Single.just(lineup))
        Mockito.`when`(lineupRepository.updateLineup(any())).thenReturn(Completable.complete())
    }

    @Test
    fun shouldBuildRosterStringFromSelectedPlayersOnly() {
        val roster = listOf(
            RosterPlayerStatus(player(1L), status = true, playerNumberOverlay = null),
            RosterPlayerStatus(player(2L), status = false, playerNumberOverlay = null),
            RosterPlayerStatus(player(3L), status = true, playerNumberOverlay = null)
        )

        updateLineupRoster(1L, roster).subscribe(observer)
        observer.await()

        observer.assertComplete()
        val captor = argumentCaptor<Lineup>()
        Mockito.verify(lineupRepository).updateLineup(captor.capture())
        assertEquals("1;3", captor.firstValue.roster)
    }

    @Test
    fun shouldBuildEmptyRosterStringWhenNoPlayerSelected() {
        val roster = listOf(
            RosterPlayerStatus(player(1L), status = false, playerNumberOverlay = null)
        )

        updateLineupRoster(1L, roster).subscribe(observer)
        observer.await()

        observer.assertComplete()
        val captor = argumentCaptor<Lineup>()
        Mockito.verify(lineupRepository).updateLineup(captor.capture())
        assertEquals("", captor.firstValue.roster)
    }

    @Test
    fun shouldPropagateErrorWhenLineupNotFound() {
        val exception = Exception("not found")
        Mockito.`when`(lineupRepository.getLineupByIdSingle(eq(1L))).thenReturn(Single.error(exception))

        updateLineupRoster(1L, emptyList()).subscribe(observer)
        observer.await()

        observer.assertError(exception)
    }
}
