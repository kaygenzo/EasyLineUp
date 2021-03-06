package com.telen.easylineup.settings

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.obsez.android.lib.filechooser.ChooserDialog
import com.telen.easylineup.BuildConfig
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.login.ImportFailure
import com.telen.easylineup.login.ImportSuccessfulEvent
import com.telen.easylineup.login.LoginActivity
import com.telen.easylineup.login.LoginViewModel
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.views.CustomEditTextView
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber


class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        const val REQUEST_WRITE_EXTERNAL_STORAGE = 0
        const val REQUEST_READ_EXTERNAL_STORAGE = 1
    }

    lateinit var viewModel: SettingsViewModel
    lateinit var loginViewModel: LoginViewModel

    private val disposables = CompositeDisposable()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        val about = findPreference<Preference>(getString(R.string.key_app_version))
        about?.summary = BuildConfig.VERSION_NAME

        updateLineupStyleSummary()

        viewModel = ViewModelProviders.of(this)[SettingsViewModel::class.java]
        loginViewModel = ViewModelProviders.of(this)[LoginViewModel::class.java]
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)

        val eventsDisposable = viewModel.observeEvent().subscribe({
            when(it) {
                DeleteAllDataEventSuccess -> {
                    FirebaseAnalyticsUtils.deleteData(activity)
                    val intent = Intent(activity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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
                ExportDataEventFailure -> {
                    activity?.run {
                        DialogFactory.getErrorDialog(this, title = R.string.settings_export_error_title, message = R.string.settings_export_error_message).show()
                    }
                }
                else -> {}
            }
        }, {
            Timber.e(it)
        })

        disposables.add(eventsDisposable)

        val errorsDisposable = loginViewModel.observeEvents().subscribe({
            activity?.run {
                when(it) {
                    ImportSuccessfulEvent -> {
                        FirebaseAnalyticsUtils.importData(this)
                        DialogFactory.getSuccessDialog(context = this,
                                title = R.string.settings_import_success_title,
                                message = R.string.settings_import_success_message
                        ).show()
                    }
                    ImportFailure -> {
                        DialogFactory.getErrorDialog(this, title = R.string.settings_import_error_title, message = R.string.settings_import_error_message).show()
                    }
                    else -> {}
                }
            }
        }, {
            Timber.e(it)
        })

        disposables.add(errorsDisposable)

        return v
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clear()
        disposables.clear()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            REQUEST_WRITE_EXTERNAL_STORAGE -> {
                if(grantResults.none { it == PackageManager.PERMISSION_DENIED }) {
                    exportData()
                }
            }
            REQUEST_READ_EXTERNAL_STORAGE -> {
                if(grantResults.none { it == PackageManager.PERMISSION_DENIED }) {
                    importData()
                }
            }
        }
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when(preference?.key) {
            getString(R.string.key_play_store) -> {
                FirebaseAnalyticsUtils.onClick(activity, "click_settings_play_store")
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
                activity?.run {
                    if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        this@SettingsFragment.requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_EXTERNAL_STORAGE)
                    }
                    else {
                        exportData()
                    }
                }
                return true
            }
            getString(R.string.key_import_data) -> {
                activity?.run {
                    if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        this@SettingsFragment.requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ_EXTERNAL_STORAGE)
                    }
                    else {
                        importData()
                    }
                }
                return true
            }
            else -> return super.onPreferenceTreeClick(preference)
        }
    }

    private fun exportData() {

        viewModel.exportDataObjectLiveData.observe(viewLifecycleOwner, Observer {
            viewModel.exportDataObjectLiveData.removeObservers(viewLifecycleOwner)
            context?.run {
                val input = CustomEditTextView(this)
                input.setPlaceholder(it.fallbackName)

                DialogFactory.getSimpleDialog(
                        context = this,
                        message = R.string.export_data_to_file_dialog_title,
                        view = input,
                        confirmText = R.string.export_button,
                        confirmClick = DialogInterface.OnClickListener { dialog, which ->
                            FirebaseAnalyticsUtils.onClick(activity, "click_settings_export_data")
                            val name = input.getName()
                            viewModel.exportDataOnExternalMemory(name, it.fallbackName)
                        }
                ).show()
            }
        })

        viewModel.exportDataTriggered()
    }

    private fun importData() {

        activity?.run {
            ChooserDialog(this)
                    .withFilter(false, false, "elu")
                    .withIcon(R.mipmap.ic_launcher)
                    .withStartFile("${Environment.getExternalStorageDirectory().path}/${Constants.EXPORTS_DIRECTORY}")
                    .withChosenListener { path, _ ->
                        FirebaseAnalyticsUtils.onClick(activity, "click_settings_import_data")
                        val updateIfExists = findPreference<CheckBoxPreference>(getString(R.string.key_import_data_update_object))?.isChecked ?: false
                        loginViewModel.importData(path, updateIfExists)
                    }
                    .withOnCancelListener { dialog -> dialog.cancel() }
                    .build()
                    .show()
        }
    }

    private fun updateLineupStyleSummary() {
        val lineupStylePreference = findPreference<Preference>(getString(R.string.key_lineup_style))
        preferenceManager.sharedPreferences.getString(getString(R.string.key_lineup_style), getString(R.string.lineup_style_default_value))?.let {
            val styleValue = it.toInt()
            lineupStylePreference?.summary = resources.getStringArray(R.array.pref_lineup_font_style_labels)[styleValue]
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when(key) {
            getString(R.string.key_lineup_style) -> {
                FirebaseAnalyticsUtils.onClick(activity, "click_settings_lineup_style")
                updateLineupStyleSummary()
            }
        }
    }
}