/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import com.telen.easylineup.R
import com.telen.easylineup.compose.ConfirmSheet
import com.telen.easylineup.compose.ErrorDialog
import com.telen.easylineup.compose.ExportConfirmSheet
import com.telen.easylineup.compose.ExportSuccessSheet
import com.telen.easylineup.compose.ImportConfirmSheet
import com.telen.easylineup.compose.ImportSuccessSheet
import com.telen.easylineup.domain.model.ThemeMode
import com.telen.easylineup.domain.model.export.toDataCounts
import com.telen.easylineup.license.LicensesManager
import com.telen.easylineup.login.ImportFailure
import com.telen.easylineup.login.ImportPreviewReady
import com.telen.easylineup.login.ImportSuccessfulEvent
import com.telen.easylineup.login.LoginActivity
import com.telen.easylineup.login.LoginViewModel
import com.telen.easylineup.navigation.Routes
import com.telen.easylineup.team.TeamViewModel
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.applyAsNightMode
import com.telen.easylineup.utils.startActivityOrNotify
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

/** The settings, the `navigation_settings` root tab. */
@Composable
fun SettingsRoute(navController: NavController) {
    val viewModel = koinViewModel<SettingsViewModel>()
    val loginViewModel = koinViewModel<LoginViewModel>()
    val teamViewModel = koinViewModel<TeamViewModel>()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val teamUiState by teamViewModel.uiState.collectAsStateWithLifecycle()
    var transferError by rememberSaveable { mutableStateOf<TransferError?>(null) }

    val openFile = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(loginViewModel::peekImportFile) }

    val createFile = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri -> uri?.let(viewModel::exportData) }

    fun openPlayStore() {
        FirebaseAnalyticsUtils.onClick(context, "click_settings_play_store")
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = "market://details?id=${context.packageName}".toUri()
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Timber.e(e)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                val link = "http://play.google.com/store/apps/details?id=${context.packageName}"
                data = link.toUri()
            }
            context.startActivityOrNotify(intent)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { event ->
                when (event) {
                    DeleteAllDataDone -> {
                        FirebaseAnalyticsUtils.deleteData(context)
                        val intent = Intent(context, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                    }

                    DeleteAllDataFailed -> Toast.makeText(
                        context,
                        R.string.error_delete_all_data_failed,
                        Toast.LENGTH_LONG
                    ).show()

                    ExportSucceeded -> {
                        FirebaseAnalyticsUtils.exportData(context)
                        Timber.d("Successfully exported data!")
                    }

                    ExportFailed -> transferError = TransferError.EXPORT

                    ImportSucceeded -> FirebaseAnalyticsUtils.importData(context)

                    ImportFailed -> transferError = TransferError.IMPORT
                }
            }
    }

    // LoginViewModel is shared with the login flow - its events are forwarded here into
    // SettingsViewModel's own state.
    LaunchedEffect(loginViewModel) {
        loginViewModel.events
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { event ->
                when (event) {
                    is ImportPreviewReady -> viewModel.onImportPreviewReady(
                        event.fileName,
                        event.exportBase
                    )

                    is ImportSuccessfulEvent -> viewModel.onImportSuccess(event.result)

                    ImportFailure -> viewModel.onImportFailure()

                    else -> {}
                }
            }
    }

    SettingsScreen(
        state = SettingsScreenState(
            teamName = teamUiState.teamName,
            teamType = teamUiState.teamType,
            teamImageUri = teamUiState.teamImageUri,
            themeValue = uiState.themeMode.preferenceValue,
            menColor = uiState.menColor,
            womenColor = uiState.womenColor
        ),
        onBackClick = { navController.navigateUp() },
        onTeamClick = { navController.navigate(Routes.TeamDetails) },
        onThemeSelected = { newValue ->
            FirebaseAnalyticsUtils.onClick(context, "click_settings_theme")
            val mode = ThemeMode.fromPreference(newValue)
            viewModel.onThemeSelected(mode)
            mode.applyAsNightMode()
        },
        onMenColorPicked = viewModel::onMenColorPicked,
        onWomenColorPicked = viewModel::onWomenColorPicked,
        onExportClick = {
            FirebaseAnalyticsUtils.onClick(context, "click_settings_export_data")
            viewModel.loadExportPreview()
        },
        onImportClick = {
            FirebaseAnalyticsUtils.onClick(context, "click_settings_import_data")
            openFile.launch(arrayOf("application/octet-stream"))
        },
        onDeleteAllDataClick = viewModel::onDeleteAllDataClick,
        onLicensesClick = {
            FirebaseAnalyticsUtils.onClick(context, "click_settings_licenses")
            LicensesManager.getDialog(context).show()
        },
        onPlayStoreClick = { openPlayStore() }
    )

    if (uiState.showDeleteAllDataSheet) {
        ConfirmSheet(
            title = stringResource(R.string.setting_delete_data_description),
            message = stringResource(R.string.dialog_delete_cannot_undo_message),
            confirmLabel = stringResource(R.string.action_delete),
            cancelLabel = stringResource(R.string.cancel_button),
            onDismiss = viewModel::onDeleteAllDataDismiss,
            onConfirm = {
                FirebaseAnalyticsUtils.onClick(context, "click_settings_delete_data")
                viewModel.deleteAllData()
            }
        )
    }

    uiState.exportPreview?.let { base ->
        ExportConfirmSheet(
            teamName = teamUiState.teamName,
            counts = base.toDataCounts(),
            onDismiss = viewModel::onExportPreviewDismiss,
            onConfirm = {
                FirebaseAnalyticsUtils.onClick(context, "click_export_confirm")
                createFile.launch(null)
            }
        )
    }

    uiState.exportSuccess?.let { success ->
        ExportSuccessSheet(
            folderName = success.folderName,
            fileName = success.fileName,
            counts = success.counts,
            onDismiss = viewModel::onExportSuccessDismiss
        )
    }

    uiState.importPreview?.let { base ->
        ImportConfirmSheet(
            fileName = uiState.importFileName,
            counts = base.toDataCounts(),
            overwriteExisting = uiState.importOverwriteExisting,
            onOverwriteChanged = viewModel::onImportOverwriteChanged,
            onDismiss = viewModel::onImportPreviewDismiss,
            onConfirm = {
                FirebaseAnalyticsUtils.onClick(context, "click_import_confirm")
                loginViewModel.confirmImport(base, uiState.importOverwriteExisting)
            }
        )
    }

    uiState.importSuccess?.let { result ->
        ImportSuccessSheet(
            fileName = uiState.importFileName,
            result = result,
            onDismiss = viewModel::onImportSuccessDismiss
        )
    }

    transferError?.let { error ->
        val isExport = error == TransferError.EXPORT
        ErrorDialog(
            title = stringResource(
                if (isExport) {
                    R.string.settings_export_error_title
                } else {
                    R.string.settings_import_error_title
                }
            ),
            message = stringResource(
                if (isExport) {
                    R.string.settings_export_error_message
                } else {
                    R.string.settings_import_error_message
                }
            ),
            onDismiss = { transferError = null }
        )
    }
}

/** Which data transfer failed, kept across rotation until the user dismisses its dialog. */
private enum class TransferError { EXPORT, IMPORT }
