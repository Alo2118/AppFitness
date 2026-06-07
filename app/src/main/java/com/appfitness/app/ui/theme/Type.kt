package com.appfitness.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.appfitness.app.R

/** Modern geometric sans (Poppins) bundled for a consistent, contemporary look. */
val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
)

private val default = Typography()

/** Material 3 type scale rebuilt on Poppins with tighter, modern headings. */
val AppTypography = Typography(
    displayLarge = default.displayLarge.copyFont(FontWeight.Bold),
    displayMedium = default.displayMedium.copyFont(FontWeight.Bold),
    displaySmall = default.displaySmall.copyFont(FontWeight.Bold),
    headlineLarge = default.headlineLarge.copyFont(FontWeight.Bold, (-0.5).sp),
    headlineMedium = default.headlineMedium.copyFont(FontWeight.Bold, (-0.5).sp),
    headlineSmall = default.headlineSmall.copyFont(FontWeight.SemiBold, (-0.25).sp),
    titleLarge = default.titleLarge.copyFont(FontWeight.SemiBold),
    titleMedium = default.titleMedium.copyFont(FontWeight.SemiBold),
    titleSmall = default.titleSmall.copyFont(FontWeight.Medium),
    bodyLarge = default.bodyLarge.copyFont(FontWeight.Normal),
    bodyMedium = default.bodyMedium.copyFont(FontWeight.Normal),
    bodySmall = default.bodySmall.copyFont(FontWeight.Normal),
    labelLarge = default.labelLarge.copyFont(FontWeight.SemiBold),
    labelMedium = default.labelMedium.copyFont(FontWeight.Medium),
    labelSmall = default.labelSmall.copyFont(FontWeight.Medium),
)

private fun TextStyle.copyFont(
    weight: FontWeight,
    letterSpacing: androidx.compose.ui.unit.TextUnit = this.letterSpacing,
): TextStyle = copy(fontFamily = Poppins, fontWeight = weight, letterSpacing = letterSpacing)
