package com.telen.easylineup.team.createTeam

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.telen.easylineup.R
import com.telen.easylineup.utils.Constants
import com.telen.easylineup.views.TeamFormListener
import kotlinx.android.synthetic.main.fragment_team_edit.view.*

class TeamEditFragment: Fragment() , TeamFormListener {

    private lateinit var viewModel: TeamViewModel

    override fun onNameChanged(name: String) {
        viewModel.teamName = name
    }

    override fun onImageChanged(imageUri: Uri) {
        viewModel.teamImage = imageUri.toString()
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
                        view.editTeamForm.setName(savedName ?: team.name)
                        val imagePath = savedImage ?: team.image
                        imagePath?.let { imageUriString ->
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
}