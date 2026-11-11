/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.settings

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.Uri
import androidx.compose.ui.graphics.toArgb
import androidx.preference.PreferenceManager
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.ExportFileWriter
import com.telen.easylineup.domain.ports.WrittenExportFile
import com.telen.easylineup.domain.usecases.DeleteAllData
import com.telen.easylineup.domain.usecases.ExportData
import com.telen.easylineup.domain.usecases.GenerateExportFileName
import com.telen.easylineup.domain.usecases.GetAllTeams
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any

/**
 * Unit tests for [SettingsViewModel]. [loadPreferencesState] reads through
 * `PreferenceManager.getDefaultSharedPreferences(context)` eagerly at construction, so `Context`
 * and that static call are mocked just deep enough to let construction succeed - the exact
 * stored values don't matter for most tests, only that every preference read/write round-trips
 * through the same mocked [SharedPreferences] without throwing. `Dispatchers.setMain` uses a
 * single shared [UnconfinedTestDispatcher] (not a fresh one per test) so [runTest]'s own
 * `advanceUntilIdle()` can also drive the real `delay(...)` inside `deleteAllData()`.
 */
@RunWith(MockitoJUnitRunner::class)
internal class SettingsViewModelTest {
    @Mock
    lateinit var deleteAllDataUseCase: DeleteAllData

    @Mock
    lateinit var exportDataUseCase: ExportData

    @Mock
    lateinit var getAllTeamsUseCase: GetAllTeams

    @Mock
    lateinit var generateExportFileName: GenerateExportFileName

    @Mock
    lateinit var exportFileWriter: ExportFileWriter

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var sharedPreferences: SharedPreferences

    @Mock
    lateinit var editor: SharedPreferences.Editor

    @Mock
    lateinit var resources: Resources

    private val testDispatcher = UnconfinedTestDispatcher()
    private val dispatcherProvider: DispatcherProvider = testDispatcherProvider(testDispatcher)
    private lateinit var preferenceManagerMock: MockedStatic<PreferenceManager>
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        preferenceManagerMock = Mockito.mockStatic(PreferenceManager::class.java)
        preferenceManagerMock.`when`<SharedPreferences> {
            PreferenceManager.getDefaultSharedPreferences(context)
        }.thenReturn(sharedPreferences)

        Mockito.lenient().`when`(context.getString(anyInt())).thenReturn("default")
        Mockito.lenient().`when`(context.resources).thenReturn(resources)
        @Suppress("DEPRECATION")
        Mockito.lenient().`when`(resources.getColor(anyInt())).thenReturn(0)
        Mockito.lenient().`when`(context.getColor(anyInt())).thenReturn(0)
        Mockito.lenient().`when`(sharedPreferences.getString(anyString(), any()))
            .thenReturn("value")
        Mockito.lenient().`when`(sharedPreferences.getInt(anyString(), anyInt())).thenReturn(0)
        Mockito.lenient().`when`(sharedPreferences.getBoolean(anyString(), anyBoolean()))
            .thenReturn(false)
        Mockito.lenient().`when`(sharedPreferences.edit()).thenReturn(editor)
        Mockito.lenient().`when`(editor.putString(anyString(), any())).thenReturn(editor)
        Mockito.lenient().`when`(editor.putInt(anyString(), anyInt())).thenReturn(editor)
        Mockito.lenient().`when`(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor)

        viewModel = SettingsViewModel(
            deleteAllDataUseCase,
            exportDataUseCase,
            getAllTeamsUseCase,
            generateExportFileName,
            exportFileWriter,
            context,
            dispatcherProvider
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        preferenceManagerMock.close()
    }

    @Test
    fun shouldUpdateThemeValueWhenSelected() = runTest(testDispatcher) {
        viewModel.onThemeSelected("2")

        assertEquals("2", viewModel.uiState.value.themeValue)
    }

    @Test
    fun shouldUpdateMenColorWhenPicked() = runTest(testDispatcher) {
        viewModel.onMenColorPicked(0xFF0000)

        assertEquals(0xFF0000, viewModel.uiState.value.menColor.toArgb())
    }

