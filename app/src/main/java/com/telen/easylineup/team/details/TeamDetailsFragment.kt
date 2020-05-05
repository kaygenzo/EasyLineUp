package com.telen.easylineup.team.details

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.lineup.list.LineupViewModel
import com.telen.easylineup.team.TeamViewModel
import com.telen.easylineup.team.createTeam.TeamCreationActivity
import com.telen.easylineup.utils.DialogFactory
import com.telen.easylineup.utils.ready
import io.reactivex.Completable
import kotlinx.android.synthetic.main.fragment_team_details.*
import timber.log.Timber

const val REQUEST_EDIT_TEAM = 0

class TeamDetailsFragment: Fragment() {

    private lateinit var teamViewModel: TeamViewModel
    private lateinit var lineupViewModel: LineupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        teamViewModel = ViewModelProviders.of(this)[TeamViewModel::class.java]
        lineupViewModel = ViewModelProviders.of(this)[LineupViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_team_details, container, false)
        load()
        return view
    }

    private fun load() {
        teamViewModel.observeTeam().observe(viewLifecycleOwner, Observer {

            teamName.text = it.name

            try {
                val imageUri = Uri.parse(it.image)
                setImage(Picasso.get().load(imageUri))
            }
            catch (e: Exception) {
                Timber.d("Image is surely null or something bad happened: ${e.message}")
                setImage(Picasso.get().load(R.drawable.ic_unknown_team))
            }

            when(it.type) {
                TeamType.SOFTBALL.id -> {
                    teamTypeImage.setImageResource(R.drawable.image_softball_ball)
                    teamTypeDescription.text = getString(R.string.team_details_type_softball)
                }
                TeamType.BASEBALL.id -> {
                    teamTypeImage.setImageResource(R.drawable.image_baseball_ball)
                    teamTypeDescription.text = getString(R.string.team_details_type_baseball)
                }
                else -> {
                    teamTypeImage.setImageResource(R.drawable.ic_warning_red_24dp)
                    teamTypeDescription.text = getString(R.string.team_details_type_unknown)
                }
            }
        })

        teamViewModel.observePlayers().observe(viewLifecycleOwner, Observer {
            teamPlayersDescription.text = resources.getQuantityString(R.plurals.team_details_team_size, it.size, it.size)
        })

        lineupViewModel.observeCategorizedLineups().observe(viewLifecycleOwner, Observer {
            val tournamentsSize = it.size
            val lineupsSize = it.map { pair -> pair.second.size }.sum()
            val tournamentsQuantity = resources.getQuantityString(R.plurals.tournaments_quantity, tournamentsSize, tournamentsSize)
            val lineupsQuantity = resources.getQuantityString(R.plurals.lineups_quantity, lineupsSize, lineupsSize)
            val headerText = getString(R.string.tournaments_summary_header, tournamentsQuantity, lineupsQuantity)
            teamTournamentsDescription.text = headerText
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        teamViewModel.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        arguments?.getInt(Constants.EXTRA_TEAM_COUNT)?.let {
            if(it < 2)
                inflater.inflate(R.menu.team_edit_menu, menu)
            else {
                inflater.inflate(R.menu.team_edit_or_delete_menu, menu)
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                teamViewModel.team?.let {
                    val intent = Intent(activity, TeamCreationActivity::class.java)
                    intent.putExtra(Constants.EXTRA_CAN_EXIT, true)
                    intent.putExtra(Constants.EXTRA_TEAM, it)
                    startActivityForResult(intent, REQUEST_EDIT_TEAM)
                }
                true
            }
            R.id.action_delete -> {
                activity?.let {
                    teamViewModel.team?.let { team ->
                        DialogFactory.getWarningDialog(it,
                                it.getString(R.string.dialog_delete_team_title, team.name),
                                it.getString(R.string.dialog_delete_cannot_undo_message),
                                Completable.create { emitter ->
                                    teamViewModel.deleteTeam(team)
                                            .subscribe({
                                                findNavController().popBackStack()
                                            }, {
                                                Timber.e(it)
                                            })
                                    emitter.onComplete()
                                })
                                .show()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_EDIT_TEAM -> {
                load()
            }
        }
    }

    private fun setImage(request: RequestCreator) {
        teamImage.ready {
            try {
                request.resize(teamImage.width, teamImage.height)
                        .centerCrop()
                        .transform(RoundedTransformationBuilder()
                                .borderColor(Color.BLACK)
                                .borderWidthDp(2f)
                                .cornerRadiusDp(16f)
                                .oval(true)
                                .build())
                        .placeholder(R.drawable.ic_unknown_team)
                        .error(R.drawable.ic_unknown_team)
                        .into(teamImage, object : Callback {
                            override fun onSuccess() {
                                Timber.d("Successfully loaded image")
                            }

                            override fun onError(e: Exception?) {
                                Timber.e(e)
                            }

                        })
            } catch (e: IllegalArgumentException) {
                Timber.e(e)
            }
        }
    }
}