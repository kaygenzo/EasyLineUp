/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.telen.easylineup.compose.ErrorDialog
import com.telen.easylineup.compose.ImportConfirmSheet
import com.telen.easylineup.compose.ImportSuccessSheet
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.model.export.toDataCounts
import com.telen.easylineup.domain.usecases.ImportResult
import com.telen.easylineup.login.GetTeamFailed
import com.telen.easylineup.login.GetTeamSuccess
import com.telen.easylineup.login.ImportFailure
import com.telen.easylineup.login.ImportPreviewReady
import com.telen.easylineup.login.ImportSuccessfulEvent
import com.telen.easylineup.login.LoginViewModel
import com.telen.easylineup.team.createTeam.TeamCreationActivity
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

abstract class BaseImportActivity : BaseActivity() {
    protected val viewModel by viewModel<LoginViewModel>()

    private var pendingFileName by mutableStateOf("")
    private var importPreview by mutableStateOf<ExportBase?>(null)
    private var importResult by mutableStateOf<ImportResult?>(null)
    private var importFailed by mutableStateOf(false)

    private val openFile = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(viewModel::peekImportFile) }

    private val createTeam = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> if (result.resultCode == Activity.RESULT_OK) onTeamCreated() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect {
                    when (it) {
                        is ImportPreviewReady -> {
                            pendingFileName = it.fileName
                            importPreview = it.exportBase
                        }

                        is ImportSuccessfulEvent -> {
                            FirebaseAnalyticsUtils.importData(this@BaseImportActivity)
                            importPreview = null
                            importResult = it.result
                        }

                        ImportFailure -> importFailed = true

                        is GetTeamSuccess -> {
                            // the file contains at least one main team
                            launchHome()
                        }

                        GetTeamFailed -> {
                            // no main team selected, let's create new one
                            launchTeamCreation()
                        }
                    }
                }
            }
        }
    }

    /**
     * Renders the import confirm/success sheets from this Activity's own shared state - a
     * sibling to call inside each subclass's own `setContent {}`, so both the cold-start
     * file-open flow and the Welcome screen's "Import an existing team" show the exact same
     * confirmation regardless of how they got here.
     */
    @Composable
    protected fun ImportSheets(onCancelled: () -> Unit = {}, onFailureDismissed: () -> Unit = {}) {
        val overwriteExisting by viewModel.importOverwriteExisting.collectAsStateWithLifecycle()
        importPreview?.let { exportBase ->
            ImportConfirmSheet(
                fileName = pendingFileName,
                counts = exportBase.toDataCounts(),
                overwriteExisting = overwriteExisting,
                onOverwriteChanged = viewModel::onImportOverwriteChanged,
                onDismiss = {
                    importPreview = null
                    onCancelled()
                },
                onConfirm = {
                    FirebaseAnalyticsUtils.onClick(this, "click_import_data_confirm")
                    viewModel.confirmImport(exportBase, overwriteExisting)
                }
            )
        }

        importResult?.let { result ->
            ImportSuccessSheet(
                fileName = pendingFileName,
                result = result,
                onDismiss = {
                    importResult = null
                    viewModel.getMainTeam()
                }
            )
        }

        if (importFailed) {
            ErrorDialog(
                title = stringResource(R.string.settings_import_error_title),
                message = stringResource(R.string.settings_import_error_message),
                onDismiss = {
                    importFailed = false
                    onFailureDismissed()
                }
            )
        }
    }

    protected fun importDataFromBrowser() {
        openFile.launch(arrayOf(IMPORT_MIME_TYPE))
    }

    protected fun launchTeamCreation() {
        FirebaseAnalyticsUtils.startTutorial(this, true)
        createTeam.launch(Intent(this, TeamCreationActivity::class.java))
    }

    /** Called once [TeamCreationActivity] saved the new team; nothing to do by default. */
    protected open fun onTeamCreated() {}

    protected fun launchHome() {
        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    private companion object {
        // What mail and messaging apps hand an .elu attachment over as.
        const val IMPORT_MIME_TYPE = "application/octet-stream"
    }
}
