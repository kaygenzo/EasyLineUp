package com.telen.easylineup.views

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.GridLayout
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.domain.model.Sex
import kotlinx.android.synthetic.main.view_create_player.view.*
import timber.log.Timber

interface PlayerFormListener {
    fun onSaveClicked(
        name: String?,
        shirtNumber: Int?,
        licenseNumber: Long?,
        imageUri: Uri?,
        positions: Int,
        pitching: Int,
        batting: Int,
        email: String?,
        phone: String?,
        sex: Int
    )

    fun onCancel()
    fun onImagePickerRequested()
}

class PlayerFormView : ConstraintLayout {

    private var listener: PlayerFormListener? = null
    private var imageUri: Uri? = null
    private var playerPositions = 0
    private val positionState: MutableMap<FieldPosition, Boolean> = mutableMapOf()

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context)
    }

    fun setListener(listener: PlayerFormListener) {
        this.listener = listener
    }

    private fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.view_create_player, this)

        playerImage.setImageResource(R.drawable.unknown_player)

        playerImage.setOnClickListener {
            listener?.onImagePickerRequested()
        }

        playerSave.setOnClickListener {
            val name = getName()
            val shirtNumber = getShirtNumber()
            val licenseNumber = getLicenseNumber()
            val positions = getPlayerPositions()
            val pitching = getPitchingSide()?.flag ?: 0
            val batting = getBattingSide()?.flag ?: 0
            val email = getEmail()
            val phone = getPhone()
            val sex = getSex()?.id ?: Sex.UNKNOWN.id

            listener?.onSaveClicked(
                name,
                shirtNumber,
                licenseNumber,
                imageUri,
                positions,
                pitching,
                batting,
                email,
                phone,
                sex
            )
        }

        cancel.setOnClickListener {
            listener?.onCancel()
        }

        favoritePositionsContainer.useDefaultMargins = true
        favoritePositionsContainer.alignmentMode = GridLayout.ALIGN_MARGINS

        setPositionsFilter(0)

        playerImageAction.run {
            setImageResource(R.drawable.add_image)
            setOnClickListener(null)
        }

        sexMale.setOnClickListener {
            if(sexFemale.isChecked) {
                setSex(Sex.MALE)
            }
        }

        sexFemale.setOnClickListener {
            if(sexMale.isChecked) {
                setSex(Sex.FEMALE)
            }
        }
    }

    fun onImageUriReceived(imageUri: Uri) {
        setImage(imageUri)
    }

    fun getName(): String {
        return playerNameInput.text.toString()
    }

    fun getShirtNumber(): Int? {
        return try {
            playerShirtNumberInput.text.toString().toInt()
        } catch (exception: NumberFormatException) {
            null
        }
    }

    fun getLicenseNumber(): Long? {
        return try {
            playerLicenseNumberInput.text.toString().toLong()
        } catch (exception: NumberFormatException) {
            null
        }
    }

    fun getImageUri(): Uri? {
        return imageUri
    }

    fun setName(name: String) {
        playerNameInput.setText(name)
    }

    fun setShirtNumber(shirtNumber: Int) {
        playerShirtNumberInput.setText(shirtNumber.toString())
    }

    fun setLicenseNumber(licenseNumber: Long) {
        playerLicenseNumberInput.setText(licenseNumber.toString())
    }

    fun setPlayerImage(@DrawableRes resId: Int) {
        Picasso.get().load(resId)
            .placeholder(R.drawable.unknown_player)
            .error(R.drawable.unknown_player)
            .into(playerImage)
    }

    fun setImage(uri: Uri) {
        imageUri = uri
        playerImage.post {
            try {
                Picasso.get().load(imageUri)
                    .resize(playerImage.width, playerImage.height)
                    .centerCrop()
                    .transform(
                        RoundedTransformationBuilder()
                            .borderColor(Color.BLACK)
                            .borderWidthDp(2f)
                            .cornerRadiusDp(16f)
                            .oval(true)
                            .build()
                    )
                    .placeholder(R.drawable.unknown_player)
                    .error(R.drawable.unknown_player)
                    .into(playerImage)
            } catch (e: IllegalArgumentException) {
                Timber.e(e)
            }
        }

        imageUri?.let {
            playerImageAction.run {
                setImageResource(R.drawable.remove_image)
                setOnClickListener {
                    imageUri = null
                    setPlayerImage(R.drawable.unknown_player)
                    setImageResource(R.drawable.add_image)
                    setOnClickListener(null)
                }
            }
        }
    }

    fun enableSaveButton() {
        playerSave.isEnabled = true
    }

    fun disableSaveButton() {
        playerSave.isEnabled = false
    }

    fun getPlayerPositions(): Int {
        return playerPositions
    }

    fun setPositionsFilter(positions: Int) {

        this.playerPositions = positions
        this.positionState.clear()

        val positionShortDescription = FieldPosition.getPositionShortNames(context, 0)

        favoritePositionsContainer.removeAllViews()

        FieldPosition.values()
            .filter { FieldPosition.isDefensePlayer(it.id) }
            .groupBy { it.mask }
            .map { it.value.first() }
            .forEach { position ->
                val isEnabled = (positions and position.mask) != 0
                positionState[position] = isEnabled

                val view = PlayerPositionFilterView(context)
                view.setText(positionShortDescription[position.ordinal])
                applyFilterOnView(view, isEnabled)

                view.setOnClickListener { _ ->

                    positionState[position]?.let { oldState ->
                        val newState = !oldState

                        if (newState) {
                            if (positionState.filterValues { it == true }.size >= 3) {
                                return@setOnClickListener
                            }
                        }

                        positionState[position] = newState
                        applyFilterOnView(view, newState)
                        playerPositions = if (newState) {
                            playerPositions or position.mask
                        } else {
                            playerPositions and position.mask.inv()
                        }
                    }
                }

                favoritePositionsContainer.addView(view)
            }
    }

    private fun applyFilterOnView(view: PlayerPositionFilterView, isEnabled: Boolean) {
        when (isEnabled) {
            true -> {
                view.setBackground(R.drawable.position_selected_background)
                view.setTextColor(R.color.white)
            }
            else -> {
                view.setBackground(R.drawable.position_unselected_background)
                view.setTextColor(R.color.tile_team_size_background_color)
            }
        }
    }

