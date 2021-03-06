package com.telen.easylineup.tournaments.statistics

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import com.telen.library.widget.tablemultiscroll.views.StyleConfiguration
import com.telen.library.widget.tablemultiscroll.views.TableConfiguration
import kotlinx.android.synthetic.main.fragment_tournament_statistics_table.view.*

class TournamentStatisticsTableFragment: BaseFragment("TournamentStatisticsTableFragment"), AdapterView.OnItemSelectedListener {

    private lateinit var viewModel: TournamentStatisticsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this)[TournamentStatisticsViewModel::class.java]
        arguments?.getSerializable(Constants.EXTRA_TOURNAMENT)?.let { tournament ->
            viewModel.tournament = tournament as Tournament
        }
        arguments?.getInt(Constants.EXTRA_TEAM_TYPE)?.let { teamType ->
            viewModel.teamType = TeamType.getTypeById(teamType)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tournament_statistics_table, container, false)

        val headersStyle = StyleConfiguration(
                cellTextSize = R.dimen.app_max_text_size_middle,
                cellTextTypeface = Typeface.DEFAULT_BOLD,
                cellDefaultBackgroundColor = R.color.tile_player_most_used_header_color,
                cellDefaultTextColor = R.color.black
        )

        view.tableMultiScroll.setLeftHeaderStyle(headersStyle)
        view.tableMultiScroll.setTopHeaderStyle(headersStyle)

        view.tableMultiScroll.setTableConfiguration(TableConfiguration(
                cellHeight = R.dimen.table_multiple_scroll_cell_height,
                cellWidth = R.dimen.table_multiple_scroll_cell_width,
                topHeaderHeight = R.dimen.table_multiple_scroll_top_header_height,
                leftHeaderWidth = R.dimen.table_multiple_scroll_left_header_width
        ))

        viewModel.leftHeadersData.observe(viewLifecycleOwner, Observer {
            view.tableMultiScroll.setLeftHeaderData(it)
        })

        viewModel.topHeadersData.observe(viewLifecycleOwner, Observer {
            view.tableMultiScroll.setTopHeaderData(it)
        })

        viewModel.mainTableData.observe(viewLifecycleOwner, Observer {
            view.tableMultiScroll.setMainData(it)
        })

        viewModel.columnHighlights.observe(viewLifecycleOwner, Observer {
            view.tableMultiScroll.setColumnHighlights(it)
        })

        viewModel.topLeftCell.observe(viewLifecycleOwner, Observer { strategies ->
            activity?.let {
                val strategyAdapter: ArrayAdapter<String> = ArrayAdapter(it, R.layout.item_team_strategy, strategies.toTypedArray())
                val spinner = Spinner(it).apply {
                    adapter = strategyAdapter
                    setSelection(viewModel.strategy.id, false)
                    onItemSelectedListener = this@TournamentStatisticsTableFragment
                }
                view.tableMultiScroll.setTopLeftCellCustomView(spinner)
            }
        })

        viewModel.getPlayersPositionForTournament()

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clear()
    }

    override fun onNothingSelected(adapter: AdapterView<*>?) {
    }

    override fun onItemSelected(adapter: AdapterView<*>?, view: View?, position: Int, id: Long) {
        viewModel.onStrategyChosen(position)
    }
}