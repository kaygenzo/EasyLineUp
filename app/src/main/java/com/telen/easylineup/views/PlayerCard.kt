package com.telen.easylineup.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import kotlinx.android.synthetic.main.item_player_list.view.*
import timber.log.Timber

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

        playerImage.post {
            // I put this test here because untilReady is too long to complete so the adapter inflate too late the image.
            // this cause the images to be at the wrong place in the recycler
            if(playerImage.width > 0 && playerImage.height > 0) {
                try {
                    Picasso.get()
                            .load(path)
                            .resize(playerImage.width, playerImage.height)
                            .centerCrop()
                            .error(R.drawable.ic_unknown_field_player)
                            .placeholder(R.drawable.ic_unknown_field_player)
                            .into(playerImage)
                } catch (e: IllegalArgumentException) {
                    Timber.e(e)
                }
            }
            else {
                Picasso.get().load(R.drawable.ic_unknown_field_player)
                        .error(R.drawable.ic_unknown_field_player)
                        .placeholder(R.drawable.ic_unknown_field_player)
                        .into(playerImage)
            }
        }
    }

    fun setName(name: String) {
        playerName.text = name.trim()
    }

    fun setShirtNumber(shirtNumber: Int) {
        playerShirtNumber.text = shirtNumber.toString()
    }

    override fun setOnClickListener(l: OnClickListener?) {
        playerCardRootView.setOnClickListener(l)
    }

}