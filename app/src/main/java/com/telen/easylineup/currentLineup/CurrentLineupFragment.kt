package com.telen.easylineup.currentLineup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModelProviders
import com.telen.easylineup.R
import com.telen.easylineup.listLineup.LineupViewModel
import kotlinx.android.synthetic.main.fragment_current_lineup.view.*

class CurrentLineupFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_current_lineup, container, false)

        val lineupViewModel = ViewModelProviders.of(this@CurrentLineupFragment).get(LineupViewModel::class.java)

        Transformations.switchMap(lineupViewModel.getLastEditedLineup()) {
            it?.let {
                view.lastEditedLineupCard.apply {
                    setLineupName(it.name)
                }
                lineupViewModel.getPlayerFieldPositionFor(it)
            }
        }.observe(this@CurrentLineupFragment, Observer {
            view.lastEditedLineupCard.apply {
                setListPlayer(it)
            }
        })

        return view
    }
}