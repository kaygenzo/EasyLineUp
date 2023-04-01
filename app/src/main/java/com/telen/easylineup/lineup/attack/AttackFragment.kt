package com.telen.easylineup.lineup.attack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.getkeepsafe.taptargetview.TapTargetView
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.databinding.FragmentListBatterBinding
import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.lineup.LineupViewModel
import com.telen.easylineup.utils.FeatureViewFactory
import com.telen.easylineup.views.ItemDecoratorAttackRecycler
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import java.util.concurrent.TimeUnit

class AttackFragment : BaseFragment("AttackFragment") {

    private lateinit var viewModel: LineupViewModel
    private lateinit var playerAdapter: BattingOrderAdapter
    private lateinit var itemTouchedCallback: AttackItemTouchCallback
    private lateinit var itemTouchedHelper: ItemTouchHelper

    private val adapterDataList = mutableListOf<BatterState>()
    private var binder: FragmentListBatterBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parentFragment?.let { parent ->
            viewModel = ViewModelProviders.of(parent).get(LineupViewModel::class.java)
        }

        playerAdapter = BattingOrderAdapter(players = adapterDataList).apply {
            setHasStableIds(true)
        }
        itemTouchedCallback = AttackItemTouchCallback(playerAdapter)
        itemTouchedHelper = ItemTouchHelper(itemTouchedCallback)
        playerAdapter.itemTouchHelper = itemTouchedHelper
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

        viewModel.observeLineup().observe(viewLifecycleOwner) {
            val batterSize = TeamStrategy.getStrategyById(it.strategy).batterSize
            val extraHitters = it.extraHitters
            val dividerItemDecoration = ItemDecoratorAttackRecycler(
                context,
                linearLayoutManager.orientation,
                batterSize,
                extraHitters
            )
            binder.recyclerView.addItemDecoration(dividerItemDecoration)
            playerAdapter.notifyDataSetChanged()
        }

        viewModel.observeLineupMode().observe(viewLifecycleOwner) {
            playerAdapter.lineupMode = it
            playerAdapter.notifyDataSetChanged()
        }

        viewModel.observeLineupTypeface(requireContext()).observe(viewLifecycleOwner) {
            playerAdapter.lineupTypeface = it
            playerAdapter.notifyDataSetChanged()
        }

        viewModel.observeBatters().observe(viewLifecycleOwner) {
            adapterDataList.clear()
            adapterDataList.addAll(it)
            playerAdapter.notifyDataSetChanged()
        }

        viewModel.observeHelpEvent().observe(viewLifecycleOwner) { show ->
            if (show) {
                Completable.timer(200, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        linearLayoutManager.findViewByPosition(0)?.let {
                            FeatureViewFactory.apply(
                                it.findViewById<ImageView>(R.id.reorderImage),
                                activity as AppCompatActivity,
                                getString(R.string.reorder_batter_title),
                                getString(R.string.reorder_batter_description),
                                object : TapTargetView.Listener() {}
                            )
                        }
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