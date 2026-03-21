package io.github.helmy2.mudawama.designsystem

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * MudawamaTypography tokens as specified in design spec.
 */
data class MudawamaTypography(
    val h1: TextStyle = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.W700, fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = 0.sp),
    val h2: TextStyle = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.W600, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp),
    val h3: TextStyle = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.W600, fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = 0.sp),
    val h4: TextStyle = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.W600, fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.sp),
    val h5: TextStyle = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.W500, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.15.sp),
    val h6: TextStyle = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.W500, fontSize = 12.sp, lineHeight = 18.sp, letterSpacing = 0.15.sp),
    val body1: TextStyle = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.W400, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.25.sp),
    val body2: TextStyle = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.W400, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    val caption: TextStyle = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.W400, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    val button: TextStyle = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.W600, fontSize = 14.sp, lineHeight = 16.sp, letterSpacing = 1.25.sp)
)

// Local provider is declared in Theme.kt


