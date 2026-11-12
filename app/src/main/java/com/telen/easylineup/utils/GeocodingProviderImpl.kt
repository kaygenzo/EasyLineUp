package com.telen.easylineup.utils

import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import com.telen.easylineup.domain.model.GeoLocation
import com.telen.easylineup.domain.ports.GeocodingProvider
import java.io.IOException
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber

/**
 * Looks addresses up with the platform [Geocoder]. Returns null, rather than throwing, when the
 * address can't be placed: no geocoding service on this device (some devices without Google
 * services), no network, or no match.
 */
class GeocodingProviderImpl(
    private val geocoder: Geocoder,
    private val isGeocoderPresent: () -> Boolean = Geocoder::isPresent
) : GeocodingProvider {
    override suspend fun getLocationFromAddress(address: String): GeoLocation? {
        if (address.isBlank() || !isGeocoderPresent()) {
            return null
        }
        val found = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            lookUp(address)
        } else {
            lookUpBlocking(address)
        }
        return found?.let { GeoLocation(it.latitude, it.longitude) }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun lookUp(address: String): Address? = suspendCancellableCoroutine {
        geocoder.getFromLocationName(address, 1, object : Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<Address>) {
                it.resume(addresses.firstOrNull())
            }

            override fun onError(errorMessage: String?) {
                Timber.w("Geocoding failed: $errorMessage")
                it.resume(null)
            }
        })
    }

    /** The only variant before Android 13, deprecated since: it blocks, on the IO dispatcher. */
    @Suppress("DEPRECATION")
    private fun lookUpBlocking(address: String): Address? = try {
        geocoder.getFromLocationName(address, 1)?.firstOrNull()
    } catch (e: IOException) {
        Timber.w(e, "Geocoding failed")
        null
    }
}
