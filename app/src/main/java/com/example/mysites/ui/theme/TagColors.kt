package com.example.mysites.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.mysites.data.TAG_COLOR_COUNT

private val TagColorHues = listOf(
    8, 24, 42, 62, 82, 102, 122, 146,
    166, 188, 208, 226, 248, 272, 302, 336,
)

fun tagColor(index: Int): Color {
    val safeIndex = index.coerceIn(0, TAG_COLOR_COUNT - 1)
    return Color.hsl(TagColorHues[safeIndex].toFloat(), 0.68f, 0.52f)
}

fun tagOnColor(index: Int): Color {
    return if (tagColor(index).luminance() > 0.179f) {
        Color(0xFF1A1C1E)
    } else {
        Color.White
    }
}
