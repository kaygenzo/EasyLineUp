package com.telen.easylineup.splashscreen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.telen.easylineup.BaseImportActivity
import com.telen.easylineup.databinding.SplashscreenBinding
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.login.LoginActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SplashScreenActivity : BaseImportActivity(), KoinComponent {

    private val domain: ApplicationInteractor by inject()
    private var binding: SplashscreenBinding? = null
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
            val disposable = domain.teams().getTeam()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    launchHome()
                }, {
                    it.printStackTrace()
                    launchLoginScreen()
                })
            disposables.add(disposable)
        }
    }

    private fun launchLoginScreen() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun launchImportActivity(data: Uri) {
        val intent = Intent(this, ImportDataActivity::class.java)
        intent.data = data
        activityResult.launch(intent)
    }
}