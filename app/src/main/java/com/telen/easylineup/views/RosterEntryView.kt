package com.telen.easylineup.views

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.R
import kotlinx.android.synthetic.main.item_roster_entry.view.*

class RosterEntryView: ConstraintLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        LayoutInflater.from(context).inflate(R.layout.item_roster_entry, this)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    fun setPlayerName(name: String) {
        rosterPlayerName.text = name
    }

    fun setNumber(number: Int) {
        rosterShirtNumberTextField.setText(number.toString())
    }

    fun setTextListener(watcher: TextWatcher) {
        rosterShirtNumberTextField.addTextChangedListener(watcher)
    }
}