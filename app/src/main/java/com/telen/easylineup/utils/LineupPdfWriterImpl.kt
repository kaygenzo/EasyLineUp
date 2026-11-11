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
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        drawCard(page.canvas, data)
        document.finishPage(page)

        val file = File(context.cacheDir, "lineup_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { stream -> document.writeTo(stream) }
        document.close()

        val authority = context.packageName + ".fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }

    private fun drawCard(canvas: Canvas, data: LineupExportData) {
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), backgroundPaint())

        drawTitle(canvas)
        drawHeader(canvas, data)
        canvas.drawLine(MARGIN, HEADER_RULE_Y, PAGE_WIDTH - MARGIN, HEADER_RULE_Y, rulePaint())

        drawBattingOrder(canvas, data.battingOrder)
        val diamondBottom = drawDefenseDiamond(canvas, data)

        // Placed under the diamond rather than after the batting order: a full roster's batting
        // order list can already reach close to the page bottom, leaving no room below it.
        drawSubstitutes(canvas, data, startX = DIAMOND_LEFT, startY = diamondBottom + SECTION_GAP)
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

    private fun drawBattingOrder(canvas: Canvas, battingOrder: List<BatterState>) {
        canvas.drawText(
            context.getString(R.string.lineup_pdf_batting_order_title),
            MARGIN,
            COLUMNS_TITLE_Y,
            sectionTitlePaint()
        )

        val orderPaint = bodyPaint(bold = true)
        val namePaint = bodyPaint(bold = false)
        var rowY = BATTING_ROWS_TOP
        battingOrder.forEachIndexed { index, batter ->
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
    }

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

    private fun drawSubstitutes(
        canvas: Canvas,
        data: LineupExportData,
        startX: Float,
        startY: Float
    ) {
        val substitutes = sortSubstitutesForDisplay(data.defensePlayers)
        if (substitutes.isEmpty()) {
            return
        }

        canvas.drawText(
            context.getString(R.string.lineup_pdf_substitutes_title),
            startX,
            startY,
            sectionTitlePaint()
        )

        val paint = bodyPaint(bold = false)
        var rowY = startY + ROW_HEIGHT
        substitutes.forEach { player ->
            canvas.drawText("${player.playerName} (#${player.shirtNumber})", startX, rowY, paint)
            rowY += ROW_HEIGHT
        }
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

        const val BATTING_ROWS_TOP = 165f
        const val BATTING_NAME_INDENT = 24f
        const val BATTING_COLUMN_WIDTH = 240f
        const val ROW_HEIGHT = 24f
        const val ROW_RULE_OFFSET = 6f

        const val DIAMOND_LEFT = 320f
        const val DIAMOND_TOP = 150f
        const val DIAMOND_SIZE = 235f
        const val DIAMOND_LABEL_TEXT_SIZE = 10f

        const val SECTION_GAP = 30f
    }
}
