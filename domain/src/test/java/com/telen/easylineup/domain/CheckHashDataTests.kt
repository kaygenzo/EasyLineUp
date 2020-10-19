package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.repository.*
import com.telen.easylineup.domain.usecases.CheckHashData
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
internal class CheckHashDataTests {

    @Mock lateinit var teamDao: TeamRepository
    @Mock lateinit var playerDao: PlayerRepository
    @Mock lateinit var tournamentDao: TournamentRepository
    @Mock lateinit var lineupDao: LineupRepository
    @Mock lateinit var playerPositionsDao: PlayerFieldPositionRepository

    private lateinit var mCheckHash: CheckHashData

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mCheckHash = CheckHashData(teamDao, playerDao, tournamentDao, lineupDao, playerPositionsDao)

        val teams = mutableListOf(
                Team(1L, "A", null, 0, true, null),
                Team(2L, "B", null, 0, false, null)
        )

        val mPlayers = mutableListOf(
                Player(1L, 1L, "A", 1, 1L, null, 1, 0, 0, null),
                Player(2L, 2L, "B", 2, 2L, null, 1, 0, 0 , null)
        )

        val mTournaments = mutableListOf(
                Tournament(1L, "A", 1L, null),
                Tournament(2L, "B", 2L, null)
        )

        val mLineups = mutableListOf(
                Lineup(1L, "A", 1L, 1L, 0, 3L, 1L, 2L, null, null),
                Lineup(2L, "B", 2L, 2L, 0, 3L, 1L, 3L, null, null)
        )

        val mPlayerPosition = mutableListOf(
                PlayerFieldPosition(1L, 1L, 1L, 0, 0f, 0f, 1, PlayerFieldPosition.FLAG_NONE,null),
                PlayerFieldPosition(2L, 2L, 2L, 0, 0f, 0f, 1, PlayerFieldPosition.FLAG_NONE,null)
        )

        Mockito.`when`(teamDao.getTeamsRx()).thenReturn(Single.just(teams))
        Mockito.`when`(teamDao.updateTeamsWithRowCount(any())).thenReturn(Single.just(2))

        Mockito.`when`(playerDao.getPlayers()).thenReturn(Single.just(mPlayers))
        Mockito.`when`(playerDao.updatePlayersWithRowCount(any())).thenReturn(Single.just(2))

        Mockito.`when`(tournamentDao.getTournaments()).thenReturn(Single.just(mTournaments))
        Mockito.`when`(tournamentDao.updateTournamentsWithRowCount(any())).thenReturn(Single.just(2))

        Mockito.`when`(lineupDao.getLineups()).thenReturn(Single.just(mLineups))
        Mockito.`when`(lineupDao.updateLineupsWithRowCount(any())).thenReturn(Single.just(2))

        Mockito.`when`(playerPositionsDao.getPlayerFieldPositions()).thenReturn(Single.just(mPlayerPosition))
        Mockito.`when`(playerPositionsDao.updatePlayerFieldPositionsWithRowCount(any())).thenReturn(Single.just(2))
    }

    @Test
    fun shouldUpdateHashForAllEntries() {

        val observer = TestObserver<CheckHashData.ResponseValue>()
        mCheckHash.executeUseCase(CheckHashData.RequestValues())
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
        verify(teamDao).updateTeamsWithRowCount(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(0, it.filter { it.hash == null }.size)
        })

        verify(playerDao).updatePlayersWithRowCount(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(0, it.filter { it.hash == null }.size)
        })

        verify(tournamentDao).updateTournamentsWithRowCount(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(0, it.filter { it.hash == null }.size)
        })

        verify(lineupDao).updateLineupsWithRowCount(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(0, it.filter { it.hash == null }.size)
        })

        verify(playerPositionsDao).updatePlayerFieldPositionsWithRowCount(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals(0, it.filter { it.hash == null }.size)
        })

        Assert.assertArrayEquals(intArrayOf(2,2,2,2,2), observer.values().first().updateResult)
    }
}