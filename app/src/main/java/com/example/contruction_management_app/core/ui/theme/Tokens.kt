package com.example.contruction_management_app.core.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Single source of truth for the "Dark Industrial Console" design language tokens.
 * Verify and adjust any values in this file only.
 */
object CmsTokens {

    object Colors {
        // Charcoal surfaces layered by subtle elevation
        val Background = Color(0xFF0F0F10)
        val Surface = Color(0xFF171718)             // bottom bar, sheets
        val Card = Color(0xFF1F1F20)                // standard card
        val CardElevated = Color(0xFF262627)        // selected/pressed/nested tile
        val Border = Color(0xFF2E2E30)              // 1px card outline
        val Divider = Color(0xFF2A2A2C)

        // Typography colors
        val TextPrimary = Color(0xFFFFFFFF)
        val TextSecondary = Color(0xFFA0A0A6)
        val TextTertiary = Color(0xFF6E6E74)

        // Signature orange accent (strictly reserved for active, primary, selected states)
        val Accent = Color(0xFFF26B1D)
        val AccentPressed = Color(0xFFD95A10)
        val AccentTint = Color(0x26F26B1D)          // ~15% alpha for selected chip backgrounds
        val AccentOn = Color(0xFFFFFFFF)

        // Semantic status colors
        val Success = Color(0xFF3DDC84)             // positive delta, completed
        val Warning = Color(0xFFF5B031)             // near deadline, low stock, pending sync
        val Error = Color(0xFFFF5A4F)               // blocked, overdue, failed
        val Info = Color(0xFF4DA3FF)

        // Charts & Toggles
        val ChartMutedBar = Color(0xFF4A4A4D)       // gray segment of stacked bars
        val ToggleOffTrack = Color(0xFF3A3A3C)
    }

    object Gradients {
        // Very subtle card surface depth
        val CardSurface = Brush.verticalGradient(
            colors = listOf(Color(0xFF232324), Color(0xFF1B1B1C))
        )
        // Primary FAB and primary accent button
        val AccentButton = Brush.verticalGradient(
            colors = listOf(Color(0xFFF57A2E), Color(0xFFE85F12))
        )
    }

    object Shapes {
        val RadiusCard: Dp = 20.dp
        val RadiusTile: Dp = 16.dp
        val RadiusChip: Dp = 14.dp
        val RadiusPill: Dp = 999.dp
        val RadiusIconTile: Dp = 12.dp
        val RadiusPhoneImage: Dp = 20.dp
    }

    object Strokes {
        val CardBorder: Dp = 1.dp
        val SelectedChip: Dp = 1.dp
        val UnderlineTab: Dp = 2.dp
        val ProgressBarThin: Dp = 3.dp
    }

    object Spacing {
        val ScreenPadding: Dp = 16.dp
        val CardPadding: Dp = 14.dp
        val GridGap: Dp = 12.dp
        val SectionGap: Dp = 24.dp
        val SectionTitleToContent: Dp = 12.dp
        val MinTouchTarget: Dp = 48.dp
    }

    object Motion {
        const val DurationQuick: Int = 150
        const val DurationStandard: Int = 250
        const val DurationEmphasized: Int = 400
        val EasingFastOutSlowIn: Easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
        const val PressScaleFactor: Float = 0.97f
    }
}
