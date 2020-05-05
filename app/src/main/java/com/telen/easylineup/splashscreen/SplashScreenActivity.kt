package com.telen.easylineup.splashscreen

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.telen.easylineup.BuildConfig
import com.telen.easylineup.HomeActivity
import com.telen.easylineup.R
import com.telen.easylineup.domain.application.ApplicationPort
import com.telen.easylineup.login.LoginActivity
import com.telen.easylineup.mock.DatabaseMockProvider
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.splashscreen.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit

class SplashScreenActivity: AppCompatActivity(), KoinComponent {

    private val domain: ApplicationPort by inject()
    private var disposable: Disposable? = null

    companion object {
        const val REQUEST_CREATE_TEAM = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splashscreen)
        appVersion.text = BuildConfig.VERSION_NAME
        var commands: Completable = Completable.timer(3000, TimeUnit.MILLISECONDS)
        if(BuildConfig.usePrefilledDatabase) {
            commands = commands.andThen(DatabaseMockProvider().createMockDatabase(this))
        }
        disposable = commands.andThen(domain.getTeam())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    launchHome()
                }, {
                    launchLoginScreen()
                })
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.takeIf { !it.isDisposed }?.dispose()
    }

    private fun launchLoginScreen() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun launchHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}