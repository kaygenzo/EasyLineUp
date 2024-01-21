/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.telen.easylineup.databinding.ViewCreateTournamentFormBinding
import java.text.DateFormat

class TournamentFormView : ConstraintLayout {
    private val binding =
        ViewCreateTournamentFormBinding.inflate(LayoutInflater.from(context), this, true)
    var callback: TournamentFormCallback? = null
        set(value) {
            field = value
            value?.onStartTimeChanged(startTime)
            value?.onEndTimeChanged(endTime)
        }
    var fragmentManager: FragmentManager? = null
    private var startTime = System.currentTimeMillis()
    private var endTime = System.currentTimeMillis()

    init {
        with(binding) {
            setDateDisplay(startTournamentValue, startTime)
            setDateDisplay(endTournamentValue, endTime)
            startTournamentValue.setOnClickListener {
                showDatePicker("createTournamentStartTimePicker", startTime) {
                    startTime = it
                    callback?.onStartTimeChanged(it)
                    setDateDisplay(startTournamentValue, it)
                }
            }

            endTournamentValue.setOnClickListener {
                showDatePicker("createTournamentEndTimePicker", endTime) {
                    endTime = it
                    callback?.onEndTimeChanged(it)
                    setDateDisplay(endTournamentValue, it)
                }
            }

            nameInput.addTextChangedListener {
                callback?.onNameChanged(it?.toString() ?: "")
            }

            tournamentAddressInput.addTextChangedListener {
                callback?.onAddressChanged(it?.toString() ?: "")
            }
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private fun showDatePicker(tag: String, initialTime: Long, value: (Long) -> Unit) {
        val datePicker = MaterialDatePicker.Builder
            .datePicker()
            .setSelection(initialTime)
            .build()
        datePicker.addOnPositiveButtonClickListener { value(it) }
        fragmentManager?.let { datePicker.show(it, tag) }
    }

    private fun setDateDisplay(view: TextView, time: Long) {
        view.text = DateFormat.getDateInstance(DateFormat.SHORT).format(time)
    }

    interface TournamentFormCallback {
        fun onStartTimeChanged(timeInMillis: Long)
        fun onEndTimeChanged(timeInMillis: Long)
        fun onNameChanged(name: String)
        fun onAddressChanged(address: String)
    }
}
