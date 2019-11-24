package com.telen.easylineup.lineup.defense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.telen.easylineup.R
import com.telen.easylineup.lineup.PlayersPositionViewModel
import kotlinx.android.synthetic.main.fragment_lineup_defense_fixed.view.*

class DefenseFragmentFixed: Fragment() {
    lateinit var viewModel: PlayersPositionViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_lineup_defense_fixed, container, false)

        parentFragment?.let { parent ->
            viewModel = ViewModelProviders.of(parent).get(PlayersPositionViewModel::class.java)

            viewModel.lineupID?.let {
                viewModel.registerLineupAndPositionsChanged().observe(this, Observer { players ->
                    view.cardDefenseView.setListPlayer(players)
                })
            }
        }

        return view
    }

}