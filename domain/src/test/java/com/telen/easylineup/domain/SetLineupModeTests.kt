package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.MODE_DISABLED
import com.telen.easylineup.domain.model.MODE_ENABLED
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.usecases.SetLineupMode
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class SetLineupModeTests {

    lateinit var mSetLineupMode: SetLineupMode
    lateinit var lineup: Lineup
    private val extraHitters = 0
    private val observer = TestObserver<SetLineupMode.ResponseValue>()

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mSetLineupMode = SetLineupMode()

        lineup = Lineup(1, "test1", 1, 1, MODE_DISABLED, TeamStrategy.STANDARD.id, extraHitters, 3L)
    }

    private fun startUseCase(mode: Boolean) {
        lineup.mode = if (mode) MODE_DISABLED else MODE_ENABLED
        val lineupMode = if (mode) MODE_ENABLED else MODE_DISABLED
        mSetLineupMode.executeUseCase(SetLineupMode.RequestValues(lineup, lineupMode))
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
    fun shouldSaveTheLineupModeDH() {
        startUseCase(true)
    }
}