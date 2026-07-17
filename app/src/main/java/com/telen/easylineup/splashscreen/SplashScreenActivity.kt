/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.splashscreen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.telen.easylineup.BaseImportActivity
import com.telen.easylineup.databinding.SplashscreenBinding
import com.telen.easylineup.domain.UseCaseHandler
import com.telen.easylineup.domain.usecases.GetTeam
import com.telen.easylineup.login.LoginActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SplashScreenActivity : BaseImportActivity(), KoinComponent {
    private val useCaseHandler: UseCaseHandler by inject()
    private val getTeam: GetTeam by inject()
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
            val disposable = useCaseHandler.execute(getTeam, GetTeam.RequestValues())
                .map { it.team }
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
        val intent = Intent(this, ImportDataActivity::class.java).apply {
            this.data = data
        }
        activityResult.launch(intent)
    }
}
