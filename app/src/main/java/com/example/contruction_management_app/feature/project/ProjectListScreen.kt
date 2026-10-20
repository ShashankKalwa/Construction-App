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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.contruction_management_app.core.ui.components.CmsStatus
import com.example.contruction_management_app.core.ui.components.CmsTextField
import com.example.contruction_management_app.core.ui.components.EmptyStateView
import com.example.contruction_management_app.core.ui.components.ErrorStateView
import com.example.contruction_management_app.core.ui.components.LoadingStateView
import com.example.contruction_management_app.core.ui.components.ProjectCard
import com.example.contruction_management_app.core.ui.theme.CmsTheme
import com.example.contruction_management_app.core.ui.theme.CmsTokens
import com.example.contruction_management_app.domain.model.Project
import com.example.contruction_management_app.domain.model.User

@Composable
fun ProjectListScreen(
    viewModel: ProjectViewModel,
    user: User,
    onProjectClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState by viewModel.listState.collectAsState()
    var showCreateSheet by remember { mutableStateOf(false) }
    var createError by remember { mutableStateOf<String?>(null) }

    val filterOptions = listOf(
        "ALL" to "All Sites",
        "ACTIVE" to "Active",
        "PLANNING" to "Planning",
        "ON_HOLD" to "On Hold",
        "COMPLETED" to "Completed"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = CmsTheme.spacing.screenPadding)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Sites & Projects",
                    style = CmsTheme.typography.screenTitle
                )
                Text(
                    text = "Active workspace environments",
                    style = CmsTheme.typography.cardSubtitle
                )
            }

            // Quick Add Project Button
            Button(
                onClick = { showCreateSheet = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CmsTheme.colors.accent,
                    contentColor = CmsTheme.colors.accentOn
                ),
                shape = CmsTheme.shapes.pill,
                modifier = Modifier.height(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "New Site",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "New Site",
                    style = CmsTheme.typography.button
                )
            }
        }

        when (val state = listState) {
            is ProjectListUiState.Loading -> {
                LoadingStateView(itemCount = 4)
            }
            is ProjectListUiState.Error -> {
                ErrorStateView(
                    message = state.message,
                    onRetryClick = { viewModel.loadProjects() }
                )
            }
            is ProjectListUiState.Success -> {
                // Search Input
                CmsTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = "Search site, code or location...",
                    leadingIcon = Icons.Outlined.Search,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Filter Chips Row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filterOptions) { (key, label) ->
                        val isSelected = state.statusFilter == key
                        Box(
                            modifier = Modifier
                                .clip(CmsTheme.shapes.chip)
                                .background(if (isSelected) CmsTheme.colors.accent else CmsTheme.colors.card)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) CmsTheme.colors.accent else CmsTheme.colors.border,
                                    shape = CmsTheme.shapes.chip
                                )
                                .clickable { viewModel.setStatusFilter(key) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                style = CmsTheme.typography.labelSmall.copy(
                                    color = if (isSelected) CmsTheme.colors.accentOn else CmsTheme.colors.textSecondary
                                )
                            )
                        }
                    }
                }

                if (state.filteredProjects.isEmpty()) {
                    EmptyStateView(
                        title = "No Sites Found",
                        message = if (state.searchQuery.isNotBlank() || state.statusFilter != "ALL") {
                            "No construction sites match your filter criteria."
                        } else {
                            "You don't have any projects yet. Tap 'New Site' to add one."
                        },
                        icon = Icons.Outlined.Apartment,
                        actionLabel = if (state.searchQuery.isNotBlank() || state.statusFilter != "ALL") "Reset Filters" else "Create Project",
                        onActionClick = {
                            if (state.searchQuery.isNotBlank() || state.statusFilter != "ALL") {
                                viewModel.setSearchQuery("")
                                viewModel.setStatusFilter("ALL")
                            } else {
                                showCreateSheet = true
                            }
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.filteredProjects, key = { it.id }) { project ->
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
                                onClick = { onProjectClick(project.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Create Project Bottom Sheet
    if (showCreateSheet) {
        val isCreating = (listState as? ProjectListUiState.Success)?.isCreating ?: false
        CreateProjectBottomSheet(
            onDismiss = {
                showCreateSheet = false
                createError = null
            },
            onSubmit = { name, code, location, budget ->
                viewModel.createProject(
                    name = name,
                    code = code,
                    location = location,
                    budgetInr = budget,
                    onSuccess = { created ->
                        showCreateSheet = false
                        createError = null
                        onProjectClick(created.id)
                    },
                    onError = { err ->
                        createError = err
                    }
                )
            },
            isSubmitting = isCreating,
            errorMessage = createError
        )
    }
}
