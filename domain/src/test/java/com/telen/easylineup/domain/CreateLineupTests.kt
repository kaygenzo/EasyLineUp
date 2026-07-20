/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.RosterPlayerStatus
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.CreateLineup
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.exceptions.LineupNameEmptyException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNameEmptyException
import io.reactivex.rxjava3.core.Single
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
internal class CreateLineupBaseballStandardTests : CreateLineupTests() {
    @Before
    override fun init() {
        strategy = TeamStrategy.STANDARD
        extraHitters = 0
        super.init()
    }
}

@RunWith(MockitoJUnitRunner::class)
internal class CreateLineupSoftballStandardTests : CreateLineupTests() {
    @Before
    override fun init() {
        strategy = TeamStrategy.STANDARD
        extraHitters = 0
        super.init()
    }
}

@RunWith(MockitoJUnitRunner::class)
internal class CreateLineupSoftballSlowpitchTests : CreateLineupTests() {
    @Before
    override fun init() {
        strategy = TeamStrategy.SLOWPITCH
        extraHitters = 0
        super.init()
    }
}

////////////// CUSTOM HITTER SIZE //////////////

@RunWith(MockitoJUnitRunner::class)
internal class CreateLineupBaseballCustomStandardTests : CreateLineupTests() {
    @Before
    override fun init() {
        strategy = TeamStrategy.STANDARD
        extraHitters = 3
        super.init()
    }
}

@RunWith(MockitoJUnitRunner::class)
internal class CreateLineupSoftballCustomStandardTests : CreateLineupTests() {
    @Before
    override fun init() {
        strategy = TeamStrategy.STANDARD
        extraHitters = 3
        super.init()
    }
}

@RunWith(MockitoJUnitRunner::class)
internal class CreateLineupSoftballCustomSlowpitchTests : CreateLineupTests() {
    @Before
    override fun init() {
        strategy = TeamStrategy.SLOWPITCH
        extraHitters = 3
        super.init()
    }
}

internal open class CreateLineupTests {
    var strategy: TeamStrategy = TeamStrategy.STANDARD
    var extraHitters: Int = 0
    private lateinit var lineup: Lineup

    @Mock
    private lateinit var lineupDao: LineupRepository
    @Mock
    private lateinit var teamDao: TeamRepository
    private lateinit var createLineup: CreateLineup
    private lateinit var roster: MutableList<RosterPlayerStatus>

    @Before
    open fun init() {
        MockitoAnnotations.initMocks(this)
        createLineup = CreateLineup(lineupDao, GetTeam(teamDao, testSchedulersProvider()), testDispatcherProvider())

        Mockito.`when`(teamDao.getTeamsRx())
            .thenReturn(Single.just(listOf(Team(id = 1L, name = "toto", main = true))))

        lineup = Lineup(
            name = "title",
            strategy = strategy.id,
            extraHitters = extraHitters,
            tournamentId = 1L
        )

        roster = mutableListOf(
            RosterPlayerStatus(Player(1, 1, "toto", 1, 1), true, null),
            RosterPlayerStatus(Player(2, 1, "tata", 1, 1), true, null),
            RosterPlayerStatus(Player(3, 1, "titi", 1, 1), true, null)
        )

        Mockito.`when`(lineupDao.insertLineup(any())).thenReturn(Single.just(1L))
    }

    private suspend fun startUseCase(roster: List<RosterPlayerStatus>): Result<Lineup> {
        val result = createLineup(lineup, roster)
        assertTrue(result.isSuccess)
        return result
    }

    @Test
    fun shouldLineupSavedWithRosterNullForAll() = runTest {
        startUseCase(roster)
        verify(lineupDao).insertLineup(check {
            Assert.assertNull(null, it.roster)
        })
    }

    @Test
    fun shouldLineupSavedWithRosterNotNullForSelection() = runTest {
        roster.add(RosterPlayerStatus(Player(4, 1, "tutu", 1, 1), false, null))
        startUseCase(roster)
        verify(lineupDao).insertLineup(check {
            Assert.assertEquals("1;2;3", it.roster)
        })
    }

    @Test
    fun shouldSavedSuccessfullyTheNewLineup() = runTest {
        val result = startUseCase(roster)
        Assert.assertEquals(1L, result.getOrNull()?.id)
    }

    @Test
    fun shouldTriggerAnExceptionIfLineupNameEmpty() = runTest {
        lineup.name = "      "
        val result = createLineup(lineup, roster)
        assertTrue(result.exceptionOrNull() is LineupNameEmptyException)
    }

    @Test
    fun shouldTriggerAnExceptionIfTournamentNameEmpty() = runTest {
        lineup.tournamentId = 0
        val result = createLineup(lineup, roster)
        assertTrue(result.exceptionOrNull() is TournamentNameEmptyException)
    }
}
