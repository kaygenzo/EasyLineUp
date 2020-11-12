package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import com.telen.easylineup.utils.StringUtils
import kotlinx.android.synthetic.main.view_multiple_state_defense_icon.view.*
import timber.log.Timber

enum class StateDefense {
    LOADING, EMPTY, DP_DH, PLAYER, NONE
}

class MultipleStateDefenseIconButton: ConstraintLayout {

    private var state = StateDefense.LOADING

    constructor(context: Context) : super(context) { init(context) }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) { init(context) }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(context) }

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.view_multiple_state_defense_icon, this)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener(l)
        fab.setOnClickListener(l)
    }

    fun getState() = state

    fun setState(state: StateDefense) {
        this.state = state
        fab.scaleType = ImageView.ScaleType.CENTER
        when(state) {
            StateDefense.LOADING -> {
                designatedPlayerLabel.visibility = View.GONE
                fab.setImageResource(0)
                imagePlayerContainer.visibility = View.GONE
                fab.visibility = View.VISIBLE
                progressIcon.visibility = View.VISIBLE
            }
            StateDefense.EMPTY -> {
                designatedPlayerLabel.visibility = View.GONE
                fab.visibility = View.VISIBLE
                fab.setImageResource(R.drawable.ic_add_white_24dp)
                imagePlayerContainer.visibility = View.GONE
                progressIcon.visibility = View.GONE
            }
            StateDefense.DP_DH -> {
                designatedPlayerLabel.visibility = View.VISIBLE
                fab.visibility = View.VISIBLE
                fab.setImageResource(0)
                imagePlayerContainer.visibility = View.GONE
                progressIcon.visibility = View.GONE
            }
            StateDefense.PLAYER -> {
                designatedPlayerLabel.visibility = View.GONE
                fab.visibility = View.GONE
                fab.setImageResource(0)
                imagePlayerContainer.visibility = View.VISIBLE
                progressIcon.visibility = View.GONE
            }
            StateDefense.NONE -> {
                designatedPlayerLabel.visibility = View.GONE
                fab.visibility = View.GONE
                fab.setImageResource(0)
                imagePlayerContainer.visibility = View.GONE
                progressIcon.visibility = View.GONE
            }
        }
    }

    fun setLabel(label: String) {
        designatedPlayerLabel.text = label
    }

    fun setPlayerImage(url: String?, fallbackName: String, size: Int) {
        setPlayerImage(url, fallbackName, size, Color.BLACK, 2f)
    }

    fun setBorderColor(@ColorInt borderColor: Int) {
        if(borderColor == Color.RED) {
            playerNameFallback.setBackgroundResource(R.drawable.circle_shape_letters_border_red)
        }
        else {
            playerNameFallback.setBackgroundResource(R.drawable.circle_shape_letters_border_black)
        }
    }

    fun setPlayerImage(@DrawableRes resId: Int) {
        playerImage.setImageResource(resId)
    }

    fun setPlayerImage(url: String?, fallbackName: String, size: Int, @ColorInt borderColor: Int, borderWidth: Float) {
        val nameFallback = StringUtils().nameToLetters(fallbackName)
        playerNameFallback.text = nameFallback
        playerNameFallback.visibility = View.VISIBLE
        playerImage.visibility = View.INVISIBLE

        setBorderColor(borderColor)

        url?.let {
            playerImage.post {
                try {
                    Picasso.get().load(url)
                            .resize(size, size)
                            .centerCrop()
                            .transform(RoundedTransformationBuilder()
                                    .borderColor(borderColor)
                                    .borderWidthDp(borderWidth)
                                    .cornerRadiusDp(16f)
                                    .oval(true)
                                    .build())
                            .into(playerImage, object : Callback {

                                override fun onSuccess() {
                                    playerImage.visibility = View.VISIBLE
                                    playerNameFallback.visibility = View.INVISIBLE
                                }

                                override fun onError(e: Exception?) {
                                    playerImage.visibility = View.INVISIBLE
                                    playerNameFallback.visibility = View.VISIBLE
                                }

                            })
                } catch (e: Exception) {
                    Timber.e(e)
                    playerNameFallback.visibility = View.VISIBLE
                    playerImage.visibility = View.INVISIBLE
                }
            }
        }
    }
}