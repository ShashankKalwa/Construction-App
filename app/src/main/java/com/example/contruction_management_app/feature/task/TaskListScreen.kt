package com.example.contruction_management_app.feature.task

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.contruction_management_app.core.ui.components.EmptyStateView
import com.example.contruction_management_app.core.ui.theme.CmsTheme
import com.example.contruction_management_app.domain.model.User
import kotlinx.coroutines.flow.Flow

@Composable
fun TaskListScreen(
    activeProjectIdFlow: Flow<String?>,
    taskViewModel: TaskViewModel,
    user: User,
    onNavigateToProjects: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeProjectId by activeProjectIdFlow.collectAsState(initial = null)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CmsTheme.colors.surface)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CmsTheme.spacing.screenPadding, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Site Tasks & Milestones",
                    style = CmsTheme.typography.screenTitle
                )
                Text(
                    text = "Phase-by-phase execution tracking",
                    style = CmsTheme.typography.cardSubtitle
                )
            }
        }

        if (activeProjectId.isNullOrBlank()) {
            EmptyStateView(
                title = "No Project Selected",
                message = "Select an active project to view and update its construction phases and tasks.",
                actionLabel = "Browse Projects",
                onActionClick = onNavigateToProjects
            )
        } else {
            PhasesAndTasksTab(
                projectId = activeProjectId!!,
                viewModel = taskViewModel
            )
        }
    }
}
