package com.telen.easylineup.lineup.defense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.lineup.PlayersPositionViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_lineup_defense_fixed.view.*
import java.util.concurrent.TimeUnit

class DefenseFragmentFixed: BaseFragment("DefenseFragmentFixed") {
    lateinit var viewModel: PlayersPositionViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_lineup_defense_fixed, container, false)

        parentFragment?.let { parent ->
            viewModel = ViewModelProviders.of(parent).get(PlayersPositionViewModel::class.java)

            //TODO to get from a strategy when it will be developed
            view.cardDefenseView.init(FieldPosition.values().filter { FieldPosition.isDefensePlayer(it.position) || it == FieldPosition.DP_DH })

            viewModel.lineupID?.let {
                viewModel.registerLineupAndPositionsChanged().observe(viewLifecycleOwner, Observer { players ->
                    val displayDisposable = Completable.timer(100, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.computation())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                view.cardDefenseView.setListPlayer(players)
                            }, {

                            })
                    disposables.add(displayDisposable)
                })
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
    }
}