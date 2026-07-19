/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.lineup.attack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.getkeepsafe.taptargetview.TapTargetView
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.databinding.FragmentListBatterBinding
import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.launch
import com.telen.easylineup.lineup.LineupViewModel
import com.telen.easylineup.utils.FeatureViewFactory
import com.telen.easylineup.utils.SharedPreferencesUtils
import com.telen.easylineup.views.ItemDecoratorAttackRecycler
import com.telen.easylineup.views.LineupTypeface
import io.reactivex.rxjava3.core.Completable
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.util.concurrent.TimeUnit

class AttackFragment : BaseFragment("AttackFragment"), BatterListener {
    private var binder: FragmentListBatterBinding? = null
    private val adapterDataList: MutableList<BatterState> = mutableListOf()
    private val playerAdapter = BattingOrderAdapter(players = adapterDataList, this).apply {
        setHasStableIds(true)
    }
    private val itemTouchedCallback = AttackItemTouchCallback(playerAdapter)
    private val itemTouchedHelper = ItemTouchHelper(itemTouchedCallback).apply {
        playerAdapter.itemTouchHelper = this
    }
    private val viewModel by viewModels<LineupViewModel>(
        ownerProducer = { requireParentFragment() }
    )
    private var dividerItemDecoration: ItemDecoratorAttackRecycler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lineupValue = SharedPreferencesUtils.getStringSetting(
            requireContext(),
            R.string.key_lineup_style,
            getString(R.string.lineup_style_default_value)
        )
        val lineupTypeface = LineupTypeface.getByValue(lineupValue)
        playerAdapter.lineupTypeface = lineupTypeface
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binder = FragmentListBatterBinding.inflate(inflater, container, false)
        this.binder = binder

        val linearLayoutManager = LinearLayoutManager(activity)
        val isEditable = viewModel.editable

        binder.recyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = playerAdapter
            setHasFixedSize(true)
        }
        binder.header.setIsEditable(isEditable)

        if (isEditable) {
            itemTouchedHelper.attachToRecyclerView(binder.recyclerView)
        }

        viewModel.observeLineup()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach {
                val batterSize = TeamStrategy.getStrategyById(it.strategy).batterSize
                val extraHitters = it.extraHitters
                dividerItemDecoration?.let { previous ->
                    binder.recyclerView.removeItemDecoration(previous)
                }
                val newDecoration = ItemDecoratorAttackRecycler(
                    context,
                    linearLayoutManager.orientation,
                    batterSize,
                    extraHitters
                )
                dividerItemDecoration = newDecoration
                binder.recyclerView.addItemDecoration(newDecoration)
                playerAdapter.notifyDataSetChanged()
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observeLineupMode()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach {
                playerAdapter.lineupMode = it
                playerAdapter.notifyDataSetChanged()
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observeLineupTypeface(requireContext())
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach {
                playerAdapter.lineupTypeface = it
                playerAdapter.notifyDataSetChanged()
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observeBatters()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach {
                adapterDataList.clear()
                adapterDataList.addAll(it)
                playerAdapter.notifyDataSetChanged()
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.observeHelpEvent()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { show ->
                if (show) {
                    launch(Completable.timer(200, TimeUnit.MILLISECONDS), {
                        linearLayoutManager.findViewByPosition(0)?.let {
                            FeatureViewFactory.apply(
                                it.findViewById<ImageView>(R.id.reorderImage),
                                activity as AppCompatActivity,
                                getString(R.string.reorder_batter_title),
                                getString(R.string.reorder_batter_description),
                                object : TapTargetView.Listener() {}
                            )
                        }
                    })
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        return binder.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binder = null
        dividerItemDecoration = null
    }

    override fun onBattersChanged(batters: List<BatterState>) {
        launch(viewModel.onBattersChanged(batters), { /* Nothing to do */ }, { Timber.e(it) })
    }
}