    @Test
    fun shouldUpdateWomenColorWhenPicked() = runTest(testDispatcher) {
        viewModel.onWomenColorPicked(0x00FF00)

        assertEquals(0x00FF00, viewModel.uiState.value.womenColor.toArgb())
    }

    @Test
    fun shouldShowDeleteAllDataSheetOnClick() = runTest(testDispatcher) {
        viewModel.onDeleteAllDataClick()

        assertTrue(viewModel.uiState.value.showDeleteAllDataSheet)
    }

    @Test
    fun shouldHideDeleteAllDataSheetOnDismiss() = runTest(testDispatcher) {
        viewModel.onDeleteAllDataClick()
        viewModel.onDeleteAllDataDismiss()

        assertFalse(viewModel.uiState.value.showDeleteAllDataSheet)
    }

    @Test
    fun shouldSendDeleteAllDataDoneEventOnSuccess() = runTest(testDispatcher) {
        runBlocking { Mockito.`when`(deleteAllDataUseCase()).thenReturn(Result.success(Unit)) }

        val events = mutableListOf<SettingsEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.deleteAllData()
        advanceUntilIdle()

        assertEquals(listOf(DeleteAllDataDone), events)
        job.cancel()
    }

    @Test
    fun shouldReportAFailedDeleteAllData() = runTest(testDispatcher) {
        val exception = Exception("db error")
        runBlocking { Mockito.`when`(deleteAllDataUseCase()).thenReturn(Result.failure(exception)) }

        val events = mutableListOf<SettingsEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.deleteAllData()
        advanceUntilIdle()

        assertEquals(listOf(DeleteAllDataFailed), events)
        job.cancel()
    }

    @Test
    fun shouldSendTeamsCountReadyEventOnSuccess() = runTest(testDispatcher) {
        runBlocking { Mockito.`when`(getAllTeamsUseCase()).thenReturn(Result.success(emptyList())) }

        val events = mutableListOf<SettingsEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.getTeamsCount()
        advanceUntilIdle()

        assertEquals(listOf(TeamsCountReady), events)
        job.cancel()
    }

    @Test
    fun shouldSendNoEventWhenGetTeamsCountFails() = runTest(testDispatcher) {
        runBlocking {
            Mockito.`when`(getAllTeamsUseCase()).thenReturn(Result.failure(RuntimeException("db")))
        }

        val events = mutableListOf<SettingsEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.getTeamsCount()
        advanceUntilIdle()

        assertTrue(events.isEmpty())
        job.cancel()
    }

    @Test
    fun shouldUpdateExportPreviewOnLoadExportPreviewSuccess() = runTest(testDispatcher) {
        val exportBase = ExportBase(emptyList())
        runBlocking { Mockito.`when`(exportDataUseCase()).thenReturn(Result.success(exportBase)) }

        viewModel.loadExportPreview()
        advanceUntilIdle()

        assertEquals(exportBase, viewModel.uiState.value.exportPreview)
    }

    @Test
    fun shouldSendExportFailedEventWhenLoadExportPreviewFails() = runTest(testDispatcher) {
        val exception = Exception("db error")
        runBlocking { Mockito.`when`(exportDataUseCase()).thenReturn(Result.failure(exception)) }

        val events = mutableListOf<SettingsEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.loadExportPreview()
        advanceUntilIdle()

        assertEquals(listOf(ExportFailed), events)
        job.cancel()
    }

    @Test
    fun shouldClearExportPreviewOnDismiss() = runTest(testDispatcher) {
        val exportBase = ExportBase(emptyList())
        runBlocking { Mockito.`when`(exportDataUseCase()).thenReturn(Result.success(exportBase)) }
        viewModel.loadExportPreview()
        advanceUntilIdle()

        viewModel.onExportPreviewDismiss()

        assertNull(viewModel.uiState.value.exportPreview)
    }

    @Test
    fun shouldClearExportSuccessOnDismiss() = runTest(testDispatcher) {
        viewModel.onExportSuccessDismiss()

        assertNull(viewModel.uiState.value.exportSuccess)
    }

