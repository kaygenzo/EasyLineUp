/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.settings

import android.net.Uri
import androidx.compose.ui.graphics.toArgb
import com.telen.easylineup.domain.model.ThemeMode
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.ports.DispatcherProvider
import com.telen.easylineup.domain.ports.ExportFileWriter
import com.telen.easylineup.domain.ports.WrittenExportFile
import com.telen.easylineup.domain.usecases.DeleteAllData
import com.telen.easylineup.domain.usecases.ExportData
import com.telen.easylineup.domain.usecases.GenerateExportFileName
import com.telen.easylineup.domain.usecases.ImportResult
import com.telen.easylineup.testDispatcherProvider
import com.telen.easylineup.utils.UserPreferences
import com.telen.easylineup.utils.UserPreferencesStore
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
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any

/**
 * Unit tests for [SettingsViewModel]. The stored preferences come from a mocked
 * [UserPreferencesStore], read once in `init`. `Dispatchers.setMain` uses a single shared
 * [UnconfinedTestDispatcher] (not a fresh one per test) so [runTest]'s own `advanceUntilIdle()`
 * drives the ViewModel's coroutines too.
 */
@RunWith(MockitoJUnitRunner::class)
internal class SettingsViewModelTest {
    @Mock
    lateinit var deleteAllDataUseCase: DeleteAllData

    @Mock
    lateinit var exportDataUseCase: ExportData

    @Mock
    lateinit var generateExportFileName: GenerateExportFileName

    @Mock
    lateinit var exportFileWriter: ExportFileWriter

    @Mock
    lateinit var preferences: UserPreferencesStore

    private val stored = UserPreferences(
        themeMode = ThemeMode.DARK,
        menColor = 0x0000FF,
        womenColor = 0xFF00FF,
        importOverwriteExisting = true
    )
    private val testDispatcher = UnconfinedTestDispatcher()
    private val dispatcherProvider: DispatcherProvider = testDispatcherProvider(testDispatcher)
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        Mockito.lenient().`when`(preferences.read()).thenReturn(stored)

        viewModel = SettingsViewModel(
            deleteAllDataUseCase,
            exportDataUseCase,
            generateExportFileName,
            exportFileWriter,
            preferences,
            dispatcherProvider
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldLoadTheStoredPreferences() = runTest(testDispatcher) {
        val state = viewModel.uiState.value

        assertEquals(ThemeMode.DARK, state.themeMode)
        assertEquals(0x0000FF, state.menColor.toArgb() and 0xFFFFFF)
        assertEquals(0xFF00FF, state.womenColor.toArgb() and 0xFFFFFF)
        assertTrue(state.importOverwriteExisting)
    }

    @Test
    fun shouldUpdateThemeValueWhenSelected() = runTest(testDispatcher) {
        viewModel.onThemeSelected(ThemeMode.LIGHT)

        assertEquals(ThemeMode.LIGHT, viewModel.uiState.value.themeMode)
        Mockito.verify(preferences).setThemeMode(ThemeMode.LIGHT)
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
        viewModel.onImportOverwriteChanged(false)

        assertFalse(viewModel.uiState.value.importOverwriteExisting)
        Mockito.verify(preferences).setImportOverwriteExisting(false)
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
