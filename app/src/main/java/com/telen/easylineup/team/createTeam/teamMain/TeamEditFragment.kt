package com.telen.easylineup.team.createTeam.teamMain

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.databinding.FragmentTeamEditBinding
import com.telen.easylineup.team.createTeam.SetupViewModel
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.views.TeamFormListener
import kotlinx.android.synthetic.main.view_create_team.*
import timber.log.Timber

class TeamEditFragment : BaseFragment("TeamEditFragment"), TeamFormListener {

    private val viewModel by activityViewModels<SetupViewModel>()
    private var binder: FragmentTeamEditBinding? = null

    override fun onImagePickerRequested() {
        FirebaseAnalyticsUtils.onClick(activity, "click_team_edit_image_pick")
//        ImagePickerUtils.launchPicker(this)
    }

    override fun onNameChanged(name: String) {
        viewModel.setTeamName(name)
    }

    override fun onImageChanged(imageUri: Uri?) {
        viewModel.setTeamImage(imageUri?.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val errorsDisposable = viewModel.errors.subscribe({
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
        }, {
            Timber.e(it)
        })

        disposables.add(errorsDisposable)
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

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (requestCode == ImagePickerUtils.REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
//            data?.data?.let {
//                teamForm?.onImageUriReceived(it)
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data)
//    }
}