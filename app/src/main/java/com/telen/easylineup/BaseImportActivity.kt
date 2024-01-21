/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import com.telen.easylineup.login.GetTeamFailed
import com.telen.easylineup.login.GetTeamSuccess
import com.telen.easylineup.login.ImportFailure
import com.telen.easylineup.login.ImportSuccessfulEvent
import com.telen.easylineup.login.LoginViewModel
import com.telen.easylineup.team.createTeam.TeamCreationActivity
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.StorageUtils
import timber.log.Timber

abstract class BaseImportActivity : BaseActivity() {
    private val viewModel by viewModels<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val disposable = viewModel.observeEvents().subscribe({
            when (it) {
                ImportSuccessfulEvent -> {
                    FirebaseAnalyticsUtils.importData(this)
                    val dialog = DialogFactory.getSuccessDialog(
                        context = this@BaseImportActivity,
                        title = R.string.settings_import_success_title,
                        message = R.string.settings_import_success_message
                    )
                    dialog.setOnDismissListener {
                        // check teams
                        viewModel.getMainTeam()
                    }
                    dialog.show()
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

    protected fun importData(uri: Uri) {
        importDataFromUri(uri)
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
                    importDataFromUri(uri)
                }
            }
        }
    }

    private fun importDataFromUri(uri: Uri) {
        viewModel.importData(uri, true)
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
