/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.SetLineupMode
import com.telen.easylineup.domain.usecases.UpdatePlayersWithLineupMode
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
internal class SetLineupModeTests {
    private val extraHitters = 0
    private val observer: TestObserver<SetLineupMode.ResponseValue> = TestObserver()
    @Mock lateinit var teamDao: TeamRepository
    lateinit var setLineupMode: SetLineupMode
    lateinit var lineup: Lineup

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        setLineupMode = SetLineupMode(GetTeam(teamDao), UpdatePlayersWithLineupMode())

        val team = Team(id = 1L, name = "toto", type = TeamType.SOFTBALL.id, main = true)
        Mockito.`when`(teamDao.getTeamsRx()).thenReturn(Single.just(listOf(team)))

        lineup = Lineup(1, "test1", 1, 1, MODE_DISABLED, TeamStrategy.STANDARD.id, extraHitters, 3L)
    }

    private fun startUseCase(mode: Boolean) {
        lineup.mode = if (mode) MODE_DISABLED else MODE_ENABLED
        val lineupMode = if (mode) MODE_ENABLED else MODE_DISABLED
        setLineupMode.executeUseCase(SetLineupMode.RequestValues(mode, lineup, emptyList()))
            .subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(lineupMode, lineup.mode)
    }

    @Test
    fun shouldSaveTheLineupModeNone() {
        startUseCase(false)
    }

    @Test
    fun shouldSaveTheLineupModeDh() {
        startUseCase(true)
    }
}
