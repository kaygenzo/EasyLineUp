package com.telen.easylineup.views

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.telen.easylineup.R
import com.telen.easylineup.data.Player
import kotlinx.android.synthetic.main.baseball_field_with_players.view.*
import kotlinx.android.synthetic.main.field_view.view.*
import org.w3c.dom.Text
import kotlin.math.roundToInt

class BaseballFieldAndPlayersView: ConstraintLayout {

    constructor(context: Context?) : super(context) { init(context)}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) { init(context)}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init(context)}

    fun init(context: Context?) {
        LayoutInflater.from(context).inflate(R.layout.baseball_field_with_players, this)

        fieldFrameLayout.setOnDragListener { v, event ->
            when(event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    Log.d("BaseballFieldView", "action=ACTION_DRAG_STARTED")
                    true
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    //Log.d("BaseballFieldView", "action=ACTION_DRAG_ENTERED")
                    true
                }
                DragEvent.ACTION_DRAG_LOCATION -> {
                    //Log.d("BaseballFieldView", "action=ACTION_DRAG_LOCATION")
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    Log.d("BaseballFieldView", "action=ACTION_DRAG_EXITED")
                    true
                }
                DragEvent.ACTION_DROP -> {
                    Log.d("BaseballFieldView", "action=ACTION_DROP")
                    Log.d("BaseballFieldView", "Position: x=${event.x} y=${event.y}")

                    val item: ClipData.Item = event.clipData.getItemAt(0)
                    val tag = item.text

                    val player: PlayerFieldIcon? = baseballFieldAndPlayersRoot.findViewWithTag(tag)
                    if(player!=null) {
                        addPlayerOnField(player, event.x, event.y)
                    }
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    Log.d("BaseballFieldView", "action=ACTION_DRAG_ENDED")
                    true
                }
                else -> {
                    Log.d("BaseballFieldView", "action=UNKNOWN")
                    false
                }
            }
            true
        }
    }

    fun setListPlayerInContainer(players: List<Player>) {
        playersContainer.removeAllViews()

        val columnCount = 6
        val rowCount = ((players.size) / columnCount)+1
        val totalCells = rowCount * columnCount

        playersContainer.columnCount = columnCount
        playersContainer.rowCount = rowCount

        for(i in 0 until players.size) {
            var playerView = PlayerFieldIcon(context).run {
                //layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)

                val param = GridLayout.LayoutParams().apply {
                    rightMargin = 10
                    topMargin = 10
                    bottomMargin = 10
                    leftMargin = 10
                }

                layoutParams = param

                setPlayerIcon(R.drawable.pikachu)
                setShirtNumber(players[i].shirtNumber)
                //replace by license id which is unique
                tag = players[i].name + "_" + players[i].shirtNumber
                setPadding(20,20,20,20)
                setOnLongClickListener { view ->
                    val item = ClipData.Item(tag as? CharSequence)
                    val dragData = ClipData(tag as? CharSequence, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)
                    val shadowBuilder = DragShadowBuilder(view)
                    view.startDrag(dragData, shadowBuilder, null, 0)
                }
                this
            }
            playersContainer.addView(playerView)
        }
    }

//    private fun addPlayerOnField(view: PlayerFieldIcon, x: Float, y: Float, drawableId: Int) {
////        val imageWidth = view.width
////        val imageHeight = view.height
////
////        val imageView = ImageView(context).apply {
////            setImageResource(drawableId)
////
////            val layoutParamImageView = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
////            with(layoutParamImageView) {
////                leftMargin = x.roundToInt() - imageWidth / 2
////                topMargin = y.roundToInt() - imageHeight / 2
////            }
////            layoutParams = layoutParamImageView
////        }
////
////        fieldFrameLayout.addView(imageView)
////    }

    private fun addPlayerOnField(view: PlayerFieldIcon, x: Float, y: Float) {
        val imageWidth = view.width
        val imageHeight = view.height

        if(playersContainer.findViewWithTag<PlayerFieldIcon>(view.tag)!=null)
            playersContainer.removeView(view)
        if(fieldFrameLayout.findViewWithTag<PlayerFieldIcon>(view.tag)!=null)
            fieldFrameLayout.removeView(view)

        val layoutParamCustom = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).run {
            leftMargin = x.roundToInt() - imageWidth / 2
            topMargin = y.roundToInt() - imageHeight / 2
            this
        }

        view.run {
            layoutParams = layoutParamCustom
            invalidate()
        }

        fieldFrameLayout.addView(view, layoutParamCustom)
    }
}