/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.login

import android.net.Uri
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.ports.ImportFileReader
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.ImportData
import com.telen.easylineup.domain.usecases.ImportResult
import com.telen.easylineup.testDispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

/**
 * Unit tests for [LoginViewModel]: [GetTeam]/[ImportData]/[ImportFileReader] are mocked directly
 * (each already has its own test suite at its own layer) - this only verifies the ViewModel's own
 * orchestration.
 */
@RunWith(MockitoJUnitRunner::class)
internal class LoginViewModelTest {
    @Mock
    lateinit var getTeamUseCase: GetTeam

    @Mock
    lateinit var importDataUseCase: ImportData

    @Mock
    lateinit var importFileReader: ImportFileReader

    @Mock
    lateinit var uri: Uri

    lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = LoginViewModel(
            getTeamUseCase,
            importDataUseCase,
            importFileReader,
            testDispatcherProvider()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldSendImportPreviewReadyWhenFileParsesSuccessfully() = runTest {
        val exportBase = ExportBase(teams = emptyList())
        Mockito.`when`(importFileReader.read(uri.toString())).thenReturn(exportBase)
        Mockito.`when`(importFileReader.queryDisplayName(uri.toString())).thenReturn("export.elu")

        val events = mutableListOf<LoginEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.peekImportFile(uri)
        advanceUntilIdle()

        assertEquals(1, events.size)
        val event = events[0] as ImportPreviewReady
        assertEquals("export.elu", event.fileName)
        assertEquals(exportBase, event.exportBase)
        job.cancel()
    }

    @Test
    fun shouldSendImportFailureWhenFileCannotBeOpened() = runTest {
        Mockito.`when`(importFileReader.read(uri.toString()))
            .thenThrow(IllegalArgumentException("Cannot open uri"))

        val events = mutableListOf<LoginEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.peekImportFile(uri)
        advanceUntilIdle()

        assertEquals(listOf(ImportFailure), events)
        job.cancel()
    }

    @Test
    fun shouldSendImportSuccessfulEventWhenImportSucceeds() = runTest {
        val exportBase = ExportBase(teams = emptyList())
        val result = ImportResult()
        runBlocking {
            Mockito.`when`(importDataUseCase(exportBase, true)).thenReturn(Result.success(result))
        }

        val events = mutableListOf<LoginEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.confirmImport(exportBase, true)
        advanceUntilIdle()

        assertEquals(listOf(ImportSuccessfulEvent(result)), events)
        job.cancel()
    }

    @Test
    fun shouldSendImportFailureWhenImportFails() = runTest {
        val exportBase = ExportBase(teams = emptyList())
        val exception = Exception("db error")
        runBlocking {
            Mockito.`when`(importDataUseCase(exportBase, false))
                .thenReturn(Result.failure(exception))
        }

        val events = mutableListOf<LoginEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.confirmImport(exportBase, false)
        advanceUntilIdle()

        assertEquals(listOf(ImportFailure), events)
        job.cancel()
    }

    @Test
    fun shouldSendGetTeamSuccessWhenMainTeamIsFound() = runTest {
        val team = Team(id = 1L, name = "Panthers", main = true)
        runBlocking { Mockito.`when`(getTeamUseCase()).thenReturn(Result.success(team)) }

        val events = mutableListOf<LoginEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.getMainTeam()
        advanceUntilIdle()

        assertEquals(listOf(GetTeamSuccess(team)), events)
        job.cancel()
    }

    @Test
    fun shouldSendGetTeamFailedWhenNoMainTeamExists() = runTest {
        val exception = Exception("no main team")
        runBlocking { Mockito.`when`(getTeamUseCase()).thenReturn(Result.failure(exception)) }

        val events = mutableListOf<LoginEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.getMainTeam()
        advanceUntilIdle()

        assertTrue(events == listOf(GetTeamFailed))
        job.cancel()
    }
}
