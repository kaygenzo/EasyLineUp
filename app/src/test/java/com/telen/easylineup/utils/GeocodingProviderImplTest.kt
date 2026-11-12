/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.app.Application
import android.location.Address
import android.location.Geocoder
import androidx.test.core.app.ApplicationProvider
import com.telen.easylineup.domain.model.GeoLocation
import java.util.Locale
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class GeocodingProviderImplTest {
    private val geocoder = Geocoder(ApplicationProvider.getApplicationContext())
    private val provider = GeocodingProviderImpl(geocoder, isGeocoderPresent = { true })

    private fun address(latitude: Double, longitude: Double) = Address(Locale.FRANCE).apply {
        this.latitude = latitude
        this.longitude = longitude
    }

    @Test
    fun returnsTheFirstMatch() = runTest {
        shadowOf(geocoder).setFromLocation(listOf(address(48.85, 2.35), address(45.76, 4.83)))

        assertEquals(GeoLocation(48.85, 2.35), provider.getLocationFromAddress("Paris"))
    }

    @Test
    fun noMatchIsNull() = runTest {
        shadowOf(geocoder).setFromLocation(emptyList())

        assertNull(provider.getLocationFromAddress("nowhere"))
    }

    @Test
    fun aServiceErrorIsNull() = runTest {
        shadowOf(geocoder).setErrorMessage("network down")

        assertNull(provider.getLocationFromAddress("Paris"))
    }

    @Test
    fun aDeviceWithoutGeocoderIsNullWithoutAsking() = runTest {
        shadowOf(geocoder).setFromLocation(listOf(address(48.85, 2.35)))
        val withoutService = GeocodingProviderImpl(geocoder, isGeocoderPresent = { false })

        assertNull(withoutService.getLocationFromAddress("Paris"))
    }

    @Test
    fun aBlankAddressIsNullWithoutAsking() = runTest {
        shadowOf(geocoder).setFromLocation(listOf(address(48.85, 2.35)))

        assertNull(provider.getLocationFromAddress("  "))
    }
}
