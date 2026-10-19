package com.example.contruction_management_app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.contruction_management_app.core.ui.theme.CmsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccentSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    startIcon: ImageVector? = null,
    endIcon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (startIcon != null) {
            Icon(
                imageVector = startIcon,
                contentDescription = null,
                tint = CmsTheme.colors.textSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = CmsTheme.colors.accent,
                inactiveTrackColor = CmsTheme.colors.cardElevated
            ),
            thumb = {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.height(6.dp),
                    colors = SliderDefaults.colors(
                        activeTrackColor = CmsTheme.colors.accent,
                        inactiveTrackColor = CmsTheme.colors.cardElevated
                    )
                )
            }
        )

        if (endIcon != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = endIcon,
                contentDescription = null,
                tint = CmsTheme.colors.textSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
