package com.emagioda.myapp.presentation.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.print.pdf.PrintedPdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withTranslation
import com.emagioda.myapp.R
import com.emagioda.myapp.domain.model.EndResult
import com.emagioda.myapp.domain.model.MaintenanceCaseDetail
import com.emagioda.myapp.domain.model.MaintenanceStatus
import com.emagioda.myapp.domain.model.MaintenanceTimelineEvent
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

private val PrintInk = "#111827".toColorInt()
private val PrintStrong = "#1F2937".toColorInt()
private val PrintBody = "#334155".toColorInt()
private val PrintMuted = "#667085".toColorInt()
private val PrintDivider = "#D8DEE6".toColorInt()
private val PrintSurface = "#F8FAFC".toColorInt()
private val PrintProblemSurface = "#FCFAF6".toColorInt()
private val PrintPending = "#8A5A00".toColorInt()
private val PrintProgress = "#1E5AA8".toColorInt()
private val PrintSuccess = "#2A7A55".toColorInt()
private val PrintDanger = "#A33B3B".toColorInt()

private data class SummaryItem(
    val label: String,
    val value: String,
    val fullWidth: Boolean = false
)

private data class MeasuredSummaryCell(
    val item: SummaryItem,
    val labelLayout: StaticLayout,
    val valueLayout: StaticLayout,
    val width: Float,
    val height: Float
)

object MaintenanceCasePrintHelper {

    fun printCase(context: Context, detail: MaintenanceCaseDetail): Boolean {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            ?: return false
        val jobName = "${context.getString(R.string.history_case_detail_title)} - ${detail.caseCode}"

        printManager.print(
            jobName,
            MaintenanceCasePrintAdapter(
                context = context,
                detail = detail,
                jobName = jobName
            ),
            PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .build()
        )
        return true
    }
}

