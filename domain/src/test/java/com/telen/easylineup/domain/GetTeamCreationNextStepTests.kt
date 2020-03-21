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
        val observer = TestObserver<GetTeamCreationStep.ResponseValue>()
        mGetTeamCreationNextStep.executeUseCase(GetTeamCreationStep.RequestValues(TeamCreationStep.FINISH)).subscribe(observer)
        observer.await()
        observer.assertError(IllegalArgumentException::class.java)
    }

    // Team -> Type
    @Test
    fun shouldReturnNextScreenTypeIfCurrentIsTeam() {
        val observer = TestObserver<GetTeamCreationStep.ResponseValue>()
        mGetTeamCreationNextStep.executeUseCase(GetTeamCreationStep.RequestValues(TeamCreationStep.TEAM)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(TeamCreationStep.TYPE, observer.values().first().nextStep)
        Assert.assertEquals(true, observer.values().first().nextButtonEnabled)
        Assert.assertEquals(R.string.team_creation_label_finish, observer.values().first().nextButtonLabel)
        Assert.assertEquals(View.VISIBLE, observer.values().first().nextButtonVisibility)

        Assert.assertEquals(true, observer.values().first().previousButtonEnabled)
        Assert.assertEquals(R.string.team_creation_label_previous, observer.values().first().previousButtonLabel)
        Assert.assertEquals(View.VISIBLE, observer.values().first().nextButtonVisibility)
    }

    // Type -> Finish
    @Test
    fun shouldReturnNextScreenFinishIfCurrentIsType() {
        val observer = TestObserver<GetTeamCreationStep.ResponseValue>()
        mGetTeamCreationNextStep.executeUseCase(GetTeamCreationStep.RequestValues(TeamCreationStep.TYPE)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(TeamCreationStep.FINISH, observer.values().first().nextStep)
        Assert.assertEquals(false, observer.values().first().nextButtonEnabled)
        Assert.assertEquals(0, observer.values().first().nextButtonLabel)
        Assert.assertEquals(View.INVISIBLE, observer.values().first().nextButtonVisibility)
        Assert.assertEquals(false, observer.values().first().previousButtonEnabled)
        Assert.assertEquals(0, observer.values().first().previousButtonLabel)
        Assert.assertEquals(View.INVISIBLE, observer.values().first().previousButtonVisibility)
    }
}