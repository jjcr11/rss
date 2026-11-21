package com.jjcr11.rss.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jjcr11.rss.resources.Res
import com.jjcr11.rss.resources.inter_bold
import com.jjcr11.rss.resources.inter_medium
import com.jjcr11.rss.resources.inter_regular
import com.jjcr11.rss.resources.inter_semibold
import com.jjcr11.rss.resources.jetbrains_mono_medium
import com.jjcr11.rss.resources.jetbrains_mono_regular
import org.jetbrains.compose.resources.Font

@Composable
fun interFontFamily(): FontFamily = FontFamily(
    Font(
        resource = Res.font.inter_regular,
        weight = FontWeight.Normal,
    ),
    Font(
        resource = Res.font.inter_medium,
        weight = FontWeight.Medium,
    ),
    Font(
        resource = Res.font.inter_semibold,
        weight = FontWeight.SemiBold,
    ),
    Font(
        resource = Res.font.inter_bold,
        weight = FontWeight.Bold,
    ),
)

@Composable
fun jetBrainsMonoFontFamily(): FontFamily = FontFamily(
    Font(
        resource = Res.font.jetbrains_mono_regular,
        weight = FontWeight.Normal,
    ),
    Font(
        resource = Res.font.jetbrains_mono_medium,
        weight = FontWeight.Medium,
    ),
)

@Composable
fun rssTypography(): Typography {
    val inter = interFontFamily()
    val jetBrainsMono = jetBrainsMonoFontFamily()

    return Typography(
        displayLarge = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Bold,
            fontSize = 40.sp,
            lineHeight = 48.sp,
            letterSpacing = (-0.03).em,
        ),
        displayMedium = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.025).em,
        ),
        displaySmall = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            lineHeight = 32.sp,
            letterSpacing = (-0.02).em,
        ),
        headlineLarge = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.025).em,
        ),
        headlineMedium = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = (-0.02).em,
        ),
        headlineSmall = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            lineHeight = 22.sp,
            letterSpacing = (-0.015).em,
        ),
        titleLarge = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = (-0.02).em,
        ),
        titleMedium = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            lineHeight = 22.sp,
            letterSpacing = (-0.015).em,
        ),
        titleSmall = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            letterSpacing = (-0.005).em,
        ),
        bodyLarge = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            lineHeight = 30.sp,
            letterSpacing = (-0.01).em,
        ),
        bodyMedium = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 24.sp,
            letterSpacing = (-0.005).em,
        ),
        bodySmall = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.em,
        ),
        labelLarge = TextStyle(
            fontFamily = jetBrainsMono,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = (-0.01).em,
        ),
        labelMedium = TextStyle(
            fontFamily = jetBrainsMono,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.em,
        ),
        labelSmall = TextStyle(
            fontFamily = jetBrainsMono,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            letterSpacing = 0.02.em,
        ),
    )
}
