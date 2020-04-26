package com.telen.easylineup.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import com.telen.easylineup.repository.model.Constants
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

        val disposable1 = viewModel.getPlayer().subscribe({
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

        }, {
            Timber.e(it)
        })

        val disposable2 = viewModel.getAllLineupsForPlayer().subscribe({
            view.gamesPlayedValue.text = it.values.sum().toString()
            view.positionsBarChart.setData(it)
        }, {
            Timber.e(it)
        })

        val disposable3 = viewModel.getTeamType()
                .subscribe({
                    view.positionsBarChart.setTeamType(it)
                }, {
                    Timber.e(it)
                })

        return view
    }
}