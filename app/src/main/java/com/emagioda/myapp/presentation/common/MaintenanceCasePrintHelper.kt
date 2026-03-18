package com.emagioda.myapp.presentation.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
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
    private val issueDateText = formatPrintDateTime(issueTimestamp)
    private val logoBitmap: Bitmap? = BitmapFactory.decodeResource(
        context.resources,
        R.drawable.splash_logo
    )

    private val contentRect = pdfDocument.pageContentRect
    private val pageLeft = contentRect.left.toFloat() + 20f
    private val pageRight = contentRect.right.toFloat() - 20f
    private val pageTop = contentRect.top.toFloat() + 16f
    private val pageBottom = contentRect.bottom.toFloat() - 12f
    private val pageWidth = pageRight - pageLeft
    private val headerHeight = 54f
    private val footerHeight = 20f
    private val contentTop = pageTop + headerHeight + 10f
    private val contentBottom = pageBottom - footerHeight

    private val brandPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#16202C")
        textSize = 13f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    private val headerTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4B5563")
        textSize = 9.5f
    }
    private val headerMetaPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#374151")
        textSize = 8.8f
        textAlign = Paint.Align.RIGHT
    }
    private val machinePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#16202C")
        textSize = 13.5f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    private val machineCodePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6B7280")
        textSize = 9f
    }
    private val sectionLabelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6B7280")
        textSize = 8.8f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    private val problemPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#16202C")
        textSize = 17f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    private val sectionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#16202C")
        textSize = 13.2f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    private val summaryLabelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6B7280")
        textSize = 9.2f
    }
    private val summaryValuePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1F2937")
        textSize = 9.8f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    private val timelineTypePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#16202C")
        textSize = 10.6f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    private val timelineDatePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6B7280")
        textSize = 8.8f
        textAlign = Paint.Align.RIGHT
    }
    private val timelineDescriptionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#243244")
        textSize = 9.8f
    }
    private val chipTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 8.4f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    private val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6B7280")
        textSize = 8.5f
        textAlign = Paint.Align.CENTER
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E4E7EC")
        strokeWidth = 1f
    }
    private val chipPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val problemTitle = resolveDisplayText(
        context,
        detail.problemSummary?.takeIf { it.isNotBlank() } ?: detail.diagnosisTitle
    )

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
        drawMachineBlock(cancellationSignal)
        drawSummarySection(cancellationSignal)
        drawTimelineSectionHeader(cancellationSignal)

        detail.events.forEach { event ->
            if (cancellationSignal.isCanceled) return
            drawTimelineRow(event)
        }
    }

    private fun drawMachineBlock(cancellationSignal: CancellationSignal) {
        val chipSpecs = listOf(
            statusLabel(detail.status) to statusChipColors(detail.status),
            resultLabel(detail.endResult) to resultChipColors(detail.endResult)
        )
        val chipMetrics = measureChipRow(chipSpecs)
        val leftColumnWidth = (pageWidth - chipMetrics.first - 18f).coerceAtLeast(pageWidth * 0.52f)
        val machineLayout = createLayout(detail.machineNameSnapshot, machinePaint, leftColumnWidth.toInt())
        val machineCodeLayout = createLayout(detail.machineId, machineCodePaint, leftColumnWidth.toInt())
        val infoBlockHeight = machineLayout.height + 2f + machineCodeLayout.height
        val headerBandHeight = max(infoBlockHeight.toFloat(), chipMetrics.second)

        ensureSpace(headerBandHeight + 8f)

        if (!countingOnly) {
            val canvas = requireCanvas()
            canvas.save()
            canvas.translate(pageLeft, y)
            machineLayout.draw(canvas)
            canvas.restore()

            canvas.save()
            canvas.translate(pageLeft, y + machineLayout.height + 2f)
            machineCodeLayout.draw(canvas)
            canvas.restore()

            drawChipRowAt(
                startX = pageRight - chipMetrics.first,
                startY = y,
                values = chipSpecs
            )
        }

        y += headerBandHeight + 8f
        drawSingleLine(context.getString(R.string.history_event_problem), sectionLabelPaint, spacingAfter = 4f)
        drawMultilineText(
            text = problemTitle,
            paint = problemPaint,
            spacingAfter = 8f,
            cancellationSignal = cancellationSignal
        )
        y += 6f
    }

    private fun drawSummarySection(cancellationSignal: CancellationSignal) {
        drawSingleLine(
            text = context.getString(R.string.history_summary_title),
            paint = sectionPaint,
            spacingAfter = 4f
        )

        drawSummaryRow(
            label = context.getString(R.string.history_case_code),
            value = detail.caseCode,
            cancellationSignal = cancellationSignal
        )
        drawSummaryRow(
            label = context.getString(R.string.history_detected_at),
            value = formatPrintDateTime(detail.openedAt),
            cancellationSignal = cancellationSignal
        )
        drawSummaryRow(
            label = context.getString(R.string.history_updated_at),
            value = formatPrintDateTime(detail.updatedAt),
            cancellationSignal = cancellationSignal
        )
        detail.resolvedAt?.let {
            drawSummaryRow(
                label = context.getString(R.string.history_resolved_at),
                value = formatPrintDateTime(it),
                cancellationSignal = cancellationSignal
            )
        }
        detail.canceledAt?.let {
            drawSummaryRow(
                label = context.getString(R.string.history_canceled_at),
                value = formatPrintDateTime(it),
                cancellationSignal = cancellationSignal
            )
        }
        detail.cancellationReason?.takeIf { it.isNotBlank() }?.let {
            drawSummaryRow(
                label = context.getString(R.string.history_cancellation_reason),
                value = it,
                cancellationSignal = cancellationSignal
            )
        }
        y += 8f
    }

    private fun drawTimelineSectionHeader(cancellationSignal: CancellationSignal) {
        drawSingleLine(
            text = context.getString(R.string.history_timeline_title),
            paint = sectionPaint,
            spacingAfter = 2f
        )
        drawMultilineText(
            text = context.getString(R.string.history_timeline_subtitle),
            paint = summaryLabelPaint,
            spacingAfter = 8f,
            cancellationSignal = cancellationSignal
        )
        ensureSpace(1f)
        if (!countingOnly) {
            requireCanvas().drawLine(pageLeft, y, pageRight, y, linePaint)
        }
        y += 6f
    }

    private fun drawTimelineRow(item: MaintenanceTimelineEvent) {
        val eventTitle = resolveDisplayText(context, item.title)
        val eventDate = formatPrintDateTime(item.createdAt)
        val description = item.note
            ?.takeIf { it.isNotBlank() }
            ?.let { resolveDisplayText(context, it) }
            .orEmpty()
        val dateWidth = max(128f, timelineDatePaint.measureText(eventDate))
        val typeWidth = max(120f, pageWidth * 0.34f)
        val descriptionWidth = (pageWidth - typeWidth - dateWidth - 16f).coerceAtLeast(110f)
        val typeText = TextUtils.ellipsize(
            eventTitle,
            timelineTypePaint,
            typeWidth,
            TextUtils.TruncateAt.END
        ).toString()
        val descriptionLayout = createLayout(
            text = description.ifBlank { "-" },
            paint = timelineDescriptionPaint,
            width = descriptionWidth.toInt()
        )
        val topLineHeight = max(
            timelineTypePaint.fontMetrics.run { bottom - top },
            timelineDatePaint.fontMetrics.run { bottom - top }
        )
        val rowHeight = max(topLineHeight + 4f + descriptionLayout.height, 28f) + 8f

        ensureSpace(rowHeight)

        if (!countingOnly) {
            val canvas = requireCanvas()
            val rowTop = y
            val typeBaseline = rowTop - timelineTypePaint.fontMetrics.top
            val dateBaseline = rowTop - timelineDatePaint.fontMetrics.top
            val descriptionX = pageLeft + typeWidth + 12f

            canvas.drawText(typeText, pageLeft, typeBaseline, timelineTypePaint)
            canvas.drawText(eventDate, pageRight, dateBaseline, timelineDatePaint)

            canvas.save()
            canvas.translate(descriptionX, rowTop + topLineHeight + 4f)
            descriptionLayout.draw(canvas)
            canvas.restore()

            canvas.drawLine(
                pageLeft,
                rowTop + rowHeight,
                pageRight,
                rowTop + rowHeight,
                linePaint
            )
        }

        y += rowHeight + 4f
    }

    private fun drawSummaryRow(
        label: String,
        value: String,
        cancellationSignal: CancellationSignal
    ) {
        val labelWidth = pageWidth * 0.28f
        val valueWidth = (pageWidth - labelWidth - 12f).coerceAtLeast(140f)
        val labelLayout = createLayout(label, summaryLabelPaint, labelWidth.toInt())
        val valueLayout = createLayout(value, summaryValuePaint, valueWidth.toInt())
        val rowHeight = max(labelLayout.height, valueLayout.height).toFloat() + 6f

        ensureSpace(rowHeight)

        if (!countingOnly) {
            val canvas = requireCanvas()
            canvas.save()
            canvas.translate(pageLeft, y)
            labelLayout.draw(canvas)
            canvas.restore()

            canvas.save()
            canvas.translate(pageLeft + labelWidth + 12f, y)
            valueLayout.draw(canvas)
            canvas.restore()

            canvas.drawLine(pageLeft, y + rowHeight, pageRight, y + rowHeight, linePaint)
        }

        y += rowHeight + if (cancellationSignal.isCanceled) 0f else 2f
    }

    private fun drawSingleLine(
        text: String,
        paint: TextPaint,
        spacingAfter: Float = 0f
    ) {
        val layout = createLayout(text, paint, pageWidth.toInt())
        ensureSpace(layout.height.toFloat())
        if (!countingOnly) {
            val canvas = requireCanvas()
            canvas.save()
            canvas.translate(pageLeft, y)
            layout.draw(canvas)
            canvas.restore()
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
                    val canvas = requireCanvas()
                    canvas.save()
                    canvas.translate(pageLeft, y)
                    layout.draw(canvas)
                    canvas.restore()
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
                val canvas = requireCanvas()
                canvas.save()
                canvas.translate(pageLeft, y)
                segmentLayout.draw(canvas)
                canvas.restore()
            }
            y += segmentLayout.height + 2f
            remaining = remaining.substring(endIndex).trimStart()
            startNewPage()
        }
    }

    private fun drawChipRow(values: List<Pair<String, Pair<Int, Int>>>) {
        var chipX = pageLeft
        var chipY = y
        val chipHeight = 18f

        values.forEach { (label, colors) ->
            val chipWidth = chipTextPaint.measureText(label) + 18f
            if (chipX + chipWidth > pageRight) {
                chipX = pageLeft
                chipY += chipHeight + 6f
            }
            ensureSpace((chipY - y) + chipHeight)

            if (!countingOnly) {
                chipPaint.color = colors.first
                val rect = RectF(chipX, chipY, chipX + chipWidth, chipY + chipHeight)
                requireCanvas().drawRoundRect(rect, 999f, 999f, chipPaint)
                chipTextPaint.color = colors.second
                val textBaseline = chipY + (chipHeight / 2f) -
                    ((chipTextPaint.descent() + chipTextPaint.ascent()) / 2f)
                requireCanvas().drawText(label, chipX + 9f, textBaseline, chipTextPaint)
            }

            chipX += chipWidth + 6f
        }

        y = chipY + chipHeight
    }

    private fun drawChipRowAt(
        startX: Float,
        startY: Float,
        values: List<Pair<String, Pair<Int, Int>>>
    ) {
        var chipX = startX
        val chipHeight = 18f

        values.forEach { (label, colors) ->
            val chipWidth = chipTextPaint.measureText(label) + 18f
            if (!countingOnly) {
                chipPaint.color = colors.first
                val rect = RectF(chipX, startY, chipX + chipWidth, startY + chipHeight)
                requireCanvas().drawRoundRect(rect, 999f, 999f, chipPaint)
                chipTextPaint.color = colors.second
                val textBaseline = startY + (chipHeight / 2f) -
                    ((chipTextPaint.descent() + chipTextPaint.ascent()) / 2f)
                requireCanvas().drawText(label, chipX + 9f, textBaseline, chipTextPaint)
            }
            chipX += chipWidth + 6f
        }
    }

    private fun measureChipRow(values: List<Pair<String, Pair<Int, Int>>>): Pair<Float, Float> {
        if (values.isEmpty()) return 0f to 0f
        val totalWidth = values.sumOf { (label, _) ->
            (chipTextPaint.measureText(label) + 18f).toDouble()
        }.toFloat() + (6f * (values.size - 1))
        return totalWidth to 18f
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
        if (currentPageNumber == 0) return

        if (!countingOnly) {
            val page = currentPage ?: return
            val canvas = canvas ?: return
            canvas.drawText(
                context.getString(
                    R.string.history_print_page_of,
                    currentPageNumber,
                    totalPages
                ),
                contentRect.exactCenterX(),
                pageBottom,
                footerPaint
            )
            pdfDocument.finishPage(page)
            currentPage = null
            this.canvas = null
        }
    }

    private fun drawPageHeader() {
        val canvas = requireCanvas()
        val logoLeft = pageLeft
        val logoTop = pageTop
        val logoSize = 28f
        logoBitmap?.let { bitmap ->
            canvas.drawBitmap(
                bitmap,
                null,
                RectF(logoLeft, logoTop, logoLeft + logoSize, logoTop + logoSize),
                null
            )
        }

        val textStart = if (logoBitmap != null) logoLeft + logoSize + 10f else logoLeft
        val brandBaseline = logoTop - brandPaint.fontMetrics.top
        val subtitleBaseline = brandBaseline + 13f
        canvas.drawText(
            context.getString(R.string.app_name),
            textStart,
            brandBaseline,
            brandPaint
        )
        canvas.drawText(
            context.getString(R.string.history_print_report_title),
            textStart,
            subtitleBaseline,
            headerTitlePaint
        )

        val issueLine = "${context.getString(R.string.history_print_issue_date)}: $issueDateText"
        val codeLine = "${context.getString(R.string.history_case_code)}: ${detail.caseCode}"
        val issueBaseline = logoTop - headerMetaPaint.fontMetrics.top
        val codeBaseline = issueBaseline + 13f
        val maxMetaWidth = (pageWidth * 0.48f).toFloat()

        canvas.drawText(
            TextUtils.ellipsize(issueLine, headerMetaPaint, maxMetaWidth, TextUtils.TruncateAt.END)
                .toString(),
            pageRight,
            issueBaseline,
            headerMetaPaint
        )
        canvas.drawText(
            TextUtils.ellipsize(codeLine, headerMetaPaint, maxMetaWidth, TextUtils.TruncateAt.END)
                .toString(),
            pageRight,
            codeBaseline,
            headerMetaPaint
        )

        val dividerY = pageTop + headerHeight
        canvas.drawLine(pageLeft, dividerY, pageRight, dividerY, linePaint)
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
        width: Int
    ): StaticLayout = StaticLayout.Builder
        .obtain(text, 0, text.length, paint, max(1, width))
        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        .setIncludePad(false)
        .build()

    private fun ensureSpace(requiredHeight: Float) {
        if (y + requiredHeight > contentBottom) {
            startNewPage()
        }
    }

    private fun resetState() {
        currentPageNumber = 0
        currentPage = null
        canvas = null
        y = contentTop
    }

    private fun requireCanvas(): Canvas = checkNotNull(canvas)

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

    private fun statusChipColors(status: MaintenanceStatus): Pair<Int, Int> =
        when (status) {
            MaintenanceStatus.PENDING -> Color.parseColor("#A76B00") to Color.WHITE
            MaintenanceStatus.IN_PROGRESS -> Color.parseColor("#1D5FA7") to Color.WHITE
            MaintenanceStatus.FINALIZED -> Color.parseColor("#2C8B57") to Color.WHITE
            MaintenanceStatus.CANCELED -> Color.parseColor("#B23A3A") to Color.WHITE
        }

    private fun resultChipColors(result: EndResult): Pair<Int, Int> =
        when (result) {
            EndResult.RESOLVED -> Color.parseColor("#2C8B57") to Color.WHITE
            EndResult.NO_ISSUE -> Color.parseColor("#A76B00") to Color.WHITE
            EndResult.COMPONENT_FAULT -> Color.parseColor("#B23A3A") to Color.WHITE
        }
}

private fun formatPrintDateTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy - HH:mm 'hs'", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
