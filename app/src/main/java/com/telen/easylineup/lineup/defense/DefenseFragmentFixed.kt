/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.defense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.databinding.FragmentLineupDefenseFixedBinding
import com.telen.easylineup.launch
import com.telen.easylineup.lineup.LineupViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

            viewModel.observeLineupStrategy()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .onEach { cardDefenseView.init(it) }
                .launchIn(viewLifecycleOwner.lifecycleScope)

            combine(
                viewModel.observeDefensePlayers(),
                viewModel.observeLineupMode()
            ) { players, mode -> Pair(players, mode) }
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .onEach {
                    launch(Completable.timer(100, TimeUnit.MILLISECONDS), {
                        cardDefenseView.setListPlayer(it.first, it.second)
                    }, {
                        /* Nothing to do */
                    }, Schedulers.computation())
                }
                .launchIn(viewLifecycleOwner.lifecycleScope)
        }.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binder = null
    }
}
