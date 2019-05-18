package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import com.telen.easylineup.R
import kotlinx.android.synthetic.main.view_radar_chart.view.*


class PositionsRadarChart: ConstraintLayout {
    constructor(context: Context?) : super(context) {init(context)}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {init(context)}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {init(context)}

    private fun init(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.view_radar_chart, this)

        val chart: RadarChart = playerPositionsChart
        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it

        chart.description.isEnabled = false
        chart.legend.form = Legend.LegendForm.NONE

        chart.webLineWidth = 2f
        chart.webLineWidthInner = 2f
        chart.webColor = Color.rgb(42, 52, 136)
        chart.webColorInner = Color.rgb(42, 52, 136)
        chart.webAlpha = 100


        setData()

        chart.animateXY(1400, 1400, Easing.EaseInOutQuad)

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
        yAxis.axisMaximum = 100f
        yAxis.setDrawLabels(false)
    }

    private fun setData() {

        val chart = playerPositionsChart

        val entries1: MutableList<RadarEntry> = mutableListOf()

        entries1.add(RadarEntry(10f))
        entries1.add(RadarEntry(30f))
        entries1.add(RadarEntry(80f))
        entries1.add(RadarEntry(60f))
        entries1.add(RadarEntry(20f))
        entries1.add(RadarEntry(70f))
        entries1.add(RadarEntry(40f))
        entries1.add(RadarEntry(80f))
        entries1.add(RadarEntry(50f))

        val set1 = RadarDataSet(entries1,"")
        set1.color = Color.rgb(103, 110, 129)
        set1.fillColor = Color.rgb(0, 168, 49)
        set1.setDrawFilled(true)
        set1.fillAlpha = 180
        set1.lineWidth = 2f
        set1.isDrawHighlightCircleEnabled = true
        set1.setDrawHighlightIndicators(false)

        val sets: MutableList<IRadarDataSet> = ArrayList()
        sets.add(set1)

        val data = RadarData(sets)
        data.setValueTextSize(8f)
        data.setDrawValues(false)
        data.setValueTextColor(Color.BLUE)

        chart.setData(data)
        chart.invalidate()
    }
}