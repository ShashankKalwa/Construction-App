package com.example.contruction_management_app.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.contruction_management_app.core.ui.theme.CmsTheme
import com.example.contruction_management_app.core.ui.theme.CmsTokens

@Composable
fun ProjectCard(
    title: String,
    subtitle: String,
    progress: Float, // 0.0f to 1.0f
    icon: ImageVector = Icons.Outlined.Apartment,
    status: CmsStatus = CmsStatus.Active,
    onClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(CmsTokens.Motion.DurationEmphasized),
        label = "projectProgress"
    )

    Column(
        modifier = modifier
            .clip(CmsTheme.shapes.card)
            .background(CmsTheme.colors.card)
            .border(
                width = CmsTokens.Strokes.CardBorder,
                color = CmsTheme.colors.border,
                shape = CmsTheme.shapes.card
            )
            .clickable { onClick() }
            .padding(CmsTheme.spacing.cardPadding)
    ) {
        // Top row: thumbnail / icon tile + kebab menu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
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

            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Project options",
                    tint = CmsTheme.colors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title and Subtitle
        Text(
            text = title,
            style = CmsTheme.typography.cardTitle,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = CmsTheme.typography.cardSubtitle,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom row: Status badge and Progress ring
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusBadge(status = status)

            // Progress Ring with percentage
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(36.dp)
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(36.dp),
                    color = CmsTheme.colors.cardElevated,
                    strokeWidth = 3.dp
                )
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(36.dp),
                    color = CmsTheme.colors.accent,
                    strokeWidth = 3.dp
                )
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = CmsTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        color = CmsTheme.colors.textPrimary
                    )
                )
            }
        }
    }
}
