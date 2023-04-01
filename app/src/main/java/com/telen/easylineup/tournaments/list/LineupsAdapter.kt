package com.telen.easylineup.tournaments.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import java.text.SimpleDateFormat
import java.util.*

class LineupsAdapter(
    private val lineups: List<Lineup>,
    private val itemClickedListener: OnItemClickedListener?,
    var teamType: TeamType = TeamType.BASEBALL
) : RecyclerView.Adapter<LineupsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineupsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_adapter_lineup, parent, false)
        return LineupsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return lineups.size
    }

    override fun onBindViewHolder(holder: LineupsViewHolder, position: Int) {
        val lineup = lineups[position]
        holder.lineupName.text = lineup.name
        val date = lineup.eventTimeInMillis.takeIf { it > 0L } ?: lineup.createdTimeInMillis
        val builder = StringBuilder(SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(date))
        holder.lineupDate.text = builder.toString()

        val strategy = TeamStrategy.getStrategyById(lineup.strategy)
        when (teamType) {
            TeamType.SOFTBALL -> {
                holder.lineupStrategy.apply {
                    visibility = View.VISIBLE
                    val array = context.resources.getStringArray(R.array.softball_strategy_array)
                    val strategyName =
                        strategy.id.takeIf { it < array.count() }?.let { array[it] } ?: array[0]
                    text = context.getString(R.string.lineup_list_strategy_type, strategyName)
                }
            }
            else -> holder.lineupStrategy.visibility = View.GONE
        }

        holder.lineupExtraHitters.apply {
            lineup.extraHitters.takeIf { it > 0 }?.let {
                visibility = View.VISIBLE
                text = when (it) {
                    in 0..4 -> {
                        context.getString(R.string.lineup_list_extra_hitters, it.toString())
                    }
                    else -> {
                        context.getString(
                            R.string.lineup_list_extra_hitters,
                            context.getString(R.string.generic_infinite)
                        )
                    }
                }
            } ?: run {
                visibility = View.GONE
            }
        }

        holder.rootView.setOnClickListener { itemClickedListener?.onLineupClicked(lineup) }
        holder.lineupEditButton.setOnClickListener {
            itemClickedListener?.onEditLineupClicked(lineup)
        }
    }
}