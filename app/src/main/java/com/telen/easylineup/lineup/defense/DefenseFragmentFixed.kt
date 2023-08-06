package com.telen.easylineup.lineup.defense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.databinding.FragmentLineupDefenseFixedBinding
import com.telen.easylineup.launch
import com.telen.easylineup.lineup.LineupViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class DefenseFragmentFixed : BaseFragment("DefenseFragmentFixed") {
    private val viewModel by viewModels<LineupViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    private var binder: FragmentLineupDefenseFixedBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentLineupDefenseFixedBinding.inflate(inflater, container, false).apply {
            this@DefenseFragmentFixed.binder = this

            viewModel.observeLineupStrategy().observe(viewLifecycleOwner) {
                cardDefenseView.init(it)
            }

            viewModel.observeDefensePlayers().switchMap { players ->
                viewModel.observeLineupMode().map { mode ->
                    Pair(players, mode)
                }
            }.observe(viewLifecycleOwner) {
                launch(Completable.timer(100, TimeUnit.MILLISECONDS), {
                    cardDefenseView.setListPlayer(it.first, it.second)
                }, {
                    /* Nothing to do */
                }, Schedulers.computation())
            }
        }.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binder = null
    }
}