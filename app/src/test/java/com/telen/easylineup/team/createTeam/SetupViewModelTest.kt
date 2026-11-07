/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team.createTeam

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.usecases.SaveTeam
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
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
        viewModel = SetupViewModel(SavedStateHandle(), saveTeam)
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

        val result = viewModel.onSaveClicked()

        assertTrue(result.isSuccess)
        assertEquals(savedTeam, result.getOrNull())
    }

    @Test
    fun shouldPropagateFailureFromSaveTeamUseCase() = runTest {
        val exception = Exception("db error")
        runBlocking { Mockito.`when`(saveTeam(any())).thenReturn(Result.failure(exception)) }

        val result = viewModel.onSaveClicked()

        assertFalse(result.isSuccess)
        assertEquals(exception, result.exceptionOrNull())
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
