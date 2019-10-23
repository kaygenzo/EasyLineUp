package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import kotlinx.android.synthetic.main.player_icon_field.view.*

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

    fun setPlayerImage(url: String?, size: Int) {
        Picasso.get().load(url)
                .resize(size, size)
                .centerCrop()
                .transform(RoundedTransformationBuilder()
                        .borderColor(Color.BLACK)
                        .borderWidthDp(2f)
                        .cornerRadiusDp(16f)
                        .oval(true)
                        .build())
                .placeholder(R.drawable.unknown_player)
                .error(R.drawable.unknown_player)
                .into(playerImage)
    }

    fun setPlayerImage(url: String?) {
        Picasso.get().load(url)
                .fit()
                .transform(RoundedTransformationBuilder()
                        .borderColor(Color.BLACK)
                        .borderWidthDp(2f)
                        .cornerRadiusDp(16f)
                        .oval(true)
                        .build())
                .placeholder(R.drawable.unknown_player)
                .error(R.drawable.unknown_player)
                .into(playerImage)
    }

    fun setPlayerImage(@DrawableRes resId: Int, size: Int) {
        Picasso.get().load(resId)
                .resize(size, size)
                .centerCrop()
                .transform(RoundedTransformationBuilder()
                        .borderColor(Color.BLACK)
                        .borderWidthDp(2f)
                        .cornerRadiusDp(16f)
                        .oval(true)
                        .build())
                .placeholder(R.drawable.unknown_player)
                .error(R.drawable.unknown_player)
                .into(playerImage)
    }
}