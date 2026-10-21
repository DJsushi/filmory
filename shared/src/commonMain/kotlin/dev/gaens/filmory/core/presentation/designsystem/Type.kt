package dev.gaens.filmory.core.presentation.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Base = Typography()

internal val FilmoryTypography = Typography(
    headlineLarge = TextStyle(
        fontSize = 36.sp,
        lineHeight = 42.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.3).sp,
    ),
    headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(
        fontSize = 24.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.4.sp,
    ),
    titleLarge = Base.titleLarge,
    titleSmall = Base.titleSmall,
    bodySmall = Base.bodySmall,
    labelSmall = Base.labelSmall,
    displayLarge = Base.displayLarge,
    displayMedium = Base.displayMedium,
    displaySmall = Base.displaySmall,
)
