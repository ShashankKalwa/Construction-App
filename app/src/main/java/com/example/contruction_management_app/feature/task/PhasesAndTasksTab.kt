package com.example.contruction_management_app.feature.task

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.contruction_management_app.core.ui.components.CmsStatus
import com.example.contruction_management_app.core.ui.components.EmptyStateView
import com.example.contruction_management_app.core.ui.components.ErrorStateView
import com.example.contruction_management_app.core.ui.components.LoadingStateView
import com.example.contruction_management_app.core.ui.components.StatusBadge
import com.example.contruction_management_app.core.ui.theme.CmsTheme
import com.example.contruction_management_app.core.ui.theme.CmsTokens
import com.example.contruction_management_app.domain.model.Phase
import com.example.contruction_management_app.domain.model.Task
import com.example.contruction_management_app.domain.model.TaskPriority
import com.example.contruction_management_app.domain.model.TaskStatus

@Composable
fun PhasesAndTasksTab(
    projectId: String,
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTaskForUpdate by remember { mutableStateOf<Task?>(null) }

    // Map of expanded phase IDs (by default first 2 phases expanded)
    val expandedPhases = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(projectId) {
        viewModel.loadPhases(projectId)
    }

    val statusFilters = listOf(
        null to "All Tasks",
        TaskStatus.IN_PROGRESS to "In Progress",
        TaskStatus.BLOCKED to "Blocked",
        TaskStatus.COMPLETED to "Completed",
        TaskStatus.PENDING to "Pending"
    )

    when (val state = uiState) {
        is PhasesUiState.Loading -> {
            LoadingStateView(itemCount = 3, modifier = modifier)
        }
        is PhasesUiState.Error -> {
            ErrorStateView(
                message = state.message,
                onRetryClick = { viewModel.loadPhases(projectId) },
                modifier = modifier
            )
        }
        is PhasesUiState.Success -> {
            // Expand first 2 phases initially if map is empty
            if (expandedPhases.isEmpty() && state.phases.isNotEmpty()) {
                state.phases.take(2).forEach { phase ->
                    expandedPhases[phase.id] = true
                }
            }

            Column(modifier = modifier.fillMaxSize()) {
                // Filter Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CmsTheme.spacing.screenPadding, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(statusFilters) { (status, label) ->
                        val isSelected = state.selectedStatus == status
                        Box(
                            modifier = Modifier
                                .clip(CmsTheme.shapes.chip)
                                .background(if (isSelected) CmsTheme.colors.accent else CmsTheme.colors.card)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) CmsTheme.colors.accent else CmsTheme.colors.border,
                                    shape = CmsTheme.shapes.chip
                                )
                                .clickable { viewModel.filterByStatus(status) }
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

                if (state.filteredPhases.isEmpty() || state.filteredPhases.all { it.tasks.isEmpty() }) {
                    EmptyStateView(
                        title = "No Tasks Found",
                        message = if (state.selectedStatus != null) {
                            "No tasks match status '${state.selectedStatus}'"
                        } else {
                            "No stages or tasks have been defined for this site yet."
                        },
                        icon = Icons.Outlined.TaskAlt,
                        actionLabel = if (state.selectedStatus != null) "Show All" else null,
                        onActionClick = { viewModel.filterByStatus(null) }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = CmsTheme.spacing.screenPadding,
                            end = CmsTheme.spacing.screenPadding,
                            bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(state.filteredPhases, key = { it.id }) { phase ->
                            val isExpanded = expandedPhases[phase.id] ?: false
                            val phaseStatus = when (phase.status.lowercase()) {
                                "completed" -> CmsStatus.Completed
                                "in progress", "in_progress" -> CmsStatus.Active
                                else -> CmsStatus.Draft
                            }

                            PhaseCard(
                                phase = phase,
                                isExpanded = isExpanded,
                                status = phaseStatus,
                                onToggleExpand = {
                                    expandedPhases[phase.id] = !isExpanded
                                },
                                onTaskClick = { task ->
                                    selectedTaskForUpdate = task
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Task Update Bottom Sheet
    selectedTaskForUpdate?.let { task ->
        val currentState = uiState as? PhasesUiState.Success
        TaskUpdateBottomSheet(
            task = task,
            onDismiss = { selectedTaskForUpdate = null },
            onSave = { progressPct, status ->
                viewModel.updateTaskProgress(
                    taskId = task.id,
                    progressPct = progressPct,
                    status = status,
                    onSuccess = {
                        selectedTaskForUpdate = null
                    }
                )
            },
            isUpdating = currentState?.isUpdating ?: false,
            errorMessage = currentState?.updateError
        )
    }
}

@Composable
private fun PhaseCard(
    phase: Phase,
    isExpanded: Boolean,
    status: CmsStatus,
    onToggleExpand: () -> Unit,
    onTaskClick: (Task) -> Unit
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "chevronRotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CmsTheme.shapes.card)
            .background(CmsTheme.colors.card)
            .border(CmsTokens.Strokes.CardBorder, CmsTheme.colors.border, CmsTheme.shapes.card)
    ) {
        // Phase Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = phase.name,
                        style = CmsTheme.typography.cardTitle
                    )
                    StatusBadge(status = status)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${phase.startDate ?: "TBD"} → ${phase.endDate ?: "TBD"} • ${phase.tasks.size} tasks",
                    style = CmsTheme.typography.cardSubtitle
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Linear progress bar for Phase
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { (phase.progressPct / 100.0).toFloat() },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(CmsTheme.shapes.pill),
                        color = CmsTheme.colors.accent,
                        trackColor = CmsTheme.colors.cardElevated
                    )
                    Text(
                        text = "${phase.progressPct.toInt()}%",
                        style = CmsTheme.typography.labelSmall.copy(
                            color = CmsTheme.colors.accent,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Expand / Collapse Chevron
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CmsTheme.shapes.tile)
                    .background(CmsTheme.colors.cardElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = CmsTheme.colors.textSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(rotationAngle)
                )
            }
        }

        // Expanded Tasks List
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CmsTheme.colors.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (phase.tasks.isEmpty()) {
                    Text(
                        text = "No tasks listed for this phase",
                        style = CmsTheme.typography.cardSubtitle,
                        modifier = Modifier.padding(12.dp)
                    )
                } else {
                    phase.tasks.forEach { task ->
                        TaskItemRow(
                            task = task,
                            onClick = { onTaskClick(task) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskItemRow(
    task: Task,
    onClick: () -> Unit
) {
    val (priorityColor, priorityText) = when (task.priority) {
        TaskPriority.HIGH -> CmsTheme.colors.warning to "HIGH"
        TaskPriority.MEDIUM -> CmsTheme.colors.accent to "MED"
        TaskPriority.LOW -> CmsTheme.colors.textTertiary to "LOW"
    }

    val (statusColor, statusBg, statusText) = when (task.status) {
        TaskStatus.COMPLETED -> Triple(CmsTheme.colors.success, CmsTheme.colors.success.copy(alpha = 0.15f), "Done")
        TaskStatus.IN_PROGRESS -> Triple(CmsTheme.colors.accent, CmsTheme.colors.accent.copy(alpha = 0.15f), "Active")
        TaskStatus.BLOCKED -> Triple(CmsTheme.colors.error, CmsTheme.colors.error.copy(alpha = 0.15f), "Blocked")
        TaskStatus.PENDING -> Triple(CmsTheme.colors.textSecondary, CmsTheme.colors.cardElevated, "Pending")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CmsTheme.shapes.tile)
            .background(CmsTheme.colors.card)
            .border(CmsTokens.Strokes.CardBorder, CmsTheme.colors.border, CmsTheme.shapes.tile)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        // Top row: Priority tag + Title + Status Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Priority pill
                Box(
                    modifier = Modifier
                        .clip(CmsTheme.shapes.chip)
                        .background(priorityColor.copy(alpha = 0.12f))
                        .border(1.dp, priorityColor.copy(alpha = 0.35f), CmsTheme.shapes.chip)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = priorityText,
                        style = CmsTheme.typography.labelSmall.copy(
                            color = priorityColor,
                            fontSize = 10.sp
                        )
                    )
                }

                Text(
                    text = task.title,
                    style = CmsTheme.typography.cardTitle,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Status chip
            Box(
                modifier = Modifier
                    .clip(CmsTheme.shapes.chip)
                    .background(statusBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = statusText,
                    style = CmsTheme.typography.labelSmall.copy(
                        color = statusColor,
                        fontSize = 11.sp
                    )
                )
            }
        }

        if (!task.description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = task.description,
                style = CmsTheme.typography.cardSubtitle,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress + Assignee + Due Date footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = CmsTheme.colors.textSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = task.assignee?.fullName ?: "Unassigned",
                    style = CmsTheme.typography.labelSmall.copy(color = CmsTheme.colors.textSecondary)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (task.dueDate != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = CmsTheme.colors.textTertiary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = task.dueDate,
                            style = CmsTheme.typography.labelSmall.copy(
                                color = CmsTheme.colors.textTertiary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Text(
                    text = "${task.progressPct.toInt()}%",
                    style = CmsTheme.typography.labelSmall.copy(
                        color = CmsTheme.colors.accent,
                        fontSize = 12.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { (task.progressPct / 100.0).toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CmsTheme.shapes.pill),
            color = if (task.status == TaskStatus.BLOCKED) CmsTheme.colors.error else CmsTheme.colors.accent,
            trackColor = CmsTheme.colors.cardElevated
        )
    }
}
