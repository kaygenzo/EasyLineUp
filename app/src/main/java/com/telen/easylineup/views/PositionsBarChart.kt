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
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.utils.getPositionShortNames
import kotlinx.android.synthetic.main.view_bar_chart.view.*


class PositionsBarChart : ConstraintLayout {

    // positions reference to display in xAxis
    private var strategy: TeamStrategy = TeamStrategy.STANDARD
    private var teamType: Int? = null

    private var data: MutableMap<FieldPosition, Int> = mutableMapOf()

    // names from arrays.xml
    private var mPositions: Array<String>? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.view_bar_chart, this)

        val chart: BarChart = playerPositionsChart
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
        chart.setExtraOffsets(0f, 0f, 0f, 0f)

        chart.animateXY(1000, 1000, Easing.EaseInOutQuad)

        val xAxis = chart.xAxis
        xAxis.textSize = resources.getDimension(R.dimen.player_bar_chart_x_axis_size)
        xAxis.textColor = Color.BLACK
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.axisLineColor = Color.BLACK

        val yAxis = chart.getAxis(YAxis.AxisDependency.LEFT)
        yAxis.setLabelCount(5, true)
        yAxis.setDrawLabels(false)
        yAxis.setDrawGridLines(false)
        yAxis.axisMinimum = 0f
        yAxis.axisLineColor = Color.BLACK

        val yRightAxis = chart.getAxis(YAxis.AxisDependency.RIGHT)
        yRightAxis.setDrawLabels(false)
        yRightAxis.setDrawGridLines(false)
    }

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

    private fun refreshXAxis() {
        teamType?.let {
            val xAxis = playerPositionsChart.xAxis
            mPositions = getPositionShortNames(context, it)
            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    if (index < strategy.positions.size) {
                        val position = strategy.positions[index]
                        return mPositions?.get(position.id) ?: "N/A"
                    } else
                        return "N/A"
                }
            }
        }
    }

    private fun refreshChart() {

        val positionsRef = strategy.positions
        if (teamType == null || data.isEmpty()) {
            return
        }

        val chart = playerPositionsChart

        val xAxis = chart.xAxis
        xAxis.labelCount = positionsRef.size

        refreshXAxis()

        val entries: MutableList<BarEntry> = mutableListOf()

        val count = data.map { it.value }.sum()

        val yAxis = chart.getAxis(YAxis.AxisDependency.LEFT)
        yAxis.axisMaximum = count.toFloat()

        var index = 0f
        positionsRef.forEach {
            val value = data[it]
            if (value != null) {
                entries.add(BarEntry(index, value.toFloat()))
            } else {
                entries.add(BarEntry(index, 0f))
            }
            index++
        }

        val set1 = BarDataSet(entries, "")
        set1.color = Color.rgb(200, 201, 163)
        set1.axisDependency = YAxis.AxisDependency.LEFT

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
}