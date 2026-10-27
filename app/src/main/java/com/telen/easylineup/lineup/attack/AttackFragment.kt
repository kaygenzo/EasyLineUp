/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.attack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.compose.DugoutTheme
import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.lineup.LineupViewModel
import com.telen.easylineup.views.LineupTypeface
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

class AttackFragment : BaseFragment("AttackFragment") {
    private val viewModel by viewModels<LineupViewModel>(
        ownerProducer = { requireParentFragment() }
    )
    private var batters by mutableStateOf<List<BatterState>>(emptyList())
    private var typeface by mutableStateOf(LineupTypeface.NORMAL)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                DugoutTheme {
                    AttackScreen(
                        batters = batters,
                        editable = viewModel.editable,
                        typeface = typeface,
                        onReorder = { updated ->
                            viewLifecycleOwner.lifecycleScope.launch {
                                viewModel.onBattersChanged(updated)
                                    .onFailure { Timber.e(it) }
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.observeBatters()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { batters = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observeLineupTypeface(requireContext())
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { typeface = it }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }
}