//    private fun getRoundedBitmap(bitmap: Bitmap): RoundedBitmapDrawable {
//        val min = Math.min(bitmap.width, bitmap.height)
//        val drawable = RoundedBitmapDrawableFactory.create(resources, Bitmap.createBitmap(bitmap, 0,0,min, min))
//        drawable.isCircular = true
//        return drawable
//    }

    fun displayInvalidName() {
        playerNameInputLayout.error = resources.getString(R.string.player_creation_error_name_empty)
        playerEmailInputLayout.error = null
        playerPhoneInputLayout.error = null
    }

    fun displayInvalidEmail() {
        playerNameInputLayout.error = null
        playerEmailInputLayout.error =
            resources.getString(R.string.player_creation_error_invalid_email_format)
        playerPhoneInputLayout.error = null
    }

    fun displayInvalidPhoneNumber() {
        playerNameInputLayout.error = null
        playerEmailInputLayout.error = null
        playerPhoneInputLayout.error =
            resources.getString(R.string.player_creation_error_invalid_phone_format)
    }

    fun getPitchingSide(): PlayerSide? {
        var pitching = 0
        if (pitchingSideLeft.isChecked)
            pitching = pitching or PlayerSide.LEFT.flag
        if (pitchingSideRight.isChecked)
            pitching = pitching or PlayerSide.RIGHT.flag
        return PlayerSide.getSideByValue(pitching)
    }

    fun getBattingSide(): PlayerSide? {
        var batting = 0
        if (battingSideLeft.isChecked)
            batting = batting or PlayerSide.LEFT.flag
        if (battingSideRight.isChecked)
            batting = batting or PlayerSide.RIGHT.flag
        return PlayerSide.getSideByValue(batting)
    }

    fun getSex(): Sex? {
        return if (sexMale.isChecked) {
            Sex.MALE
        } else if (sexFemale.isChecked) {
            Sex.FEMALE
        } else {
            null
        }
    }

    fun setPitchingSide(side: PlayerSide?) {
        when (side) {
            PlayerSide.LEFT -> {
                pitchingSideLeft.isChecked = true
                pitchingSideRight.isChecked = false
            }
            PlayerSide.RIGHT -> {
                pitchingSideLeft.isChecked = false
                pitchingSideRight.isChecked = true
            }
            PlayerSide.BOTH -> {
                pitchingSideLeft.isChecked = true
                pitchingSideRight.isChecked = true
            }
            null -> {
                pitchingSideLeft.isChecked = false
                pitchingSideRight.isChecked = false
            }
        }
    }

    fun setSex(sex: Sex?) {
        when (sex) {
            Sex.MALE -> {
                sexMale.isChecked = true
                sexFemale.isChecked = false
            }
            Sex.FEMALE -> {
                sexMale.isChecked = false
                sexFemale.isChecked = true
            }
            else -> {
                sexMale.isChecked = false
                sexFemale.isChecked = false
            }
        }
    }

    fun setBattingSide(side: PlayerSide?) {
        when (side) {
            PlayerSide.LEFT -> {
                battingSideLeft.isChecked = true
                battingSideRight.isChecked = false
            }
            PlayerSide.RIGHT -> {
                battingSideLeft.isChecked = false
                battingSideRight.isChecked = true
            }
            PlayerSide.BOTH -> {
                battingSideLeft.isChecked = true
                battingSideRight.isChecked = true
            }
            null -> {
                battingSideLeft.isChecked = false
                battingSideRight.isChecked = false
            }
        }
    }

    fun getEmail(): String? {
        return playerEmailInput.text.toString()
    }

    fun setEmail(email: String?) {
        playerEmailInput.setText(email ?: "")
    }

    fun getPhone(): String? {
        return playerPhoneInput.text.toString()
    }

    fun setPhone(phone: String?) {
        playerPhoneInput.setText(phone ?: "")
    }
}