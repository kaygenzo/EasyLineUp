package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.textfield.TextInputLayout
import com.telen.easylineup.R
import com.telen.easylineup.utils.hideSoftKeyboard
import kotlinx.android.synthetic.main.view_material_search_bar.view.*

interface OnSearchBarListener {
    fun onSearchConfirmed(text: String?)
}

class MaterialSearchBar: ConstraintLayout {

    @DrawableRes private var endIcon: Int = 0
    var onSearchBarListener: OnSearchBarListener? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_material_search_bar, this)

        val clickListener = OnClickListener {
            searchBarLayout.apply {
                when (endIcon) {
                    R.drawable.ic_baseline_search_24 -> {
                        val text = this.editText?.text.toString()
                        onSearchConfirmed(text)
                        // if the text is blank, we just hide the trailing icon
                        if (text.isNotBlank()) {
                            endIcon = R.drawable.ic_close_white_24dp
                            setEndIconDrawable(endIcon)
                        } else {
                            endIcon = 0
                            endIconMode = TextInputLayout.END_ICON_NONE
                        }
                    }
                    R.drawable.ic_close_white_24dp -> {
                        this.editText?.text?.clear()
                        onSearchConfirmed("")
                        endIcon = 0
                        endIconMode = TextInputLayout.END_ICON_NONE
                    }
                    else -> {
                    }
                }
                clearFocus()
            }
        }

        searchBarInput.setOnFocusChangeListener { _, focused ->
            if(focused) {
                searchBarLayout.run {
                    endIconMode = TextInputLayout.END_ICON_CUSTOM
                    endIcon = R.drawable.ic_baseline_search_24
                    setEndIconDrawable(endIcon)
                    setEndIconOnClickListener(clickListener)
                }
            }
        }

        searchBarInput.setOnEditorActionListener { _, actionId, _ ->
            when(actionId) {
                EditorInfo.IME_ACTION_DONE,
                EditorInfo.IME_ACTION_SEARCH -> {
                    clickListener.onClick(searchBarInput)
                    true
                }
                else -> false
            }
        }
    }

    private fun onSearchConfirmed(text: String?) {
        onSearchBarListener?.onSearchConfirmed(text)
    }

    fun setIdle() {
        searchBarInput.clearFocus()
    }
}