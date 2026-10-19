package com.example.contruction_management_app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.contruction_management_app.core.ui.theme.CmsTheme

enum class CmsStatus(val label: String) {
    Draft("Draft"),
    Active("Active"),
    OnHold("On Hold"),
    Completed("Completed"),
    Blocked("Blocked"),
    Overdue("Overdue"),
    PendingSync("Pending Sync"),
    Confirmed("Confirmed"),
    Failed("Failed")
}

@Composable
fun StatusBadge(
    status: CmsStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor) = when (status) {
        CmsStatus.Draft -> Triple(
            CmsTheme.colors.cardElevated,
            CmsTheme.colors.textSecondary,
            CmsTheme.colors.border
        )
        CmsStatus.Active -> Triple(
            CmsTheme.colors.accentTint,
            CmsTheme.colors.accent,
            CmsTheme.colors.accent.copy(alpha = 0.5f)
        )
        CmsStatus.OnHold, CmsStatus.PendingSync -> Triple(
            CmsTheme.colors.warning.copy(alpha = 0.15f),
            CmsTheme.colors.warning,
            CmsTheme.colors.warning.copy(alpha = 0.4f)
        )
        CmsStatus.Completed, CmsStatus.Confirmed -> Triple(
            CmsTheme.colors.success.copy(alpha = 0.15f),
            CmsTheme.colors.success,
            CmsTheme.colors.success.copy(alpha = 0.4f)
        )
        CmsStatus.Blocked, CmsStatus.Overdue, CmsStatus.Failed -> Triple(
            CmsTheme.colors.error.copy(alpha = 0.15f),
            CmsTheme.colors.error,
            CmsTheme.colors.error.copy(alpha = 0.4f)
        )
    }

    Box(
        modifier = modifier
            .background(color = bgColor, shape = CmsTheme.shapes.pill)
            .border(width = 1.dp, color = borderColor, shape = CmsTheme.shapes.pill)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.label,
            style = CmsTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                color = textColor,
                fontSize = 11.sp
            )
        )
    }
}
