package com.telen.easylineup.tournaments.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.telen.easylineup.R
import com.telen.easylineup.databinding.ItemAdapterLineupBinding
import com.telen.easylineup.domain.model.Lineup
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import java.text.SimpleDateFormat
import java.util.Locale

class LineupsAdapter(
    private val lineups: List<Lineup>,
    private val itemClickedListener: OnItemClickedListener?,
    var teamType: TeamType = TeamType.BASEBALL
) : RecyclerView.Adapter<LineupsAdapter.LineupsViewHolder>() {

    data class LineupsViewHolder(val view: ItemAdapterLineupBinding) :
        RecyclerView.ViewHolder(view.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineupsViewHolder {
        val binding =
            ItemAdapterLineupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LineupsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return lineups.size
    }

    override fun onBindViewHolder(holder: LineupsViewHolder, position: Int) {
        val lineup = lineups[position]
        with(holder.view) {
            lineupName.text = lineup.name
            val date = lineup.eventTimeInMillis.takeIf { it > 0L } ?: lineup.createdTimeInMillis
            val builder = StringBuilder(SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(date))
            lineupDate.text = builder.toString()

            val strategy = TeamStrategy.getStrategyById(lineup.strategy)
            lineupStrategy.apply {
                teamType.getStrategiesDisplayName(context)?.let { array ->
                    val strategies = teamType.getStrategies()
                    strategies.indexOf(strategy).takeIf { it != -1 }?.let {
                        visibility = View.VISIBLE
                        text = context.getString(R.string.lineup_list_strategy_type, array[it])
                    } ?: let {
                        visibility = View.GONE
                    }
                } ?: let {
                    visibility = View.GONE
                }
            }

            lineupExtraHitters.apply {
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

            rootView.setOnClickListener { itemClickedListener?.onLineupClicked(lineup) }
            editLineup.setOnClickListener {
                itemClickedListener?.onEditLineupClicked(lineup)
            }
        }
    }
}