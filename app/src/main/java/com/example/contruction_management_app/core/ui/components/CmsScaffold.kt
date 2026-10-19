package com.example.contruction_management_app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.contruction_management_app.core.ui.theme.CmsTheme

@Composable
fun CmsScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(CmsTheme.colors.background),
        containerColor = CmsTheme.colors.background,
        contentColor = CmsTheme.colors.textPrimary,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = {
            // Overlapping the bottom bar by ~16dp as specified in the master prompt
            Box(
                modifier = Modifier.offset(y = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                floatingActionButton()
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        contentWindowInsets = WindowInsets.systemBars,
        content = content
    )
}
