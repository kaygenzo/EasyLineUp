package com.telen.easylineup.splashscreen

import android.annotation.SuppressLint
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

    private val getTeamUseCase = GetTeam(App.database.teamDao(), App.prefs)
    private var disposable: Disposable? = null

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
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }, {
                    val intent = Intent(this, TeamCreationActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                })
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.takeIf { !it.isDisposed }?.dispose()
    }
}