/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team.createTeam

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.usecases.SaveTeam
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any

/**
 * Unit tests for [SetupViewModel]. [SaveTeam] is mocked directly (it has its own domain-level
 * test suite) - this only verifies the ViewModel's own state-holding/emission logic.
 *
 * Every `observeX()` flow is backed by a replay=1 [kotlinx.coroutines.flow.MutableSharedFlow]
 * that starts with nothing cached, so a setter must be called at least once before a test can
 * collect a value from it - mirroring how the real screen always calls [SetupViewModel.setTeam]
 * on entry before observing anything.
 */
@RunWith(MockitoJUnitRunner::class)
internal class SetupViewModelTest {
    @Mock
    lateinit var saveTeam: SaveTeam

    lateinit var viewModel: SetupViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = SetupViewModel(SavedStateHandle(), saveTeam)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** Starts collecting [flow] on the test scheduler, for the rest of the test. */
    private fun <T> TestScope.collectEvents(flow: Flow<T>): List<T> {
        val events = mutableListOf<T>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flow.toList(events) }
        return events
    }

    @Test
    fun shouldEmitUpdatedNameAfterSetTeamName() = runTest {
        viewModel.setTeamName("Panthers")

        assertEquals("Panthers", viewModel.observeTeamName().first())
    }

    @Test
    fun shouldEmitUpdatedTypeAfterSetTeamType() = runTest {
        viewModel.setTeamType(TeamType.SOFTBALL)

        assertEquals(TeamType.SOFTBALL, viewModel.observeTeamType().first())
    }

    @Test
    fun shouldEmitUpdatedImageAfterSetTeamImage() = runTest {
        // android.net.Uri.parse isn't implemented on the unit test JVM - mockStatic intercepts
        // it so observeTeamImage()'s own Uri.parse(...) call doesn't throw "not mocked".
        val mockUri = Mockito.mock(Uri::class.java)
        Mockito.mockStatic(Uri::class.java).use { mockedUri ->
            mockedUri.`when`<Uri> { Uri.parse("content://photo") }.thenReturn(mockUri)

            viewModel.setTeamImage("content://photo")

            assertEquals(mockUri, viewModel.observeTeamImage().first())
        }
    }

    @Test
    fun shouldEmitNullImageWhenImageIsCleared() = runTest {
        viewModel.setTeamImage(null)

        assertEquals(null, viewModel.observeTeamImage().first())
    }

    @Test
    fun shouldReplaceCurrentTeamWhenSetTeamReceivesATeam() = runTest {
        val team = Team(id = 5L, name = "Lions", type = TeamType.SOFTBALL.id, main = true)

        viewModel.setTeam(team)

        assertEquals("Lions", viewModel.observeTeamName().first())
        assertEquals(TeamType.SOFTBALL, viewModel.observeTeamType().first())
    }

    @Test
    fun shouldKeepTheDefaultNewTeamWhenSetTeamReceivesNull() = runTest {
        viewModel.setTeam(null)

        assertEquals("", viewModel.observeTeamName().first())
        assertEquals(TeamType.BASEBALL, viewModel.observeTeamType().first())
    }

    @Test
    fun shouldDelegateSaveToSaveTeamUseCase() = runTest {
        val savedTeam = Team(id = 1L, name = "Panthers", type = TeamType.BASEBALL.id, main = true)
        runBlocking { Mockito.`when`(saveTeam(any())).thenReturn(Result.success(savedTeam)) }

        val events = collectEvents(viewModel.events)

        viewModel.onSaveClicked()

        assertEquals(listOf(SetupEvent.TeamSaved), events)
    }

    @Test
    fun shouldSendNoEventWhenSaveTeamFailsForAnotherReason() = runTest {
        val exception = Exception("db error")
        runBlocking { Mockito.`when`(saveTeam(any())).thenReturn(Result.failure(exception)) }
        val events = collectEvents(viewModel.events)

        viewModel.onSaveClicked()

        assertTrue(events.isEmpty())
    }

    @Test
    fun shouldReportAnEmptyTeamName() = runTest {
        runBlocking {
            Mockito.`when`(saveTeam(any())).thenReturn(Result.failure(NameEmptyException()))
        }
        val events = collectEvents(viewModel.events)

        viewModel.onSaveClicked()

        assertEquals(listOf(SetupEvent.TeamNameEmpty), events)
    }

    @Test
    fun shouldRestoreTheTypedTeamAfterTheProcessWasKilled() = runTest {
        val handle = SavedStateHandle()
        SetupViewModel(handle, saveTeam).apply {
            setTeam(null)
            setTeamName("Panthers")
            setTeamType(TeamType.SOFTBALL)
        }

        val restored = SetupViewModel(handle, saveTeam)
        restored.setTeam(null)

        assertEquals("Panthers", restored.observeTeamName().first())
        assertEquals(TeamType.SOFTBALL, restored.observeTeamType().first())
    }

    @Test
    fun shouldNotResetAnEditInProgressWhenTheScreenIsRecreated() = runTest {
        val edited = Team(id = 3L, name = "Original", type = TeamType.BASEBALL.id)
        viewModel.setTeam(edited)
        viewModel.setTeamName("Renamed")

        viewModel.setTeam(edited)

        assertEquals("Renamed", viewModel.observeTeamName().first())
    }
}
