package com.telen.easylineup.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.telen.easylineup.domain.model.*
import com.telen.easylineup.domain.repository.LineupRepository
import com.telen.easylineup.domain.usecases.CreateLineup
import com.telen.easylineup.domain.usecases.exceptions.LineupNameEmptyException
import com.telen.easylineup.domain.usecases.exceptions.TournamentNameEmptyException
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
internal class CreateLineupBaseballStandardTests : CreateLineupTests(TeamStrategy.STANDARD, 0)

@RunWith(MockitoJUnitRunner::class)
internal class CreateLineupSoftballStandardTests : CreateLineupTests(TeamStrategy.STANDARD, 0)

@RunWith(MockitoJUnitRunner::class)
internal class CreateLineupSoftballSlowpitchTests : CreateLineupTests(TeamStrategy.SLOWPITCH, 0)

////////////// CUSTOM HITTER SIZE //////////////

@RunWith(MockitoJUnitRunner::class)
internal class CreateLineupBaseballCustomStandardTests : CreateLineupTests(TeamStrategy.STANDARD, 3)

@RunWith(MockitoJUnitRunner::class)
internal class CreateLineupSoftballCustomStandardTests : CreateLineupTests(TeamStrategy.STANDARD, 3)

@RunWith(MockitoJUnitRunner::class)
internal class CreateLineupSoftballCustomSlowpitchTests :
    CreateLineupTests(TeamStrategy.SLOWPITCH, 3)

@RunWith(MockitoJUnitRunner::class)
internal abstract class CreateLineupTests(strategy: TeamStrategy, extraHitters: Int) {

    @Mock
    private lateinit var lineupDao: LineupRepository
    private lateinit var mCreateLineup: CreateLineup
    private lateinit var roster: MutableList<RosterPlayerStatus>
    private val lineup = Lineup(
        name = "title",
        strategy = strategy.id,
        extraHitters = extraHitters,
        tournamentId = 1L
    )
    private val observer = TestObserver<CreateLineup.ResponseValue>()

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mCreateLineup = CreateLineup(lineupDao)

        roster = mutableListOf(
            RosterPlayerStatus(Player(1, 1, "toto", 1, 1), true, null),
            RosterPlayerStatus(Player(2, 1, "tata", 1, 1), true, null),
            RosterPlayerStatus(Player(3, 1, "titi", 1, 1), true, null)
        )

        Mockito.`when`(lineupDao.insertLineup(any())).thenReturn(Single.just(1L))
    }

    private fun startUseCase(roster: List<RosterPlayerStatus>) {
        mCreateLineup.executeUseCase(CreateLineup.RequestValues(1L, lineup, roster))
            .subscribe(observer)
        observer.await()
        observer.assertComplete()
    }

    @Test
    fun shouldLineupSavedWithRosterNullForAll() {
        startUseCase(roster)
        verify(lineupDao).insertLineup(com.nhaarman.mockitokotlin2.check {
            Assert.assertNull(null, it.roster)
        })
    }

    @Test
    fun shouldLineupSavedWithRosterNotNullForSelection() {
        roster.add(RosterPlayerStatus(Player(4, 1, "tutu", 1, 1), false, null))
        startUseCase(roster)
        verify(lineupDao).insertLineup(com.nhaarman.mockitokotlin2.check {
            Assert.assertEquals("1;2;3", it.roster)
        })
    }

    @Test
    fun shouldSavedSuccessfullyTheNewLineup() {
        startUseCase(roster)
        Assert.assertEquals(1L, observer.values().first().lineup.id)
    }

    @Test
    fun shouldTriggerAnExceptionIfLineupNameEmpty() {
        lineup.name = "      "
        mCreateLineup.executeUseCase(CreateLineup.RequestValues(1L, lineup, roster))
            .subscribe(observer)
        observer.await()
        observer.assertError(LineupNameEmptyException::class.java)
    }

    @Test
    fun shouldTriggerAnExceptionIfTournamentNameEmpty() {
        lineup.tournamentId = 0
        mCreateLineup.executeUseCase(CreateLineup.RequestValues(1L, lineup, roster))
            .subscribe(observer)
        observer.await()
        observer.assertError(TournamentNameEmptyException::class.java)
    }
}