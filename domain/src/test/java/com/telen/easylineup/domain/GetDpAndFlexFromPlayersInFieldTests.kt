/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.DpAndFlexConfiguration
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerFieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.isDpDh
import com.telen.easylineup.domain.model.isPitcher
import com.telen.easylineup.domain.model.isRightField
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.GetDpAndFlexFromPlayersInField
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignPitcherFirstException
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
internal class GetDpAndFlexFromPlayersInFieldTests : BaseUseCaseTests() {
    private val observer: TestObserver<DpAndFlexConfiguration> =
        TestObserver()
    @Mock lateinit var teamDao: TeamRepository
    lateinit var useCase: GetDpAndFlexFromPlayersInField
    lateinit var players: MutableList<PlayerWithPosition>

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        useCase = GetDpAndFlexFromPlayersInField(
            GetTeam(teamDao, testSchedulersProvider()),
            testSchedulersProvider()
        )
        val noFlag = PlayerFieldPosition.FLAG_NONE
        players = mutableListOf(
            generate(1L, FieldPosition.PITCHER, noFlag, 1),
            generate(2L, FieldPosition.RIGHT_FIELD, noFlag, 2),
            generate(3L, FieldPosition.DP_DH, noFlag, 3),
            generate(4L, FieldPosition.SHORT_STOP, noFlag, 4),
            generate(5L, FieldPosition.SUBSTITUTE, noFlag, Constants.SUBSTITUTE_ORDER_VALUE)
        )
    }

    private fun startUseCase(
        players: List<PlayerWithPosition> = this.players,
        teamType: TeamType,
        exception: Class<out Throwable>? = null
    ) {
        Mockito.`when`(teamDao.getTeamsRx())
            .thenReturn(Single.just(listOf(Team(id = 1L, name = "toto", type = teamType.id, main = true))))
        val playersSize = players.size
        useCase(players).subscribe(observer)
        observer.await()
        exception?.let {
            observer.assertError(exception)
        } ?: let {
            observer.assertComplete()
            Assert.assertEquals("Size of player list must not change", playersSize, players.size)
        }
    }

    @Test
    fun shouldTriggerNeedAssignPitcherFirstExceptionIfListEmpty() {
        startUseCase(
            players = mutableListOf(),
            teamType = TeamType.BASEBALL,
            exception = NeedAssignPitcherFirstException::class.java
        )
    }

    @Test
    fun shouldTriggerNeedAssignPitcherFirstExceptionIfPitcherNotAssignedBaseball() {
        players.removeIf { it.isPitcher() }
        startUseCase(
            teamType = TeamType.BASEBALL,
            exception = NeedAssignPitcherFirstException::class.java
        )
    }

    @Test
    fun shouldReturnOnlyFlexPitcherBaseball() {
        players.removeIf { it.isDpDh() }
        startUseCase(teamType = TeamType.BASEBALL)
        observer.values().first().let {
            Assert.assertNull(it.dp)
            Assert.assertEquals(players.first { it.playerId == 1L }, it.flex)
            Assert.assertFalse(it.dpLocked)
            Assert.assertTrue(it.flexLocked)
        }
    }

    @Test
    fun shouldReturnDpandFlexPitcherBaseball() {
        startUseCase(teamType = TeamType.BASEBALL)
        observer.values().first().let {
            Assert.assertEquals(players.first { it.playerId == 3L }, it.dp)
            Assert.assertEquals(players.first { it.playerId == 1L }, it.flex)
            Assert.assertFalse(it.dpLocked)
            Assert.assertTrue(it.flexLocked)
        }
    }

    @Test
    fun shouldReturnDpandFlexRightFieldSoftball() {
        players.first { it.isRightField() }.flags = PlayerFieldPosition.FLAG_FLEX
        startUseCase(teamType = TeamType.SOFTBALL)
        observer.values().first().let {
            Assert.assertEquals(players.first { it.playerId == 3L }, it.dp)
            Assert.assertEquals(players.first { it.playerId == 2L }, it.flex)
            Assert.assertFalse(it.dpLocked)
            Assert.assertFalse(it.flexLocked)
        }
    }

    @Test
    fun shouldReturnOnlyFlexRightFieldSoftball() {
        players.first { it.isRightField() }.flags = PlayerFieldPosition.FLAG_FLEX
        players.removeIf { it.isDpDh() }
        startUseCase(teamType = TeamType.SOFTBALL)
        observer.values().first().let {
            Assert.assertNull(it.dp)
            Assert.assertEquals(players.first { it.playerId == 2L }, it.flex)
            Assert.assertFalse(it.dpLocked)
            Assert.assertFalse(it.flexLocked)
        }
    }

    @Test
    fun shouldReturnDpAndFlexWithSlowPitch() {
        players.add(generate(6L, FieldPosition.SLOWPITCH_RF, PlayerFieldPosition.FLAG_FLEX, 6))
        startUseCase(teamType = TeamType.SOFTBALL)
        observer.values().first().let {
            Assert.assertEquals(players.first { it.playerId == 6L }, it.flex)
        }
    }

    @Test
    fun shouldReturnDpAndFlexWithBaseball5() {
        players.add(generate(6L, FieldPosition.MID_FIELDER, PlayerFieldPosition.FLAG_FLEX, 6))
        startUseCase(teamType = TeamType.SOFTBALL)
        observer.values().first().let {
            Assert.assertEquals(players.first { it.playerId == 6L }, it.flex)
        }
    }
}
