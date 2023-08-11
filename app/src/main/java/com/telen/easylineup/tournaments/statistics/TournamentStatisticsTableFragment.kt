package com.telen.easylineup.tournaments.statistics

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.telen.easylineup.BaseFragment
import com.telen.easylineup.R
import com.telen.easylineup.databinding.FragmentTournamentStatisticsTableBinding
import com.telen.easylineup.domain.Constants
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.domain.model.Tournament
import io.github.kaygenzo.androidtable.api.StyleConfiguration
import io.github.kaygenzo.androidtable.api.TableConfiguration

class TournamentStatisticsTableFragment : BaseFragment("TournamentStatisticsTableFragment"),
    AdapterView.OnItemSelectedListener {

    private val viewModel by viewModels<TournamentStatisticsViewModel>()
    private var binding: FragmentTournamentStatisticsTableBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getSerializable(Constants.EXTRA_TOURNAMENT)?.let { tournament ->
            viewModel.tournament = tournament as Tournament
        }
        arguments?.getInt(Constants.EXTRA_TEAM_TYPE)?.let { teamType ->
            viewModel.teamType = TeamType.getTypeById(teamType)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentTournamentStatisticsTableBinding.inflate(inflater, container, false).apply {
            binding = this
            val headersStyle = StyleConfiguration(
                cellTextSize = R.dimen.app_max_text_size_middle,
                cellTextTypeface = Typeface.DEFAULT_BOLD,
                cellDefaultBackgroundColor = R.color.tile_player_most_used_header_color,
                cellDefaultTextColor = R.color.black
            )

            with(tableMultiScroll) {
                setLeftHeaderStyle(headersStyle)
                setTopHeaderStyle(headersStyle)

                setTableConfiguration(
                    TableConfiguration(
                        cellHeight = R.dimen.table_multiple_scroll_cell_height,
                        cellWidth = R.dimen.table_multiple_scroll_cell_width,
                        topHeaderHeight = R.dimen.table_multiple_scroll_top_header_height,
                        leftHeaderWidth = R.dimen.table_multiple_scroll_left_header_width
                    )
                )

                viewModel.leftHeadersData.observe(viewLifecycleOwner, Observer {
                    setLeftHeaderData(it)
                })

                viewModel.topHeadersData.observe(viewLifecycleOwner, Observer {
                    setTopHeaderData(it)
                })

                viewModel.mainTableData.observe(viewLifecycleOwner, Observer {
                    setMainData(it)
                })

                viewModel.columnHighlights.observe(viewLifecycleOwner, Observer {
                    setColumnHighlights(it)
                })

                viewModel.topLeftCell.observe(viewLifecycleOwner, Observer { strategies ->
                    activity?.let {
                        val strategyAdapter: ArrayAdapter<String> =
                            ArrayAdapter(it, R.layout.item_team_strategy, strategies.toTypedArray())
                        val spinner = Spinner(it).apply {
                            adapter = strategyAdapter
                            setSelection(viewModel.strategy.id, false)
                            onItemSelectedListener = this@TournamentStatisticsTableFragment
                        }
                        setTopLeftCellCustomView(spinner)
                    }
                })
            }

            viewModel.getPlayersPositionForTournament()
        }.root
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