package com.telen.easylineup.splashscreen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.telen.easylineup.*
import com.telen.easylineup.domain.GetTeam
import com.telen.easylineup.mock.DatabaseMockProvider
import com.telen.easylineup.team.createTeam.TeamCreationActivity
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.splashscreen.*
import java.util.concurrent.TimeUnit

class SplashScreenActivity: AppCompatActivity() {

    private val getTeamUseCase = GetTeam(App.database.teamDao())
    private var disposable: Disposable? = null

    companion object {
        const val REQUEST_CREATE_TEAM = 0
    }

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splashscreen)
        appVersion.text = BuildConfig.VERSION_NAME
        var commands: Completable = Completable.timer(3000, TimeUnit.MILLISECONDS)
        if(BuildConfig.usePrefilledDatabase) {
            commands = commands.andThen(DatabaseMockProvider().createMockDatabase(this))
        }
        disposable = commands.andThen(UseCaseHandler.execute(getTeamUseCase, GetTeam.RequestValues()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    launchHome()
                }, {
                    launchTeamCreation()
                })
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.takeIf { !it.isDisposed }?.dispose()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CREATE_TEAM && resultCode == Activity.RESULT_OK) {
            launchHome()
        }
    }

    private fun launchHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun launchTeamCreation() {
        val intent = Intent(this, TeamCreationActivity::class.java)
        startActivityForResult(intent, REQUEST_CREATE_TEAM)
    }
}