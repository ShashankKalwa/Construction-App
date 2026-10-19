package com.example.contruction_management_app.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

@Immutable
data class CmsColors(
    val background: Color = CmsTokens.Colors.Background,
    val surface: Color = CmsTokens.Colors.Surface,
    val card: Color = CmsTokens.Colors.Card,
    val cardElevated: Color = CmsTokens.Colors.CardElevated,
    val border: Color = CmsTokens.Colors.Border,
    val divider: Color = CmsTokens.Colors.Divider,
    val textPrimary: Color = CmsTokens.Colors.TextPrimary,
    val textSecondary: Color = CmsTokens.Colors.TextSecondary,
    val textTertiary: Color = CmsTokens.Colors.TextTertiary,
    val accent: Color = CmsTokens.Colors.Accent,
    val accentPressed: Color = CmsTokens.Colors.AccentPressed,
    val accentTint: Color = CmsTokens.Colors.AccentTint,
    val accentOn: Color = CmsTokens.Colors.AccentOn,
    val success: Color = CmsTokens.Colors.Success,
    val warning: Color = CmsTokens.Colors.Warning,
    val error: Color = CmsTokens.Colors.Error,
    val info: Color = CmsTokens.Colors.Info,
    val chartMutedBar: Color = CmsTokens.Colors.ChartMutedBar,
    val toggleOffTrack: Color = CmsTokens.Colors.ToggleOffTrack
)

@Immutable
data class CmsShapes(
    val card: Shape = RoundedCornerShape(CmsTokens.Shapes.RadiusCard),
    val tile: Shape = RoundedCornerShape(CmsTokens.Shapes.RadiusTile),
    val chip: Shape = RoundedCornerShape(CmsTokens.Shapes.RadiusChip),
    val pill: Shape = RoundedCornerShape(CmsTokens.Shapes.RadiusPill),
    val iconTile: Shape = RoundedCornerShape(CmsTokens.Shapes.RadiusIconTile)
)

@Immutable
data class CmsSpacing(
    val screenPadding: Dp = CmsTokens.Spacing.ScreenPadding,
    val cardPadding: Dp = CmsTokens.Spacing.CardPadding,
    val gridGap: Dp = CmsTokens.Spacing.GridGap,
    val sectionGap: Dp = CmsTokens.Spacing.SectionGap,
    val sectionTitleToContent: Dp = CmsTokens.Spacing.SectionTitleToContent,
    val minTouchTarget: Dp = CmsTokens.Spacing.MinTouchTarget
)

val LocalCmsColors = staticCompositionLocalOf { CmsColors() }
val LocalCmsTypography = staticCompositionLocalOf { CmsTypography() }
val LocalCmsShapes = staticCompositionLocalOf { CmsShapes() }
val LocalCmsSpacing = staticCompositionLocalOf { CmsSpacing() }

object CmsTheme {
    val colors: CmsColors
        @Composable
        get() = LocalCmsColors.current

    val typography: CmsTypography
        @Composable
        get() = LocalCmsTypography.current

    val shapes: CmsShapes
        @Composable
        get() = LocalCmsShapes.current

    val spacing: CmsSpacing
        @Composable
        get() = LocalCmsSpacing.current
}

/**
 * Standard Material 3 darkColorScheme mapped to CmsTokens
 */
private val MaterialDarkColorScheme = darkColorScheme(
    primary = CmsTokens.Colors.Accent,
    onPrimary = CmsTokens.Colors.AccentOn,
    primaryContainer = CmsTokens.Colors.AccentTint,
    onPrimaryContainer = CmsTokens.Colors.Accent,
    secondary = CmsTokens.Colors.CardElevated,
    onSecondary = CmsTokens.Colors.TextPrimary,
    background = CmsTokens.Colors.Background,
    onBackground = CmsTokens.Colors.TextPrimary,
    surface = CmsTokens.Colors.Surface,
    onSurface = CmsTokens.Colors.TextPrimary,
    surfaceVariant = CmsTokens.Colors.Card,
    onSurfaceVariant = CmsTokens.Colors.TextSecondary,
    outline = CmsTokens.Colors.Border,
    outlineVariant = CmsTokens.Colors.Divider,
    error = CmsTokens.Colors.Error,
    onError = Color.White
)

/**
 * Main application theme wrapper. Forces Dark Industrial Console style, disables dynamic colors.
 */
@Composable
fun CmsAppTheme(
    colors: CmsColors = CmsColors(),
    typography: CmsTypography = CmsTypography(),
    shapes: CmsShapes = CmsShapes(),
    spacing: CmsSpacing = CmsSpacing(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalCmsColors provides colors,
        LocalCmsTypography provides typography,
        LocalCmsShapes provides shapes,
        LocalCmsSpacing provides spacing
    ) {
        MaterialTheme(
            colorScheme = MaterialDarkColorScheme,
            content = content
        )
    }
}
