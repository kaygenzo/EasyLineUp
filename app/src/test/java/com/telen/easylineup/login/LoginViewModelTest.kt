/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.login

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.domain.usecases.ImportData
import com.telen.easylineup.domain.usecases.ImportResult
import com.telen.easylineup.export.json.JsonExportBase
import com.telen.easylineup.export.json.exportGson
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
import java.io.ByteArrayInputStream

/**
 * Unit tests for [LoginViewModel]: [GetTeam]/[ImportData] are mocked directly (each already has
 * its own domain-level test suite). [Context]/[Uri] are mocked too since this ViewModel talks to
 * the content resolver directly for file import - `peekImportFile`'s success path feeds a real
 * serialized [JsonExportBase] through a real [ByteArrayInputStream] rather than mocking the
 * parsing itself, and its display-name lookup falls back to [Uri.getLastPathSegment] (real
 * behavior, not a workaround) since mocking a [android.database.Cursor] just to exercise that
 * one branch would be more fragile than useful here.
 */
@RunWith(MockitoJUnitRunner::class)
internal class LoginViewModelTest {
    @Mock
    lateinit var getTeamUseCase: GetTeam

    @Mock
    lateinit var importDataUseCase: ImportData

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var contentResolver: ContentResolver

    @Mock
    lateinit var uri: Uri

    lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        Mockito.lenient().`when`(context.contentResolver).thenReturn(contentResolver)
        Mockito.lenient().`when`(uri.lastPathSegment).thenReturn("export.elu")
        viewModel = LoginViewModel(
            getTeamUseCase,
            importDataUseCase,
            context,
            testDispatcherProvider()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldSendImportPreviewReadyWhenFileParsesSuccessfully() = runTest {
        val json = exportGson.toJson(JsonExportBase(teams = emptyList()))
        Mockito.`when`(contentResolver.openInputStream(uri))
            .thenReturn(ByteArrayInputStream(json.toByteArray()))
        Mockito.`when`(contentResolver.query(uri, null, null, null, null)).thenReturn(null)

        val events = mutableListOf<LoginEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.peekImportFile(uri)
        advanceUntilIdle()

        assertEquals(1, events.size)
        val event = events[0] as ImportPreviewReady
        assertEquals("export.elu", event.fileName)
        assertEquals(ExportBase(teams = emptyList()), event.exportBase)
        job.cancel()
    }

    @Test
    fun shouldSendImportFailureWhenFileCannotBeOpened() = runTest {
        Mockito.`when`(contentResolver.openInputStream(uri)).thenReturn(null)

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
