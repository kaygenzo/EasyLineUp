/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.usecases.CheckTeam
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class CheckTeamTests {
    private val observer: TestObserver<CheckTeam.ResponseValue> = TestObserver()
    val team = Team(1L, "A", null, 0, true, null)
    lateinit var checkTeam: CheckTeam

    @Before
    fun init() {
        MockitoAnnotations.initMocks(this)
        checkTeam = CheckTeam()
    }

    @Test
    fun shouldAcceptTeamWithNameNotEmpty() {
        checkTeam.executeUseCase(CheckTeam.RequestValues(team))
            .subscribe(observer)
        observer.await()
        observer.assertComplete()
    }

    @Test
    fun shouldRejectTeamWithNameEmpty() {
        team.name = ""
        checkTeam.executeUseCase(CheckTeam.RequestValues(team))
            .subscribe(observer)
        observer.await()
        observer.assertError(NameEmptyException::class.java)
    }

    @Test
    fun shouldRejectTeamWithNameOnlyWhitespaces() {
        team.name = "    "
        checkTeam.executeUseCase(CheckTeam.RequestValues(team))
            .subscribe(observer)
        observer.await()
        observer.assertError(NameEmptyException::class.java)
    }
}
