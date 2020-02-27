package com.telen.easylineup.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import java.lang.StringBuilder

class SwashedStyledTextView: AppCompatTextView {

    constructor(context: Context?) : super(context) {initView(context)}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {initView(context)}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {initView(context)}

    private fun initView(context: Context?) {

    }

    override fun setTypeface(tf: Typeface?, style: Int) {
        super.setTypeface(Typeface.createFromAsset(context.assets, "KrinkesDecorPERSONAL.ttf"))
    }

//    override fun setText(text: CharSequence?, type: BufferType?) {
//        val nameBuilder = StringBuilder()
//        var textToSet = text
//        if(text != null) {
//            nameBuilder.append(textToSet).append('9')
////            text.split(" ").forEach {
////                builder.append(it)
////                builder.append("9 ")
////            }
//            textToSet = nameBuilder.toString().trim()
//        }
//        super.setText(textToSet, type)
//    }
}