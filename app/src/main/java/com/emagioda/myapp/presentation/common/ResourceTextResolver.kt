package com.emagioda.myapp.presentation.common

import android.content.Context

fun resolveDisplayText(context: Context, rawText: String): String {
    val resId = context.resources.getIdentifier(rawText, "string", context.packageName)
    return if (resId != 0) context.getString(resId) else rawText
}
