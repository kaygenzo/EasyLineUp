package com.telen.easylineup.newLineup.defense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.telen.easylineup.R
import com.telen.easylineup.newLineup.PlayersPositionViewModel
import kotlinx.android.synthetic.main.card_defense_editable.view.*

class DefenseFragment: Fragment() {

    lateinit var viewModel: PlayersPositionViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.new_lineup_defense_fragment, container, false)

        viewModel = ViewModelProviders.of(activity as AppCompatActivity).get(PlayersPositionViewModel::class.java)
//        viewModel.team.observe(this, Observer { team ->
//            view.fieldAndPlayersRootView.setListPlayerInContainer(team.players)
//        })
        return view
    }
}