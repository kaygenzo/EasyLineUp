package com.telen.easylineup.domain

import android.view.View
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class GetTeamCreationNextStepTests {

    lateinit var mGetTeamCreationNextStep: GetTeamCreationNextStep

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mGetTeamCreationNextStep = GetTeamCreationNextStep()
    }

    @Test
    fun shouldTriggerAnExceptionIfStepNotManaged() {
        val observer = TestObserver<GetTeamCreationNextStep.ResponseValue>()
        mGetTeamCreationNextStep.executeUseCase(GetTeamCreationNextStep.RequestValues(GetTeamCreationNextStep.TeamCreationStep.FINISH)).subscribe(observer)
        observer.await()
        observer.assertError(IllegalArgumentException::class.java)
    }

    // Team -> Type
    @Test
    fun shouldReturnTypeScreenIfCurrentIsTeam() {
        val observer = TestObserver<GetTeamCreationNextStep.ResponseValue>()
        mGetTeamCreationNextStep.executeUseCase(GetTeamCreationNextStep.RequestValues(GetTeamCreationNextStep.TeamCreationStep.TEAM)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(GetTeamCreationNextStep.TeamCreationStep.TYPE, observer.values().first().nextStep)
        Assert.assertEquals(true, observer.values().first().nextButtonEnabled)
        Assert.assertEquals(R.string.team_creation_label_next, observer.values().first().nextButtonLabel)
        Assert.assertEquals(View.VISIBLE, observer.values().first().nextButtonVisibility)
    }

    // Type -> Players
    @Test
    fun shouldReturnPlayersScreenIfCurrentIsType() {
        val observer = TestObserver<GetTeamCreationNextStep.ResponseValue>()
        mGetTeamCreationNextStep.executeUseCase(GetTeamCreationNextStep.RequestValues(GetTeamCreationNextStep.TeamCreationStep.TYPE)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(GetTeamCreationNextStep.TeamCreationStep.PLAYERS, observer.values().first().nextStep)
        Assert.assertEquals(true, observer.values().first().nextButtonEnabled)
        Assert.assertEquals(R.string.team_creation_label_finish, observer.values().first().nextButtonLabel)
        Assert.assertEquals(View.VISIBLE, observer.values().first().nextButtonVisibility)
    }

    // Players -> Finish
    @Test
    fun shouldReturnFinishScreenIfCurrentIsPlayers() {
        val observer = TestObserver<GetTeamCreationNextStep.ResponseValue>()
        mGetTeamCreationNextStep.executeUseCase(GetTeamCreationNextStep.RequestValues(GetTeamCreationNextStep.TeamCreationStep.PLAYERS)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(GetTeamCreationNextStep.TeamCreationStep.FINISH, observer.values().first().nextStep)
        Assert.assertEquals(false, observer.values().first().nextButtonEnabled)
        Assert.assertEquals(0, observer.values().first().nextButtonLabel)
        Assert.assertEquals(View.INVISIBLE, observer.values().first().nextButtonVisibility)
    }
}