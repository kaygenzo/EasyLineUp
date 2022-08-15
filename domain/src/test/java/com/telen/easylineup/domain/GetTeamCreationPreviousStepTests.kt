package com.telen.easylineup.domain

import android.view.View
import com.telen.easylineup.domain.model.TeamCreationStep
import com.telen.easylineup.domain.usecases.GetTeamCreationPreviousStep
import com.telen.easylineup.domain.usecases.GetTeamCreationStep
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
internal class GetTeamCreationPreviousStepTests {

    lateinit var mGetTeamCreationPreviousStep: GetTeamCreationPreviousStep

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mGetTeamCreationPreviousStep = GetTeamCreationPreviousStep()
    }

    @Test
    fun shouldTriggerAnExceptionIfStepNotManaged() {
        val observer = TestObserver<GetTeamCreationStep.ResponseValue>()
        mGetTeamCreationPreviousStep.executeUseCase(GetTeamCreationStep.RequestValues(TeamCreationStep.CANCEL)).subscribe(observer)
        observer.await()
        observer.assertError(IllegalArgumentException::class.java)
    }

    // Type -> Team
    @Test
    fun shouldReturnNextScreenTypeIfCurrentIsTeam() {
        val observer = TestObserver<GetTeamCreationStep.ResponseValue>()
        mGetTeamCreationPreviousStep.executeUseCase(GetTeamCreationStep.RequestValues(TeamCreationStep.TYPE)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(TeamCreationStep.TEAM, observer.values().first().config.nextStep)
        Assert.assertEquals(true, observer.values().first().config.nextButtonEnabled)
        Assert.assertEquals(R.string.team_creation_label_next, observer.values().first().config.nextButtonLabel)
        Assert.assertEquals(View.VISIBLE, observer.values().first().config.nextButtonVisibility)

        Assert.assertEquals(true, observer.values().first().config.previousButtonEnabled)
        Assert.assertEquals(R.string.team_creation_label_cancel, observer.values().first().config.previousButtonLabel)
        Assert.assertEquals(View.VISIBLE, observer.values().first().config.nextButtonVisibility)
    }

    // Team -> Cancel
    @Test
    fun shouldReturnNextScreenFinishIfCurrentIsType() {
        val observer = TestObserver<GetTeamCreationStep.ResponseValue>()
        mGetTeamCreationPreviousStep.executeUseCase(GetTeamCreationStep.RequestValues(TeamCreationStep.TEAM)).subscribe(observer)
        observer.await()
        observer.assertComplete()
        Assert.assertEquals(TeamCreationStep.CANCEL, observer.values().first().config.nextStep)
        Assert.assertEquals(false, observer.values().first().config.nextButtonEnabled)
        Assert.assertEquals(0, observer.values().first().config.nextButtonLabel)
        Assert.assertEquals(View.INVISIBLE, observer.values().first().config.nextButtonVisibility)
        Assert.assertEquals(false, observer.values().first().config.previousButtonEnabled)
        Assert.assertEquals(0, observer.values().first().config.previousButtonLabel)
        Assert.assertEquals(View.INVISIBLE, observer.values().first().config.previousButtonVisibility)
    }
}