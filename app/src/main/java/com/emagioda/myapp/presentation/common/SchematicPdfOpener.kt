package com.emagioda.myapp.presentation.common

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.emagioda.myapp.domain.model.SchematicDocument
import java.io.File
import java.io.IOException

object SchematicPdfOpener {
    private const val CACHE_DIR_NAME = "shared_pdfs"

    fun open(context: Context, document: SchematicDocument): Boolean {
        val cachedFile = try {
            cacheAssetPdf(context, document)
        } catch (_: IOException) {
            return false
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cachedFile
        )

        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            clipData = ClipData.newUri(context.contentResolver, document.title, uri)
        }

        return try {
            context.startActivity(viewIntent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }

    @Throws(IOException::class)
    private fun cacheAssetPdf(context: Context, document: SchematicDocument): File {
        val cacheDir = File(context.cacheDir, CACHE_DIR_NAME).apply {
            if (!exists()) {
                mkdirs()
            }
        }
        val extension = document.assetPath.substringAfterLast('.', "pdf")
        val cachedFile = File(cacheDir, "${document.id}.$extension")

        context.assets.open(document.assetPath).use { input ->
            cachedFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return cachedFile
    }
}
