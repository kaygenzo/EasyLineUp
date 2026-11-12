/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.splashscreen

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.telen.easylineup.BaseImportActivity
import com.telen.easylineup.login.LoginActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * Pure bootstrap/routing - decides between Home, Login or an import flow. The system
 * SplashScreen API's own icon-on-background splash (Theme.App.Starting, set in the
 * manifest) stays up the whole time (setKeepOnScreenCondition { true }), so this
 * Activity never actually shows its own UI; it used to inflate a custom splashscreen.xml
 * layout that was permanently hidden behind that system splash and has been removed.
 */
// Not a second launch screen: it shows nothing of its own and only routes (see above).
@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : BaseImportActivity() {
    private val splashViewModel by viewModel<SplashViewModel>()
    private val activityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { true }

        val data = intent.data
        data?.let {
            launchImportActivity(it)
        } ?: run {
            lifecycleScope.launch {
                splashViewModel.getTeam()
                    .onSuccess { launchHome() }
                    .onFailure {
                        Timber.e(it)
                        launchLoginScreen()
                    }
            }
        }
    }

    private fun launchLoginScreen() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun launchImportActivity(data: Uri) {
        val intent = Intent(this, ImportDataActivity::class.java).apply {
            this.data = data
        }
        activityResult.launch(intent)
    }
}
