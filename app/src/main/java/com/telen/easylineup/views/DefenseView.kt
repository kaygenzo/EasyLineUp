/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.PorterDuff
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.Player
import com.telen.easylineup.domain.model.Sex
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.getPositionPercentage
import com.telen.easylineup.utils.drawn
import com.telen.easylineup.utils.getColor
import com.telen.easylineup.utils.ready
import kotlin.math.min
import kotlin.math.roundToInt

const val ICON_SIZE_SCALE = 0.12f
const val INDICATOR_RADIUS_FACTOR = 6f

abstract class DefenseView : ConstraintLayout {
    private var containerSize: Float? = 0f
    protected val positionMarkers: MutableMap<FieldPosition, MultipleStateDefenseIconButton> =
        mutableMapOf()
    private var strategy: TeamStrategy = TeamStrategy.STANDARD
    private var canvas: Canvas? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    abstract fun getFieldCanvas(): ImageView
    abstract fun getFieldImage(): ImageView
    abstract fun getContainerView(): ViewGroup

    protected open fun onFieldPositionClicked(position: FieldPosition) {
        // to implement if needed
    }

    fun initField(strategy: TeamStrategy) {
        this.strategy = strategy
        positionMarkers.clear()
        cleanPlayerIcons()

        getContainerSize {
            Bitmap.createBitmap(it.toInt(), it.toInt(), Bitmap.Config.ARGB_8888).let { bitmap ->
                getFieldCanvas().setImageBitmap(bitmap)
                canvas = Canvas(bitmap).apply { drawColor(Color.TRANSPARENT) }
            }

            val iconSize = (it * ICON_SIZE_SCALE).roundToInt()
            strategy.positions.forEach { position ->
                val view = MultipleStateDefenseIconButton(context).apply {
                    layoutParams = LayoutParams(iconSize, iconSize)
                    setState(StateDefense.LOADING)
                    setOnClickListener { _ ->
                        onFieldPositionClicked(position)
                    }
                }
                positionMarkers[position] = view
                val coordinates = position.getPositionPercentage(strategy)
                addPlayerOnFieldWithPercentage(it, view, coordinates.x, coordinates.y)
            }

            getFieldImage().ready {
                val image = when (strategy) {
                    TeamStrategy.B5_DEFAULT -> VectorDrawableCompat.create(
                        resources,
                        R.drawable.baseball5_field,
                        null
                    )

                    else -> VectorDrawableCompat.create(resources, R.drawable.baseball_field, null)
                }
                getFieldImage().setImageDrawable(image)
            }
        }
    }

    fun clear() {
        // cleanPlayerIcons()
    }

    protected fun addPlayerOnFieldWithPercentage(
        containerSize: Float,
        view: View,
        x: Float,
        y: Float
    ) {
        val point = percentageToCoordinates(containerSize, PointF(x, y))
        addPlayerOnFieldWithCoordinate(view, containerSize, point.x, point.y)
    }

    private fun percentageToCoordinates(containerSize: Float, point: PointF): PointF {
        val horizontalPosition = ((point.x * containerSize) / 100f)
        val verticalPosition = ((point.y * containerSize) / 100f)
        return PointF(horizontalPosition, verticalPosition)
    }

    private fun addPlayerOnFieldWithCoordinate(
        view: View,
        parentWidth: Float,
        x: Float,
        y: Float
    ) {
        getContainerView().findViewWithTag<MultipleStateDefenseIconButton>(view.tag)?.let {
            getContainerView().removeView(view)
        }

        view.visibility = View.INVISIBLE

        val iconSize = (parentWidth * ICON_SIZE_SCALE).roundToInt()

        view.drawn {
            val imageWidth = view.width.toFloat()
            val imageHeight = view.height.toFloat()

            checkBounds(
                parentWidth,
                x,
                y,
                imageWidth,
                imageHeight
            ) { horizontalCorrectedPos: Float, verticalCorrectedPos: Float ->
                val horizontalPosition = horizontalCorrectedPos - imageWidth / 2
                val verticalPosition = verticalCorrectedPos - imageHeight / 2

                val layoutParamCustom = FrameLayout.LayoutParams(iconSize, iconSize).run {
                    leftMargin = horizontalPosition.toInt()
                    topMargin = verticalPosition.toInt()
                    this
                }

                view.run {
                    layoutParams = layoutParamCustom
                    visibility = View.VISIBLE
                    invalidate()
                }

                (view as? MultipleStateDefenseIconButton)?.takeIf { it.getState() == StateDefense.ADD_PLAYER }
                    ?.let {
                        val shake = AnimationUtils.loadAnimation(context, R.anim.shake_effect)
                        view.animation = shake
                    }
            }
        }

        getContainerView().addView(view)
    }

    private fun cleanPlayerIcons() {
        if (getContainerView().childCount > 1) {
            for (i in getContainerView().childCount - 1 downTo 0) {
                val view = getContainerView().getChildAt(i)
                if (view is MultipleStateDefenseIconButton) {
                    view.clearAnimation()
                    view.setOnDragListener(null)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        view.cancelDragAndDrop()
                    }
                    getContainerView().removeView(getContainerView().getChildAt(i))
                }
            }
        }
    }

    private fun checkBounds(
        containerSize: Float,
        x: Float,
        y: Float,
        imageWidth: Float,
        imageHeight: Float,
        callback: (x: Float, y: Float) -> Unit
    ) {
        var axisHorizontalPosition: Float = x
        var axisVerticalPosition: Float = y

        if (axisHorizontalPosition + imageWidth / 2 > containerSize) {
            axisHorizontalPosition = containerSize - imageWidth / 2
        }
        if (axisHorizontalPosition - imageWidth / 2 < 0) {
            axisHorizontalPosition = imageWidth / 2
        }

        if (axisVerticalPosition - imageHeight / 2 < 0) {
            axisVerticalPosition = imageHeight / 2
        }
        if (axisVerticalPosition + imageHeight / 2 > containerSize) {
            axisVerticalPosition = containerSize - imageHeight / 2
        }

        callback(axisHorizontalPosition, axisVerticalPosition)
    }

    protected fun getContainerSize(result: (Float) -> Unit) {
        containerSize?.takeIf { it > 0f }?.let {
            result(it)
        } ?: let {
            getContainerView().drawn {
                val size =
                    min(getContainerView().width, getContainerView().height).toFloat()
                this.containerSize = size
                result(size)
            }
        }
    }

    protected fun setSexIndicator(player: Player, position: FieldPosition) {
        when (val sex = Sex.getById(player.sex)) {
            Sex.MALE, Sex.FEMALE -> drawIndicatorOnPositions(position, sex.getColor(context))

            else -> {
                // set a color for unknown sex
            }
        }
    }

    protected fun cleanSexIndicators() {
        this.canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    }

    private fun drawIndicatorOnPositions(position: FieldPosition, color: Int) {
        getContainerSize {
            val iconSize = (it * ICON_SIZE_SCALE).roundToInt()
            val indicatorRadius = iconSize / INDICATOR_RADIUS_FACTOR
            val percentage = position.getPositionPercentage(strategy)
            val pos = percentageToCoordinates(it, percentage)
            val size = iconSize.toFloat()
            checkBounds(it, pos.x, pos.y, size, size) { x: Float, y: Float ->
                val cx = x + iconSize / 2
                val cy = y - iconSize / 2
                this.canvas?.drawCircle(cx, cy, indicatorRadius, Paint().apply {
                    this.color = color
                    style = Paint.Style.FILL
                })
            }
        }
    }
}
