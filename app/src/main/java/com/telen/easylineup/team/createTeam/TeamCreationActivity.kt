/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team.createTeam

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.core.content.IntentCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.telen.easylineup.BaseActivity
import com.telen.easylineup.R
import com.telen.easylineup.compose.ConfirmSheet
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.ImagePickerUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel

class TeamCreationActivity : BaseActivity() {
    private val viewModel by viewModel<SetupViewModel>()
    private val teamToEdit: Team? by lazy {
        IntentCompat.getSerializableExtra(intent, Constants.EXTRA_TEAM, Team::class.java)
    }
    private val isEditing get() = teamToEdit != null

    private var teamName by mutableStateOf("")
    private var teamImageUri by mutableStateOf<Uri?>(null)
    private var teamType by mutableStateOf(TeamType.BASEBALL)
    private var nameError by mutableStateOf<String?>(null)
    private var showDiscardSheet by mutableStateOf(false)

    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        it?.let { uri ->
            ImagePickerUtils.persistImage(contentResolver, uri)
            viewModel.setTeamImage(uri.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setTeam(teamToEdit)

        viewModel.observeTeamName()
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { teamName = it }
            .launchIn(lifecycleScope)

        viewModel.observeTeamImage()
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { teamImageUri = it }
            .launchIn(lifecycleScope)

        viewModel.observeTeamType()
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { teamType = it }
            .launchIn(lifecycleScope)

        viewModel.events
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { event ->
                when (event) {
                    SetupEvent.TeamSaved -> {
                        FirebaseAnalyticsUtils.endTutorial(this@TeamCreationActivity)
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                    SetupEvent.TeamNameEmpty -> {
                        nameError = getString(R.string.team_creation_error_name_empty)
                        FirebaseAnalyticsUtils.emptyTeamName(this@TeamCreationActivity)
                    }
                    SetupEvent.TeamSaveFailed -> Toast.makeText(
                        this@TeamCreationActivity,
                        R.string.error_team_save_failed,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .launchIn(lifecycleScope)

        setContent {
            DugoutTheme {
                // Not an onBackPressed() override: with targetSdk 36, Android 16 dispatches the
                // system back through OnBackPressedDispatcher and never calls it. While the
                // sheet is open it handles back itself.
                BackHandler(enabled = !showDiscardSheet) { showDiscardDialog("back_pressed") }

                TeamFormScreen(
                    state = TeamFormState(
                        isEditing = isEditing,
                        teamName = teamName,
                        teamImageUri = teamImageUri,
                        teamType = teamType,
                        nameError = nameError
                    ),
                    onCloseClick = { showDiscardDialog("close") },
                    onAvatarClick = {
                        FirebaseAnalyticsUtils.onClick(this, "click_team_edit_image_pick")
                        ImagePickerUtils.launchPicker(pickImage)
                    },
                    onAvatarRemoveClick = {
                        FirebaseAnalyticsUtils.onClick(this, "click_team_edit_image_remove")
                        viewModel.setTeamImage(null)
                    },
                    onNameChanged = {
                        nameError = null
                        viewModel.setTeamName(it)
                    },
                    onTypeSelected = { viewModel.setTeamType(it) },
                    onSaveClick = ::onSaveClicked
                )

                if (showDiscardSheet) {
                    ConfirmSheet(
                        title = stringResource(R.string.discard_change_title),
                        message = stringResource(R.string.discard_change_message),
                        confirmLabel = stringResource(R.string.generic_discard),
                        cancelLabel = stringResource(R.string.discard_keep_editing),
                        onDismiss = { showDiscardSheet = false },
                        onConfirm = ::finish
                    )
                }
            }
        }
    }

    private fun onSaveClicked() {
        viewModel.onSaveClicked()
    }

    private fun showDiscardDialog(trigger: String) {
        FirebaseAnalyticsUtils.onClick(this, "click_create_team_cancel_$trigger")
        showDiscardSheet = true
    }
}
