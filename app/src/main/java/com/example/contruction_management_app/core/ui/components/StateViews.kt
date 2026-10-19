package com.example.contruction_management_app.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.contruction_management_app.core.ui.theme.CmsTheme
import com.example.contruction_management_app.core.ui.theme.CmsTokens

@Composable
fun ShimmerSkeletonBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = CmsTokens.Shapes.RadiusTile
) {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = listOf(
        CmsTheme.colors.card,
        CmsTheme.colors.cardElevated,
        CmsTheme.colors.card
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
            .border(CmsTokens.Strokes.CardBorder, CmsTheme.colors.border, RoundedCornerShape(cornerRadius))
    )
}

@Composable
fun LoadingStateView(
    modifier: Modifier = Modifier,
    itemCount: Int = 3
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(CmsTheme.spacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(CmsTheme.spacing.gridGap)
    ) {
        repeat(itemCount) {
            ShimmerSkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
            )
        }
    }
}

@Composable
fun EmptyStateView(
    title: String,
    message: String,
    icon: ImageVector = Icons.Outlined.Inbox,
    actionLabel: String? = null,
    onActionClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CmsTheme.shapes.tile)
                .background(CmsTheme.colors.card)
                .border(CmsTokens.Strokes.CardBorder, CmsTheme.colors.border, CmsTheme.shapes.tile),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CmsTheme.colors.textSecondary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = CmsTheme.typography.screenTitle,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = message,
            style = CmsTheme.typography.body.copy(color = CmsTheme.colors.textSecondary),
            textAlign = TextAlign.Center
        )

        if (actionLabel != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CmsTheme.colors.accent,
                    contentColor = CmsTheme.colors.accentOn
                ),
                shape = CmsTheme.shapes.pill,
                modifier = Modifier.height(44.dp)
            ) {
                Text(
                    text = actionLabel,
                    style = CmsTheme.typography.button
                )
            }
        }
    }
}

@Composable
fun ErrorStateView(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CmsTheme.shapes.tile)
                .background(CmsTheme.colors.error.copy(alpha = 0.1f))
                .border(CmsTokens.Strokes.CardBorder, CmsTheme.colors.error.copy(alpha = 0.3f), CmsTheme.shapes.tile),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = CmsTheme.colors.error,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Something went wrong",
            style = CmsTheme.typography.screenTitle,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = message,
            style = CmsTheme.typography.body.copy(color = CmsTheme.colors.textSecondary),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onRetryClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = CmsTheme.colors.accent,
                contentColor = CmsTheme.colors.accentOn
            ),
            shape = CmsTheme.shapes.pill,
            modifier = Modifier.height(44.dp)
        ) {
            Text(
                text = "Retry",
                style = CmsTheme.typography.button
            )
        }
    }
}
