/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain.application.impl

import com.telen.easylineup.domain.testUseCaseHandler
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.repository.TeamRepository
import com.telen.easylineup.domain.usecases.CheckTeam
import com.telen.easylineup.domain.usecases.DeleteTeam
import com.telen.easylineup.domain.usecases.GetAllTeams
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.SaveCurrentTeam
import com.telen.easylineup.domain.usecases.SaveTeam
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import com.nhaarman.mockitokotlin2.any
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

/**
 * Characterization tests for [TeamsInteractorImpl], written as a safety net before
 * merging the Interactor layer into the UseCase layer. Wires the interactor with the
 * real UseCases (already tested in isolation) and a mocked [TeamRepository], the same
 * way [com.telen.easylineup.domain.DomainModule] wires them in production.
 */
@RunWith(MockitoJUnitRunner::class)
internal class TeamsInteractorImplTest {

    @Mock
    lateinit var teamRepository: TeamRepository

    lateinit var interactor: TeamsInteractorImpl

    private val mainTeam = Team(id = 1L, name = "Panthers", type = 0, main = true)
    private val secondaryTeam = Team(id = 2L, name = "Wolves", type = 0, main = false)

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        interactor = TeamsInteractorImpl(
            teamsRepo = teamRepository,
            getTeam = GetTeam(teamRepository),
            getAllTeamsUseCase = GetAllTeams(teamRepository),
            saveCurrentTeam = SaveCurrentTeam(teamRepository),
            deleteTeamUseCase = DeleteTeam(teamRepository),
            saveTeamUseCase = SaveTeam(teamRepository),
            checkTeamUseCase = CheckTeam(),
            useCaseHandler = testUseCaseHandler()
        )
    }

    @Test
    fun shouldGetMainTeam() {
        Mockito.`when`(teamRepository.getTeamsRx())
            .thenReturn(Single.just(listOf(mainTeam, secondaryTeam)))

        val observer = TestObserver<Team>()
        interactor.getTeam().subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(mainTeam, observer.values().first())
    }

    @Test
    fun shouldGetAllTeams() {
        Mockito.`when`(teamRepository.getTeamsRx())
            .thenReturn(Single.just(listOf(mainTeam, secondaryTeam)))

        val observer = TestObserver<List<Team>>()
        interactor.getAllTeams().subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(listOf(mainTeam, secondaryTeam), observer.values().first())
    }

    @Test
    fun shouldGetTeamsCount() {
        Mockito.`when`(teamRepository.getTeamsRx())
            .thenReturn(Single.just(listOf(mainTeam, secondaryTeam)))

        val observer = TestObserver<Int>()
        interactor.getTeamsCount().subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(2, observer.values().first())
    }

    @Test
    fun shouldDelegateInsertTeamToRepository() {
        Mockito.`when`(teamRepository.insertTeam(mainTeam)).thenReturn(Single.just(1L))

        val observer = TestObserver<Long>()
        interactor.insertTeam(mainTeam).subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(1L, observer.values().first())
    }

    @Test
    fun shouldUpdateCurrentTeam() {
        Mockito.`when`(teamRepository.getTeamsRx())
            .thenReturn(Single.just(listOf(mainTeam, secondaryTeam)))
        Mockito.`when`(teamRepository.updateTeams(Mockito.anyList())).thenReturn(Completable.complete())

        val observer = TestObserver<Void>()
        interactor.updateCurrentTeam(secondaryTeam).subscribe(observer)
        observer.await()

        observer.assertComplete()
    }

    @Test
    fun shouldSaveTeamWhenNameIsValid() {
        Mockito.`when`(teamRepository.insertTeam(any())).thenReturn(Single.just(3L))
        Mockito.`when`(teamRepository.getTeamsRx())
            .thenReturn(Single.just(listOf(mainTeam, secondaryTeam)))
        Mockito.`when`(teamRepository.updateTeams(Mockito.anyList())).thenReturn(Completable.complete())

        val newTeam = Team(id = 0L, name = "Eagles")
        val observer = TestObserver<Void>()
        interactor.saveTeam(newTeam).subscribe(observer)
        observer.await()

        observer.assertComplete()
        Mockito.verify(teamRepository).insertTeam(newTeam)
    }

    @Test
    fun shouldFailToSaveTeamWhenNameIsEmpty() {
        val invalidTeam = Team(id = 0L, name = "  ")

        val observer = TestObserver<Void>()
        interactor.saveTeam(invalidTeam).subscribe(observer)
        observer.await()

        observer.assertError(NameEmptyException::class.java)
        Mockito.verifyZeroInteractions(teamRepository)
    }

    @Test
    fun shouldGetTeamType() {
        Mockito.`when`(teamRepository.getTeamsRx())
            .thenReturn(Single.just(listOf(mainTeam.copy(type = 2))))

        val observer = TestObserver<Int>()
        interactor.getTeamType().subscribe(observer)
        observer.await()

        observer.assertComplete()
        assertEquals(2, observer.values().first())
    }

    @Test
    fun shouldDeleteTeam() {
        Mockito.`when`(teamRepository.deleteTeam(secondaryTeam)).thenReturn(Completable.complete())

        val observer = TestObserver<Void>()
        interactor.deleteTeam(secondaryTeam).subscribe(observer)
        observer.await()

        observer.assertComplete()
        Mockito.verify(teamRepository).deleteTeam(secondaryTeam)
    }

    @Test
    fun shouldObserveTeamsDelegateToRepository() {
        val liveData = androidx.lifecycle.MutableLiveData<List<Team>>()
        Mockito.`when`(teamRepository.getTeams()).thenReturn(liveData)

        assertSame(liveData, interactor.observeTeams())
    }

    @Test
    fun shouldExposeAnErrorsSubject() {
        val observer = TestObserver<Any>()
        interactor.observeErrors().subscribe(observer)

        observer.assertNotComplete()
        observer.assertNoErrors()
    }
}
