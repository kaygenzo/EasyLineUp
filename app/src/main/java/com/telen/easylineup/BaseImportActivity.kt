/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.preference.PreferenceManager
import com.telen.easylineup.compose.ImportConfirmSheet
import com.telen.easylineup.compose.ImportSuccessSheet
import com.telen.easylineup.compose.toDataCounts
import com.telen.easylineup.domain.model.export.ExportBase
import com.telen.easylineup.domain.usecases.ImportResult
import com.telen.easylineup.login.GetTeamFailed
import com.telen.easylineup.login.GetTeamSuccess
import com.telen.easylineup.login.ImportFailure
import com.telen.easylineup.login.ImportPreviewReady
import com.telen.easylineup.login.ImportSuccessfulEvent
import com.telen.easylineup.login.LoginViewModel
import com.telen.easylineup.team.createTeam.TeamCreationActivity
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.StorageUtils
import timber.log.Timber

abstract class BaseImportActivity : BaseActivity() {
    protected val viewModel by viewModels<LoginViewModel>()

    private var pendingFileName by mutableStateOf("")
    private var importPreview by mutableStateOf<ExportBase?>(null)
    private var importResult by mutableStateOf<ImportResult?>(null)
    protected var updateExistingOnImport by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateExistingOnImport = PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean(getString(R.string.key_import_data_update_object), false)

        val disposable = viewModel.observeEvents().subscribe({
            when (it) {
                is ImportPreviewReady -> {
                    pendingFileName = it.fileName
                    importPreview = it.exportBase
                }

                is ImportSuccessfulEvent -> {
                    FirebaseAnalyticsUtils.importData(this)
                    importPreview = null
                    importResult = it.result
                }

                ImportFailure -> displayImportFailure()

                is GetTeamSuccess -> {
                    // the file contains at least one main team
                    launchHome()
                }

                GetTeamFailed -> {
                    // no main team selected, let's create new one
                    launchTeamCreation()
                }
            }
        }, {
            Timber.e(it)
        })

        this.disposables.add(disposable)
    }

    /**
     * Renders the import confirm/success sheets from this Activity's own shared state - a
     * sibling to call inside each subclass's own `setContent {}`, so both the cold-start
     * file-open flow and the Welcome screen's "Import an existing team" show the exact same
     * confirmation regardless of how they got here.
     */
    @Composable
    protected fun ImportSheets(onCancelled: () -> Unit = {}) {
        importPreview?.let { exportBase ->
            ImportConfirmSheet(
                fileName = pendingFileName,
                counts = exportBase.toDataCounts(),
                overwriteExisting = updateExistingOnImport,
                onOverwriteChanged = { checked ->
                    updateExistingOnImport = checked
                    PreferenceManager.getDefaultSharedPreferences(this)
                        .edit()
                        .putBoolean(getString(R.string.key_import_data_update_object), checked)
                        .apply()
                },
                onDismiss = {
                    importPreview = null
                    onCancelled()
                },
                onConfirm = {
                    FirebaseAnalyticsUtils.onClick(this, "click_import_data_confirm")
                    viewModel.confirmImport(exportBase, updateExistingOnImport)
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
    }

    protected fun importDataFromBrowser() {
        StorageUtils.openFile(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                StorageUtils.REQUEST_CODE_OPEN_DOCUMENT -> {
                    val uri = data?.data ?: return
                    viewModel.peekImportFile(uri)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clear()
    }

    protected fun launchTeamCreation() {
        FirebaseAnalyticsUtils.startTutorial(this, true)
        val intent = Intent(this, TeamCreationActivity::class.java)
        startActivityForResult(intent, REQUEST_CREATE_TEAM)
    }

    protected fun launchHome() {
        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    protected open fun displayImportFailure() {
        DialogFactory.getErrorDialog(
            context = this@BaseImportActivity,
            title = R.string.settings_import_error_title,
            message = R.string.settings_import_error_message
        ).show()
    }

    companion object {
        const val REQUEST_CREATE_TEAM = 2
    }
}
