package com.example.contruction_management_app.feature.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.contruction_management_app.core.ui.components.ActionTile
import com.example.contruction_management_app.core.ui.components.CmsStatus
import com.example.contruction_management_app.core.ui.components.EmptyStateView
import com.example.contruction_management_app.core.ui.components.ErrorStateView
import com.example.contruction_management_app.core.ui.components.LoadingStateView
import com.example.contruction_management_app.core.ui.components.StatCard
import com.example.contruction_management_app.core.ui.components.StatusBadge
import com.example.contruction_management_app.core.ui.components.UnderlineTabs
import com.example.contruction_management_app.core.ui.theme.CmsTheme
import com.example.contruction_management_app.core.ui.theme.CmsTokens
import com.example.contruction_management_app.domain.model.ProjectDetail
import com.example.contruction_management_app.domain.model.ProjectMember
import com.example.contruction_management_app.feature.task.PhasesAndTasksTab
import com.example.contruction_management_app.feature.task.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: String,
    projectViewModel: ProjectViewModel,
    taskViewModel: TaskViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val detailState by projectViewModel.detailState.collectAsState()

    LaunchedEffect(projectId) {
        projectViewModel.loadProjectDetail(projectId)
    }

    val tabs = listOf("Overview", "Phases & Tasks", "Team", "Details")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CmsTheme.colors.surface)
    ) {
        when (val state = detailState) {
            is ProjectDetailUiState.Loading -> {
                LoadingStateView(itemCount = 4)
            }
            is ProjectDetailUiState.Error -> {
                ErrorStateView(
                    message = state.message,
                    onRetryClick = { projectViewModel.loadProjectDetail(projectId) }
                )
            }
            is ProjectDetailUiState.Success -> {
                val project = state.project
                val status = when (project.status.lowercase()) {
                    "completed" -> CmsStatus.Completed
                    "on hold", "on_hold" -> CmsStatus.OnHold
                    "planning", "draft" -> CmsStatus.Draft
                    else -> CmsStatus.Active
                }

                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CmsTheme.colors.textPrimary
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = project.name,
                            style = CmsTheme.typography.screenTitle,
                            maxLines = 1
                        )
                        Text(
                            text = "${project.code} • ${project.location}",
                            style = CmsTheme.typography.cardSubtitle,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(status = status)
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Underline Tabs Navigation
                UnderlineTabs(
                    tabs = tabs,
                    selectedTabIndex = state.selectedTabIndex,
                    onTabSelected = { projectViewModel.setDetailTab(it) }
                )

                // Tab Content
                when (state.selectedTabIndex) {
                    0 -> OverviewTabContent(
                        project = project,
                        onSwitchToTasks = { projectViewModel.setDetailTab(1) }
                    )
                    1 -> PhasesAndTasksTab(
                        projectId = projectId,
                        viewModel = taskViewModel
                    )
                    2 -> TeamTabContent(members = project.members)
                    3 -> ProjectMetaDetailsTabContent(project = project)
                }
            }
        }
    }
}

