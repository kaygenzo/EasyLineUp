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
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetDpAndFlexFromPlayersInFieldTests : BaseUseCaseTests() {
    @Mock lateinit var teamDao: TeamRepository
    lateinit var useCase: GetDpAndFlexFromPlayersInField
    lateinit var players: MutableList<PlayerWithPosition>

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        useCase = GetDpAndFlexFromPlayersInField(
            GetTeam(teamDao, testDispatcherProvider()),
            testDispatcherProvider()
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

    private suspend fun startUseCase(
        players: List<PlayerWithPosition> = this.players,
        teamType: TeamType,
        exception: Class<out Throwable>? = null
    ): Result<DpAndFlexConfiguration> {
        Mockito.`when`(teamDao.getTeamsRx())
            .thenReturn(listOf(Team(id = 1L, name = "toto", type = teamType.id, main = true)))
        val playersSize = players.size
        val result = useCase(players)
        exception?.let {
            assertTrue(exception.isInstance(result.exceptionOrNull()))
        } ?: let {
            assertTrue(result.isSuccess)
            Assert.assertEquals("Size of player list must not change", playersSize, players.size)
        }
        return result
    }

    @Test
    fun shouldTriggerNeedAssignPitcherFirstExceptionIfListEmpty() = runTest {
        startUseCase(
            players = mutableListOf(),
            teamType = TeamType.BASEBALL,
            exception = NeedAssignPitcherFirstException::class.java
        )
    }

    @Test
    fun shouldTriggerNeedAssignPitcherFirstExceptionIfPitcherNotAssignedBaseball() = runTest {
        players.removeIf { it.isPitcher() }
        startUseCase(
            teamType = TeamType.BASEBALL,
            exception = NeedAssignPitcherFirstException::class.java
        )
    }

    @Test
    fun shouldReturnOnlyFlexPitcherBaseball() = runTest {
        players.removeIf { it.isDpDh() }
        val result = startUseCase(teamType = TeamType.BASEBALL)
        result.getOrThrow().let {
            Assert.assertNull(it.dp)
            Assert.assertEquals(players.first { it.playerId == 1L }, it.flex)
            Assert.assertFalse(it.dpLocked)
            Assert.assertTrue(it.flexLocked)
        }
    }

    @Test
    fun shouldReturnDpandFlexPitcherBaseball() = runTest {
        val result = startUseCase(teamType = TeamType.BASEBALL)
        result.getOrThrow().let {
            Assert.assertEquals(players.first { it.playerId == 3L }, it.dp)
            Assert.assertEquals(players.first { it.playerId == 1L }, it.flex)
            Assert.assertFalse(it.dpLocked)
            Assert.assertTrue(it.flexLocked)
        }
    }

    @Test
    fun shouldReturnDpandFlexRightFieldSoftball() = runTest {
        players.first { it.isRightField() }.flags = PlayerFieldPosition.FLAG_FLEX
        val result = startUseCase(teamType = TeamType.SOFTBALL)
        result.getOrThrow().let {
            Assert.assertEquals(players.first { it.playerId == 3L }, it.dp)
            Assert.assertEquals(players.first { it.playerId == 2L }, it.flex)
            Assert.assertFalse(it.dpLocked)
            Assert.assertFalse(it.flexLocked)
        }
    }

    @Test
    fun shouldReturnOnlyFlexRightFieldSoftball() = runTest {
        players.first { it.isRightField() }.flags = PlayerFieldPosition.FLAG_FLEX
        players.removeIf { it.isDpDh() }
        val result = startUseCase(teamType = TeamType.SOFTBALL)
        result.getOrThrow().let {
            Assert.assertNull(it.dp)
            Assert.assertEquals(players.first { it.playerId == 2L }, it.flex)
            Assert.assertFalse(it.dpLocked)
            Assert.assertFalse(it.flexLocked)
        }
    }

    @Test
    fun shouldReturnDpAndFlexWithSlowPitch() = runTest {
        players.add(generate(6L, FieldPosition.SLOWPITCH_RF, PlayerFieldPosition.FLAG_FLEX, 6))
        val result = startUseCase(teamType = TeamType.SOFTBALL)
        result.getOrThrow().let {
            Assert.assertEquals(players.first { it.playerId == 6L }, it.flex)
        }
    }

    @Test
    fun shouldReturnDpAndFlexWithBaseball5() = runTest {
        players.add(generate(6L, FieldPosition.MID_FIELDER, PlayerFieldPosition.FLAG_FLEX, 6))
        val result = startUseCase(teamType = TeamType.SOFTBALL)
        result.getOrThrow().let {
            Assert.assertEquals(players.first { it.playerId == 6L }, it.flex)
        }
    }
}