    @Test
    fun shouldWriteExportFileAndSendExportSucceededEventOnExportData() = runTest(testDispatcher) {
        val exportBase = ExportBase(emptyList())
        runBlocking { Mockito.`when`(exportDataUseCase()).thenReturn(Result.success(exportBase)) }
        viewModel.loadExportPreview()
        advanceUntilIdle()

        val written = WrittenExportFile(fileName = "export.elu", folderName = "Documents")
        Mockito.lenient().`when`(generateExportFileName(Mockito.anyLong())).thenReturn("export.elu")
        Mockito.lenient().`when`(exportFileWriter.write(any(), any(), any())).thenReturn(written)

        val events = mutableListOf<SettingsEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        val dirUri = Mockito.mock(Uri::class.java)
        viewModel.exportData(dirUri)
        advanceUntilIdle()

        assertEquals(listOf(ExportSucceeded), events)
        assertEquals("export.elu", viewModel.uiState.value.exportSuccess?.fileName)
        assertEquals("Documents", viewModel.uiState.value.exportSuccess?.folderName)
        assertNull(viewModel.uiState.value.exportPreview)
        job.cancel()
    }

    @Test
    fun shouldSendExportFailedEventWhenWritingTheExportFileFails() = runTest(testDispatcher) {
        val exportBase = ExportBase(emptyList())
        runBlocking { Mockito.`when`(exportDataUseCase()).thenReturn(Result.success(exportBase)) }
        viewModel.loadExportPreview()
        advanceUntilIdle()

        Mockito.lenient().`when`(generateExportFileName(Mockito.anyLong())).thenReturn("export.elu")
        Mockito.lenient().`when`(exportFileWriter.write(any(), any(), any()))
            .thenThrow(RuntimeException("disk error"))

        val events = mutableListOf<SettingsEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        val dirUri = Mockito.mock(Uri::class.java)
        viewModel.exportData(dirUri)
        advanceUntilIdle()

        assertEquals(listOf(ExportFailed), events)
        job.cancel()
    }

    @Test
    fun shouldDoNothingWhenExportDataIsCalledWithoutAPreview() = runTest(testDispatcher) {
        val events = mutableListOf<SettingsEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        val dirUri = Mockito.mock(Uri::class.java)
        viewModel.exportData(dirUri)
        advanceUntilIdle()

        assertTrue(events.isEmpty())
        job.cancel()
    }

    @Test
    fun shouldUpdateImportPreviewOnImportPreviewReady() = runTest(testDispatcher) {
        val exportBase = ExportBase(emptyList())

        viewModel.onImportPreviewReady("backup.elu", exportBase)

        assertEquals("backup.elu", viewModel.uiState.value.importFileName)
        assertEquals(exportBase, viewModel.uiState.value.importPreview)
    }

    @Test
    fun shouldUpdateImportOverwritePreferenceWhenChanged() = runTest(testDispatcher) {
        viewModel.onImportOverwriteChanged(true)

        assertTrue(viewModel.uiState.value.importOverwriteExisting)
        Mockito.verify(editor).putBoolean(anyString(), eq(true))
    }

    @Test
    fun shouldClearImportPreviewOnDismiss() = runTest(testDispatcher) {
        viewModel.onImportPreviewReady("backup.elu", ExportBase(emptyList()))

        viewModel.onImportPreviewDismiss()

        assertNull(viewModel.uiState.value.importPreview)
    }

    @Test
    fun shouldUpdateImportSuccessAndSendEventOnImportSuccess() = runTest(testDispatcher) {
        val result = ImportResult()

        val events = mutableListOf<SettingsEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.onImportSuccess(result)
        advanceUntilIdle()

        assertEquals(result, viewModel.uiState.value.importSuccess)
        assertNull(viewModel.uiState.value.importPreview)
        assertEquals(listOf(ImportSucceeded), events)
        job.cancel()
    }

    @Test
    fun shouldSendImportFailedEventOnImportFailure() = runTest(testDispatcher) {
        val events = mutableListOf<SettingsEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.onImportFailure()
        advanceUntilIdle()

        assertEquals(listOf(ImportFailed), events)
        job.cancel()
    }

    @Test
    fun shouldClearImportSuccessOnDismiss() = runTest(testDispatcher) {
        viewModel.onImportSuccessDismiss()

        assertNull(viewModel.uiState.value.importSuccess)
    }
}
