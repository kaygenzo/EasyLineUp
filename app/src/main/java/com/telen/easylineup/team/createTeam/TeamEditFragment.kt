package com.telen.easylineup.team.createTeam

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.nguyenhoanglam.imagepicker.model.Config
import com.nguyenhoanglam.imagepicker.model.Image
import com.telen.easylineup.R
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.utils.ImagePickerUtils
import com.telen.easylineup.views.TeamFormListener
import com.telen.easylineup.views.TeamFormView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_team_edit.view.*
import timber.log.Timber

class TeamEditFragment: Fragment() , TeamFormListener {

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
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_team_edit, container, false)
        viewModel = ViewModelProviders.of(activity as AppCompatActivity).get(SetupViewModel::class.java)

        val savedName = savedInstanceState?.getString(Constants.NAME)
        val savedImage = savedInstanceState?.getString(Constants.IMAGE)

        teamForm = view.editTeamForm

        viewModel.getTeam().subscribe({ team ->
            team?.let {
                val name = savedName ?: team.name
                val imagePath = savedImage ?: team.image

                viewModel.setTeamName(name)
                view.editTeamForm.setName(name)

                imagePath?.let { imageUriString ->
                    viewModel.setTeamImage(imageUriString)
                    view.editTeamForm.setImage(imageUriString)
                }

                view.editTeamForm.setListener(this)
            }
        }, { throwable ->
            Timber.e(throwable)
            view.editTeamForm.setListener(this)
        })

        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val name = view?.editTeamForm?.getName()
        val image = view?.editTeamForm?.getImageUri()

        if(!TextUtils.isEmpty(name))
            outState.putString(Constants.NAME, name)

        image?.let {
            outState.putString(Constants.IMAGE, it.toString())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.team_edit_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_submit -> {
                viewModel.saveTeam()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            findNavController().popBackStack()
                        }, {
                            Timber.e(it)
                            if(it is NameEmptyException)
                                teamForm?.displayInvalidName()
                        })
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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