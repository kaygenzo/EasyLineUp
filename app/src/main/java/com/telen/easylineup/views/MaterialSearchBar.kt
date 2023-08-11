package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.inputmethod.EditorInfo
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.textfield.TextInputLayout
import com.telen.easylineup.R
import com.telen.easylineup.databinding.ViewMaterialSearchBarBinding

interface OnSearchBarListener {
    fun onSearchConfirmed(text: String?)
}

class MaterialSearchBar : ConstraintLayout {

    @DrawableRes
    private var endIcon: Int = 0
    var onSearchBarListener: OnSearchBarListener? = null
    private val binding =
        ViewMaterialSearchBarBinding.inflate(LayoutInflater.from(context), this, true)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        val clickListener = OnClickListener {
            binding.searchBarLayout.apply {
                when (endIcon) {
                    R.drawable.ic_baseline_search_24 -> {
                        val text = this.editText?.text.toString()
                        onSearchConfirmed(text)
                        // if the text is blank, we just hide the trailing icon
                        if (text.isNotBlank()) {
                            endIcon = R.drawable.ic_close_24dp
                            setEndIconDrawable(endIcon)
                        } else {
                            endIcon = 0
                            endIconMode = TextInputLayout.END_ICON_NONE
                        }
                    }

                    R.drawable.ic_close_24dp -> {
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

        binding.searchBarInput.setOnFocusChangeListener { _, focused ->
            if (focused) {
                binding.searchBarLayout.run {
                    endIconMode = TextInputLayout.END_ICON_CUSTOM
                    endIcon = R.drawable.ic_baseline_search_24
                    setEndIconDrawable(endIcon)
                    setEndIconOnClickListener(clickListener)
                }
            }
        }

        binding.searchBarInput.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE,
                EditorInfo.IME_ACTION_SEARCH -> {
                    clickListener.onClick(binding.searchBarInput)
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
        binding.searchBarInput.clearFocus()
    }
}