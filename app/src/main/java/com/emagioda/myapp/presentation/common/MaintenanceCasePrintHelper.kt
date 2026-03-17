package com.emagioda.myapp.presentation.common

import android.content.Context
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
        val jobName = "${context.getString(R.string.history_case_detail_title)} - ${detail.machineId}"

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
    private val contentRect = pdfDocument.pageContentRect
    private val left = contentRect.left.toFloat() + 36f
    private val right = contentRect.right.toFloat() - 36f
    private val top = contentRect.top.toFloat() + 34f
    private val bottom = contentRect.bottom.toFloat() - 28f
    private val footerHeight = 24f
    private val contentBottom = bottom - footerHeight
    private val contentWidth = right - left

    private val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#18202B")
        textSize = 22f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    private val machinePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#243244")
        textSize = 15f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    private val captionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6B7280")
        textSize = 10f
    }
    private val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#243244")
        textSize = 12f
    }
    private val valuePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#18202B")
        textSize = 12f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    private val sectionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#18202B")
        textSize = 16f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    private val timelineTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#18202B")
        textSize = 13f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    private val timelineDatePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#5C6470")
        textSize = 10f
        textAlign = Paint.Align.RIGHT
    }
    private val timelineBodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#243244")
        textSize = 11.5f
    }
    private val chipTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 10f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    private val chipPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E3E7EB")
        strokeWidth = 1f
    }
    private val timelineCardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F6F7F9")
        style = Paint.Style.FILL
    }
    private val timelineCardStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E3E7EB")
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }
    private val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#7B8190")
        textSize = 10f
        textAlign = Paint.Align.CENTER
    }

    private val problemTitle = resolveDisplayText(
        context,
        detail.problemSummary?.takeIf { it.isNotBlank() } ?: detail.diagnosisTitle
    )
    private val diagnosisDescription = detail.diagnosisDescription
        ?.takeIf { it.isNotBlank() }
        ?.let { resolveDisplayText(context, it) }

    private var currentPageNumber = 0
    private var currentPage: android.graphics.pdf.PdfDocument.Page? = null
    private var canvas: Canvas? = null
    private var y = top

    fun render(cancellationSignal: CancellationSignal) {
        startNewPage()

        drawDocumentHeader(cancellationSignal)
        drawSummarySection(cancellationSignal)
        drawTimelineSectionHeader(cancellationSignal)
        detail.events.forEach { event ->
            if (cancellationSignal.isCanceled) return
            drawTimelineItem(event)
        }

        finishCurrentPage()
    }

    private fun drawDocumentHeader(cancellationSignal: CancellationSignal) {
        drawMultilineText(
            text = context.getString(R.string.history_case_detail_title),
            paint = captionPaint,
            spacingAfter = 8f,
            cancellationSignal = cancellationSignal
        )
        drawMultilineText(
            text = problemTitle,
            paint = titlePaint,
            spacingAfter = 10f,
            cancellationSignal = cancellationSignal
        )
        drawSingleLine(detail.machineNameSnapshot, machinePaint, spacingAfter = 4f)
        drawSingleLine(detail.machineId, captionPaint, spacingAfter = 12f)
        drawChipRow(
            values = listOf(
                statusLabel(detail.status) to statusChipColors(detail.status),
                resultLabel(detail.endResult) to resultChipColors(detail.endResult)
            )
        )
        diagnosisDescription?.let {
            y += 12f
            drawMultilineText(
                text = it,
                paint = bodyPaint,
                spacingAfter = 16f,
                cancellationSignal = cancellationSignal
            )
        } ?: run {
            y += 16f
        }
    }

    private fun drawSummarySection(cancellationSignal: CancellationSignal) {
        drawSingleLine(
            context.getString(R.string.history_summary_title),
            sectionPaint,
            spacingAfter = 10f
        )

        drawSummaryRow(
            label = context.getString(R.string.history_sheet_status),
            value = statusLabel(detail.status),
            cancellationSignal = cancellationSignal
        )
        drawSummaryRow(
            label = context.getString(R.string.history_result_label),
            value = resultLabel(detail.endResult),
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
        y += 16f
    }

    private fun drawTimelineSectionHeader(cancellationSignal: CancellationSignal) {
        drawSingleLine(
            context.getString(R.string.history_timeline_title),
            sectionPaint,
            spacingAfter = 6f
        )
        drawMultilineText(
            text = context.getString(R.string.history_timeline_subtitle),
            paint = captionPaint,
            spacingAfter = 12f,
            cancellationSignal = cancellationSignal
        )
    }

    private fun drawTimelineItem(item: MaintenanceTimelineEvent) {
        val resolvedTitle = resolveDisplayText(context, item.title)
        val resolvedNote = item.note
            ?.takeIf { it.isNotBlank() }
            ?.let { resolveDisplayText(context, it) }
            .orEmpty()
        val dateText = formatPrintDateTime(item.createdAt)
        val innerPadding = 14f
        val dateWidth = max(150f, timelineDatePaint.measureText(dateText))
        val titleWidth = max(80f, contentWidth - innerPadding * 2f - dateWidth - 12f)
        val headerHeight = max(
            timelineTitlePaint.fontMetrics.run { bottom - top },
            timelineDatePaint.fontMetrics.run { bottom - top }
        )
        val noteLayout = createLayout(
            text = resolvedNote.ifBlank { "-" },
            paint = timelineBodyPaint,
            width = (contentWidth - innerPadding * 2f).toInt()
        )
        val blockHeight = innerPadding * 2f + headerHeight + 10f + noteLayout.height

        ensureSpace(blockHeight)

        val canvas = requireCanvas()
        val top = y
        val rect = RectF(left, top, right, top + blockHeight)
        canvas.drawRoundRect(rect, 16f, 16f, timelineCardPaint)
        canvas.drawRoundRect(rect, 16f, 16f, timelineCardStrokePaint)

        val titleBaseline = top + innerPadding - timelineTitlePaint.fontMetrics.top
        val dateBaseline = top + innerPadding - timelineDatePaint.fontMetrics.top
        val titleText = TextUtils.ellipsize(
            resolvedTitle,
            timelineTitlePaint,
            titleWidth,
            TextUtils.TruncateAt.END
        ).toString()
        canvas.drawText(titleText, left + innerPadding, titleBaseline, timelineTitlePaint)
        canvas.drawText(dateText, right - innerPadding, dateBaseline, timelineDatePaint)

        canvas.save()
        canvas.translate(left + innerPadding, top + innerPadding + headerHeight + 10f)
        noteLayout.draw(canvas)
        canvas.restore()

        y = rect.bottom + 12f
    }

    private fun drawChipRow(values: List<Pair<String, Pair<Int, Int>>>) {
        var chipX = left
        var chipY = y
        val maxRowWidth = right
        val chipHeight = 22f

        values.forEach { (label, colors) ->
            val textWidth = chipTextPaint.measureText(label)
            val chipWidth = textWidth + 24f
            if (chipX + chipWidth > maxRowWidth) {
                chipX = left
                chipY += chipHeight + 8f
            }
            ensureSpace((chipY - y) + chipHeight)
            chipPaint.color = colors.first
            val rect = RectF(chipX, chipY, chipX + chipWidth, chipY + chipHeight)
            requireCanvas().drawRoundRect(rect, 999f, 999f, chipPaint)
            chipTextPaint.color = colors.second
            val textBaseline = chipY + (chipHeight / 2f) - ((chipTextPaint.descent() + chipTextPaint.ascent()) / 2f)
            requireCanvas().drawText(label, chipX + 12f, textBaseline, chipTextPaint)
            chipX += chipWidth + 8f
        }

        y = chipY + chipHeight
    }

    private fun drawSummaryRow(
        label: String,
        value: String,
        cancellationSignal: CancellationSignal
    ) {
        val labelWidth = contentWidth * 0.34f
        val valueWidth = contentWidth - labelWidth - 14f
        val labelLayout = createLayout(label, captionPaint, labelWidth.toInt())
        val valueLayout = createLayout(value, valuePaint, valueWidth.toInt())
        val rowHeight = max(labelLayout.height, valueLayout.height).toFloat() + 14f

        ensureSpace(rowHeight)

        val canvas = requireCanvas()
        val rowTop = y
        canvas.drawLine(left, rowTop + rowHeight - 1f, right, rowTop + rowHeight - 1f, linePaint)

        canvas.save()
        canvas.translate(left, rowTop + 2f)
        labelLayout.draw(canvas)
        canvas.restore()

        canvas.save()
        canvas.translate(left + labelWidth + 14f, rowTop + 2f)
        valueLayout.draw(canvas)
        canvas.restore()

        y += rowHeight
        if (!cancellationSignal.isCanceled) {
            y += 2f
        }
    }

    private fun drawSingleLine(
        text: String,
        paint: TextPaint,
        spacingAfter: Float = 0f
    ) {
        val layout = createLayout(text, paint, contentWidth.toInt())
        ensureSpace(layout.height.toFloat())
        val canvas = requireCanvas()
        canvas.save()
        canvas.translate(left, y)
        layout.draw(canvas)
        canvas.restore()
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
            val layout = createLayout(remaining, paint, contentWidth.toInt())
            if (layout.height <= availableHeight) {
                val canvas = requireCanvas()
                canvas.save()
                canvas.translate(left, y)
                layout.draw(canvas)
                canvas.restore()
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
            val segmentLayout = createLayout(segment, paint, contentWidth.toInt())
            val canvas = requireCanvas()
            canvas.save()
            canvas.translate(left, y)
            segmentLayout.draw(canvas)
            canvas.restore()
            y += segmentLayout.height + 4f
            remaining = remaining.substring(endIndex).trimStart()
            startNewPage()
        }
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

    private fun startNewPage() {
        finishCurrentPage()
        currentPageNumber += 1
        currentPage = pdfDocument.startPage(currentPageNumber)
        canvas = currentPage?.canvas
        y = top
    }

    private fun finishCurrentPage() {
        val page = currentPage ?: return
        val canvas = canvas ?: return
        canvas.drawText(
            context.getString(R.string.history_print_page, currentPageNumber),
            contentRect.exactCenterX(),
            bottom,
            footerPaint
        )
        pdfDocument.finishPage(page)
        currentPage = null
        this.canvas = null
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
