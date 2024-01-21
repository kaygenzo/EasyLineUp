/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import com.telen.easylineup.databinding.ViewMultipleStateDefenseIconBinding
import com.telen.easylineup.utils.toLetters
import timber.log.Timber

enum class StateDefense {
    LOADING, ADD_PLAYER, DP_DH, PLAYER, NONE, EMPTY
}

class MultipleStateDefenseIconButton : ConstraintLayout {
    private val binding =
        ViewMultipleStateDefenseIconBinding.inflate(LayoutInflater.from(context), this, true)
    private var state = StateDefense.LOADING

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun setOnClickListener(listener: OnClickListener?) {
        super.setOnClickListener(listener)
        binding.fab.setOnClickListener(listener)
    }

    fun getState() = state

    fun setState(state: StateDefense) {
        this.state = state
        binding.fab.scaleType = ImageView.ScaleType.CENTER
        when (state) {
            StateDefense.LOADING -> {
                binding.designatedPlayerLabel.visibility = View.GONE
                binding.fab.setImageResource(0)
                binding.imagePlayerContainer.visibility = View.GONE
                binding.fab.visibility = View.VISIBLE
                binding.progressIcon.visibility = View.VISIBLE
            }

            StateDefense.ADD_PLAYER -> {
                binding.designatedPlayerLabel.visibility = View.GONE
                binding.fab.visibility = View.VISIBLE
                binding.fab.setImageResource(R.drawable.ic_add_white_24dp)
                binding.imagePlayerContainer.visibility = View.GONE
                binding.progressIcon.visibility = View.GONE
            }

            StateDefense.DP_DH -> {
                binding.designatedPlayerLabel.visibility = View.VISIBLE
                binding.fab.visibility = View.VISIBLE
                binding.fab.setImageResource(0)
                binding.imagePlayerContainer.visibility = View.GONE
                binding.progressIcon.visibility = View.GONE
            }

            StateDefense.PLAYER -> {
                binding.designatedPlayerLabel.visibility = View.GONE
                binding.fab.visibility = View.INVISIBLE
                binding.fab.setImageResource(0)
                binding.imagePlayerContainer.visibility = View.VISIBLE
                binding.progressIcon.visibility = View.GONE
            }

            StateDefense.NONE -> {
                binding.designatedPlayerLabel.visibility = View.GONE
                binding.fab.visibility = View.INVISIBLE
                binding.fab.setImageResource(0)
                binding.imagePlayerContainer.visibility = View.GONE
                binding.progressIcon.visibility = View.GONE
            }

            StateDefense.EMPTY -> {
                binding.designatedPlayerLabel.visibility = View.GONE
                binding.fab.visibility = View.VISIBLE
                binding.fab.isEnabled = false
                binding.fab.setImageResource(0)
                binding.imagePlayerContainer.visibility = View.GONE
                binding.progressIcon.visibility = View.GONE
            }
        }
    }

    fun setLabel(label: String) {
        binding.designatedPlayerLabel.text = label
    }

    fun setBorderColor(@ColorInt borderColor: Int) {
        if (borderColor == Color.RED) {
            binding.playerNameFallback.setBackgroundResource(R.drawable.circle_shape_letters_border_red)
        } else {
            binding.playerNameFallback.setBackgroundResource(R.drawable.circle_shape_letters_border_black)
        }
    }

    fun setPlayerImage(
        url: String?,
        fallbackName: String,
        size: Int,
        @ColorInt borderColor: Int = Color.BLACK,
        borderWidth: Float = 2f
    ) {
        val nameFallback = fallbackName.toLetters()
        binding.playerNameFallback.text = nameFallback
        binding.playerNameFallback.visibility = View.VISIBLE
        binding.playerImage.visibility = View.INVISIBLE

        setBorderColor(borderColor)

        url?.let {
            binding.playerImage.post {
                try {
                    Picasso.get().load(url)
                        .resize(size, size)
                        .centerCrop()
                        .transform(
                            RoundedTransformationBuilder()
                                .borderColor(borderColor)
                                .borderWidthDp(borderWidth)
                                .cornerRadiusDp(16f)
                                .oval(true)
                                .build()
                        )
                        .into(binding.playerImage, object : Callback {
                            override fun onSuccess() {
                                binding.playerImage.visibility = View.VISIBLE
                                binding.playerNameFallback.visibility = View.INVISIBLE
                            }

                            override fun onError(ex: Exception?) {
                                binding.playerImage.visibility = View.INVISIBLE
                                binding.playerNameFallback.visibility = View.VISIBLE
                            }
                        })
                } catch (e: Exception) {
                    Timber.e(e)
                    binding.playerNameFallback.visibility = View.VISIBLE
                    binding.playerImage.visibility = View.INVISIBLE
                }
            }
        }
    }
}
