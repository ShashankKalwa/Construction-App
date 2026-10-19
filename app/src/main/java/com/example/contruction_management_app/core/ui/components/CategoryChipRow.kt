package com.example.contruction_management_app.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
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

data class CategoryChipItem(
    val id: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun CategoryChipRow(
    items: List<CategoryChipItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = CmsTheme.spacing.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(CmsTheme.spacing.gridGap)
    ) {
        itemsIndexed(items) { index, item ->
            val isSelected = index == selectedIndex

            val bgColor by animateColorAsState(
                targetValue = if (isSelected) CmsTheme.colors.accentTint else CmsTheme.colors.card,
                animationSpec = tween(CmsTokens.Motion.DurationQuick),
                label = "chipBg"
            )

            val borderColor by animateColorAsState(
                targetValue = if (isSelected) CmsTheme.colors.accent else CmsTheme.colors.border,
                animationSpec = tween(CmsTokens.Motion.DurationQuick),
                label = "chipBorder"
            )

            val contentColor by animateColorAsState(
                targetValue = if (isSelected) CmsTheme.colors.accent else CmsTheme.colors.textSecondary,
                animationSpec = tween(CmsTokens.Motion.DurationQuick),
                label = "chipContent"
            )

            Column(
                modifier = Modifier
                    .size(width = 72.dp, height = 64.dp)
                    .clip(CmsTheme.shapes.tile)
                    .background(bgColor)
                    .border(width = 1.dp, color = borderColor, shape = CmsTheme.shapes.tile)
                    .clickable { onItemSelected(index) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.label,
                    style = CmsTheme.typography.labelSmall.copy(
                        color = contentColor,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
