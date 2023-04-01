package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.usecases.GetDPAndFlexFromPlayersInField
import com.telen.easylineup.domain.usecases.exceptions.NeedAssignPitcherFirstException
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class GetDpAndFlexFromPlayersInFieldTests : BaseUseCaseTests() {

    lateinit var useCase: GetDPAndFlexFromPlayersInField
    lateinit var players: MutableList<PlayerWithPosition>
    private val observer = TestObserver<GetDPAndFlexFromPlayersInField.ResponseValue>()

    @Before
    fun init() {
        useCase = GetDPAndFlexFromPlayersInField()
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
        val request = GetDPAndFlexFromPlayersInField.RequestValues(players, teamType.id)
        val playersSize = players.size
        useCase.executeUseCase(request).subscribe(observer)
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
    fun shouldTriggerNeedAssignPitcherFirstExceptionIf_PitcherNotAssigned_Baseball() {
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
        observer.values().first().configResult.let {
            Assert.assertNull(it.dp)
            Assert.assertEquals(players.first { it.playerID == 1L }, it.flex)
            Assert.assertFalse(it.dpLocked)
            Assert.assertTrue(it.flexLocked)
        }
    }

    @Test
    fun shouldReturnDPAndFlexPitcherBaseball() {
        startUseCase(teamType = TeamType.BASEBALL)
        observer.values().first().configResult.let {
            Assert.assertEquals(players.first { it.playerID == 3L }, it.dp)
            Assert.assertEquals(players.first { it.playerID == 1L }, it.flex)
            Assert.assertFalse(it.dpLocked)
            Assert.assertTrue(it.flexLocked)
        }
    }

    @Test
    fun shouldReturnDPAndFlexRightFieldSoftball() {
        players.first { it.isRightField() }.flags = PlayerFieldPosition.FLAG_FLEX
        startUseCase(teamType = TeamType.SOFTBALL)
        observer.values().first().configResult.let {
            Assert.assertEquals(players.first { it.playerID == 3L }, it.dp)
            Assert.assertEquals(players.first { it.playerID == 2L }, it.flex)
            Assert.assertFalse(it.dpLocked)
            Assert.assertFalse(it.flexLocked)
        }
    }

    @Test
    fun shouldReturnOnlyFlexRightFieldSoftball() {
        players.first { it.isRightField() }.flags = PlayerFieldPosition.FLAG_FLEX
        players.removeIf { it.isDpDh() }
        startUseCase(teamType = TeamType.SOFTBALL)
        observer.values().first().configResult.let {
            Assert.assertNull(it.dp)
            Assert.assertEquals(players.first { it.playerID == 2L }, it.flex)
            Assert.assertFalse(it.dpLocked)
            Assert.assertFalse(it.flexLocked)
        }
    }

    @Test
    fun shouldReturnDpAndFlexWithSlowPitch() {
        players.add(generate(6L, FieldPosition.SLOWPITCH_RF, PlayerFieldPosition.FLAG_FLEX, 6))
        startUseCase(teamType = TeamType.SOFTBALL)
        observer.values().first().configResult.let {
            Assert.assertEquals(players.first { it.playerID == 6L }, it.flex)
        }
    }

    @Test
    fun shouldReturnDpAndFlexWithBaseball5() {
        players.add(generate(6L, FieldPosition.MID_FIELDER, PlayerFieldPosition.FLAG_FLEX, 6))
        startUseCase(teamType = TeamType.SOFTBALL)
        observer.values().first().configResult.let {
            Assert.assertEquals(players.first { it.playerID == 6L }, it.flex)
        }
    }
}