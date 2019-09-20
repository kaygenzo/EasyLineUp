package com.telen.easylineup.team.createTeam

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.telen.easylineup.R
import com.telen.easylineup.utils.Constants
import com.telen.easylineup.views.TeamFormListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_team_edit.view.*
import timber.log.Timber

class TeamEditFragment: Fragment() , TeamFormListener {

    private lateinit var viewModel: TeamViewModel

    override fun onNameChanged(name: String) {
        viewModel.setTeamName(name)
    }

    override fun onImageChanged(imageUri: Uri) {
        viewModel.setTeamImage(imageUri.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_team_edit, container, false)
        viewModel = ViewModelProviders.of(activity as AppCompatActivity).get(TeamViewModel::class.java)
        viewModel.teamID = arguments?.getLong(Constants.TEAM_ID) ?: 0

        val savedName = savedInstanceState?.getString(Constants.NAME)
        val savedImage = savedInstanceState?.getString(Constants.IMAGE)

        viewModel.teamID?.let {
            if(it > 0) {
                viewModel.getTeam().observe(this, Observer { team ->
                    team?.let {
                        val name = savedName ?: team.name
                        val imagePath = savedImage ?: team.image

                        viewModel.setTeamName(name)
                        view.editTeamForm.setName(name)

                        imagePath?.let { imageUriString ->
                            viewModel.setTeamImage(imageUriString)
                            view.editTeamForm.setImage(Uri.parse(imageUriString))
                        }

                        view.editTeamForm.setListener(this)
                    }
                })
            }
            else {
                view.editTeamForm.setListener(this)
            }
        }

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
                        })
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}