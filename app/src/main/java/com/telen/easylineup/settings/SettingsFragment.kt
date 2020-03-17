package com.telen.easylineup.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.telen.easylineup.BuildConfig
import com.telen.easylineup.R
import com.telen.easylineup.login.LoginActivity
import com.telen.easylineup.utils.DialogFactory
import io.reactivex.Completable
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SettingsFragment : PreferenceFragmentCompat() {

    lateinit var viewModel: SettingsViewModel

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        val about = findPreference<Preference>(getString(R.string.key_app_version))
        about?.summary = BuildConfig.VERSION_NAME

        viewModel = ViewModelProviders.of(this)[SettingsViewModel::class.java]
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when(preference?.key) {
            getString(R.string.key_play_store) -> {
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("market://details?id=${activity?.packageName}")
                    startActivity(intent)
                }
                catch (e: ActivityNotFoundException) {
                    Timber.e(e)
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("http://play.google.com/store/apps/details?id=${activity?.packageName}")
                    startActivity(intent)
                }
                return true
            }
            getString(R.string.key_delete_data) -> {
                activity?.let {
                    DialogFactory.getWarningDialog(it, "", getString(R.string.dialog_delete_cannot_undo_message),
                            viewModel.deleteAllData().doOnComplete {
                                Completable.timer(1000, TimeUnit.MILLISECONDS).subscribe({
                                    val intent = Intent(activity, LoginActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                }, {

                                })
                            }.doOnError {
                                Timber.e(it)
                            }
                    ).show()
                }

                return true
            }
            else -> return super.onPreferenceTreeClick(preference)
        }
    }
}