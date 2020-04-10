package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import com.telen.easylineup.utils.StringUtils
import kotlinx.android.synthetic.main.player_icon_field.view.*
import timber.log.Timber

class PlayerFieldIcon: LinearLayout {

    constructor(context: Context?) : super(context) { init(context) }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) { init(context) }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(context) }

    fun init(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.player_icon_field, this)
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