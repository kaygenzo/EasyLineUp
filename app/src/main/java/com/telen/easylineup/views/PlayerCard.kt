package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import kotlinx.android.synthetic.main.item_player_list.view.*

class PlayerCard: CardView {

    constructor(context: Context) : super(context) { init(context) }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) { init(context) }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(context) }

    fun init(context: Context?) {
        context?.let {
            LayoutInflater.from(context).inflate(R.layout.item_player_list, this)
            setBackgroundColor(ContextCompat.getColor(context,android.R.color.transparent))
        }
    }

    fun setImage(path: String?) {
        val sizePixel = context.resources.getDimensionPixelSize(R.dimen.player_item_image_size)

        Picasso.get()
                .load(path)
                .resize(sizePixel, sizePixel)
                .centerCrop()
                .error(R.drawable.ic_unknown_field_player)
                .placeholder(R.drawable.ic_unknown_field_player)
                .into(playerImage)
    }

    fun setName(name: String) {
        playerName.text = name
    }

    fun setShirtNumber(shirtNumber: Int) {
        playerShirtNumber.text = shirtNumber.toString()
    }

    override fun setOnClickListener(l: OnClickListener?) {
        playerCardRootView.setOnClickListener(l)
    }

}