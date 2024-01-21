/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CompoundButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.databinding.ItemRosterEntryBinding

class RosterEntryView : ConstraintLayout, TextWatcher {
    private var watcherListener: TextWatcher? = null
    private val binding = ItemRosterEntryBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        binding.rosterShirtNumberTextField.addTextChangedListener(this)
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setPlayerName(name: String) {
        binding.rosterPlayerName.text = name
    }

    fun setNumber(number: Int) {
        binding.rosterShirtNumberTextField.setText(number.toString())
    }

    fun setTextListener(watcher: TextWatcher?) {
        watcherListener = watcher
    }

    override fun afterTextChanged(text: Editable?) {
        watcherListener?.afterTextChanged(text)
    }

    override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) {
        watcherListener?.beforeTextChanged(text, start, count, after)
    }

    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
        watcherListener?.onTextChanged(text, start, before, count)
    }

    fun setSelectedStateListener(listener: CompoundButton.OnCheckedChangeListener?) {
        binding.selectedState.setOnCheckedChangeListener(listener)
    }

    fun setSelectedState(state: Boolean) {
        binding.selectedState.isChecked = state
    }
}
