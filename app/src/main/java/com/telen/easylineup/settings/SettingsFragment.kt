/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.settings

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.viewModels
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.telen.easylineup.BuildConfig
import com.telen.easylineup.R
import com.telen.easylineup.license.LicensesManager
import com.telen.easylineup.login.ImportFailure
import com.telen.easylineup.login.ImportSuccessfulEvent
import com.telen.easylineup.login.LoginActivity
import com.telen.easylineup.login.LoginViewModel
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.SharedPreferencesUtils
import com.telen.easylineup.utils.StorageUtils
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber

class SettingsFragment : PreferenceFragmentCompat(),
SharedPreferences.OnSharedPreferenceChangeListener {
    private val viewModel by viewModels<SettingsViewModel>()
    private val loginViewModel by viewModels<LoginViewModel>()
    private val disposables = CompositeDisposable()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        val about: Preference? = findPreference(getString(R.string.key_app_version))
        about?.summary = BuildConfig.VERSION_NAME
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val eventsDisposable = viewModel.observeEvent().subscribe({
            when (it) {
                DeleteAllDataEventSuccess -> {
                    FirebaseAnalyticsUtils.deleteData(activity)
                    val intent = Intent(activity, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                }

                is ExportDataEventSuccess -> {
                    activity?.run {
                        FirebaseAnalyticsUtils.exportData(this)
                        DialogFactory.getSuccessDialog(
                            context = this,
                            title = R.string.settings_export_success_title,
                            message = R.string.settings_export_success_message,
                            messageArgs = arrayOf(it.pathDirectory)
                        ).show()
                    }

                    Timber.d("Successfully exported data!")
                }

                ExportDataEventFailure -> activity?.run {
                    DialogFactory.getErrorDialog(
                        this,
                        title = R.string.settings_export_error_title,
                        message = R.string.settings_export_error_message
                    ).show()
                }

                else -> {}
            }
        }, {
            Timber.e(it)
        })

        disposables.add(eventsDisposable)

        val errorsDisposable = loginViewModel.observeEvents().subscribe({
            activity?.run {
                when (it) {
                    ImportSuccessfulEvent -> {
                        FirebaseAnalyticsUtils.importData(this)
                        DialogFactory.getSuccessDialog(
                            context = this,
                            title = R.string.settings_import_success_title,
                            message = R.string.settings_import_success_message
                        ).show()
                    }

                    ImportFailure -> DialogFactory.getErrorDialog(
                        this,
                        title = R.string.settings_import_error_title,
                        message = R.string.settings_import_error_message
                    ).show()

                    else -> {}
                }
            }
        }, {
            Timber.e(it)
        })

        disposables.add(errorsDisposable)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clear()
        disposables.clear()
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            getString(R.string.key_play_store) -> {
                FirebaseAnalyticsUtils.onClick(activity, "click_settings_play_store")
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("market://details?id=${activity?.packageName}")
                    }
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Timber.e(e)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        val link =
                            "http://play.google.com/store/apps/details?id=${activity?.packageName}"
                        data = Uri.parse(link)
                    }
                    startActivity(intent)
                }
                return true
            }

            getString(R.string.key_delete_data) -> {
                activity?.let {
                    DialogFactory.getWarningTaskDialog(context = it,
                        message = R.string.dialog_delete_cannot_undo_message,
                        task = Completable.create { emitter ->
                            FirebaseAnalyticsUtils.onClick(activity, "click_settings_delete_data")
                            viewModel.deleteAllData()
                            emitter.onComplete()
                        }
                    ).show()
                }
                return true
            }

            getString(R.string.key_export_data) -> {
                FirebaseAnalyticsUtils.onClick(activity, "click_settings_export_data")
                exportData()
                return true
            }

            getString(R.string.key_import_data) -> {
                FirebaseAnalyticsUtils.onClick(activity, "click_settings_import_data")
                importData()
                return true
            }

            getString(R.string.key_app_licenses) -> {
                FirebaseAnalyticsUtils.onClick(activity, "click_settings_licenses")
                activity?.let { LicensesManager.getDialog(it).show() }
                return true
            }

            else -> return super.onPreferenceTreeClick(preference)
        }
    }

    private fun importData() {
        StorageUtils.openFile(this)
    }

    private fun exportData() {
        StorageUtils.createFile(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            getString(R.string.key_lineup_style) -> FirebaseAnalyticsUtils.onClick(
                activity,
                "click_settings_lineup_style"
            )

            getString(R.string.key_bug_report_trigger) -> {
                FirebaseAnalyticsUtils.onClick(activity, "click_settings_bug_report_trigger")
                viewModel.onReportMethodsChosen(requireActivity())
            }

            getString(R.string.key_day_night_theme) -> {
                FirebaseAnalyticsUtils.onClick(activity, "click_settings_theme")
                SharedPreferencesUtils.getStringSetting(
                    requireContext(),
                    R.string.key_day_night_theme,
                    getString(R.string.lineup_theme_default_value)
                ).let {
                    val styleValue = it.toInt()
                    AppCompatDelegate.setDefaultNightMode(
                        when (styleValue) {
                            1 -> AppCompatDelegate.MODE_NIGHT_NO
                            2 -> AppCompatDelegate.MODE_NIGHT_YES
                            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                        }
                    )
                }
            }

            else -> {
                // this is a generated else block
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                StorageUtils.REQUEST_CODE_OPEN_DOCUMENT -> {
                    val uri = data?.data ?: return
                    val key = getString(R.string.key_import_data_update_object)
                    val updateIfExists = findPreference<CheckBoxPreference>(key)?.isChecked ?: false
                    loginViewModel.importData(uri, updateIfExists)
                }

                StorageUtils.REQUEST_CODE_CHOOSE_DIRECTORY -> {
                    val dirUri = data?.data ?: return
                    viewModel.exportData(dirUri)
                }
            }
        }
    }
}
