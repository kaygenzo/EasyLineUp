package com.telen.easylineup.views

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.telen.easylineup.R
import com.telen.easylineup.utils.LoadingCallback
import kotlinx.android.synthetic.main.field_view.view.*
import kotlin.math.roundToInt

const val ICON_SIZE_SCALE = 0.12f

abstract class DefenseView: ConstraintLayout {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun clear() {
        cleanPlayerIcons()
    }

    protected fun addPlayerOnFieldWithPercentage(view: View, x: Float, y: Float, loadingCallback: LoadingCallback?) {
        fieldFrameLayout.post {
            val layoutHeight = fieldFrameLayout.height
            val layoutWidth = fieldFrameLayout.width

            val positionX = ((x * layoutWidth) / 100f)
            val positionY = ((y * layoutHeight) / 100f)

            addPlayerOnFieldWithCoordinate(view, layoutWidth, positionX, positionY, loadingCallback)
        }
    }

    protected fun addPlayerOnFieldWithCoordinate(view: View, parentWidth: Int, x: Float, y: Float,
                                                 loadingCallback: LoadingCallback?) {
        if(fieldFrameLayout.findViewWithTag<PlayerFieldIcon>(view.tag)!=null)
            fieldFrameLayout.removeView(view)

        view.visibility = View.INVISIBLE

        val iconSize = (parentWidth * ICON_SIZE_SCALE).roundToInt()

        view.post {
            val imageWidth = view.width.toFloat()
            val imageHeight = view.height.toFloat()

            checkBounds(x, y, imageWidth, imageHeight) { correctedX: Float, correctedY: Float ->

                val positionX = correctedX - imageWidth / 2
                val positionY = correctedY - imageHeight / 2

                val layoutParamCustom = FrameLayout.LayoutParams(iconSize, iconSize).run {
                    leftMargin = positionX.toInt()
                    topMargin = positionY.toInt()
                    this
                }

                view.run {
                    layoutParams = layoutParamCustom
                    visibility = View.VISIBLE
                    invalidate()
                }

                if(view is AddPlayerButton) {
                    val shake = AnimationUtils.loadAnimation(context, R.anim.shake_effect)
                    view.animation = shake
                }

                loadingCallback?.onFinishLoading()
            }
        }

        fieldFrameLayout.addView(view)
    }

    protected fun cleanPlayerIcons() {
        if(fieldFrameLayout.childCount > 1) {
            for (i in fieldFrameLayout.childCount-1 downTo 0) {
                val view = fieldFrameLayout.getChildAt(i)
                if(view is PlayerFieldIcon || view is AddPlayerButton
                        || view is AddDesignatedPlayerButton || view is TrashFieldButton
                        || view is SmallBaseballImageView) {
                    view.clearAnimation()
                    view.setOnDragListener(null)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        view.cancelDragAndDrop()
                    }
                    fieldFrameLayout.removeView(fieldFrameLayout.getChildAt(i))
                }
            }
        }
    }

    private fun checkBounds(x: Float, y: Float, imageWidth: Float, imageHeight: Float, callback: (x: Float,y: Float) -> Unit) {
        val containerWidth = fieldFrameLayout.width.toFloat()
        val containerHeight = fieldFrameLayout.height.toFloat()

        var positionX: Float = x
        var positionY: Float = y

        if(positionX + imageWidth/2 > containerWidth)
            positionX = containerWidth - imageWidth/2
        if(positionX - imageWidth/2 < 0)
            positionX = imageWidth/2

        if(positionY - imageHeight/2 < 0)
            positionY = imageHeight/2
        if(positionY + imageHeight/2 > containerHeight)
            positionY = containerHeight - imageHeight/2

        callback(positionX, positionY)
    }
}