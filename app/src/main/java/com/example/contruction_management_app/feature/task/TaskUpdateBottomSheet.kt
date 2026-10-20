package com.example.contruction_management_app.feature.task

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.contruction_management_app.core.ui.components.AccentSlider
import com.example.contruction_management_app.core.ui.components.CmsPrimaryButton
import com.example.contruction_management_app.core.ui.components.CmsSecondaryButton
import com.example.contruction_management_app.core.ui.components.CmsStatus
import com.example.contruction_management_app.core.ui.components.StatusBadge
import com.example.contruction_management_app.core.ui.theme.CmsTheme
import com.example.contruction_management_app.core.ui.theme.CmsTokens
import com.example.contruction_management_app.domain.model.Task
import com.example.contruction_management_app.domain.model.TaskPriority
import com.example.contruction_management_app.domain.model.TaskStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskUpdateBottomSheet(
    task: Task,
    onDismiss: () -> Unit,
    onSave: (progressPct: Double, status: TaskStatus) -> Unit,
    isUpdating: Boolean = false,
    errorMessage: String? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentProgress by remember { mutableFloatStateOf(task.progressPct.toFloat()) }
    var currentStatus by remember { mutableStateOf(task.status) }

    val statusOptions = listOf(
        TaskStatus.PENDING to "Pending",
        TaskStatus.IN_PROGRESS to "In Progress",
        TaskStatus.BLOCKED to "Blocked",
        TaskStatus.COMPLETED to "Completed"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CmsTheme.colors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CmsTheme.shapes.pill)
                    .background(CmsTheme.colors.divider)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Task Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Update Milestone Progress",
                    style = CmsTheme.typography.screenTitle
                )

                // Priority pill
                val (priorityColor, priorityText) = when (task.priority) {
                    TaskPriority.HIGH -> CmsTheme.colors.warning to "HIGH"
                    TaskPriority.MEDIUM -> CmsTheme.colors.accent to "MEDIUM"
                    TaskPriority.LOW -> CmsTheme.colors.textTertiary to "LOW"
                }

                Box(
                    modifier = Modifier
                        .clip(CmsTheme.shapes.chip)
                        .background(priorityColor.copy(alpha = 0.15f))
                        .border(1.dp, priorityColor.copy(alpha = 0.5f), CmsTheme.shapes.chip)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = priorityText,
                        style = CmsTheme.typography.labelSmall.copy(
                            color = priorityColor,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = task.title,
                style = CmsTheme.typography.cardTitle
            )
            Text(
                text = "${task.phaseName ?: "General Phase"} • Due: ${task.dueDate ?: "TBD"}",
                style = CmsTheme.typography.cardSubtitle
            )

            if (!task.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CmsTheme.shapes.tile)
                        .background(CmsTheme.colors.card)
                        .padding(12.dp)
                ) {
                    Text(
                        text = task.description,
                        style = CmsTheme.typography.body.copy(color = CmsTheme.colors.textSecondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Progress Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progress Completion",
                    style = CmsTheme.typography.cardTitle
                )
                Text(
                    text = "${currentProgress.toInt()}%",
                    style = CmsTheme.typography.statValue.copy(color = CmsTheme.colors.accent)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            AccentSlider(
                value = currentProgress,
                onValueChange = { newVal ->
                    currentProgress = newVal
                    if (newVal >= 100f) {
                        currentStatus = TaskStatus.COMPLETED
                    } else if (newVal > 0f && currentStatus == TaskStatus.PENDING) {
                        currentStatus = TaskStatus.IN_PROGRESS
                    }
                },
                valueRange = 0f..100f
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Status Selector
            Text(
                text = "Task Status",
                style = CmsTheme.typography.cardTitle
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statusOptions.forEach { (status, label) ->
                    val isSelected = currentStatus == status
                    val (activeColor, activeBg) = when (status) {
                        TaskStatus.COMPLETED -> CmsTheme.colors.success to CmsTheme.colors.success.copy(alpha = 0.2f)
                        TaskStatus.BLOCKED -> CmsTheme.colors.error to CmsTheme.colors.error.copy(alpha = 0.2f)
                        TaskStatus.IN_PROGRESS -> CmsTheme.colors.accent to CmsTheme.colors.accent.copy(alpha = 0.2f)
                        TaskStatus.PENDING -> CmsTheme.colors.textSecondary to CmsTheme.colors.cardElevated
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CmsTheme.shapes.chip)
                            .background(if (isSelected) activeBg else CmsTheme.colors.card)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) activeColor else CmsTheme.colors.border,
                                shape = CmsTheme.shapes.chip
                            )
                            .clickable {
                                currentStatus = status
                                if (status == TaskStatus.COMPLETED && currentProgress < 100f) {
                                    currentProgress = 100f
                                }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = CmsTheme.typography.labelSmall.copy(
                                color = if (isSelected) activeColor else CmsTheme.colors.textSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = errorMessage,
                    style = CmsTheme.typography.labelSmall.copy(color = CmsTheme.colors.error)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CmsSecondaryButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    enabled = !isUpdating
                )
                CmsPrimaryButton(
                    text = if (isUpdating) "Saving..." else "Update Progress",
                    onClick = {
                        onSave(currentProgress.toDouble(), currentStatus)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isUpdating
                )
            }
        }
    }
}
