/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.telen.easylineup.R
import com.telen.easylineup.databinding.ViewBarChartBinding
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.utils.getPositionShortNames

class PositionsBarChart : ConstraintLayout {
    val binding = ViewBarChartBinding.inflate(LayoutInflater.from(context), this, true)

    // positions reference to display in xAxis
    private var strategy: TeamStrategy = TeamStrategy.STANDARD
    private var teamType: Int? = null
    private var data: MutableMap<FieldPosition, Int> = mutableMapOf()

    // names from arrays.xml
    private var positions: Array<String>? = null

    init {
        val chart: BarChart = binding.playerPositionsChart
        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it

        chart.description.isEnabled = false
        chart.legend.form = Legend.LegendForm.NONE
        chart.setDrawBarShadow(false)
        chart.setDrawValueAboveBar(true)
        chart.setPinchZoom(false)
        chart.isDoubleTapToZoomEnabled = false
        chart.setScaleEnabled(false)
        chart.setDrawGridBackground(false)
        chart.setFitBars(true)
        chart.setExtraOffsets(EXTRA_OFFSET, EXTRA_OFFSET, EXTRA_OFFSET, EXTRA_OFFSET)

        chart.animateXY(ANIMATION_DURATION, ANIMATION_DURATION, Easing.EaseInOutQuad)

        val horizontalAxis = chart.xAxis.apply {
            textSize = resources.getDimension(R.dimen.player_bar_chart_x_axis_size)
            textColor = Color.BLACK
            position = XAxis.XAxisPosition.BOTTOM
        }
        horizontalAxis.setDrawGridLines(false)
        horizontalAxis.axisLineColor = Color.BLACK

        val verticalAxis = chart.getAxis(YAxis.AxisDependency.LEFT)
        verticalAxis.setLabelCount(LABEL_COUNT, true)
        verticalAxis.setDrawLabels(false)
        verticalAxis.setDrawGridLines(false)
        verticalAxis.axisMinimum = 0f
        verticalAxis.axisLineColor = Color.BLACK

        val verticalRightAxis = chart.getAxis(YAxis.AxisDependency.RIGHT)
        verticalRightAxis.setDrawLabels(false)
        verticalRightAxis.setDrawGridLines(false)
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setData(data: Map<FieldPosition, Int>) {
        this.data.putAll(data)
        refreshChart()
    }

    fun setStrategy(strategy: TeamStrategy) {
        this.strategy = strategy
        refreshChart()
    }

    fun setTeamType(teamType: Int) {
        this.teamType = teamType
        refreshChart()
    }

    private fun refreshHorizontalaxis() {
        teamType?.let {
            val horizontalAxis = binding.playerPositionsChart.xAxis
            positions = getPositionShortNames(context, it)
            horizontalAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    if (index < strategy.positions.size) {
                        val position = strategy.positions[index]
                        return positions?.get(position.id) ?: "N/A"
                    } else {
                        return "N/A"
                    }
                }
            }
        }
    }

    private fun refreshChart() {
        val positionsRef = strategy.positions
        if (teamType == null || data.isEmpty()) {
            return
        }

        val chart = binding.playerPositionsChart

        chart.xAxis.apply {
            labelCount = positionsRef.size
        }

        refreshHorizontalaxis()

        val entries: MutableList<BarEntry> = mutableListOf()

        val count = data.map { it.value }.sum()

        chart.getAxis(YAxis.AxisDependency.LEFT).apply {
            axisMaximum = count.toFloat()
        }

        var index = 0f
        positionsRef.forEach {
            val value = data[it]
            value?.let {
                entries.add(BarEntry(index, value.toFloat()))
            } ?: entries.add(BarEntry(index, 0f))
            index++
        }

        val set1 = BarDataSet(entries, "").apply {
            color = barDatasetColor
            axisDependency = YAxis.AxisDependency.LEFT
        }

        val sets: MutableList<IBarDataSet> = mutableListOf()
        sets.add(set1)

        val data = BarData(sets)
        data.setValueTextSize(resources.getDimension(R.dimen.player_bar_chart_x_axis_size))
        data.setValueTypeface(Typeface.DEFAULT)
        data.setDrawValues(true)
        data.setValueTextColor(Color.BLACK)
        data.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value.toInt() > 0) {
                    value.toInt().toString()
                } else {
                    ""
                }
            }
        })

        chart.data = data
        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    companion object {
        private const val ANIMATION_DURATION = 1_000
        private const val EXTRA_OFFSET = 0f
        private const val LABEL_COUNT = 5
        private val barDatasetColor = Color.rgb(200, 201, 163)
    }
}
