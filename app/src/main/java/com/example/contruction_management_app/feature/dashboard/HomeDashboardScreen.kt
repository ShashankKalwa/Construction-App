package com.example.contruction_management_app.feature.dashboard

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.AssignmentLate
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.contruction_management_app.core.ui.components.ActionTile
import com.example.contruction_management_app.core.ui.components.CategoryChipItem
import com.example.contruction_management_app.core.ui.components.CategoryChipRow
import com.example.contruction_management_app.core.ui.components.CmsSecondaryButton
import com.example.contruction_management_app.core.ui.components.CmsStatus
import com.example.contruction_management_app.core.ui.components.ConsumerListRow
import com.example.contruction_management_app.core.ui.components.EmptyStateView
import com.example.contruction_management_app.core.ui.components.ErrorStateView
import com.example.contruction_management_app.core.ui.components.LoadingStateView
import com.example.contruction_management_app.core.ui.components.ProjectCard
import com.example.contruction_management_app.core.ui.components.ProjectSwitcher
import com.example.contruction_management_app.core.ui.components.StatCard
import com.example.contruction_management_app.core.ui.components.StatusBadge
import com.example.contruction_management_app.core.ui.theme.CmsTheme
import com.example.contruction_management_app.core.ui.theme.CmsTokens
import com.example.contruction_management_app.domain.model.Project
import com.example.contruction_management_app.domain.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
    viewModel: HomeDashboardViewModel,
    user: User,
    onNavigateToProjectDetail: (String) -> Unit,
    onNavigateToProjectsTab: () -> Unit,
    onNavigateToTasksTab: () -> Unit,
    onQuickActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showProjectSelector by remember { mutableStateOf(false) }

    when (val state = uiState) {
        is HomeDashboardUiState.Loading -> {
            LoadingStateView(modifier = modifier, itemCount = 4)
        }
        is HomeDashboardUiState.Error -> {
            ErrorStateView(
                message = state.message,
                onRetryClick = { viewModel.loadDashboard(forceRefresh = true) },
                modifier = modifier
            )
        }
        is HomeDashboardUiState.Success -> {
            val activeProject = state.activeProject
            val summary = state.summary
            val categoryIndex = when (state.selectedCategory) {
                "projects" -> 1
                "tasks" -> 2
                "materials" -> 3
                "finance" -> 4
                else -> 0
            }

            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                // Project Switcher
                item {
                    ProjectSwitcher(
                        currentProjectName = activeProject?.name ?: "All Active Sites",
                        onClick = { showProjectSelector = true }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Category Filter Chips
                item {
                    CategoryChipRow(
                        items = listOf(
                            CategoryChipItem("all", "All", Icons.Outlined.Category),
                            CategoryChipItem("projects", "Projects", Icons.Outlined.Apartment),
                            CategoryChipItem("tasks", "Tasks", Icons.Outlined.TaskAlt),
                            CategoryChipItem("materials", "Materials", Icons.Outlined.Inventory2),
                            CategoryChipItem("finance", "Finance", Icons.Outlined.CurrencyRupee)
                        ),
                        selectedIndex = categoryIndex,
                        onItemSelected = { idx ->
                            val cat = when (idx) {
                                1 -> "projects"
                                2 -> "tasks"
                                3 -> "materials"
                                4 -> "finance"
                                else -> "all"
                            }
                            viewModel.selectCategory(cat)
                        }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Summary StatCards: 2-column key metrics
                if (state.selectedCategory == "all" || state.selectedCategory == "finance") {
                    item {
                        val spentPct = (summary.overallProgressPct * 0.88).coerceIn(0.0, 100.0)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = CmsTheme.spacing.screenPadding),
                            horizontalArrangement = Arrangement.spacedBy(CmsTheme.spacing.gridGap)
                        ) {
                            StatCard(
                                label = "Project Budget",
                                value = summary.totalBudgetDisplay,
                                icon = Icons.Outlined.CurrencyRupee,
                                deltaText = "${spentPct.toInt()}% budget utilized",
                                isPositiveDelta = spentPct <= 85.0,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                label = "Overall Progress",
                                value = "${(summary.overallProgressPct).toInt()}%",
                                icon = Icons.Outlined.Engineering,
                                deltaText = if (summary.blockedTasksCount > 0) "${summary.blockedTasksCount} Blockers" else "On schedule",
                                isPositiveDelta = summary.blockedTasksCount == 0,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Task & Milestone Stats
                if (state.selectedCategory == "all" || state.selectedCategory == "tasks") {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = CmsTheme.spacing.screenPadding),
                            horizontalArrangement = Arrangement.spacedBy(CmsTheme.spacing.gridGap)
                        ) {
                            StatCard(
                                label = "Active Tasks",
                                value = "${summary.inProgressTasksCount}",
                                unit = "tasks",
                                icon = Icons.Outlined.TaskAlt,
                                deltaText = "${summary.completedTasksCount} completed",
                                isPositiveDelta = true,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                label = "Active Site",
                                value = activeProject?.name?.take(16) ?: "General Site",
                                icon = Icons.Outlined.Layers,
                                deltaText = "${(summary.overallProgressPct).toInt()}% complete",
                                isPositiveDelta = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                // Pinned Projects Section
                if (state.selectedCategory == "all" || state.selectedCategory == "projects") {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = CmsTheme.spacing.screenPadding),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Active Sites & Buildings",
                                style = CmsTheme.typography.sectionTitle
                            )
                            Text(
                                text = "View All (${state.projects.size})",
                                style = CmsTheme.typography.labelSmall.copy(color = CmsTheme.colors.accent),
                                modifier = Modifier.clickable { onNavigateToProjectsTab() }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (state.projects.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "No Projects Found",
                                message = "Get started by creating your first construction project."
                            )
                        }
                    } else {
                        // 2-column or list view of projects
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = CmsTheme.spacing.screenPadding),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                state.projects.take(3).forEach { project ->
                                    val status = when (project.status.lowercase()) {
                                        "completed" -> CmsStatus.Completed
                                        "on hold", "on_hold" -> CmsStatus.OnHold
                                        "planning", "draft" -> CmsStatus.Draft
                                        else -> CmsStatus.Active
                                    }

                                    ProjectCard(
                                        title = project.name,
                                        subtitle = "${project.code} • ${project.location} • ${project.budgetDisplay}",
                                        progress = (project.progressPct / 100.0).toFloat(),
                                        status = status,
                                        onClick = { onNavigateToProjectDetail(project.id) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }

                // Quick Actions Section
                if (state.selectedCategory == "all") {
                    item {
                        Text(
                            text = "Quick Actions",
                            style = CmsTheme.typography.sectionTitle,
                            modifier = Modifier.padding(horizontal = CmsTheme.spacing.screenPadding)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = CmsTheme.spacing.screenPadding),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ActionTile(
                                label = "Daily Report",
                                icon = Icons.Outlined.Description,
                                onClick = { onQuickActionClick("daily_report") },
                                modifier = Modifier.weight(1f)
                            )
                            ActionTile(
                                label = "Material Req",
                                icon = Icons.Outlined.Inventory2,
                                onClick = { onQuickActionClick("material_request") },
                                modifier = Modifier.weight(1f)
                            )
                            ActionTile(
                                label = "Tasks",
                                icon = Icons.Outlined.TaskAlt,
                                onClick = { onNavigateToTasksTab() },
                                modifier = Modifier.weight(1f)
                            )
                            ActionTile(
                                label = "Calculator",
                                icon = Icons.Outlined.Calculate,
                                onClick = { onQuickActionClick("calculation") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                // Material Consumption
                if (state.selectedCategory == "all" || state.selectedCategory == "materials") {
                    item {
                        Text(
                            text = "Key Material Consumption",
                            style = CmsTheme.typography.sectionTitle,
                            modifier = Modifier.padding(horizontal = CmsTheme.spacing.screenPadding)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = CmsTheme.spacing.screenPadding),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ConsumerListRow(
                                name = "OPC 53 Grade Cement",
                                value = "5,420 bags",
                                percentage = 78,
                                icon = Icons.Outlined.Inventory2
                            )
                            ConsumerListRow(
                                name = "TMT Steel Rebars (Fe 500)",
                                value = "42.0 MT",
                                percentage = 85,
                                icon = Icons.Outlined.Layers
                            )
                            ConsumerListRow(
                                name = "Ready-Mix Concrete (M25)",
                                value = "380 cu.m",
                                percentage = 62,
                                icon = Icons.Outlined.Engineering
                            )
                        }
                    }
                }
            }
        }
    }

    // Project Switcher Dialog
    if (showProjectSelector && uiState is HomeDashboardUiState.Success) {
        val successState = uiState as HomeDashboardUiState.Success
        BasicAlertDialog(
            onDismissRequest = { showProjectSelector = false }
        ) {
            Column(
                modifier = Modifier
                    .clip(CmsTheme.shapes.card)
                    .background(CmsTheme.colors.card)
                    .border(CmsTokens.Strokes.CardBorder, CmsTheme.colors.border, CmsTheme.shapes.card)
                    .padding(20.dp)
            ) {
                Text(
                    text = "Select Active Site / Project",
                    style = CmsTheme.typography.screenTitle
                )
                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    successState.projects.forEach { project ->
                        val isSelected = project.id == successState.activeProject?.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CmsTheme.shapes.tile)
                                .background(if (isSelected) CmsTheme.colors.cardElevated else CmsTheme.colors.card)
                                .border(
                                    1.dp,
                                    if (isSelected) CmsTheme.colors.accent else CmsTheme.colors.border,
                                    CmsTheme.shapes.tile
                                )
                                .clickable {
                                    viewModel.selectProject(project.id)
                                    showProjectSelector = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    viewModel.selectProject(project.id)
                                    showProjectSelector = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = CmsTheme.colors.accent,
                                    unselectedColor = CmsTheme.colors.textSecondary
                                )
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(
                                    text = project.name,
                                    style = CmsTheme.typography.cardTitle
                                )
                                Text(
                                    text = "${project.code} • ${project.location}",
                                    style = CmsTheme.typography.cardSubtitle
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                CmsSecondaryButton(
                    text = "Close",
                    onClick = { showProjectSelector = false },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
