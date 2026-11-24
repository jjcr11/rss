package com.jjcr11.rss.presentation.components

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ScreenSize(
    val width: Dp = 0.dp,
    val height: Dp = 0.dp,
) {
    val isCompact: Boolean get() = width < 600.dp
    val isMedium: Boolean get() = width in 600.dp..839.dp
    val isExpanded: Boolean get() = width >= 840.dp
    val isWide: Boolean get() = width >= 600.dp
}

val LocalScreenSize: ProvidableCompositionLocal<ScreenSize> = compositionLocalOf {
    ScreenSize()
}
