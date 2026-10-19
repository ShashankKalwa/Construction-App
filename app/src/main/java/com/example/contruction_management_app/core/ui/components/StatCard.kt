package com.example.contruction_management_app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.contruction_management_app.core.ui.theme.CmsTheme
import com.example.contruction_management_app.core.ui.theme.CmsTokens

@Composable
fun StatCard(
    label: String,
    value: String,
    unit: String = "",
    icon: ImageVector,
    deltaText: String? = null,
    isPositiveDelta: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(CmsTheme.shapes.card)
            .background(CmsTheme.colors.card)
            .border(
                width = CmsTokens.Strokes.CardBorder,
                color = CmsTheme.colors.border,
                shape = CmsTheme.shapes.card
            )
            .padding(CmsTheme.spacing.cardPadding)
    ) {
        // Label left, 32dp icon tile top-right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = CmsTheme.typography.cardSubtitle
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CmsTheme.shapes.iconTile)
                    .background(CmsTheme.colors.cardElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CmsTheme.colors.accent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Large stat value with unit
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                style = CmsTheme.typography.statValue
            )
            if (unit.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = CmsTheme.typography.statUnit,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }

        // Delta row
        if (deltaText != null) {
            Spacer(modifier = Modifier.height(8.dp))
            val deltaColor = if (isPositiveDelta) CmsTheme.colors.success else CmsTheme.colors.error
            val deltaIcon = if (isPositiveDelta) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = deltaIcon,
                    contentDescription = null,
                    tint = deltaColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = deltaText,
                    style = CmsTheme.typography.labelSmall.copy(color = deltaColor)
                )
            }
        }
    }
}
