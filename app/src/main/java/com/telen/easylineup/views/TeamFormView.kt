/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import com.telen.easylineup.databinding.TeamTypeCarouselItemBinding
import com.telen.easylineup.databinding.ViewCreateTeamBinding
import com.telen.easylineup.domain.model.TeamType
import com.telen.easylineup.utils.drawn
import com.telen.easylineup.utils.ready
import timber.log.Timber
import kotlin.math.abs

interface TeamFormListener {
    fun onNameChanged(name: String)
    fun onImageChanged(imageUri: Uri?)
    fun onTeamTypeChanged(teamType: TeamType)
    fun onImagePickerRequested()
}

/**
 * @property type
 * @property title
 * @property ballResourceId
 * @property compatBallResourceId
 * @property representationId
 */
data class TeamTypeCardItem(
    val type: Int,
    @StringRes val title: Int,
    @DrawableRes val ballResourceId: Int,
    @DrawableRes val compatBallResourceId: Int,
    @DrawableRes val representationId: Int
)

class TeamFormView : ConstraintLayout, TextWatcher {
    private var listener: TeamFormListener? = null
    private var imageUri: Uri? = null
    val binding: ViewCreateTeamBinding =
        ViewCreateTeamBinding.inflate(LayoutInflater.from(context), this, true)
    private val teamTypeList: MutableList<TeamTypeCardItem> = mutableListOf()
    private val teamTypeAdapter = CardPagerAdapter(teamTypeList)

    init {
        Picasso.get().load(R.drawable.ic_unknown_team)
            .fit()
            .transform(
                RoundedTransformationBuilder()
                    .borderColor(Color.BLACK)
                    .borderWidthDp(2f)
                    .cornerRadiusDp(16f)
                    .oval(true)
                    .build()
            )
            .placeholder(R.drawable.ic_unknown_team)
            .error(R.drawable.ic_unknown_team)
            .into(binding.teamImage)

        binding.teamImage.setOnClickListener {
            listener?.onImagePickerRequested()
        }

        binding.teamNameInput.addTextChangedListener(this)

        binding.teamImageAction.run {
            setImageResource(R.drawable.add_image)
            setOnClickListener(null)
        }

        binding.teamTypeCarousel.apply {
            this.offscreenPageLimit = 1
            this.adapter = teamTypeAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    Timber.d("onPageSelected position=$position")
                    val teamType = TeamType.getTypeById(teamTypeList[position].type)
                    listener?.onTeamTypeChanged(teamType)
                }
            })
            setPageTransformer(TeamTypePageTransformer())
        }
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @SuppressLint("NotifyDataSetChanged")
    fun setTeamTypes(types: List<TeamTypeCardItem>) {
        this.teamTypeList.clear()
        this.teamTypeList.addAll(types)
        teamTypeAdapter.notifyDataSetChanged()
    }

    fun setTeamType(type: TeamType) {
        binding.teamTypeCarousel.ready {
            binding.teamTypeCarousel.currentItem = type.position
        }
    }

    fun setListener(listener: TeamFormListener) {
        this.listener = listener
    }

    fun onImageUriReceived(imageUri: Uri) {
        setImage(imageUri)
        this.listener?.onImageChanged(imageUri)
    }

    fun getName(): String {
        return binding.teamNameInput.text.toString()
    }

    fun getImageUri(): Uri? {
        return imageUri
    }

    fun setName(name: String) {
        binding.teamNameInput.setText(name)
    }

    fun setImage(uri: Uri) {
        imageUri = uri
        binding.teamImage.ready {
            try {
                Picasso.get().load(uri)
                    .resize(binding.teamImage.width, binding.teamImage.height)
                    .centerCrop()
                    .transform(
                        RoundedTransformationBuilder()
                            .borderColor(Color.BLACK)
                            .borderWidthDp(2f)
                            .cornerRadiusDp(16f)
                            .oval(true)
                            .build()
                    )
                    .placeholder(R.drawable.ic_unknown_team)
                    .error(R.drawable.ic_unknown_team)
                    .into(binding.teamImage, object : Callback {
                        override fun onSuccess() {
                            Timber.e("Successfully loaded image")
                        }

                        override fun onError(ex: Exception?) {
                            Timber.e(ex)
                        }
                    })
            } catch (e: IllegalArgumentException) {
                Timber.e(e)
            }
        }

        imageUri?.let {
            binding.teamImageAction.run {
                setImageResource(R.drawable.remove_image)
                setOnClickListener {
                    imageUri = null
                    setTeamImage(R.drawable.ic_unknown_team)
                    setImageResource(R.drawable.add_image)
                    setOnClickListener(null)
                    listener?.onImageChanged(null)
                }
            }
        }
    }

    private fun setTeamImage(@DrawableRes resId: Int) {
        Picasso.get().load(resId)
            .placeholder(R.drawable.ic_unknown_team)
            .error(R.drawable.ic_unknown_team)
            .into(binding.teamImage)
    }

    override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun afterTextChanged(text: Editable?) {}

    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
        listener?.onNameChanged(text.toString())
    }
}

private class CardPagerAdapter(private val data: MutableList<TeamTypeCardItem>) :
    RecyclerView.Adapter<CardPagerAdapter.CardViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view =
            TeamTypeCarouselItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(data[position])
    }

    data class CardViewHolder(private val view: TeamTypeCarouselItemBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bind(item: TeamTypeCardItem) {
            view.teamTypeRepresentation.drawn {
                view.teamTypeRepresentation.apply {
                    when (item.type) {
                        TeamType.BASEBALL.id -> Picasso.get().load(TeamType.BASEBALL.sportResId)
                            .resize(width, height)
                            .centerCrop()
                            .into(view.teamTypeRepresentation)

                        TeamType.SOFTBALL.id -> Picasso.get().load(TeamType.SOFTBALL.sportResId)
                            .resize(width, height)
                            .centerCrop()
                            .into(view.teamTypeRepresentation)

                        TeamType.BASEBALL_5.id -> Picasso.get().load(TeamType.BASEBALL_5.sportResId)
                            .resize(width, height)
                            .centerCrop()
                            .into(view.teamTypeRepresentation)
                    }
                }
            }
        }
    }
}

private class TeamTypePageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(view: View, position: Float) {
        view.translationX = 0f
        view.scaleY = 1 - (0.25f * abs(position))
    }
}
