package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import kotlinx.android.synthetic.main.player_icon_field.view.*
import java.util.regex.Pattern

class PlayerFieldIcon: LinearLayout {

    constructor(context: Context?) : super(context) { init(context) }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) { init(context) }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(context) }

    fun init(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.player_icon_field, this)
    }

    fun setShirtNumber(shirtNumber: Int) {
        playerShirtNumber.text = shirtNumber.toString()
    }

    fun setShirtNumber(shirtNumber: String) {
        playerShirtNumber.text = shirtNumber
    }

    fun setPlayerImage(url: String?, fallbackName: String, size: Int) {
        setPlayerImage(url, fallbackName, size, Color.BLACK, 2f)
    }

    fun setPlayerImage(url: String?, fallbackName: String, size: Int, borderColor: Int, borderWidth: Float) {
        val nameFallback = nameToLetters(fallbackName)
        playerNameFallback.text = nameFallback
        playerNameFallback.visibility = View.VISIBLE
        playerImage.visibility = View.INVISIBLE

        url?.let {
                    Picasso.get().load(url)
                .resize(size, size)
                .centerCrop()
                .transform(RoundedTransformationBuilder()
                        .borderColor(borderColor)
                        .borderWidthDp(borderWidth)
                        .cornerRadiusDp(16f)
                        .oval(true)
                        .build())
                .into(playerImage, object: Callback {

                    override fun onSuccess() {
                        playerImage.visibility = View.VISIBLE
                        playerNameFallback.visibility = View.INVISIBLE
                    }

                    override fun onError(e: Exception?) {
                        playerImage.visibility = View.INVISIBLE
                        playerNameFallback.visibility = View.VISIBLE
                    }

                })
        }
    }

    private fun nameToLetters(name: String): String {
        val parts = name.split(Pattern.compile("[\\s-]+"), 2).map { it[0] }
        val builder = StringBuilder()
        parts.forEach {
            builder.append(it)
        }
        return builder.toString()
    }
}