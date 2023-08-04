package com.telen.easylineup.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CompoundButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.R
import kotlinx.android.synthetic.main.item_roster_entry.view.*

class RosterEntryView : ConstraintLayout, TextWatcher {

    private var watcherListener: TextWatcher? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.item_roster_entry, this)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        rosterShirtNumberTextField.addTextChangedListener(this)
    }

    fun setPlayerName(name: String) {
        rosterPlayerName.text = name
    }

    fun setNumber(number: Int) {
        rosterShirtNumberTextField.setText(number.toString())
    }

    fun setTextListener(watcher: TextWatcher?) {
        watcherListener = watcher
    }

    override fun afterTextChanged(p0: Editable?) {
        watcherListener?.afterTextChanged(p0)
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        watcherListener?.beforeTextChanged(p0, p1, p2, p3)
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        watcherListener?.onTextChanged(p0, p1, p2, p3)
    }

    fun setSelectedStateListener(listener: CompoundButton.OnCheckedChangeListener?) {
        selectedState.setOnCheckedChangeListener(listener)
    }

    fun setSelectedState(state: Boolean) {
        selectedState.isChecked = state
    }
}