private class MaintenanceCasePrintAdapter(
    private val context: Context,
    private val detail: MaintenanceCaseDetail,
    private val jobName: String
) : PrintDocumentAdapter() {

    private var printAttributes: PrintAttributes? = null

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal,
        callback: LayoutResultCallback,
        extras: Bundle?
    ) {
        printAttributes = newAttributes

        if (cancellationSignal.isCanceled) {
            callback.onLayoutCancelled()
            return
        }

        val info = PrintDocumentInfo.Builder("$jobName.pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
            .build()

        callback.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<out PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback
    ) {
        val attributes = printAttributes
        if (attributes == null) {
            callback.onWriteFailed(context.getString(R.string.history_print_error))
            return
        }

        val pdfDocument = PrintedPdfDocument(context, attributes)

        try {
            val renderer = MaintenanceCasePdfRenderer(
                context = context,
                detail = detail,
                pdfDocument = pdfDocument
            )
            renderer.render(cancellationSignal)

            if (cancellationSignal.isCanceled) {
                callback.onWriteCancelled()
                return
            }

            FileOutputStream(destination.fileDescriptor).use { output ->
                pdfDocument.writeTo(output)
            }
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (_: Exception) {
            callback.onWriteFailed(context.getString(R.string.history_print_error))
        } finally {
            pdfDocument.close()
        }
    }
}

private class MaintenanceCasePdfRenderer(
    private val context: Context,
    private val detail: MaintenanceCaseDetail,
    private val pdfDocument: PrintedPdfDocument
) {
    private val issueTimestamp = System.currentTimeMillis()
    private val issueDateText = formatPrintDateTimeLabel(issueTimestamp)
    private val logoBitmap: Bitmap? = BitmapFactory.decodeResource(
        context.resources,
        R.drawable.splash_logo
    )

    private val contentRect = pdfDocument.pageContentRect
    private val pageLeft = contentRect.left.toFloat() + 24f
    private val pageRight = contentRect.right.toFloat() - 24f
    private val pageTop = contentRect.top.toFloat() + 18f
    private val pageBottom = contentRect.bottom.toFloat() - 18f
    private val pageWidth = pageRight - pageLeft
    private val headerHeight = 66f
    private val footerHeight = 28f
    private val contentTop = pageTop + headerHeight + 14f
    private val contentBottom = pageBottom - footerHeight - 6f
    private val blockGap = 16f
    private val panelGap = 10f
    private val panelPadding = 14f
    private val panelRadius = 10f
    private val timelineGuideX = pageLeft + 6f
    private val timelineContentX = timelineGuideX + 16f
    private val timelineContentWidth = pageRight - timelineContentX

    private val brandPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PrintInk
        textSize = 10.2f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
    }
    private val reportTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PrintInk
        textSize = 14.2f
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
    }
    private val headerMetaLabelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PrintMuted
        textSize = 6.9f
        textAlign = Paint.Align.LEFT
    }
    private val headerInfoValuePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PrintStrong
        textSize = 8.7f
        textAlign = Paint.Align.LEFT
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
    }
    private val headerCodeValuePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PrintStrong
        textSize = 7.4f
        textAlign = Paint.Align.LEFT
        typeface = android.graphics.Typeface.create("monospace", android.graphics.Typeface.NORMAL)
    }
    private val headerStatusValuePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PrintInk
        textSize = 9.8f
        textAlign = Paint.Align.LEFT
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
    }
    private val sectionTagPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PrintMuted
        textSize = 8.1f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
    }
    private val assetTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PrintInk
        textSize = 13.8f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
    }
    private val assetCodePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PrintMuted
        textSize = 9f
        typeface = android.graphics.Typeface.create("monospace", android.graphics.Typeface.NORMAL)
    }
    private val problemTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PrintInk
        textSize = 16.1f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
    }
    private val sectionTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PrintInk
        textSize = 12.6f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
    }
    private val sectionSubtitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PrintMuted
        textSize = 8.9f
    }
    private val summaryLabelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PrintMuted
        textSize = 8.4f
    }
    private val summaryValuePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PrintStrong
        textSize = 9.9f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
    }
    private val timelineDatePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PrintMuted
        textSize = 8.5f
    }
    private val timelineTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PrintInk
        textSize = 11.2f
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
    }
    private val timelineBodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PrintBody
        textSize = 9.6f
    }
    private val footerTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PrintMuted
        textSize = 8.3f
    }
    private val footerPagePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PrintMuted
        textSize = 8.3f
        textAlign = Paint.Align.RIGHT
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PrintDivider
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }
    private val panelFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val panelStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PrintDivider
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }
    private val timelineGuidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PrintDivider
        strokeWidth = 1.2f
        style = Paint.Style.STROKE
    }
    private val timelineMarkerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PrintStrong
        style = Paint.Style.FILL
    }
    private val problemAccentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PrintStrong
        style = Paint.Style.FILL
    }

    private val problemTitle = resolveText(
        context,
        detail.problemSummary?.takeIf { it.isNotBlank() } ?: detail.diagnosisTitle
    )
    private val closureSummaryLabel = when {
        detail.resolvedAt != null -> context.getString(R.string.history_resolved_at)
        detail.canceledAt != null -> context.getString(R.string.history_canceled_at)
        else -> null
    }
    private val closureSummaryValue = when {
        detail.resolvedAt != null -> formatPrintDateTimeLabel(detail.resolvedAt)
        detail.canceledAt != null -> formatPrintDateTimeLabel(detail.canceledAt)
        else -> null
    }
    private var countingOnly = false
    private var totalPages = 0
    private var currentPageNumber = 0
    private var currentPage: android.graphics.pdf.PdfDocument.Page? = null
    private var canvas: Canvas? = null
    private var y = contentTop

    fun render(cancellationSignal: CancellationSignal) {
        totalPages = countPages(cancellationSignal)
        if (cancellationSignal.isCanceled) return

        countingOnly = false
        resetState()
        startNewPage()
        renderContent(cancellationSignal)
        finishCurrentPage()
    }

    private fun countPages(cancellationSignal: CancellationSignal): Int {
        countingOnly = true
        resetState()
        startNewPage()
        renderContent(cancellationSignal)
        finishCurrentPage()
        return currentPageNumber
    }

    private fun renderContent(cancellationSignal: CancellationSignal) {
        drawOverviewSection(cancellationSignal)
        y += blockGap

        drawSummarySection(cancellationSignal)
        y += blockGap

        drawTimelineSectionHeader(cancellationSignal, compact = false)
        detail.events.forEachIndexed { index, event ->
            if (cancellationSignal.isCanceled) return
            drawTimelineEvent(
                item = event,
                isLast = index == detail.events.lastIndex,
                cancellationSignal = cancellationSignal
            )
        }
    }

    private fun drawOverviewSection(cancellationSignal: CancellationSignal) {
        val machineWidth = max(pageWidth * 0.48f, 232f)
        val problemWidth = pageWidth - machineWidth - panelGap

        val machineLabelLayout = createLayout(
            context.getString(R.string.machine_detail_title),
            sectionTagPaint,
            machineWidth.toInt()
        )
        val machineTextWidth = machineWidth - panelPadding * 2f
        val machineNamePaint = fittedSingleLinePaint(
            basePaint = assetTitlePaint,
            text = detail.machineNameSnapshot,
            maxWidth = machineTextWidth,
            minSize = 8.2f
        )
        val machineNameText = TextUtils.ellipsize(
            detail.machineNameSnapshot,
            machineNamePaint,
            machineTextWidth,
            TextUtils.TruncateAt.END
        ).toString()
        val machineNameHeight = machineNamePaint.fontMetrics.run { bottom - top }
        val machineCodeLayout = createLayout(
            detail.machineId,
            assetCodePaint,
            machineTextWidth.toInt()
        )

        val problemLabelLayout = createLayout(
            context.getString(R.string.history_event_problem),
            sectionTagPaint,
            problemWidth.toInt()
        )
        val problemLayout = createLayout(
            problemTitle,
            problemTitlePaint,
            problemWidth.toInt()
        )

        val machineHeight = panelPadding * 2f +
            machineLabelLayout.height +
            6f +
            machineNameHeight +
            4f +
            machineCodeLayout.height
        val problemHeight = panelPadding * 2f +
            problemLabelLayout.height +
            6f +
            problemLayout.height
        val panelHeight = max(machineHeight, problemHeight)

        ensureSpace(panelHeight)
        if (cancellationSignal.isCanceled) return

        if (!countingOnly) {
            val machineRect = RectF(pageLeft, y, pageLeft + machineWidth, y + panelHeight)
            val problemRect = RectF(
                machineRect.right + panelGap,
                y,
                pageRight,
                y + panelHeight
            )

            drawRoundedPanel(machineRect, PrintSurface)
            drawRoundedPanel(problemRect, PrintProblemSurface)

            val machineTextX = machineRect.left + panelPadding
            var machineTextY = machineRect.top + panelPadding
            requireCanvas().withTranslation(machineTextX, machineTextY) {
                machineLabelLayout.draw(this)
            }
            machineTextY += machineLabelLayout.height + 6f
            val machineNameBaseline = machineTextY - machineNamePaint.fontMetrics.top
            requireCanvas().drawText(
                machineNameText,
                machineTextX,
                machineNameBaseline,
                machineNamePaint
            )
            machineTextY += machineNameHeight + 4f
            requireCanvas().withTranslation(machineTextX, machineTextY) {
                machineCodeLayout.draw(this)
            }

            val accentLeft = problemRect.left + panelPadding
            val accentTop = problemRect.top + panelPadding + problemLabelLayout.height + 6f
            val accentBottom = problemRect.bottom - panelPadding
            requireCanvas().drawRoundRect(
                RectF(accentLeft, accentTop, accentLeft + 3f, accentBottom),
                3f,
                3f,
                problemAccentPaint
            )

            val problemTextX = accentLeft + 11f
            var problemTextY = problemRect.top + panelPadding
            requireCanvas().withTranslation(problemTextX, problemTextY) {
                problemLabelLayout.draw(this)
            }
            problemTextY += problemLabelLayout.height + 6f
            requireCanvas().withTranslation(problemTextX, problemTextY) {
                problemLayout.draw(this)
            }
        }

        y += panelHeight
    }

    private fun drawSummarySection(cancellationSignal: CancellationSignal) {
        drawSingleLine(
            text = context.getString(R.string.history_summary_title),
            paint = sectionTitlePaint,
            spacingAfter = 6f
        )
        if (cancellationSignal.isCanceled) return

        val panelInnerPadding = 12f
        val horizontalGutter = 10f
        val verticalSpacing = 10f
        val separatorSpacing = 8f
        val summaryWidth = pageWidth - panelInnerPadding * 2f
        val primaryColumnWidth = (summaryWidth - (horizontalGutter * 3f)) / 4f
        val trailingSummaryLabel = closureSummaryLabel ?: context.getString(R.string.history_updated_at)
        val trailingSummaryValue = closureSummaryValue ?: formatPrintDateTimeLabel(detail.updatedAt)

        val primaryRowCells = listOf(
            measureSummaryCell(
                SummaryItem(
                    label = context.getString(R.string.history_print_status_label),
                    value = statusLabel(detail.status)
                ),
                primaryColumnWidth
            ),
            measureSummaryCell(
                SummaryItem(
                    label = context.getString(R.string.history_print_result_label),
                    value = resultLabel(detail.endResult)
                ),
                primaryColumnWidth
            ),
            measureSummaryCell(
                SummaryItem(
                    label = context.getString(R.string.history_detected_at),
                    value = formatPrintDateTimeLabel(detail.openedAt)
                ),
                primaryColumnWidth
            ),
            measureSummaryCell(
                SummaryItem(
                    label = trailingSummaryLabel,
                    value = trailingSummaryValue
                ),
                primaryColumnWidth
            )
        )
        val reasonCell = detail.cancellationReason
            ?.takeIf { it.isNotBlank() }
            ?.let { reasonText ->
                measureSummaryCell(
                    SummaryItem(
                        label = context.getString(R.string.history_cancellation_reason),
                        value = reasonText
                    ),
                    summaryWidth
                )
            }

        val primaryRowHeight = primaryRowCells.maxOf { it.height }
        val extraRows = listOfNotNull(reasonCell)
        val extraRowsHeight = extraRows.sumOf { it.height.toDouble() }.toFloat()
        val separatorCount = extraRows.size
        val panelHeight = panelInnerPadding * 2f +
            primaryRowHeight +
            extraRowsHeight +
            if (separatorCount > 0) {
                (verticalSpacing * separatorCount) + (separatorSpacing * separatorCount)
            } else {
                0f
            }

        ensureSpace(panelHeight)

        if (!countingOnly) {
            val panelRect = RectF(pageLeft, y, pageRight, y + panelHeight)
            drawRoundedPanel(panelRect, PrintSurface)

            var rowY = panelRect.top + panelInnerPadding
            drawSummaryColumnsRow(
                cells = primaryRowCells,
                startX = panelRect.left + panelInnerPadding,
                startY = rowY,
                gutter = horizontalGutter,
                rowHeight = primaryRowHeight
            )

            extraRows.forEach { extraCell ->
                rowY += primaryRowHeight.takeIf { rowY == panelRect.top + panelInnerPadding } ?: 0f
                rowY += verticalSpacing * 0.5f
                requireCanvas().drawLine(
                    panelRect.left + panelInnerPadding,
                    rowY,
                    panelRect.right - panelInnerPadding,
                    rowY,
                    linePaint
                )
                rowY += verticalSpacing * 0.5f + separatorSpacing
                drawMeasuredSummaryCell(
                    cell = extraCell,
                    startX = panelRect.left + panelInnerPadding,
                    startY = rowY
                )
                rowY += extraCell.height
            }
        }

        y += panelHeight
    }

    private fun drawTimelineSectionHeader(
        cancellationSignal: CancellationSignal,
        compact: Boolean
    ) {
        drawSingleLine(
            text = context.getString(R.string.history_timeline_title),
            paint = sectionTitlePaint,
            spacingAfter = if (compact) 3f else 2f
        )
        if (!compact) {
            drawMultilineText(
                text = context.getString(R.string.history_timeline_subtitle),
                paint = sectionSubtitlePaint,
                spacingAfter = 8f,
                cancellationSignal = cancellationSignal
            )
        }
        ensureSpace(1f)
        if (!countingOnly) {
            requireCanvas().drawLine(pageLeft, y, pageRight, y, linePaint)
        }
        y += if (compact) 8f else 10f
    }

    private fun drawTimelineEvent(
        item: MaintenanceTimelineEvent,
        isLast: Boolean,
        cancellationSignal: CancellationSignal
    ) {
        val dateLayout = createLayout(
            formatPrintDateTimeLabel(item.createdAt),
            timelineDatePaint,
            timelineContentWidth.toInt()
        )
        val titleLayout = createLayout(
            resolveText(context, item.title),
            timelineTitlePaint,
            timelineContentWidth.toInt()
        )
        val descriptionLayout: StaticLayout? = item.note
            ?.takeIf { it.isNotBlank() }
            ?.let { noteText ->
                resolveText(context, noteText)
            }
            ?.let { descriptionText ->
                createLayout(descriptionText, timelineBodyPaint, timelineContentWidth.toInt())
            }

        val blockHeight = 6f +
            dateLayout.height +
            4f +
            titleLayout.height +
            (descriptionLayout?.let { 6f + it.height } ?: 0f) +
            12f

        ensureSpace(blockHeight) {
            drawTimelineSectionHeader(cancellationSignal, compact = true)
        }

        if (!countingOnly) {
            val markerCenterY = y + 10f
            val guideEndY = if (isLast) {
                y + blockHeight - 2f
            } else {
                y + blockHeight + 10f
            }

            requireCanvas().drawCircle(timelineGuideX, markerCenterY, 3.2f, timelineMarkerPaint)
            if (!isLast) {
                requireCanvas().drawLine(
                    timelineGuideX,
                    markerCenterY + 6f,
                    timelineGuideX,
                    guideEndY,
                    timelineGuidePaint
                )
            }

            var contentY = y
            requireCanvas().withTranslation(timelineContentX, contentY) {
                dateLayout.draw(this)
            }
            contentY += dateLayout.height + 4f
            requireCanvas().withTranslation(timelineContentX, contentY) {
                titleLayout.draw(this)
            }
            descriptionLayout?.let {
                contentY += titleLayout.height + 6f
                requireCanvas().withTranslation(timelineContentX, contentY) {
                    it.draw(this)
                }
            }

            requireCanvas().drawLine(
                timelineContentX,
                y + blockHeight,
                pageRight,
                y + blockHeight,
                linePaint
            )
        }

        y += blockHeight + 10f
    }

    private fun drawSummaryColumnsRow(
        cells: List<MeasuredSummaryCell>,
        startX: Float,
        startY: Float,
        gutter: Float,
        rowHeight: Float
    ) {
        cells.forEachIndexed { index, cell ->
            val x = startX + index * (cell.width + gutter)
            drawMeasuredSummaryCell(cell, x, startY)
            if (index != cells.lastIndex) {
                val dividerX = x + cell.width + (gutter / 2f)
                requireCanvas().drawLine(
                    dividerX,
                    startY,
                    dividerX,
                    startY + rowHeight,
                    linePaint
                )
            }
        }
    }

    private fun drawMeasuredSummaryCell(
        cell: MeasuredSummaryCell,
        startX: Float,
        startY: Float
    ) {
        requireCanvas().withTranslation(startX, startY) {
            cell.labelLayout.draw(this)
        }
        requireCanvas().withTranslation(startX, startY + cell.labelLayout.height + 4f) {
            cell.valueLayout.draw(this)
        }
    }

    private fun drawSingleLine(
        text: String,
        paint: TextPaint,
        spacingAfter: Float = 0f
    ) {
        val layout = createLayout(text, paint, pageWidth.toInt())
        ensureSpace(layout.height.toFloat())
        if (!countingOnly) {
            requireCanvas().withTranslation(pageLeft, y) {
                layout.draw(this)
            }
        }
        y += layout.height + spacingAfter
    }

    private fun drawMultilineText(
        text: String,
        paint: TextPaint,
        spacingAfter: Float = 0f,
        cancellationSignal: CancellationSignal
    ) {
        var remaining = text.trim()
        while (remaining.isNotEmpty() && !cancellationSignal.isCanceled) {
            val availableHeight = (contentBottom - y).toInt().coerceAtLeast(1)
            val layout = createLayout(remaining, paint, pageWidth.toInt())
            if (layout.height <= availableHeight) {
                if (!countingOnly) {
                    requireCanvas().withTranslation(pageLeft, y) {
                        layout.draw(this)
                    }
                }
                y += layout.height + spacingAfter
                break
            }

            val fittingLines = fittingLineCount(layout, availableHeight)
            if (fittingLines <= 0) {
                startNewPage()
                continue
            }

            val endIndex = layout.getLineEnd(fittingLines - 1)
            val segment = remaining.substring(0, endIndex).trimEnd()
            val segmentLayout = createLayout(segment, paint, pageWidth.toInt())
            if (!countingOnly) {
                requireCanvas().withTranslation(pageLeft, y) {
                    segmentLayout.draw(this)
                }
            }
            y += segmentLayout.height + 2f
            remaining = remaining.substring(endIndex).trimStart()
            startNewPage()
        }
    }

    private fun startNewPage() {
        finishCurrentPage()
        currentPageNumber += 1
        if (!countingOnly) {
            currentPage = pdfDocument.startPage(currentPageNumber)
            canvas = currentPage?.canvas
            drawPageHeader()
        }
        y = contentTop
    }

    private fun finishCurrentPage() {
        if (currentPageNumber == 0 || countingOnly) return

        val page = currentPage ?: return
        drawPageFooter()
        pdfDocument.finishPage(page)
        currentPage = null
        canvas = null
    }

    private fun drawPageHeader() {
        val headerTop = pageTop + 3f
        val horizontalGap = 10f
        val metaHeight = 48f
        val statusHeight = 36f
        val statusWidth = (pageWidth * 0.17f).coerceIn(102f, 114f)
        val titleMinWidth = 170f
        val metaHorizontalInset = 7f
        val labelMeasurePaint = TextPaint(headerMetaLabelPaint)
        val dateMeasurePaint = TextPaint(headerInfoValuePaint)
        val codeMeasurePaint = TextPaint(headerCodeValuePaint)
        val metaContentWidth = max(
            max(
                labelMeasurePaint.measureText(context.getString(R.string.history_print_issue_date)),
                dateMeasurePaint.measureText(issueDateText)
            ),
            max(
                labelMeasurePaint.measureText(context.getString(R.string.history_case_code)),
                codeMeasurePaint.measureText(detail.caseCode)
            )
        )
        val maxMetaWidth = max(
            170f,
            pageWidth - statusWidth - horizontalGap * 2f - titleMinWidth
        )
        val metaWidth = (metaContentWidth + metaHorizontalInset * 2f + 4f)
            .coerceAtMost(maxMetaWidth)
            .coerceAtLeast(168f)
        val metaRect = RectF(pageLeft, headerTop, pageLeft + metaWidth, headerTop + metaHeight)
        val statusTop = headerTop + ((metaHeight - statusHeight) / 2f)
        val statusRect = RectF(pageRight - statusWidth, statusTop, pageRight, statusTop + statusHeight)
        val titleRect = RectF(
            metaRect.right + horizontalGap,
            headerTop + 4f,
            statusRect.left - horizontalGap,
            headerTop + metaHeight - 4f
        )
        val reportTitleText = context.getString(R.string.history_print_report_title)
        val fittedTitlePaint = fittedSingleLinePaint(
            basePaint = reportTitlePaint,
            text = reportTitleText,
            maxWidth = titleRect.width(),
            minSize = 12f
        )
        val titleBaseline = titleRect.centerY() -
            ((fittedTitlePaint.descent() + fittedTitlePaint.ascent()) / 2f)
        val statusValuePaint = TextPaint(headerStatusValuePaint).apply {
            color = statusAccentColor(detail.status)
        }

        if (!countingOnly) {
            drawRoundedPanel(metaRect, PrintSurface)
            drawHeaderMetaBlock(
                rect = metaRect,
                issueDateLabel = context.getString(R.string.history_print_issue_date),
                issueDateValue = issueDateText,
                codeLabel = context.getString(R.string.history_case_code),
                codeValue = detail.caseCode
            )

            drawRoundedPanel(
                rect = statusRect,
                fillColor = withAlpha(statusAccentColor(detail.status), 0.10f),
                strokeColor = withAlpha(statusAccentColor(detail.status), 0.28f)
            )
            drawHeaderStatusBlock(
                label = context.getString(R.string.history_print_status_label),
                value = statusLabel(detail.status),
                rect = statusRect,
                valuePaint = statusValuePaint
            )

            requireCanvas().drawText(
                reportTitleText,
                titleRect.centerX(),
                titleBaseline,
                fittedTitlePaint
            )

            val dividerY = pageTop + headerHeight
            requireCanvas().drawLine(pageLeft, dividerY, pageRight, dividerY, linePaint)
        }
    }

    private fun drawPageFooter() {
        val footerDividerY = contentBottom + 8f
        val footerBaseline = footerDividerY + 14f
        val logoSize = 16f
        val logoTop = footerDividerY + 2f
        val footerTitle =
            "${context.getString(R.string.app_name)} - ${context.getString(R.string.history_print_report_title)}"

        requireCanvas().drawLine(pageLeft, footerDividerY, pageRight, footerDividerY, linePaint)
        logoBitmap?.let { bitmap ->
            requireCanvas().drawBitmap(
                bitmap,
                null,
                RectF(pageLeft, logoTop, pageLeft + logoSize, logoTop + logoSize),
                null
            )
        }
        requireCanvas().drawText(
            TextUtils.ellipsize(
                footerTitle,
                footerTextPaint,
                pageWidth * 0.56f,
                TextUtils.TruncateAt.END
            ).toString(),
            pageLeft + if (logoBitmap != null) logoSize + 6f else 0f,
            footerBaseline,
            footerTextPaint
        )
        requireCanvas().drawText(
            context.getString(
                R.string.history_print_page_of,
                currentPageNumber,
                totalPages
            ),
            pageRight,
            footerBaseline,
            footerPagePaint
        )
    }

    private fun drawHeaderMetaBlock(
        rect: RectF,
        issueDateLabel: String,
        issueDateValue: String,
        codeLabel: String,
        codeValue: String
    ) {
        val horizontalInset = 7f
        val centerX = rect.centerX()
        val availableWidth = rect.width() - horizontalInset * 2f
        val labelPaint = TextPaint(headerMetaLabelPaint).apply {
            textAlign = Paint.Align.CENTER
        }
        val datePaint = fittedSingleLinePaint(
            basePaint = TextPaint(headerInfoValuePaint).apply {
                textAlign = Paint.Align.CENTER
            },
            text = issueDateValue,
            maxWidth = availableWidth,
            minSize = 7.8f
        )
        val codePaint = fittedSingleLinePaint(
            basePaint = TextPaint(headerCodeValuePaint).apply {
                textAlign = Paint.Align.CENTER
            },
            text = codeValue,
            maxWidth = availableWidth,
            minSize = 5.8f
        )
        val labelHeight = labelPaint.fontMetrics.run { bottom - top }
        val dateHeight = datePaint.fontMetrics.run { bottom - top }
        val codeHeight = codePaint.fontMetrics.run { bottom - top }
        val totalHeight = labelHeight + dateHeight + labelHeight + codeHeight + 5f
        var currentTop = rect.top + ((rect.height() - totalHeight) / 2f)

        val dateLabelBaseline = currentTop - labelPaint.fontMetrics.top
        requireCanvas().drawText(issueDateLabel, centerX, dateLabelBaseline, labelPaint)

        currentTop += labelHeight + 1f
        val dateValueBaseline = currentTop - datePaint.fontMetrics.top
        requireCanvas().drawText(issueDateValue, centerX, dateValueBaseline, datePaint)

        currentTop += dateHeight + 2f
        val codeLabelBaseline = currentTop - labelPaint.fontMetrics.top
        requireCanvas().drawText(codeLabel, centerX, codeLabelBaseline, labelPaint)

        currentTop += labelHeight + 1f
        val codeValueBaseline = currentTop - codePaint.fontMetrics.top
        requireCanvas().drawText(codeValue, centerX, codeValueBaseline, codePaint)
    }

    private fun drawHeaderStatusBlock(
        label: String,
        value: String,
        rect: RectF,
        valuePaint: TextPaint
    ) {
        val availableWidth = rect.width() - 16f
        val centerX = rect.centerX()
        val labelPaint = TextPaint(headerMetaLabelPaint).apply {
            textAlign = Paint.Align.CENTER
        }
        val fittedValuePaint = fittedSingleLinePaint(
            basePaint = TextPaint(valuePaint).apply {
                textAlign = Paint.Align.CENTER
            },
            text = value,
            maxWidth = availableWidth,
            minSize = 7.8f
        )
        val labelHeight = labelPaint.fontMetrics.run { bottom - top }
        val valueHeight = fittedValuePaint.fontMetrics.run { bottom - top }
        val totalHeight = labelHeight + valueHeight + 2f
        var currentTop = rect.top + ((rect.height() - totalHeight) / 2f)

        val labelBaseline = currentTop - labelPaint.fontMetrics.top
        requireCanvas().drawText(label, centerX, labelBaseline, labelPaint)

        currentTop += labelHeight + 2f
        val valueBaseline = currentTop - fittedValuePaint.fontMetrics.top
        requireCanvas().drawText(value, centerX, valueBaseline, fittedValuePaint)
    }

    private fun drawRoundedPanel(
        rect: RectF,
        fillColor: Int,
        strokeColor: Int = PrintDivider
    ) {
        panelFillPaint.color = fillColor
        panelStrokePaint.color = strokeColor
        requireCanvas().drawRoundRect(rect, panelRadius, panelRadius, panelFillPaint)
        requireCanvas().drawRoundRect(rect, panelRadius, panelRadius, panelStrokePaint)
    }

    private fun fittingLineCount(layout: StaticLayout, availableHeight: Int): Int {
        var lines = 0
        for (index in 0 until layout.lineCount) {
            if (layout.getLineBottom(index) > availableHeight) break
            lines++
        }
        return lines
    }

    private fun createLayout(
        text: String,
        paint: TextPaint,
        width: Int,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL
    ): StaticLayout = StaticLayout.Builder
        .obtain(text, 0, text.length, paint, max(1, width))
        .setAlignment(alignment)
        .setIncludePad(false)
        .build()

    private fun measureSummaryCell(
        item: SummaryItem,
        width: Float
    ): MeasuredSummaryCell {
        val labelLayout = createLayout(item.label, summaryLabelPaint, width.toInt())
        val valueLayout = createLayout(item.value, summaryValuePaint, width.toInt())
        return MeasuredSummaryCell(
            item = item,
            labelLayout = labelLayout,
            valueLayout = valueLayout,
            width = width,
            height = labelLayout.height + 4f + valueLayout.height
        )
    }

    private fun ensureSpace(
        requiredHeight: Float,
        onPageBreak: (() -> Unit)? = null
    ) {
        if (y + requiredHeight > contentBottom) {
            startNewPage()
            onPageBreak?.invoke()
        }
    }

    private fun resetState() {
        currentPageNumber = 0
        currentPage = null
        canvas = null
        y = contentTop
    }

    private fun requireCanvas(): Canvas = checkNotNull(canvas)

    private fun fittedSingleLinePaint(
        basePaint: TextPaint,
        text: String,
        maxWidth: Float,
        minSize: Float = 11.2f
    ): TextPaint {
        val fittedPaint = TextPaint(basePaint)
        while (fittedPaint.measureText(text) > maxWidth && fittedPaint.textSize > minSize) {
            fittedPaint.textSize -= 0.35f
        }
        return fittedPaint
    }

    private fun fittedMultilinePaint(
        basePaint: TextPaint,
        text: String,
        maxWidth: Float,
        maxLines: Int,
        minSize: Float = 7f,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL
    ): TextPaint {
        val fittedPaint = TextPaint(basePaint)
        while (
            createLayout(text, fittedPaint, maxWidth.toInt(), alignment).lineCount > maxLines &&
            fittedPaint.textSize > minSize
        ) {
            fittedPaint.textSize -= 0.25f
        }
        return fittedPaint
    }

    private fun resolveText(
        context: Context,
        rawText: String
    ): String =
        when (rawText) {
            "history_event_problem" -> context.getString(R.string.history_event_problem)
            "history_event_technician" -> context.getString(R.string.history_event_technician)
            "history_event_component" -> context.getString(R.string.history_event_component)
            "history_event_test" -> context.getString(R.string.history_event_test)
            "history_event_observation" -> context.getString(R.string.history_event_observation)
            "history_event_other" -> context.getString(R.string.history_event_other)
            "history_event_resolution" -> context.getString(R.string.history_event_resolution)
            "history_event_case_updated" -> context.getString(R.string.history_event_case_updated)
            "history_event_case_reopened" -> context.getString(R.string.history_event_case_reopened)
            "history_event_case_canceled" -> context.getString(R.string.history_event_case_canceled)
            else -> rawText
        }

    private fun statusLabel(status: MaintenanceStatus): String =
        when (status) {
            MaintenanceStatus.PENDING -> context.getString(R.string.history_status_pending)
            MaintenanceStatus.IN_PROGRESS -> context.getString(R.string.history_status_in_progress)
            MaintenanceStatus.FINALIZED -> context.getString(R.string.history_status_finalized)
            MaintenanceStatus.CANCELED -> context.getString(R.string.history_status_canceled)
        }

    private fun resultLabel(result: EndResult): String =
        when (result) {
            EndResult.RESOLVED -> context.getString(R.string.history_result_resolved)
            EndResult.NO_ISSUE -> context.getString(R.string.history_result_no_issue)
            EndResult.COMPONENT_FAULT -> context.getString(R.string.history_result_component_fault)
        }

    private fun statusAccentColor(status: MaintenanceStatus): Int =
        when (status) {
            MaintenanceStatus.PENDING -> PrintPending
            MaintenanceStatus.IN_PROGRESS -> PrintProgress
            MaintenanceStatus.FINALIZED -> PrintSuccess
            MaintenanceStatus.CANCELED -> PrintDanger
        }

}

private fun withAlpha(color: Int, alpha: Float): Int =
    ((alpha * 255).toInt().coerceIn(0, 255) shl 24) or (color and 0x00FFFFFF)

private fun formatPrintDateTimeLabel(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy - HH:mm 'hs'", Locale.ITALIAN)
    return formatter.format(Date(timestamp))
}
