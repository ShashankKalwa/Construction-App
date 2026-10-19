package com.example.contruction_management_app.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.contruction_management_app.core.ui.theme.CmsTheme
import com.example.contruction_management_app.core.ui.theme.CmsTokens

@Composable
fun ConsumerListRow(
    name: String,
    value: String,
    percentage: Int,
    icon: ImageVector,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (percentage / 100f).coerceIn(0f, 1f),
        animationSpec = tween(CmsTokens.Motion.DurationEmphasized),
        label = "consumerProgress"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CmsTheme.shapes.tile)
            .background(CmsTheme.colors.card)
            .border(CmsTokens.Strokes.CardBorder, CmsTheme.colors.border, CmsTheme.shapes.tile)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon in rounded tile
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CmsTheme.shapes.iconTile)
                .background(CmsTheme.colors.cardElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CmsTheme.colors.accent,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Center: Name and thin 3dp accent progress bar
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = CmsTheme.typography.cardTitle
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CmsTokens.Strokes.ProgressBarThin)
                    .clip(CmsTheme.shapes.pill),
                color = CmsTheme.colors.accent,
                trackColor = CmsTheme.colors.cardElevated
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Right side: Value and percentage in text_secondary
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = value,
                style = CmsTheme.typography.cardTitle
            )
            Text(
                text = "$percentage%",
                style = CmsTheme.typography.labelSmall
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Chevron
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = CmsTheme.colors.textSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}
