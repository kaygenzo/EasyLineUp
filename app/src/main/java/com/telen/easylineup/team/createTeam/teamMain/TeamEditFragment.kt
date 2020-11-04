package com.telen.easylineup.team.createTeam.teamMain

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.nguyenhoanglam.imagepicker.model.Config
import com.nguyenhoanglam.imagepicker.model.Image
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.Team
import com.telen.easylineup.team.createTeam.SetupViewModel
import com.telen.easylineup.utils.FirebaseAnalyticsUtils
import com.telen.easylineup.utils.ImagePickerUtils
import com.telen.easylineup.views.TeamFormListener
import com.telen.easylineup.views.TeamFormView
import kotlinx.android.synthetic.main.fragment_team_edit.view.*
import kotlinx.android.synthetic.main.view_create_team.*
import timber.log.Timber

class TeamEditFragment: BaseFragment("TeamEditFragment") , TeamFormListener {

    private lateinit var viewModel: SetupViewModel
    private var teamForm: TeamFormView? = null

    override fun onImagePickerRequested() {
        ImagePickerUtils.launchPicker(this)
    }

    override fun onNameChanged(name: String) {
        viewModel.setTeamName(name)
    }

    override fun onImageChanged(imageUri: Uri?) {
        imageUri?.let {
            viewModel.setTeamImage(it.toString())
        } ?: viewModel.setTeamImage(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(activity as AppCompatActivity).get(SetupViewModel::class.java)

        val errorsDisposable = viewModel.errorLiveData.subscribe({
            when(it) {
                SetupViewModel.Error.NAME_EMPTY -> {
                    teamNameInputLayout.error = getString(R.string.team_creation_error_name_empty)
                    FirebaseAnalyticsUtils.emptyTeamName(activity)
                }
                SetupViewModel.Error.NONE -> {
                    teamNameInputLayout.error = ""
                }
                else -> Toast.makeText(activity, "Something wrong happened, please try again", Toast.LENGTH_SHORT).show()
            }
        }, {
            Timber.e(it)
        })

        disposables.add(errorsDisposable)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_team_edit, container, false)

        teamForm = view.editTeamForm

        val disposable = viewModel.getTeam().subscribe({ team ->
            team?.let {
                view.editTeamForm.setName(team.name)
                team.image?.let { imageUriString ->
                    view.editTeamForm.setImage(imageUriString)
                }
            }
        }, { throwable ->
            Timber.e(throwable)
        })
        disposables.add(disposable)

        view.editTeamForm.setListener(this)

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Config.RC_PICK_IMAGES && resultCode == Activity.RESULT_OK) {
            data?.let {
                val pickedImages: ArrayList<Image> = it.getParcelableArrayListExtra(Config.EXTRA_IMAGES)
                pickedImages.firstOrNull()?.let {image ->
                    teamForm?.onImageUriReceived(image)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}