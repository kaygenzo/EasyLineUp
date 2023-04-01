package com.telen.easylineup.lineup.defense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModelProviders
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.databinding.FragmentLineupDefenseFixedBinding
import com.telen.easylineup.launch
import com.telen.easylineup.lineup.LineupViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class DefenseFragmentFixed : BaseFragment("DefenseFragmentFixed") {
    private var viewModel: LineupViewModel? = null

    private var binder: FragmentLineupDefenseFixedBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binder = FragmentLineupDefenseFixedBinding.inflate(inflater, container, false)
        this.binder = binder

        parentFragment?.let { parent ->
            viewModel = ViewModelProviders.of(parent).get(LineupViewModel::class.java)

            viewModel?.run {
                observeLineupStrategy().observe(viewLifecycleOwner) {
                    binder.cardDefenseView.init(it)
                }

                Transformations.switchMap(observeDefensePlayers()) { players ->
                    Transformations.map(observeLineupMode()) { mode ->
                        Pair(players, mode)
                    }
                }.observe(viewLifecycleOwner) {
                    launch(Completable.timer(100, TimeUnit.MILLISECONDS), {
                        binder.cardDefenseView.setListPlayer(it.first, it.second)
                    }, {
                        /* Nothing to do */
                    }, Schedulers.computation())
                }
            }
        }

        return binder.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binder = null
    }
}