package com.telen.easylineup.views

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.utils.drawn
import com.telen.easylineup.utils.ready
import kotlinx.android.synthetic.main.field_view.view.*
import kotlin.math.min
import kotlin.math.roundToInt

const val ICON_SIZE_SCALE = 0.12f

abstract class DefenseView: ConstraintLayout {

    private var containerSize: Float? = 0f
    protected val positionMarkers: MutableMap<FieldPosition, MultipleStateDefenseIconButton> = mutableMapOf()
    private var strategy: TeamStrategy = TeamStrategy.STANDARD

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    protected open fun onFieldPositionClicked(position: FieldPosition) {
        // to implement if needed
    }

    fun initField(strategy: TeamStrategy) {

        this.strategy = strategy
        positionMarkers.clear()

        getContainerSize {
            val iconSize = (it * ICON_SIZE_SCALE).roundToInt()
            strategy.positions.forEach { position ->
                val view = MultipleStateDefenseIconButton(context).apply {
                    layoutParams = LayoutParams(iconSize, iconSize)
                    setState(StateDefense.LOADING)
                    setOnClickListener { view ->
                        onFieldPositionClicked(position)
                    }
                }
                positionMarkers[position] = view
                val coordinates = FieldPosition.getPositionCoordinates(position, strategy)
                addPlayerOnFieldWithPercentage(it, view, coordinates.x, coordinates.y)
            }

            baseballField.ready {
                val image = when(strategy) {
                    TeamStrategy.B5_DEFAULT -> {
                        VectorDrawableCompat.create(resources, R.drawable.baseball5_field, null)
                    }
                    else -> {
                        VectorDrawableCompat.create(resources, R.drawable.baseball_field, null)
                    }
                }
                baseballField.setImageDrawable(image)
            }
        }
    }

    fun clear() {
        //cleanPlayerIcons()
    }

    protected fun addPlayerOnFieldWithPercentage(containerSize: Float, view: View, x: Float, y: Float) {
        val positionX = ((x * containerSize) / 100f)
        val positionY = ((y * containerSize) / 100f)
        addPlayerOnFieldWithCoordinate(view, containerSize, positionX, positionY)
    }

    protected fun addPlayerOnFieldWithCoordinate(view: View, parentWidth: Float, x: Float, y: Float) {
        if(fieldFrameLayout.findViewWithTag<MultipleStateDefenseIconButton>(view.tag)!=null)
            fieldFrameLayout.removeView(view)

        view.visibility = View.INVISIBLE

        val iconSize = (parentWidth * ICON_SIZE_SCALE).roundToInt()

        view.drawn {
            val imageWidth = view.width.toFloat()
            val imageHeight = view.height.toFloat()

            checkBounds(parentWidth, x, y, imageWidth, imageHeight) { correctedX: Float, correctedY: Float ->
                val positionX = correctedX - imageWidth / 2
                val positionY = correctedY - imageHeight / 2
                //Timber.d("containerSize=$parentWidth x=$x y=$y correctedX=$correctedX correctedY=$correctedY positionX=$positionX positionY=$positionY")

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

                (view as? MultipleStateDefenseIconButton)?.takeIf { it.getState() == StateDefense.ADD_PLAYER }?.let {
                    val shake = AnimationUtils.loadAnimation(context, R.anim.shake_effect)
                    view.animation = shake
                }
            }
        }

        fieldFrameLayout.addView(view)
    }

    private fun cleanPlayerIcons() {
        if(fieldFrameLayout.childCount > 1) {
            for (i in fieldFrameLayout.childCount-1 downTo 0) {
                val view = fieldFrameLayout.getChildAt(i)
                if(view is MultipleStateDefenseIconButton || view is SmallBaseballImageView) {
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

    private fun checkBounds(containerSize: Float, x: Float, y: Float, imageWidth: Float, imageHeight: Float, callback: (x: Float,y: Float) -> Unit) {

        var positionX: Float = x
        var positionY: Float = y

        if(positionX + imageWidth/2 > containerSize)
            positionX = containerSize - imageWidth/2
        if(positionX - imageWidth/2 < 0)
            positionX = imageWidth/2

        if(positionY - imageHeight/2 < 0)
            positionY = imageHeight/2
        if(positionY + imageHeight/2 > containerSize)
            positionY = containerSize - imageHeight/2

        callback(positionX, positionY)
    }

    protected fun getContainerSize(result: (Float) -> Unit) {
        containerSize?.takeIf { it > 0f }?.let {
            result(it)
        } ?: let {
            fieldFrameLayout.drawn {
                val size = min(fieldFrameLayout.width, fieldFrameLayout.height).toFloat()
                this.containerSize = size
                result(size)
            }
        }
    }
}