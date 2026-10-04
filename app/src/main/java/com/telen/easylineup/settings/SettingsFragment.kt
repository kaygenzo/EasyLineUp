/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.settings

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.BuildConfig
import com.telen.easylineup.R
import com.telen.easylineup.databinding.FragmentSettingsBinding
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.license.LicensesManager
import com.telen.easylineup.login.ImportFailure
import com.telen.easylineup.login.ImportSuccessfulEvent
import com.telen.easylineup.login.LoginActivity
import com.telen.easylineup.login.LoginViewModel
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.NavigationUtils
import com.telen.easylineup.utils.SharedPreferencesUtils
import com.telen.easylineup.utils.StorageUtils
import io.reactivex.rxjava3.core.Completable
import timber.log.Timber

class SettingsFragment : BaseFragment("Settings") {

    private val viewModel by viewModels<SettingsViewModel>()
    private val loginViewModel by viewModels<LoginViewModel>()
    private var binding: FragmentSettingsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSettingsBinding.inflate(inflater, container, false)
        this.binding = binding

        bindTeamRow(binding)
        bindStyleSection(binding)
        bindDataSection(binding)
        bindAboutSection(binding)
        observeViewModelEvents(binding)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    // ----- Team -----

    private fun bindTeamRow(binding: FragmentSettingsBinding) {
        binding.rowTeamTitle.text = getString(R.string.settings_category_team)
        binding.rowTeam.setOnClickListener {
            viewModel.getTeamsCount()
        }
    }

    // ----- Style -----

    private fun bindStyleSection(binding: FragmentSettingsBinding) {
        refreshLineupStyleValue(binding)
        refreshDayNightValue(binding)
        refreshColorSwatches(binding)

        binding.rowLineupStyle.setOnClickListener {
            showSingleChoiceDialog(
                titleRes = R.string.setting_lineup_style_description,
                labelsRes = R.array.pref_lineup_font_style_labels,
                valuesRes = R.array.pref_lineup_font_style_values,
                key = getString(R.string.key_lineup_style),
                default = getString(R.string.lineup_style_default_value)
            ) {
                FirebaseAnalyticsUtils.onClick(activity, "click_settings_lineup_style")
                refreshLineupStyleValue(binding)
            }
        }

        binding.rowMenColor.setOnClickListener { showColorPicker(R.string.key_men_style, R.color.default_men_color) { refreshColorSwatches(binding) } }
        binding.rowWomenColor.setOnClickListener { showColorPicker(R.string.key_women_style, R.color.default_women_color) { refreshColorSwatches(binding) } }

        binding.rowDayNight.setOnClickListener {
            showSingleChoiceDialog(
                titleRes = R.string.setting_day_night_style_description,
                labelsRes = R.array.pref_theme_style_labels,
                valuesRes = R.array.pref_theme_style_values,
                key = getString(R.string.key_day_night_theme),
                default = getString(R.string.lineup_theme_default_value)
            ) { newValue ->
                FirebaseAnalyticsUtils.onClick(activity, "click_settings_theme")
                applyNightMode(newValue)
                refreshDayNightValue(binding)
            }
        }
    }

    private fun refreshLineupStyleValue(binding: FragmentSettingsBinding) {
        val value = SharedPreferencesUtils.getStringSetting(
            requireContext(),
            R.string.key_lineup_style,
            getString(R.string.lineup_style_default_value)
        )
        binding.rowLineupStyleValue.text = labelForValue(R.array.pref_lineup_font_style_labels, R.array.pref_lineup_font_style_values, value)
    }

    private fun refreshDayNightValue(binding: FragmentSettingsBinding) {
        val value = SharedPreferencesUtils.getStringSetting(
            requireContext(),
            R.string.key_day_night_theme,
            getString(R.string.lineup_theme_default_value)
        )
        binding.rowDayNightValue.text = labelForValue(R.array.pref_theme_style_labels, R.array.pref_theme_style_values, value)
    }

    private fun refreshColorSwatches(binding: FragmentSettingsBinding) {
        val menColor = SharedPreferencesUtils.getIntSetting(
            requireContext(),
            R.string.key_men_style,
            ContextCompat.getColor(requireContext(), R.color.default_men_color)
        )
        val womenColor = SharedPreferencesUtils.getIntSetting(
            requireContext(),
            R.string.key_women_style,
            ContextCompat.getColor(requireContext(), R.color.default_women_color)
        )
        binding.rowMenColorSwatch.backgroundTintList = ColorStateList.valueOf(menColor)
        binding.rowWomenColorSwatch.backgroundTintList = ColorStateList.valueOf(womenColor)
    }

    private fun labelForValue(labelsRes: Int, valuesRes: Int, value: String): String {
        val labels = resources.getStringArray(labelsRes)
        val values = resources.getStringArray(valuesRes)
        val index = values.indexOf(value)
        return if (index >= 0) labels[index] else ""
    }

