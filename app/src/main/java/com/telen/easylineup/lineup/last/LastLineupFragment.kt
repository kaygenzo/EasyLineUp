package com.telen.easylineup.currentLineup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModelProviders
import com.telen.easylineup.R
import com.telen.easylineup.lineup.list.LineupViewModel
import kotlinx.android.synthetic.main.fragment_last_lineup.view.*

class LastLineupFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_last_lineup, container, false)

        val lineupViewModel = ViewModelProviders.of(this@LastLineupFragment).get(LineupViewModel::class.java)
        Transformations.switchMap(lineupViewModel.getLastEditedLineup()) {
            //I don't know why but sometime the first live data is null
            it?.let {
                view.lastEditedLineupCard.apply {
                    setLineupName(it.name)
                }
                lineupViewModel.getPlayersWithPositionsFor(it)
            }
        }.observe(this@LastLineupFragment, Observer {
            view.lastEditedLineupCard.apply {
                setListPlayer(it)
            }
        })

        return view
    }
}