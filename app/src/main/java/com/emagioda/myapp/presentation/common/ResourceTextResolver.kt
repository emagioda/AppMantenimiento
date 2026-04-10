package com.emagioda.myapp.presentation.common

import android.content.Context
import androidx.annotation.DrawableRes
import com.emagioda.myapp.R

private val displayTextResourcesByName = mapOf(
    "history_event_problem" to R.string.history_event_problem,
    "history_event_technician" to R.string.history_event_technician,
    "history_event_component" to R.string.history_event_component,
    "history_event_test" to R.string.history_event_test,
    "history_event_observation" to R.string.history_event_observation,
    "history_event_other" to R.string.history_event_other,
    "history_event_resolution" to R.string.history_event_resolution,
    "history_event_case_updated" to R.string.history_event_case_updated,
    "history_event_case_reopened" to R.string.history_event_case_reopened,
    "history_event_case_canceled" to R.string.history_event_case_canceled
)

private val drawableResourcesByName: Map<String, Int> by lazy(LazyThreadSafetyMode.NONE) {
    R.drawable::class.java.fields.associate { field ->
        field.name to field.getInt(null)
    }
}

@DrawableRes
fun resolveDrawableResId(name: String?): Int? = name?.let(drawableResourcesByName::get)

fun resolveDisplayText(context: Context, rawText: String): String {
    val resId = displayTextResourcesByName[rawText] ?: return rawText
    return context.getString(resId)
}
