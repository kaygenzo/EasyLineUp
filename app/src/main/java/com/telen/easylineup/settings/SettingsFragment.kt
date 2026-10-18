/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.settings

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.license.LicensesManager
import com.telen.easylineup.login.ImportFailure
import com.telen.easylineup.login.ImportSuccessfulEvent
import com.telen.easylineup.login.LoginActivity
import com.telen.easylineup.login.LoginViewModel
import com.telen.easylineup.team.TeamViewModel
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.NavigationUtils
import com.telen.easylineup.utils.StorageUtils
import io.reactivex.rxjava3.core.Completable
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SettingsFragment : BaseFragment("Settings") {

    private val viewModel: SettingsViewModel by viewModel()
    private val loginViewModel by viewModels<LoginViewModel>()

    private val teamViewModel by viewModel<TeamViewModel>()

    private var updateExistingOnImport = false
    private var teamName by mutableStateOf("")
    private var teamType by mutableStateOf(TeamType.UNKNOWN)
    private var teamImageUri by mutableStateOf<Uri?>(null)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        updateExistingOnImport = currentSettingsScreenState(
            requireContext(),
            teamName,
            teamType,
            teamImageUri
        ).updateExistingOnImport

        return ComposeView(requireContext()).apply {
            setContent {
                var prefsVersion by remember { mutableStateOf(0) }
                val state = remember(teamName, teamType, teamImageUri, prefsVersion) {
                    currentSettingsScreenState(requireContext(), teamName, teamType, teamImageUri)
                }

                DugoutTheme {
                    SettingsScreen(
                        state = state,
                        onBackClick = { findNavController().navigateUp() },
                        onTeamClick = { viewModel.getTeamsCount() },
                        onLineupStyleSelected = { newValue ->
                            FirebaseAnalyticsUtils.onClick(activity, "click_settings_lineup_style")
                            putStringPreference(R.string.key_lineup_style, newValue)
                            prefsVersion++
                        },
                        onThemeSelected = { newValue ->
                            FirebaseAnalyticsUtils.onClick(activity, "click_settings_theme")
                            putStringPreference(R.string.key_day_night_theme, newValue)
                            applyNightMode(newValue)
                            prefsVersion++
                        },
                        onMenColorPicked = { color ->
                            putIntPreference(R.string.key_men_style, color)
                            prefsVersion++
                        },
                        onWomenColorPicked = { color ->
                            putIntPreference(R.string.key_women_style, color)
                            prefsVersion++
                        },
                        onUpdateExistingChanged = { checked ->
                            updateExistingOnImport = checked
                            PreferenceManager.getDefaultSharedPreferences(requireContext())
                                .edit()
                                .putBoolean(
                                    getString(R.string.key_import_data_update_object),
                                    checked
                                )
                                .apply()
                            prefsVersion++
                        },
                        onExportClick = {
                            FirebaseAnalyticsUtils.onClick(activity, "click_settings_export_data")
                            StorageUtils.createFile(this@SettingsFragment)
                        },
                        onImportClick = {
                            FirebaseAnalyticsUtils.onClick(activity, "click_settings_import_data")
                            StorageUtils.openFile(this@SettingsFragment)
                        },
                        onDeleteAllDataClick = {
                            activity?.let {
                                DialogFactory.getWarningTaskDialog(
                                    context = it,
                                    message = R.string.dialog_delete_cannot_undo_message,
                                    task = Completable.create { emitter ->
                                        FirebaseAnalyticsUtils.onClick(
                                            activity,
                                            "click_settings_delete_data"
                                        )
                                        viewModel.deleteAllData()
                                        emitter.onComplete()
                                    }
                                ).show()
                            }
                        },
                        onLicensesClick = {
                            FirebaseAnalyticsUtils.onClick(activity, "click_settings_licenses")
                            activity?.let { LicensesManager.getDialog(it).show() }
                        },
                        onPlayStoreClick = { openPlayStore() }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModelEvents()
        observeLoginEvents()
        observeTeam()
    }

    private fun observeTeam() {
        teamViewModel.observeCurrentTeamName()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { teamName = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        teamViewModel.observeCurrentTeamType()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { teamType = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        teamViewModel.observeCurrentTeamImage()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { teamImageUri = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun putStringPreference(keyRes: Int, value: String) {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .edit()
            .putString(getString(keyRes), value)
            .apply()
    }

    private fun putIntPreference(keyRes: Int, value: Int) {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .edit()
            .putInt(getString(keyRes), value)
            .apply()
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

    private fun openPlayStore() {
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

    // ----- ViewModel events -----

    private fun observeViewModelEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        DeleteAllDataEventSuccess -> {
                            FirebaseAnalyticsUtils.deleteData(activity)
                            val intent = Intent(activity, LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
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
                                    messageArgs = arrayOf(event.pathDirectory)
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
                            argument.putInt(Constants.EXTRA_TEAM_COUNT, event.count)
                            findNavController().navigate(
                                R.id.teamDetailsFragment,
                                argument,
                                NavigationUtils().getOptions()
                            )
                        }

                        GetTeamsCountFailure -> Timber.e("Failed to get teams count")

                        DeleteAllDataEventFailure -> Timber.e("Failed to delete all data")
                    }
                }
            }
        }
    }

    private fun observeLoginEvents() {
        // LoginViewModel is shared with the login flow and still RxJava-based;
        // out of scope for this screen's modernization, so kept as-is.
        val disposable = loginViewModel.observeEvents().subscribe({ event ->
            activity?.run {
                when (event) {
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
        }, { Timber.e(it) })
        disposables.add(disposable)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                StorageUtils.REQUEST_CODE_OPEN_DOCUMENT -> {
                    val uri = data?.data ?: return
                    loginViewModel.importData(uri, updateExistingOnImport)
                }

                StorageUtils.REQUEST_CODE_CHOOSE_DIRECTORY -> {
                    val dirUri = data?.data ?: return
                    viewModel.exportData(dirUri)
                }
            }
        }
    }
}
