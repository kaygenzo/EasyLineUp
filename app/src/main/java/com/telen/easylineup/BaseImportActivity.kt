package com.telen.easylineup

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders
import com.obsez.android.lib.filechooser.ChooserDialog
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.login.*
import com.telen.easylineup.login.GetTeamSuccess
import com.telen.easylineup.team.createTeam.TeamCreationActivity
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import timber.log.Timber
import java.io.FileNotFoundException


abstract class BaseImportActivity: BaseActivity() {

    companion object {
        const val REQUEST_READ_EXTERNAL_STORAGE_FOR_BROWSER = 0
        const val REQUEST_READ_EXTERNAL_STORAGE_FOR_URI = 1
        const val REQUEST_CREATE_TEAM = 2
    }

    private lateinit var viewModel: LoginViewModel
    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)

        val disposable = viewModel.observeEvents().subscribe({
            when(it) {
                ImportSuccessfulEvent -> {
                    FirebaseAnalyticsUtils.importData(this)
                    val dialog = DialogFactory.getSuccessDialog(context = this@BaseImportActivity,
                            title = R.string.settings_import_success_title,
                            message = R.string.settings_import_success_message
                    )
                    dialog.setOnDismissListener {
                        //check teams
                        viewModel.getMainTeam()
                    }
                    dialog.show()
                }
                ImportFailure -> {
                    displayImportFailure()
                }
                is GetTeamSuccess -> {
                    //the file contains at least one main team
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

    protected fun importData() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_READ_EXTERNAL_STORAGE_FOR_BROWSER)
        }
        else {
            importDataFromBrowser()
        }
    }

    protected fun importData(uri: Uri) {
        this.uri = uri
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_READ_EXTERNAL_STORAGE_FOR_URI)
        }
        else {
            importDataFromUri(uri)
        }
    }

    private fun importDataFromBrowser() {
        ChooserDialog(this)
                .withFilter(false, false, "elu")
                .withIcon(R.mipmap.ic_launcher)
                .withStartFile("${Environment.getExternalStorageDirectory().path}/${Constants.EXPORTS_DIRECTORY}")
                .withChosenListener { path, _ -> viewModel.importData(path, true) }
                .withOnCancelListener { dialog -> dialog.cancel() }
                .build()
                .show()
    }

    private fun importDataFromUri(uri: Uri) {
        when {
            uri.scheme.equals("file") -> {
                uri.path?.let {
                    viewModel.importData(it, true)
                }
            }
            uri.scheme.equals("content") -> {
                try {
                    contentResolver.openInputStream(uri)?.let {
                        viewModel.importData(it, true)
                    } ?: let {
                        displayImportFailure()
                    }
                } catch (e: FileNotFoundException) {
                    Timber.e(e)
                    displayImportFailure()
                }
            }
            else -> {
                displayImportFailure()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clear()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.none { it == PackageManager.PERMISSION_DENIED }) {
            when (requestCode) {
                REQUEST_READ_EXTERNAL_STORAGE_FOR_BROWSER -> {
                    importDataFromBrowser()
                }
                REQUEST_READ_EXTERNAL_STORAGE_FOR_URI -> {
                    uri?.let {
                        importDataFromUri(it)
                    }
                }
            }
        }
    }

    protected fun launchTeamCreation() {
        FirebaseAnalyticsUtils.startTutorial(this, true)
        val intent = Intent(this, TeamCreationActivity::class.java)
        startActivityForResult(intent, REQUEST_CREATE_TEAM)
    }

    protected fun launchHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    protected open fun displayImportFailure() {
        DialogFactory.getErrorDialog(
                context = this@BaseImportActivity,
                title = R.string.settings_import_error_title,
                message = R.string.settings_import_error_message).show()
    }
}