    private fun showSingleChoiceDialog(
        titleRes: Int,
        labelsRes: Int,
        valuesRes: Int,
        key: String,
        default: String,
        onSelected: (newValue: String) -> Unit
    ) {
        val context = context ?: return
        val labels = resources.getStringArray(labelsRes)
        val values = resources.getStringArray(valuesRes)
        val currentValue = PreferenceManager
            .getDefaultSharedPreferences(context)
            .getString(key, default) ?: default
        val checkedIndex = values.indexOf(currentValue).takeIf { it >= 0 } ?: 0

        AlertDialog.Builder(context)
            .setTitle(titleRes)
            .setSingleChoiceItems(labels, checkedIndex) { dialog, which ->
                PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putString(key, values[which])
                    .apply()
                onSelected(values[which])
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun applyNightMode(value: String) {
        AppCompatDelegate.setDefaultNightMode(
            when (value.toIntOrNull()) {
                1 -> AppCompatDelegate.MODE_NIGHT_NO
                2 -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

    private fun showColorPicker(keyRes: Int, defaultColorRes: Int, onPicked: () -> Unit) {
        val context = context ?: return
        val key = getString(keyRes)
        val defaultColor = ContextCompat.getColor(context, defaultColorRes)
        val currentColor = SharedPreferencesUtils.getIntSetting(context, keyRes, defaultColor)

        ColorPickerDialog.Builder(context)
            .setPreferenceName(key)
            .attachAlphaSlideBar(false)
            .attachBrightnessSlideBar(true)
            .setPositiveButton(
                getString(android.R.string.ok),
                ColorEnvelopeListener { envelope, _ ->
                    PreferenceManager.getDefaultSharedPreferences(context)
                        .edit()
                        .putInt(key, envelope.color)
                        .apply()
                    onPicked()
                }
            )
            .setNegativeButton(android.R.string.cancel) { dialogInterface, _ -> dialogInterface.dismiss() }
            .also { it.colorPickerView.setInitialColor(currentColor) }
            .show()
    }

    // ----- Data -----

    private fun bindDataSection(binding: FragmentSettingsBinding) {
        binding.switchUpdateExisting.isChecked = PreferenceManager
            .getDefaultSharedPreferences(requireContext())
            .getBoolean(getString(R.string.key_import_data_update_object), false)
        binding.switchUpdateExisting.setOnCheckedChangeListener { _, isChecked ->
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit()
                .putBoolean(getString(R.string.key_import_data_update_object), isChecked)
                .apply()
        }

        binding.rowExport.setOnClickListener {
            FirebaseAnalyticsUtils.onClick(activity, "click_settings_export_data")
            StorageUtils.createFile(this)
        }

        binding.rowImport.setOnClickListener {
            FirebaseAnalyticsUtils.onClick(activity, "click_settings_import_data")
            StorageUtils.openFile(this)
        }

        binding.rowDelete.setOnClickListener {
            activity?.let {
                DialogFactory.getWarningTaskDialog(
                    context = it,
                    message = R.string.dialog_delete_cannot_undo_message,
                    task = Completable.create { emitter ->
                        FirebaseAnalyticsUtils.onClick(activity, "click_settings_delete_data")
                        viewModel.deleteAllData()
                        emitter.onComplete()
                    }
                ).show()
            }
        }
    }

    // ----- About -----

    private fun bindAboutSection(binding: FragmentSettingsBinding) {
        binding.rowVersionValue.text = BuildConfig.VERSION_NAME

        binding.rowLicenses.setOnClickListener {
            FirebaseAnalyticsUtils.onClick(activity, "click_settings_licenses")
            activity?.let { LicensesManager.getDialog(it).show() }
        }

        binding.rowPlayStore.setOnClickListener {
            FirebaseAnalyticsUtils.onClick(activity, "click_settings_play_store")
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("market://details?id=${activity?.packageName}")
                }
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Timber.e(e)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    val link = "http://play.google.com/store/apps/details?id=${activity?.packageName}"
                    data = Uri.parse(link)
                }
                startActivity(intent)
            }
        }
    }

    // ----- ViewModel events -----

    private fun observeViewModelEvents(binding: FragmentSettingsBinding) {
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

                is GetTeamsCountSuccess -> {
                    val argument = Bundle()
                    argument.putInt(Constants.EXTRA_TEAM_COUNT, it.count)
                    findNavController().navigate(
                        R.id.teamDetailsFragment,
                        argument,
                        NavigationUtils().getOptions()
                    )
                }

                GetTeamsCountFailure -> Timber.e("Failed to get teams count")

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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                StorageUtils.REQUEST_CODE_OPEN_DOCUMENT -> {
                    val uri = data?.data ?: return
                    val updateIfExists = binding?.switchUpdateExisting?.isChecked ?: false
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
