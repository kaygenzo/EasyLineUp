package com.telen.easylineup.lineup.statistics

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.telen.easylineup.R
import com.telen.easylineup.repository.model.Constants
import com.telen.easylineup.repository.model.Tournament
import com.telen.library.widget.tablemultiscroll.views.StyleConfiguration
import com.telen.library.widget.tablemultiscroll.views.TableConfiguration
import kotlinx.android.synthetic.main.fragment_tournament_statistics_table.view.*

class TournamentStatisticsTableFragment: Fragment() {

    private lateinit var viewModel: TournamentStatisticsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this)[TournamentStatisticsViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tournament_statistics_table, container, false)

        val headersStyle = StyleConfiguration(
                cellTextSize = R.dimen.text_size_middle,
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

        viewModel.leftHeadersData.observe(this, Observer {
            view.tableMultiScroll.setLeftHeaderData(it)
        })

        viewModel.topHeadersData.observe(this, Observer {
            view.tableMultiScroll.setTopHeaderData(it)
        })

        viewModel.mainTableData.observe(this, Observer {
            view.tableMultiScroll.setMainData(it)
        })

        viewModel.columnHighlights.observe(this, Observer {
            view.tableMultiScroll.setColumnHighlights(it)
        })

        arguments?.getSerializable(Constants.EXTRA_TOURNAMENT)?.let { tournament ->
            context?.let { context ->
                viewModel.getPlayersPositionForTournament(context, tournament as Tournament)
            }
        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clear()
    }
}