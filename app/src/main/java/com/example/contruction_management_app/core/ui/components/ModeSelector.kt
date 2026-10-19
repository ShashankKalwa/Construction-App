package com.example.contruction_management_app.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.contruction_management_app.core.ui.theme.CmsTheme
import com.example.contruction_management_app.core.ui.theme.CmsTokens

data class ModeOption(
    val id: String,
    val title: String,
    val subtitle: String
)

@Composable
fun ModeSelector(
    options: List<ModeOption>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CmsTheme.spacing.gridGap)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex

            val bgColor by animateColorAsState(
                targetValue = if (isSelected) CmsTheme.colors.accent else CmsTheme.colors.card,
                animationSpec = tween(CmsTokens.Motion.DurationQuick),
                label = "modeBg"
            )

            val borderColor by animateColorAsState(
                targetValue = if (isSelected) CmsTheme.colors.accent else CmsTheme.colors.border,
                animationSpec = tween(CmsTokens.Motion.DurationQuick),
                label = "modeBorder"
            )

            val titleColor by animateColorAsState(
                targetValue = if (isSelected) CmsTheme.colors.accentOn else CmsTheme.colors.textPrimary,
                animationSpec = tween(CmsTokens.Motion.DurationQuick),
                label = "modeTitle"
            )

            val subtitleColor by animateColorAsState(
                targetValue = if (isSelected) CmsTheme.colors.accentOn.copy(alpha = 0.8f) else CmsTheme.colors.textSecondary,
                animationSpec = tween(CmsTokens.Motion.DurationQuick),
                label = "modeSubtitle"
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(CmsTheme.shapes.tile)
                    .background(bgColor)
                    .border(CmsTokens.Strokes.CardBorder, borderColor, CmsTheme.shapes.tile)
                    .clickable { onOptionSelected(index) }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = option.title,
                    style = CmsTheme.typography.cardTitle.copy(color = titleColor)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = option.subtitle,
                    style = CmsTheme.typography.labelSmall.copy(color = subtitleColor)
                )
            }
        }
    }
}
