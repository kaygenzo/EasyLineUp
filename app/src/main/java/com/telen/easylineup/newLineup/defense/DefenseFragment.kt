package com.telen.easylineup.newLineup.defense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.telen.easylineup.R
import com.telen.easylineup.newLineup.PlayersPositionViewModel

class DefenseFragment: Fragment() {

    lateinit var viewModel: PlayersPositionViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_new_lineup_defense, container, false)

        viewModel = ViewModelProviders.of(activity as AppCompatActivity).get(PlayersPositionViewModel::class.java)
//        viewModel.team.observe(this, Observer { team ->
//            view.fieldAndPlayersRootView.setListPlayerInContainer(team.players)
//        })
        return view
    }
}