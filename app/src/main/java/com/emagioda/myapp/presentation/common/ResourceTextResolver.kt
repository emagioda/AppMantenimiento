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

@DrawableRes
fun resolveDrawableResId(name: String?): Int? =
    when (name) {
        "img_p_01" -> R.drawable.img_p_01
        "img_p_02" -> R.drawable.img_p_02
        "img_p_04" -> R.drawable.img_p_04
        "img_p_06" -> R.drawable.img_p_06
        "img_p_07" -> R.drawable.img_p_07
        "img_p_08" -> R.drawable.img_p_08
        "img_p_09" -> R.drawable.img_p_09
        "img_p_10" -> R.drawable.img_p_10
        "img_p_11" -> R.drawable.img_p_11
        "img_p_12" -> R.drawable.img_p_12
        "img_p_13" -> R.drawable.img_p_13
        "img_p_15" -> R.drawable.img_p_15
        "img_p_16" -> R.drawable.img_p_16
        "img_p_17" -> R.drawable.img_p_17
        "img_p_18" -> R.drawable.img_p_18
        "img_p_19" -> R.drawable.img_p_19
        "img_p_20" -> R.drawable.img_p_20
        "img_p_21" -> R.drawable.img_p_21
        "img_p_22" -> R.drawable.img_p_22
        "img_p_23" -> R.drawable.img_p_23
        "img_p_24" -> R.drawable.img_p_24
        "img_p_25" -> R.drawable.img_p_25
        "silo_mv18" -> R.drawable.silo_mv18
        else -> null
    }

fun resolveDisplayText(context: Context, rawText: String): String {
    val resId = displayTextResourcesByName[rawText] ?: return rawText
    return context.getString(resId)
}
