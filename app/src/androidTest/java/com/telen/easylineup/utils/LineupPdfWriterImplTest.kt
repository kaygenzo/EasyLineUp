/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.TeamStrategy
import com.telen.easylineup.domain.model.TeamType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The PDF has no readable text API, so this renders the real file back to a bitmap and checks
 * which areas of the page hold ink. It proves the document opens, goes through the app's
 * FileProvider and draws each section where expected - not that the card looks right.
 */
@RunWith(AndroidJUnit4::class)
class LineupPdfWriterImplTest : KoinComponent {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val writer: LineupPdfWriter by inject()

    private fun batter(order: Int) = BatterState(
        playerId = order.toLong(),
        playerFlag = 0,
        playerOrder = order,
        playerName = "Batter $order",
        playerNumber = "$order",
        playerPosition = FieldPosition.PITCHER,
        playerPositionDesc = "",
        canShowPosition = true,
        canMove = false,
        canShowDescription = true,
        canShowOrder = true,
        applyBackground = false,
        isEditable = false
    )

    private fun player(id: Long, position: FieldPosition) = PlayerWithPosition(
        playerName = "Player $id",
        playerSex = 0,
        shirtNumber = id.toInt(),
        licenseNumber = id.toString(),
        teamId = 1L,
        image = null,
        position = position.id,
        x = 50f,
        y = 50f,
        order = id.toInt(),
        playerId = id,
        lineupId = 1L,
        playerPositions = 0
    )

    private fun data(
        battingOrder: List<BatterState> = (1..9).map(::batter),
        defensePlayers: List<PlayerWithPosition> = listOf(player(1L, FieldPosition.PITCHER))
    ) = LineupExportData(
        lineupName = "Game 1",
        eventTimeInMillis = 1_000L,
        teamType = TeamType.BASEBALL,
        strategy = TeamStrategy.STANDARD,
        battingOrder = battingOrder,
        defensePlayers = defensePlayers
    )

    private fun render(uri: Uri): Bitmap {
        val descriptor = context.contentResolver.openFileDescriptor(uri, "r")!!
        return PdfRenderer(descriptor).use { renderer ->
            assertEquals(1, renderer.pageCount)
            renderer.openPage(0).use { page ->
                assertEquals(PAGE_WIDTH, page.width)
                assertEquals(PAGE_HEIGHT, page.height)
                Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888).also {
                    it.eraseColor(Color.WHITE)
                    page.render(it, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                }
            }
        }
    }

    private fun Bitmap.hasInk(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        for (x in left until right) {
            for (y in top until bottom) {
                if (getPixel(x, y) != Color.WHITE) {
                    return true
                }
            }
        }
        return false
    }

    @Test
    fun writesASinglePageThroughTheAppsFileProvider() {
        val uri = writer.write(data())

        assertEquals("content", uri.scheme)
        assertEquals(context.packageName + ".fileprovider", uri.authority)
        render(uri)
    }

    @Test
    fun drawsTheBattingOrderRows() {
        val withBatters = render(writer.write(data()))
        val withoutBatters = render(writer.write(data(battingOrder = emptyList())))

        assertTrue(withBatters.hasInk(40, 155, 280, 370))
        assertFalse(withoutBatters.hasInk(40, 155, 280, 370))
    }

    @Test
    fun drawsTheSubstitutesSectionOnlyWhenThereAreSome() {
        val starter = player(1L, FieldPosition.PITCHER)
        val substitute = player(2L, FieldPosition.SUBSTITUTE)

        val withSubstitute = render(
            writer.write(data(defensePlayers = listOf(starter, substitute)))
        )
        val withoutSubstitute = render(writer.write(data(defensePlayers = listOf(starter))))

        assertTrue(withSubstitute.hasInk(320, 400, 555, 480))
        assertFalse(withoutSubstitute.hasInk(320, 400, 555, 480))
    }

    private companion object {
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842
    }
}
