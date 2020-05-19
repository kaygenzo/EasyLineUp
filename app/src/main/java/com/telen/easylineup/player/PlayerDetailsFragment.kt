package com.telen.easylineup.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.utils.ready
import kotlinx.android.synthetic.main.fragment_player_details.view.*
import timber.log.Timber

class PlayerDetailsFragment: Fragment() {

    private lateinit var viewModel: PlayerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(PlayerViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_player_details, container, false)

        val playerID = arguments?.getLong(Constants.PLAYER_ID, 0) ?: 0
        viewModel.playerID = playerID

        viewModel.observePlayer().observe(viewLifecycleOwner, Observer {

            view.playerLicenseValue.text = it.licenseNumber.toString()
            view.shirtNumberValue.text = it.shirtNumber.toString()
            view.playerName.text = it.name.trim()

            view.playerImage.ready {
                try {
                    Picasso.get().load(it.image)
                            .resize(view.playerImage.width, view.playerImage.height)
                            .centerCrop()
                            .placeholder(R.drawable.ic_unknown_field_player)
                            .error(R.drawable.ic_unknown_field_player)
                            .into(view.playerImage)
                } catch (e: IllegalArgumentException) {
                    Timber.e(e)
                }
            }

            when(PlayerSide.getSideByValue(it.pitching)) {
                PlayerSide.LEFT -> {
                    view.pitchingSideValue.text = getString(R.string.generic_left)
                }
                PlayerSide.RIGHT -> {
                    view.pitchingSideValue.text = getString(R.string.generic_right)
                }
                PlayerSide.BOTH -> {
                    view.pitchingSideValue.text = getString(R.string.generic_both)
                }
                null -> {
                    view.pitchingSideValue.text = getString(R.string.generic_unknown)
                }
            }

            when(PlayerSide.getSideByValue(it.batting)) {
                PlayerSide.LEFT -> {
                    view.battingSideValue.text = getString(R.string.generic_left)
                }
                PlayerSide.RIGHT -> {
                    view.battingSideValue.text = getString(R.string.generic_right)
                }
                PlayerSide.BOTH -> {
                    view.battingSideValue.text = getString(R.string.generic_both)
                }
                null -> {
                    view.battingSideValue.text = getString(R.string.generic_unknown)
                }
            }
        })

        viewModel.observeLineups().observe(viewLifecycleOwner, Observer {
            view.gamesPlayedValue.text = it.values.sum().toString()
            view.positionsBarChart.setData(it)
        })

        viewModel.observeTeamType().observe(viewLifecycleOwner, Observer {
            view.positionsBarChart.setTeamType(it)
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clear()
    }
}