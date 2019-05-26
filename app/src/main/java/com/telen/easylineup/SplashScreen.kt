package com.telen.easylineup

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Completable
import kotlinx.android.synthetic.main.splashscreen.*
import java.util.concurrent.TimeUnit

class SplashScreen: AppCompatActivity() {

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splashscreen)
        appVersion.text = BuildConfig.VERSION_NAME
        Completable.timer(3000, TimeUnit.MILLISECONDS)
                .subscribe {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
    }
}