package com.telen.easylineup.views

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.R
import kotlinx.android.synthetic.main.view_card_create_player.view.*

class PlayerFormCardView: ConstraintLayout {

    constructor(context: Context?) : super(context) {initView(context)}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {initView(context)}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {initView(context)}

    fun setListener(listener: PlayerFormListener) {
        viewForm.setListener(listener)
    }

    private fun initView(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.view_card_create_player, this)
    }

//    class Builder {
//        private var name: String = ""
//        private var shirtNumber: Int = 0
//        private var licenseNumber: Long = 0
//
//        fun setName(name: String): Builder {
//            this.name = name
//            return this
//        }
//
//        fun setShirtNumber(shirtNumber: Int): Builder {
//            this.shirtNumber = shirtNumber
//            return this
//        }
//
//        fun setLicenseNumber(licenseNumber: Long): Builder {
//            this.licenseNumber = licenseNumber
//            return this
//        }
//
//        fun build(context: Context): PlayerFormCardView {
//            val view = PlayerFormCardView(context)
//            with(view) {
//                setName(name)
//                setShirtNumber(shirtNumber)
//                setLicenseNumber(licenseNumber)
//            }
//
//            return view
//        }
//    }

    fun setName(name: String) {
        viewForm.setName(name)
    }

    fun setShirtNumber(shirtNumber: Int) {
        viewForm.setShirtNumber(shirtNumber)
    }

    fun setLicenseNumber(licenseNumber: Long) {
        viewForm.setLicenseNumber(licenseNumber)
    }

    fun setImage(uri: Uri) {
        viewForm.setImage(uri)
    }
}