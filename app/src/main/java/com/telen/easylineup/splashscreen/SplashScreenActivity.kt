package com.telen.easylineup.splashscreen

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.telen.easylineup.BaseImportActivity
import com.telen.easylineup.BuildConfig
import com.telen.easylineup.R
import com.telen.easylineup.domain.application.ApplicationInteractor
import com.telen.easylineup.login.LoginActivity
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.splashscreen.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit

class SplashScreenActivity: BaseImportActivity(), KoinComponent {

    private val domain: ApplicationInteractor by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splashscreen)
        appVersion.text = BuildConfig.VERSION_NAME

        val data = intent.data
        data?.let {
            checkDataUri(it)
        } ?: run {
            val disposable = Completable.timer(3000, TimeUnit.MILLISECONDS)
                    .andThen(domain.teams().getTeam())
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

    private fun checkDataUri(uri: Uri) {

        DialogFactory.getSimpleDialog(context = this,
                message = R.string.app_deeplink_file_import_title,
                confirmClick = DialogInterface.OnClickListener { dialogInterface, i ->
                    FirebaseAnalyticsUtils.onClick(this, "click_splashscreen_import")
                    importData(uri)
                },
                cancelClick = DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                    finish()
                },
                cancelable = false
        ).show()
    }

    override fun displayImportFailure() {
        DialogFactory.getErrorDialog(
                context = this@SplashScreenActivity,
                title = R.string.settings_import_error_title,
                message = R.string.settings_import_error_message,
                confirmClick = DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                    finish()
                }).show()
    }
}