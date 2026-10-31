/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import androidx.preference.PreferenceManager
import com.telen.easylineup.R
import com.telen.easylineup.compose.ConfirmSheet
import com.telen.easylineup.compose.ExportConfirmSheet
import com.telen.easylineup.compose.ExportSuccessSheet
import com.telen.easylineup.compose.ImportConfirmSheet
import com.telen.easylineup.compose.ImportSuccessSheet
import com.telen.easylineup.compose.toDataCounts
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.usecases.ImportResult
import com.telen.easylineup.license.LicensesManager
import com.telen.easylineup.login.ImportFailure
import com.telen.easylineup.login.ImportPreviewReady
import com.telen.easylineup.login.ImportSuccessfulEvent
import com.telen.easylineup.login.LoginActivity
import com.telen.easylineup.login.LoginViewModel
import com.telen.easylineup.navigation.Routes
import com.telen.easylineup.team.TeamViewModel
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

/** Replaces `SettingsFragment` - the `navigation_settings` root tab. */
@Composable
fun SettingsRoute(navController: NavController) {
    val viewModel = koinViewModel<SettingsViewModel>()
    val loginViewModel = koinViewModel<LoginViewModel>()
    val teamViewModel = koinViewModel<TeamViewModel>()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var teamName by remember { mutableStateOf("") }
    var teamType by remember { mutableStateOf(TeamType.UNKNOWN) }
    var teamImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDeleteAllDataSheet by remember { mutableStateOf(false) }

    var exportPreview by remember { mutableStateOf<ExportBase?>(null) }
    var exportSuccess by remember { mutableStateOf<ExportDataEventSuccess?>(null) }

    var importFileName by remember { mutableStateOf("") }
    var importPreview by remember { mutableStateOf<ExportBase?>(null) }
    var importOverwriteExisting by remember {
        mutableStateOf(
            PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.key_import_data_update_object), false)
        )
    }
    var importSuccess by remember { mutableStateOf<ImportResult?>(null) }
    var prefsVersion by remember { mutableStateOf(0) }

    val openFile = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(loginViewModel::peekImportFile) }

    val createFile = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        val base = exportPreview
        if (uri != null && base != null) {
            exportPreview = null
            viewModel.exportData(base, uri)
        }
    }

    fun putStringPreference(keyRes: Int, value: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(context.getString(keyRes), value)
            .apply()
    }

    fun putIntPreference(keyRes: Int, value: Int) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putInt(context.getString(keyRes), value)
            .apply()
    }

    fun applyNightMode(value: String) {
        AppCompatDelegate.setDefaultNightMode(
            when (value.toIntOrNull()) {
                1 -> AppCompatDelegate.MODE_NIGHT_NO
                2 -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

    fun openPlayStore() {
        FirebaseAnalyticsUtils.onClick(context, "click_settings_play_store")
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=${context.packageName}")
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Timber.e(e)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                val link = "http://play.google.com/store/apps/details?id=${context.packageName}"
                data = Uri.parse(link)
            }
            context.startActivity(intent)
        }
    }

    LaunchedEffect(teamViewModel) {
        teamViewModel.observeCurrentTeamName()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { teamName = it }
            .launchIn(this)

        teamViewModel.observeCurrentTeamType()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { teamType = it }
            .launchIn(this)

        teamViewModel.observeCurrentTeamImage()
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { teamImageUri = it }
            .launchIn(this)
    }

    LaunchedEffect(viewModel) {
        viewModel.events
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { event ->
                when (event) {
                    DeleteAllDataEventSuccess -> {
                        FirebaseAnalyticsUtils.deleteData(context)
                        val intent = Intent(context, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                    }

                    is ExportPreviewReady -> exportPreview = event.exportBase

                    is ExportDataEventSuccess -> {
                        FirebaseAnalyticsUtils.exportData(context)
                        exportSuccess = event
                        Timber.d("Successfully exported data!")
                    }

                    ExportDataEventFailure -> DialogFactory.getErrorDialog(
                        context,
                        title = R.string.settings_export_error_title,
                        message = R.string.settings_export_error_message
                    ).show()

                    is GetTeamsCountSuccess -> navController.navigate(Routes.TeamDetails)

                    GetTeamsCountFailure -> Timber.e("Failed to get teams count")

                    DeleteAllDataEventFailure -> Timber.e("Failed to delete all data")
                }
            }
    }

    // LoginViewModel is shared with the login flow and still RxJava-based; out of scope for
    // this screen's modernization, so its existing Subject subscription is relocated here as-is.
    DisposableEffect(loginViewModel) {
        val disposable = loginViewModel.observeEvents().subscribe({ event ->
            when (event) {
                is ImportPreviewReady -> {
                    importFileName = event.fileName
                    importPreview = event.exportBase
                }

                is ImportSuccessfulEvent -> {
                    FirebaseAnalyticsUtils.importData(context)
                    importPreview = null
                    importSuccess = event.result
                }

                ImportFailure -> DialogFactory.getErrorDialog(
                    context,
                    title = R.string.settings_import_error_title,
                    message = R.string.settings_import_error_message
                ).show()

                else -> {}
            }
        }, { Timber.e(it) })
        onDispose { disposable.dispose() }
    }

    val state = remember(teamName, teamType, teamImageUri, prefsVersion) {
        currentSettingsScreenState(context, teamName, teamType, teamImageUri)
    }

    SettingsScreen(
        state = state,
        onBackClick = { navController.navigateUp() },
        onTeamClick = { viewModel.getTeamsCount() },
        onLineupStyleSelected = { newValue ->
            FirebaseAnalyticsUtils.onClick(context, "click_settings_lineup_style")
            putStringPreference(R.string.key_lineup_style, newValue)
            prefsVersion++
        },
        onThemeSelected = { newValue ->
            FirebaseAnalyticsUtils.onClick(context, "click_settings_theme")
            putStringPreference(R.string.key_day_night_theme, newValue)
            applyNightMode(newValue)
            prefsVersion++
        },
        onMenColorPicked = { color ->
            putIntPreference(R.string.key_men_style, color)
            prefsVersion++
        },
        onWomenColorPicked = { color ->
            putIntPreference(R.string.key_women_style, color)
            prefsVersion++
        },
        onExportClick = {
            FirebaseAnalyticsUtils.onClick(context, "click_settings_export_data")
            viewModel.loadExportPreview()
        },
        onImportClick = {
            FirebaseAnalyticsUtils.onClick(context, "click_settings_import_data")
            openFile.launch(arrayOf("application/octet-stream"))
        },
        onDeleteAllDataClick = { showDeleteAllDataSheet = true },
        onLicensesClick = {
            FirebaseAnalyticsUtils.onClick(context, "click_settings_licenses")
            LicensesManager.getDialog(context).show()
        },
        onPlayStoreClick = { openPlayStore() }
    )

    if (showDeleteAllDataSheet) {
        ConfirmSheet(
            title = stringResource(R.string.setting_delete_data_description),
            message = stringResource(R.string.dialog_delete_cannot_undo_message),
            confirmLabel = stringResource(R.string.action_delete),
            cancelLabel = stringResource(R.string.cancel_button),
            onDismiss = { showDeleteAllDataSheet = false },
            onConfirm = {
                FirebaseAnalyticsUtils.onClick(context, "click_settings_delete_data")
                viewModel.deleteAllData()
            }
        )
    }

    exportPreview?.let { base ->
        ExportConfirmSheet(
            teamName = teamName,
            counts = base.toDataCounts(),
            onDismiss = { exportPreview = null },
            onConfirm = {
                FirebaseAnalyticsUtils.onClick(context, "click_export_confirm")
                createFile.launch(null)
            }
        )
    }

    exportSuccess?.let { success ->
        ExportSuccessSheet(
            folderName = success.folderName,
            fileName = success.fileName,
            counts = success.counts,
            onDismiss = { exportSuccess = null }
        )
    }

    importPreview?.let { base ->
        ImportConfirmSheet(
            fileName = importFileName,
            counts = base.toDataCounts(),
            overwriteExisting = importOverwriteExisting,
            onOverwriteChanged = { checked ->
                importOverwriteExisting = checked
                PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putBoolean(context.getString(R.string.key_import_data_update_object), checked)
                    .apply()
            },
            onDismiss = { importPreview = null },
            onConfirm = {
                FirebaseAnalyticsUtils.onClick(context, "click_import_confirm")
                loginViewModel.confirmImport(base, importOverwriteExisting)
            }
        )
    }

    importSuccess?.let { result ->
        ImportSuccessSheet(
            fileName = importFileName,
            result = result,
            onDismiss = { importSuccess = null }
        )
    }
}
