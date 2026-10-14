/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team.createTeam

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.telen.easylineup.BaseActivity
import com.telen.easylineup.R
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.usecases.exceptions.NameEmptyException
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.ImagePickerUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class TeamCreationActivity : BaseActivity() {
    private val viewModel by viewModel<SetupViewModel>()
    private val isEditing by lazy { intent.extras?.get(Constants.EXTRA_TEAM) != null }

    private var teamName by mutableStateOf("")
    private var teamImageUri by mutableStateOf<Uri?>(null)
    private var teamType by mutableStateOf(TeamType.BASEBALL)
    private var nameError by mutableStateOf<String?>(null)

    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        it?.let { uri ->
            ImagePickerUtils.persistImage(contentResolver, uri)
            viewModel.setTeamImage(uri.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setTeam(intent.extras?.get(Constants.EXTRA_TEAM) as? Team)

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

        setContent {
            DugoutTheme {
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
                    onNameChanged = {
                        nameError = null
                        viewModel.setTeamName(it)
                    },
                    onTypeSelected = { viewModel.setTeamType(it) },
                    onCancelClick = { showDiscardDialog("cancel") },
                    onSaveClick = ::onSaveClicked
                )
            }
        }
    }

    private fun onSaveClicked() {
        lifecycleScope.launch {
            viewModel.onSaveClicked()
                .onSuccess {
                    FirebaseAnalyticsUtils.endTutorial(this@TeamCreationActivity)
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                .onFailure { throwable ->
                    if (throwable is NameEmptyException) {
                        nameError = getString(R.string.team_creation_error_name_empty)
                        FirebaseAnalyticsUtils.emptyTeamName(this@TeamCreationActivity)
                    } else {
                        Timber.e(throwable)
                    }
                }
        }
    }

    override fun onBackPressed() {
        showDiscardDialog("back_pressed")
    }

    private fun showDiscardDialog(trigger: String) {
        FirebaseAnalyticsUtils.onClick(this, "click_create_team_cancel_$trigger")
        DialogFactory.getDiscardDialog(this) { _, _ -> finish() }.show()
    }
}
