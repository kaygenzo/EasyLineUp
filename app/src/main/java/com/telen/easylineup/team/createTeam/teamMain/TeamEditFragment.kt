/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.team.createTeam.teamMain

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.databinding.FragmentTeamEditBinding
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.launch
import com.telen.easylineup.team.createTeam.SetupViewModel
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.ImagePickerUtils
import com.telen.easylineup.views.TeamFormListener
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class TeamEditFragment : BaseFragment("TeamEditFragment"), TeamFormListener {
    private val viewModel by activityViewModels<SetupViewModel>()
    private var binding: FragmentTeamEditBinding? = null
    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        it?.let { uri ->
            context?.contentResolver?.let { ImagePickerUtils.persistImage(it, uri) }
            binding?.editTeamForm?.onImageUriReceived(uri)
        }
    }

    override fun onImagePickerRequested() {
        FirebaseAnalyticsUtils.onClick(activity, "click_team_edit_image_pick")
        activity?.let { ImagePickerUtils.launchPicker(pickImage) }
    }

    override fun onNameChanged(name: String) {
        viewModel.setTeamName(name)
    }

    override fun onImageChanged(imageUri: Uri?) {
        viewModel.setTeamImage(imageUri?.toString())
    }

    override fun onTeamTypeChanged(teamType: TeamType) {
        viewModel.setTeamType(teamType.position)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launch(viewModel.errors, {
            when (it) {
                SetupViewModel.StepError.NAME_EMPTY -> {
                    binding?.run {
                        editTeamForm.binding.teamNameInputLayout.error =
                                getString(R.string.team_creation_error_name_empty)
                    }
                    FirebaseAnalyticsUtils.emptyTeamName(activity)
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentTeamEditBinding.inflate(inflater, container, false).apply {
            binding = this

            with(editTeamForm) {
                setListener(this@TeamEditFragment)

                viewModel.observeTeamName()
                    .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                    .onEach { setName(it) }
                    .launchIn(viewLifecycleOwner.lifecycleScope)

                viewModel.observeTeamImage()
                    .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                    .onEach { it?.let { setImage(it) } }
                    .launchIn(viewLifecycleOwner.lifecycleScope)

                viewModel.observeTeamType()
                    .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                    .onEach {
                        setTeamTypes(viewModel.getTeamTypeCardItems())
                        setTeamType(TeamType.getTypeById(it))
                    }
                    .launchIn(viewLifecycleOwner.lifecycleScope)
            }
        }.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
