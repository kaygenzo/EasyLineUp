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
import com.nguyenhoanglam.imagepicker.model.Image
import com.squareup.picasso.Picasso
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerSide
import com.telen.easylineup.utils.ready
import kotlinx.android.synthetic.main.view_create_player.view.*
import timber.log.Timber

interface PlayerFormListener {
    fun onSaveClicked(name: String?, shirtNumber: Int?, licenseNumber: Long?, imageUri: Uri?, positions: Int, pitching: Int, batting: Int)
    fun onCancel()
    fun onImagePickerRequested()
}

class PlayerFormView: ConstraintLayout {

    private var listener: PlayerFormListener? = null
    private var imageUri: Uri? = null
    private var playerPositions = 0
    private val positionState: MutableMap<FieldPosition, Boolean> = mutableMapOf()

    constructor(context: Context) : super(context) {initView(context)}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {initView(context)}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {initView(context)}

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

            listener?.onSaveClicked(name, shirtNumber, licenseNumber, imageUri, positions, pitching, batting)
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
    }

    fun onImageUriReceived(image: Image) {
        val filePathUri = android.content.ContentResolver.SCHEME_FILE + ":///" + image.path
        setImage(filePathUri)
    }

    fun getName(): String {
        return playerNameInput.text.toString()
    }

    fun getShirtNumber(): Int? {
        return try {
            playerShirtNumberInput.text.toString().toInt()
        }
        catch (exception: NumberFormatException) {
            null
        }
    }

    fun getLicenseNumber(): Long? {
        return try {
            playerLicenseNumberInput.text.toString().toLong()
        }
        catch (exception: NumberFormatException) {
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

    fun setImage(imagePath: String) {

        imageUri = Uri.parse(imagePath)

        playerImage.ready {
            try {
                Picasso.get().load(imageUri)
                        .resize(playerImage.width, playerImage.height)
                        .centerCrop()
                        .transform(RoundedTransformationBuilder()
                                .borderColor(Color.BLACK)
                                .borderWidthDp(2f)
                                .cornerRadiusDp(16f)
                                .oval(true)
                                .build())
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

    fun getPlayerPositions() : Int {
        return playerPositions
    }

    fun setPositionsFilter(positions: Int) {

        this.playerPositions = positions
        this.positionState.clear()

        val positionShortDescription = FieldPosition.getPositionShortNames(context, 0)

        favoritePositionsContainer.removeAllViews()

        FieldPosition.values()
                .filter { FieldPosition.isDefensePlayer(it.position) }
                .forEach { position ->
            val isEnabled = (positions and position.mask) != 0
            positionState[position] = isEnabled

            val view = PlayerPositionFilterView(context)
            view.setText(positionShortDescription[position.ordinal])
            applyFilterOnView(view, isEnabled)

            view.setOnClickListener { _ ->

                positionState[position]?.let { oldState ->
                    val newState = !oldState

                    if(newState) {
                        if(positionState.filterValues { it == true }.size >= 3) {
                            return@setOnClickListener
                        }
                    }

                    positionState[position] = newState
                    applyFilterOnView(view, newState)
                    playerPositions = if(newState) {
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
        when(isEnabled) {
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
        playerLicenseNumberInputLayout.error = null
        playerShirtNumberInputLayout.error = null
    }

    fun displayInvalidLicense() {
        playerNameInputLayout.error = null
        playerLicenseNumberInputLayout.error = resources.getString(R.string.player_creation_error_license_empty)
        playerShirtNumberInputLayout.error = null
    }

    fun displayInvalidNumber() {
        playerNameInputLayout.error = null
        playerLicenseNumberInputLayout.error = null
        playerShirtNumberInputLayout.error = resources.getString(R.string.player_creation_error_shirt_empty)
    }

    fun getPitchingSide(): PlayerSide? {
        var pitching = 0
        if(pitchingSideLeft.isChecked)
            pitching = pitching or PlayerSide.LEFT.flag
        if(pitchingSideRight.isChecked)
            pitching = pitching or PlayerSide.RIGHT.flag
        return PlayerSide.getSideByValue(pitching)
    }

    fun getBattingSide(): PlayerSide? {
        var batting = 0
        if(battingSideLeft.isChecked)
            batting = batting or PlayerSide.LEFT.flag
        if(battingSideRight.isChecked)
            batting = batting or PlayerSide.RIGHT.flag
        return PlayerSide.getSideByValue(batting)
    }

    fun setPitchingSide(side: PlayerSide?) {
        when(side) {
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

    fun setBattingSide(side: PlayerSide?) {
        when(side) {
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
}