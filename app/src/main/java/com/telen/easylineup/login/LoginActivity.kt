package com.telen.easylineup.login

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders
import com.obsez.android.lib.filechooser.ChooserDialog
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.splashscreen.SplashScreenActivity
import com.telen.easylineup.team.createTeam.TeamCreationActivity
import com.telen.easylineup.utils.DialogFactory
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber


class LoginActivity: AppCompatActivity() {

    companion object {
        const val REQUEST_READ_EXTERNAL_STORAGE = 0
    }

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)

        login_create.setOnClickListener {
            launchTeamCreation()
        }

        login_import.setOnClickListener {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_READ_EXTERNAL_STORAGE)
            }
            else {
                importData()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE -> {
                if(grantResults.none { it == PackageManager.PERMISSION_DENIED }) {
                    importData()
                }
            }
        }
    }

    private fun importData() {
        ChooserDialog(this)
                .withFilter(false, false, "elu")
                .withIcon(R.mipmap.ic_launcher)
                .withStartFile("${Environment.getExternalStorageDirectory().path}/${Constants.EXPORTS_DIRECTORY}")
                .withChosenListener { path, _ ->
                    viewModel.importData(path)
                            .subscribe({
                                DialogFactory.getSimpleDialog(this@LoginActivity, getString(R.string.settings_import_success))
                                        .setConfirmClickListener {
                                            //check teams
                                            viewModel.getMainTeam().subscribe({
                                                //the file contains at least one main team
                                                launchHome()
                                            }, {
                                                // no main team selected, let's create new one
                                                launchTeamCreation()
                                            })
                                            it.dismiss()
                                        }
                                        .show()
                            }, {
                                Timber.e(it)
                                DialogFactory.getErrorDialog(this@LoginActivity, getString(R.string.settings_import_error))
                                        .setConfirmClickListener { it.dismiss() }
                                        .show()
                            })
                }
                .withOnCancelListener { dialog -> dialog.cancel() }
                .build()
                .show()
    }

    private fun launchTeamCreation() {
        val intent = Intent(this, TeamCreationActivity::class.java)
        startActivityForResult(intent, SplashScreenActivity.REQUEST_CREATE_TEAM)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == SplashScreenActivity.REQUEST_CREATE_TEAM && resultCode == Activity.RESULT_OK) {
            launchHome()
        }
    }

    private fun launchHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}