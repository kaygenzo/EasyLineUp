package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import com.telen.easylineup.repository.model.FieldPosition
import com.telen.easylineup.R


class PositionsRadarChart: RadarChart {
    constructor(context: Context?) : super(context) {init(context)}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {init(context)}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {init(context)}

    private fun init(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.view_radar_chart, this)

        val chart: RadarChart = this
        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it

        chart.description.isEnabled = false
        chart.legend.form = Legend.LegendForm.NONE

        chart.webLineWidth = 2f
        chart.webLineWidthInner = 2f
        chart.webColor = Color.rgb(42, 52, 136)
        chart.webColorInner = Color.rgb(42, 52, 136)
        chart.webAlpha = 100

        chart.animateXY(1000, 1000, Easing.EaseInOutQuad)

        val xAxis = chart.xAxis
        xAxis.valueFormatter = object : ValueFormatter() {

            private val mPositions = context?.resources?.getStringArray(R.array.field_positions_list)

            override fun getFormattedValue(value: Float): String {
                return if(mPositions!=null)
                    mPositions[value.toInt()]
                else
                    ""
            }
        }
        xAxis.textSize = 15f
        xAxis.textColor = Color.BLACK
        xAxis.setLabelCount(9, true)
        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = 8f

        val yAxis = chart.yAxis
        yAxis.setLabelCount(5, true)
        yAxis.textSize = 9f
        yAxis.axisMinimum = 0f
        yAxis.axisMaximum = 120f
        yAxis.setDrawLabels(false)
    }

    fun setData(positionsMap: Map<FieldPosition, Int>) {

        val chart = this

        val entries: MutableList<RadarEntry> = mutableListOf()

        val offsetMin = 20f
        var count = 0
        positionsMap.forEach {
            count += it.value
        }

        FieldPosition.values().forEach {
            val value = positionsMap[it]
            if(value!=null) {
                entries.add(it.ordinal, RadarEntry(offsetMin + (value.toFloat()/count.toFloat())*100))
            }
            else {
                entries.add(it.ordinal, RadarEntry(offsetMin))
            }
        }

        val set1 = RadarDataSet(entries,"")
        set1.color = Color.rgb(103, 110, 129)
        set1.fillColor = Color.rgb(0, 168, 49)
        set1.setDrawFilled(true)
        set1.fillAlpha = 180
        set1.lineWidth = 2f
        set1.isDrawHighlightCircleEnabled = true
        set1.setDrawHighlightIndicators(false)

        val sets: MutableList<IRadarDataSet> = mutableListOf()
        sets.add(set1)

        val data = RadarData(sets)
        data.setValueTextSize(8f)
        data.setDrawValues(false)
        data.setValueTextColor(Color.BLUE)

        chart.setData(data)
        chart.setExtraOffsets(10f, 10f, 10f, -200f)
        chart.invalidate()
    }
}