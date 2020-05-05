package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.usecases.CheckTeam
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
internal class CheckTeamTests {

    lateinit var mCheckTeam: CheckTeam

    val team = Team(1L, "A", null, 0, true, null)

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        mCheckTeam = CheckTeam()
    }

    @Test
    fun shouldAcceptTeamWithNameNotEmpty() {
        val observer = TestObserver<CheckTeam.ResponseValue>()
        mCheckTeam.executeUseCase(CheckTeam.RequestValues(team))
                .subscribe(observer)
        observer.await()
        observer.assertComplete()
    }

    @Test
    fun shouldRejectTeamWithNameEmpty() {
        team.name = ""
        val observer = TestObserver<CheckTeam.ResponseValue>()
        mCheckTeam.executeUseCase(CheckTeam.RequestValues(team))
                .subscribe(observer)
        observer.await()
        observer.assertError(NameEmptyException::class.java)
    }

    @Test
    fun shouldRejectTeamWithNameOnlyWhitespaces() {
        team.name = "    "
        val observer = TestObserver<CheckTeam.ResponseValue>()
        mCheckTeam.executeUseCase(CheckTeam.RequestValues(team))
                .subscribe(observer)
        observer.await()
        observer.assertError(NameEmptyException::class.java)
    }
}