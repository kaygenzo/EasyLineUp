package com.telen.easylineup.team.createTeam.teamMain

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.databinding.FragmentTeamEditBinding
import com.telen.easylineup.launch
import com.telen.easylineup.team.createTeam.SetupViewModel
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.ImagePickerUtils
import com.telen.easylineup.views.TeamFormListener
import kotlinx.android.synthetic.main.view_create_team.*
import timber.log.Timber

class TeamEditFragment : BaseFragment("TeamEditFragment"), TeamFormListener {

    private val viewModel by activityViewModels<SetupViewModel>()
    private var binder: FragmentTeamEditBinding? = null

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.data?.let { imageUri ->
                    context?.contentResolver?.let { ImagePickerUtils.persistImage(it, imageUri) }
                    binder?.editTeamForm?.onImageUriReceived(imageUri)
                }
            }
        }

    override fun onImagePickerRequested() {
        FirebaseAnalyticsUtils.onClick(activity, "click_team_edit_image_pick")
        activity?.let { ImagePickerUtils.launchPicker(it, view, pickImage) }
    }

    override fun onNameChanged(name: String) {
        viewModel.setTeamName(name)
    }

    override fun onImageChanged(imageUri: Uri?) {
        viewModel.setTeamImage(imageUri?.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launch(viewModel.errors, {
            when (it) {
                SetupViewModel.StepError.NAME_EMPTY -> {
                    teamNameInputLayout.error = getString(R.string.team_creation_error_name_empty)
                    FirebaseAnalyticsUtils.emptyTeamName(activity)
                }
                else -> {
                    Toast.makeText(
                        activity,
                        "Something wrong happened, please try again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binder = FragmentTeamEditBinding.inflate(inflater, container, false)
        this.binder = binder

        binder.editTeamForm.setListener(this@TeamEditFragment)

        viewModel.observeTeamName().observe(viewLifecycleOwner) {
            binder.editTeamForm.setName(it)
        }

        viewModel.observeTeamImage().observe(viewLifecycleOwner) {
            it?.let { binder.editTeamForm.setImage(it) }
        }

        return binder.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binder = null
    }
}