@Composable
private fun OverviewTabContent(
    project: ProjectDetail,
    onSwitchToTasks: () -> Unit
) {
    val estimatedSpent = (project.budgetInr.toDouble() * (project.progressPct / 100.0) * 0.9)
    val formattedSpent = when {
        estimatedSpent >= 10000000.0 -> "₹%.2f Cr".format(estimatedSpent / 10000000.0)
        estimatedSpent >= 100000.0 -> "₹%.2f L".format(estimatedSpent / 100000.0)
        else -> "₹%,.0f".format(estimatedSpent)
    }

    val completedTasks = if (project.tasksCount > 0) {
        ((project.progressPct / 100.0) * project.tasksCount).toInt()
    } else 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = CmsTheme.spacing.screenPadding,
            end = CmsTheme.spacing.screenPadding,
            top = 16.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Financial & Progress StatCards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CmsTheme.spacing.gridGap)
            ) {
                StatCard(
                    label = "Project Budget",
                    value = project.budgetDisplay,
                    icon = Icons.Outlined.CurrencyRupee,
                    deltaText = "${(project.progressPct * 0.9).toInt()}% utilized",
                    isPositiveDelta = project.progressPct <= 85.0,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Spent Account",
                    value = formattedSpent,
                    icon = Icons.Outlined.CurrencyRupee,
                    deltaText = "INR Account",
                    isPositiveDelta = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Overall Progress Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CmsTheme.shapes.card)
                    .background(CmsTheme.colors.card)
                    .border(CmsTokens.Strokes.CardBorder, CmsTheme.colors.border, CmsTheme.shapes.card)
                    .padding(CmsTheme.spacing.cardPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Overall Construction Progress",
                            style = CmsTheme.typography.cardTitle
                        )
                        Text(
                            text = "$completedTasks of ${project.tasksCount} milestones completed",
                            style = CmsTheme.typography.cardSubtitle
                        )
                    }

                    Text(
                        text = "${project.progressPct.toInt()}%",
                        style = CmsTheme.typography.statValue.copy(color = CmsTheme.colors.accent)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { (project.progressPct / 100.0).toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CmsTheme.shapes.pill),
                    color = CmsTheme.colors.accent,
                    trackColor = CmsTheme.colors.cardElevated
                )
            }
        }

        // Key Milestones & Phases Breakdown Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CmsTheme.shapes.card)
                    .background(CmsTheme.colors.card)
                    .border(CmsTokens.Strokes.CardBorder, CmsTheme.colors.border, CmsTheme.shapes.card)
                    .padding(CmsTheme.spacing.cardPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Phases & Milestones",
                        style = CmsTheme.typography.sectionTitle
                    )
                    Text(
                        text = "View Breakdown (${project.phasesCount})",
                        style = CmsTheme.typography.labelSmall.copy(color = CmsTheme.colors.accent),
                        modifier = Modifier.clickable { onSwitchToTasks() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CmsTheme.shapes.tile)
                        .background(CmsTheme.colors.cardElevated)
                        .clickable { onSwitchToTasks() }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${project.phasesCount} Construction Phases Active",
                            style = CmsTheme.typography.cardTitle
                        )
                        Text(
                            text = "Tap to view stages, sub-tasks, and track completion",
                            style = CmsTheme.typography.cardSubtitle
                        )
                    }
                    Text(
                        text = "Open →",
                        style = CmsTheme.typography.cardTitle.copy(color = CmsTheme.colors.accent)
                    )
                }
            }
        }

        // Quick Actions for this site
        item {
            Text(
                text = "Site Execution Actions",
                style = CmsTheme.typography.sectionTitle
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionTile(
                    label = "Site Report",
                    icon = Icons.Outlined.Description,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                ActionTile(
                    label = "Indents",
                    icon = Icons.Outlined.Inventory2,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                ActionTile(
                    label = "Milestones",
                    icon = Icons.Outlined.Engineering,
                    onClick = { onSwitchToTasks() },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TeamTabContent(members: List<ProjectMember>) {
    if (members.isEmpty()) {
        EmptyStateView(
            title = "No Members Assigned",
            message = "Team members assigned to this construction site will appear here."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = CmsTheme.spacing.screenPadding,
                end = CmsTheme.spacing.screenPadding,
                top = 16.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Project Team & Personnel (${members.size})",
                    style = CmsTheme.typography.sectionTitle
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(members, key = { it.id }) { member ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CmsTheme.shapes.tile)
                        .background(CmsTheme.colors.card)
                        .border(CmsTokens.Strokes.CardBorder, CmsTheme.colors.border, CmsTheme.shapes.tile)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar circle
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CmsTheme.colors.accent.copy(alpha = 0.15f))
                            .border(1.dp, CmsTheme.colors.accent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.user.fullName.take(2).uppercase(),
                            style = CmsTheme.typography.cardTitle.copy(color = CmsTheme.colors.accent)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = member.user.fullName,
                            style = CmsTheme.typography.cardTitle
                        )
                        Text(
                            text = member.assignedRole ?: "Team Member",
                            style = CmsTheme.typography.cardSubtitle.copy(color = CmsTheme.colors.accent)
                        )
                        Text(
                            text = member.user.email,
                            style = CmsTheme.typography.labelSmall.copy(color = CmsTheme.colors.textSecondary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectMetaDetailsTabContent(project: ProjectDetail) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = CmsTheme.spacing.screenPadding,
            end = CmsTheme.spacing.screenPadding,
            top = 16.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Site & Contract Specifications",
                style = CmsTheme.typography.sectionTitle
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            MetaDetailCard(label = "Project Full Title", value = project.name)
        }
        item {
            MetaDetailCard(label = "Site Code", value = project.code)
        }
        item {
            MetaDetailCard(label = "Location / Geo Coordinates", value = project.location)
        }
        item {
            MetaDetailCard(label = "Total Approved Budget", value = "${project.budgetDisplay} (INR ${project.budgetInr})")
        }
        item {
            MetaDetailCard(label = "Operating Status", value = project.status)
        }
        item {
            MetaDetailCard(label = "Phases Count", value = "${project.phasesCount} Construction Phases")
        }
        item {
            MetaDetailCard(label = "Total Work Items", value = "${project.tasksCount} Tasks Registered")
        }
    }
}

@Composable
private fun MetaDetailCard(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CmsTheme.shapes.tile)
            .background(CmsTheme.colors.card)
            .border(CmsTokens.Strokes.CardBorder, CmsTheme.colors.border, CmsTheme.shapes.tile)
            .padding(14.dp)
    ) {
        Text(
            text = label,
            style = CmsTheme.typography.cardSubtitle
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = CmsTheme.typography.cardTitle
        )
    }
}
