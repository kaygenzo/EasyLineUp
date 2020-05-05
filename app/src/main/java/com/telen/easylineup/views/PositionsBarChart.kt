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
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.FieldPosition
import kotlinx.android.synthetic.main.view_bar_chart.view.*


class PositionsBarChart: ConstraintLayout {
    constructor(context: Context) : super(context) {init(context)}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {init(context)}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {init(context)}

    fun setTeamType(teamType: Int) {
        val xAxis = playerPositionsChart.xAxis
        xAxis.valueFormatter = object : ValueFormatter() {

            private val mPositions = FieldPosition.getPositionShortNames(context, teamType)

            override fun getFormattedValue(value: Float): String {
                return mPositions[value.toInt()]
            }
        }
        playerPositionsChart.invalidate()
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
        xAxis.valueFormatter = object : ValueFormatter() {

            private val mPositions = FieldPosition.getPositionShortNames(context, 0)

            override fun getFormattedValue(value: Float): String {
                return mPositions[value.toInt()]
            }
        }
        xAxis.textSize = 10f
        xAxis.textColor = Color.BLACK
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.labelCount = FieldPosition.values().size
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

    fun setData(positionsMap: Map<FieldPosition, Int>) {

        val chart = playerPositionsChart

        val entries: MutableList<BarEntry> = mutableListOf()

        var count = 0
        positionsMap.forEach {
            count += it.value
        }

        val yAxis = chart.getAxis(YAxis.AxisDependency.LEFT)
        yAxis.axisMaximum = count.toFloat()

        FieldPosition.values().forEach {
            val value = positionsMap[it]
            if(value!=null) {
                entries.add(BarEntry(it.position.toFloat(), value.toFloat()))
            }
            else {
                entries.add(it.position, BarEntry(it.ordinal.toFloat(), 0f))
            }
        }

        val set1 = BarDataSet(entries,"")
        set1.color = Color.rgb(200, 201, 163)
        set1.axisDependency = YAxis.AxisDependency.LEFT

        val sets: MutableList<IBarDataSet> = mutableListOf()
        sets.add(set1)

        val data = BarData(sets)
        data.setValueTextSize(10f)
        data.setValueTypeface(Typeface.DEFAULT_BOLD)
        data.setDrawValues(true)
        data.setValueTextColor(Color.BLACK)
        data.setValueFormatter(object : ValueFormatter(){
            override fun getFormattedValue(value: Float): String {
                return if(value.toInt() > 0) {
                    value.toInt().toString()
                } else {
                    ""
                }
            }
        })

        chart.data = data
        chart.invalidate()
    }
}