/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.telen.easylineup.R
import com.telen.easylineup.domain.model.BatterState
import com.telen.easylineup.domain.model.FieldPosition
import com.telen.easylineup.domain.model.PlayerWithPosition
import com.telen.easylineup.domain.model.isSubstitute
import com.telen.easylineup.domain.ports.StringResourcesProvider
import com.telen.easylineup.domain.usecases.SortSubstitutesForDisplay
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat
import java.util.Date
import java.util.Locale

/**
 * Draws a printable lineup card (batting order + defense diagram + bench) directly with
 * [PdfDocument]/[Canvas] - a light, print-friendly page independent of the app's own dark
 * "Dugout Card" theme, since this is meant to be printed and written on, not viewed on screen.
 */
class LineupPdfWriterImpl(
    private val context: Context,
    private val stringResourcesProvider: StringResourcesProvider,
    private val sortSubstitutesForDisplay: SortSubstitutesForDisplay
) : LineupPdfWriter {

    override fun write(data: LineupExportData): Uri {
        val now = System.currentTimeMillis()
        deleteOldCards(now)
        val file = File(context.cacheDir, "$FILE_PREFIX$now$FILE_SUFFIX")

        val document = PdfDocument()
        try {
            drawCard(document, data)
            FileOutputStream(file).use { stream -> document.writeTo(stream) }
        } finally {
            document.close()
        }

        val authority = context.getString(R.string.file_provider_authority)
        return FileProvider.getUriForFile(context, authority, file)
    }

    /**
     * Each share writes a new file to the cache. The ones from earlier days go: a day is long
     * past the app the card was shared with reading it, a minute might not be.
     */
    private fun deleteOldCards(now: Long) {
        context.cacheDir
            .listFiles { file ->
                file.name.startsWith(FILE_PREFIX) && file.name.endsWith(FILE_SUFFIX) &&
                    now - file.lastModified() > OLD_CARD_AGE_MILLIS
            }
            ?.forEach { it.delete() }
    }

    /**
     * The first page holds the whole card. A batting order or a bench too long for it - extra
     * hitters can be unlimited - carries on over as many pages as needed, each column where it
     * was.
     */
    private fun drawCard(document: PdfDocument, data: LineupExportData) {
        var pageNumber = 1
        val page = document.startPage(pageInfo(pageNumber))
        val canvas = page.canvas
        drawBackground(canvas)
        drawTitle(canvas)
        drawHeader(canvas, data)
        canvas.drawLine(MARGIN, HEADER_RULE_Y, PAGE_WIDTH - MARGIN, HEADER_RULE_Y, rulePaint())

        val batters = data.battingOrder.withIndex().toList()
        var battersLeft = drawBattingOrder(canvas, batters, COLUMNS_TITLE_Y)
        val diamondBottom = drawDefenseDiamond(canvas, data)

        // Placed under the diamond rather than after the batting order: a full roster's batting
        // order list can already reach close to the page bottom, leaving no room below it.
        var substitutesLeft = drawSubstitutes(
            canvas,
            sortSubstitutesForDisplay(data.defensePlayers),
            startY = diamondBottom + SECTION_GAP
        )
        document.finishPage(page)

        while (battersLeft.isNotEmpty() || substitutesLeft.isNotEmpty()) {
            pageNumber++
            val next = document.startPage(pageInfo(pageNumber))
            drawBackground(next.canvas)
            battersLeft =
                drawBattingOrder(next.canvas, battersLeft, CONTINUED_TITLE_Y, continued = true)
            substitutesLeft = drawSubstitutes(next.canvas, substitutesLeft, CONTINUED_TITLE_Y)
            document.finishPage(next)
        }
    }

    private fun pageInfo(number: Int) =
        PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, number).create()

    private fun drawBackground(canvas: Canvas) {
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), backgroundPaint())
    }

    private fun drawTitle(canvas: Canvas) {
        canvas.drawText(
            context.getString(R.string.lineup_pdf_title),
            PAGE_WIDTH / 2f,
            TITLE_Y,
            titlePaint()
        )
    }

    private fun drawHeader(canvas: Canvas, data: LineupExportData) {
        val dateLabel = context.getString(
            R.string.lineup_pdf_date_label,
            dateFormatter().format(Date(data.eventTimeInMillis))
        )
        val opponentLabel = context.getString(R.string.lineup_pdf_opponent_label, data.lineupName)
        val paint = headerPaint()
        canvas.drawText(dateLabel, MARGIN, HEADER_Y, paint)
        canvas.drawText(opponentLabel, OPPONENT_X, HEADER_Y, paint)
    }

    /**
     * Draws the batters that fit under [titleY] and returns the ones left for the next page. The
     * first page shows the section even empty, a page after it only to carry on a long one.
     */
    private fun drawBattingOrder(
        canvas: Canvas,
        batters: List<IndexedValue<BatterState>>,
        titleY: Float,
        continued: Boolean = false
    ): List<IndexedValue<BatterState>> {
        if (batters.isEmpty() && continued) {
            return emptyList()
        }
        canvas.drawText(
            context.getString(R.string.lineup_pdf_batting_order_title),
            MARGIN,
            titleY,
            sectionTitlePaint()
        )

        val orderPaint = bodyPaint(bold = true)
        val namePaint = bodyPaint(bold = false)
        val firstRowY = titleY + BATTING_ROWS_GAP
        val (fitting, left) = batters.splitAt(rowsThatFit(firstRowY))
        var rowY = firstRowY
        fitting.forEach { (index, batter) ->
            canvas.drawText("${index + 1}", MARGIN, rowY, orderPaint)
            canvas.drawText(
                "${batter.playerName} (#${batter.playerNumber})",
                MARGIN + BATTING_NAME_INDENT,
                rowY,
                namePaint
            )
            canvas.drawLine(
                MARGIN,
                rowY + ROW_RULE_OFFSET,
                MARGIN + BATTING_COLUMN_WIDTH,
                rowY + ROW_RULE_OFFSET,
                rulePaint()
            )
            rowY += ROW_HEIGHT
        }
        return left
    }

    /** How many rows starting at [firstRowY] fit above the bottom margin. */
    private fun rowsThatFit(firstRowY: Float): Int =
        ((PAGE_HEIGHT - MARGIN - firstRowY) / ROW_HEIGHT).toInt() + 1

    private fun <T> List<T>.splitAt(count: Int): Pair<List<T>, List<T>> =
        take(count) to drop(count)

    /** Returns the y just past the diamond, so the substitutes section can start below it. */
    private fun drawDefenseDiamond(canvas: Canvas, data: LineupExportData): Float {
        canvas.drawText(
            context.getString(R.string.lineup_pdf_defense_title),
            DIAMOND_LEFT,
            COLUMNS_TITLE_Y,
            sectionTitlePaint()
        )

        canvas.drawPath(outfieldPath(), outfieldPaint())
        canvas.drawPath(infieldPath(), infieldPaint())
        canvas.drawPath(infieldPath(), diamondOutlinePaint())

        val shortNames = stringResourcesProvider.positionShortNames(data.teamType.id)
        val labelPaint = diamondLabelPaint()
        data.defensePlayers.filterNot { it.isSubstitute() }.forEach { player ->
            val position = FieldPosition.getFieldPositionById(player.position) ?: return@forEach
            val label = shortNames.getOrNull(position.id).orEmpty()
            val px = DIAMOND_LEFT + player.x / 100f * DIAMOND_SIZE
            val py = DIAMOND_TOP + player.y / 100f * DIAMOND_SIZE
            canvas.drawText("$label ${player.playerName}", px, py, labelPaint)
        }
        return DIAMOND_TOP + DIAMOND_SIZE
    }

    /**
     * Draws the substitutes that fit in the right column under [startY] and returns the ones left
     * for the next page.
     */
    private fun drawSubstitutes(
        canvas: Canvas,
        substitutes: List<PlayerWithPosition>,
        startY: Float
    ): List<PlayerWithPosition> {
        if (substitutes.isEmpty()) {
            return emptyList()
        }

        canvas.drawText(
            context.getString(R.string.lineup_pdf_substitutes_title),
            DIAMOND_LEFT,
            startY,
            sectionTitlePaint()
        )

        val paint = bodyPaint(bold = false)
        val firstRowY = startY + ROW_HEIGHT
        val (fitting, left) = substitutes.splitAt(rowsThatFit(firstRowY))
        var rowY = firstRowY
        fitting.forEach { player ->
            canvas.drawText(
                "${player.playerName} (#${player.shirtNumber})",
                DIAMOND_LEFT,
                rowY,
                paint
            )
            rowY += ROW_HEIGHT
        }
        return left
    }

    private fun outfieldPath(): Path {
        return Path().apply {
            moveTo(DIAMOND_LEFT + DIAMOND_SIZE * 0.5f, DIAMOND_TOP + DIAMOND_SIZE * 0.85f)
            lineTo(DIAMOND_LEFT, DIAMOND_TOP + DIAMOND_SIZE * 0.35f)
            lineTo(DIAMOND_LEFT + DIAMOND_SIZE * 0.5f, DIAMOND_TOP)
            lineTo(DIAMOND_LEFT + DIAMOND_SIZE, DIAMOND_TOP + DIAMOND_SIZE * 0.35f)
            close()
        }
    }

    private fun infieldPath(): Path {
        val centerX = DIAMOND_LEFT + DIAMOND_SIZE * 0.5f
        val homeY = DIAMOND_TOP + DIAMOND_SIZE * 0.85f
        val baseSpread = DIAMOND_SIZE * 0.28f
        val baseY = homeY - DIAMOND_SIZE * 0.28f
        return Path().apply {
            moveTo(centerX, homeY)
            lineTo(centerX - baseSpread, baseY)
            lineTo(centerX, baseY - DIAMOND_SIZE * 0.28f)
            lineTo(centerX + baseSpread, baseY)
            close()
        }
    }

    // The language's own short date ("11/10/26" in English, "10/11/2026" in French), not a
    // fixed day-first pattern.
    private fun dateFormatter() = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())

    private fun backgroundPaint() = Paint().apply { color = Color.WHITE }

    private fun titlePaint() = Paint().apply {
        color = INK_COLOR
        textSize = TITLE_TEXT_SIZE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private fun headerPaint() = Paint().apply {
        color = INK_COLOR
        textSize = HEADER_TEXT_SIZE
        isAntiAlias = true
    }

    private fun sectionTitlePaint() = Paint().apply {
        color = INK_COLOR
        textSize = SECTION_TITLE_TEXT_SIZE
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        isAntiAlias = true
    }

    private fun bodyPaint(bold: Boolean) = Paint().apply {
        color = INK_COLOR
        textSize = BODY_TEXT_SIZE
        typeface = Typeface.create(Typeface.DEFAULT, if (bold) Typeface.BOLD else Typeface.NORMAL)
        isAntiAlias = true
    }

    private fun diamondLabelPaint() = Paint().apply {
        color = INK_COLOR
        textSize = DIAMOND_LABEL_TEXT_SIZE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private fun rulePaint() = Paint().apply {
        color = INK_COLOR
        strokeWidth = 1f
    }

    private fun outfieldPaint() = Paint().apply {
        color = FIELD_GREEN
        isAntiAlias = true
    }

    private fun infieldPaint() = Paint().apply {
        color = FIELD_DIRT
        isAntiAlias = true
    }

    private fun diamondOutlinePaint() = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
        isAntiAlias = true
    }

    private companion object {
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842
        const val MARGIN = 40f

        const val INK_COLOR = 0xFF1A237E.toInt() // dark navy
        const val FIELD_GREEN = 0xFF7BA56D.toInt() // muted green
        const val FIELD_DIRT = 0xFFC4A484.toInt() // tan/dirt

        const val TITLE_Y = 60f
        const val TITLE_TEXT_SIZE = 32f
        const val HEADER_Y = 95f
        const val HEADER_TEXT_SIZE = 13f
        const val OPPONENT_X = 320f
        const val HEADER_RULE_Y = 110f

        const val COLUMNS_TITLE_Y = 140f
        const val SECTION_TITLE_TEXT_SIZE = 15f
        const val BODY_TEXT_SIZE = 12f

        // The first row sits this far under its section title, as on the first page (y = 165).
        const val BATTING_ROWS_GAP = 25f
        const val CONTINUED_TITLE_Y = 60f
        const val BATTING_NAME_INDENT = 24f
        const val BATTING_COLUMN_WIDTH = 240f
        const val ROW_HEIGHT = 24f
        const val ROW_RULE_OFFSET = 6f

        const val DIAMOND_LEFT = 320f
        const val DIAMOND_TOP = 150f
        const val DIAMOND_SIZE = 235f
        const val DIAMOND_LABEL_TEXT_SIZE = 10f

        const val SECTION_GAP = 30f

        const val FILE_PREFIX = "lineup_"
        const val FILE_SUFFIX = ".pdf"
        const val OLD_CARD_AGE_MILLIS = 24 * 60 * 60 * 1000L
    }
}
