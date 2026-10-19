package com.example.contruction_management_app.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.contruction_management_app.core.ui.theme.CmsTheme
import com.example.contruction_management_app.core.ui.theme.CmsTokens

@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CmsTheme.shapes.pill)
            .background(CmsTheme.colors.card)
            .border(1.dp, CmsTheme.colors.border, CmsTheme.shapes.pill)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex
            val interactionSource = remember { MutableInteractionSource() }

            val segmentBgColor by animateColorAsState(
                targetValue = if (isSelected) CmsTheme.colors.accent else CmsTheme.colors.card,
                animationSpec = tween(CmsTokens.Motion.DurationQuick),
                label = "segmentBg"
            )

            val segmentTextColor by animateColorAsState(
                targetValue = if (isSelected) CmsTheme.colors.accentOn else CmsTheme.colors.textSecondary,
                animationSpec = tween(CmsTokens.Motion.DurationQuick),
                label = "segmentText"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CmsTheme.shapes.pill)
                    .background(segmentBgColor)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onOptionSelected(index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    style = CmsTheme.typography.button.copy(
                        color = segmentTextColor,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                )
            }
        }
    }
}
