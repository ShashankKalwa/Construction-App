package com.example.contruction_management_app.feature.shell

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.contruction_management_app.core.ui.theme.CmsTheme
import com.example.contruction_management_app.core.ui.theme.CmsTokens
import com.example.contruction_management_app.domain.model.Permission
import com.example.contruction_management_app.domain.model.User

data class QuickActionItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val requiredPermission: Permission
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCreateBottomSheet(
    user: User,
    onDismiss: () -> Unit,
    onActionSelected: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val allActions = listOf(
        QuickActionItem(
            id = "daily_report",
            title = "Daily Site Report",
            subtitle = "Record progress, photos & labor counts",
            icon = Icons.Outlined.Description,
            requiredPermission = Permission.DAILY_REPORT_WRITE
        ),
        QuickActionItem(
            id = "material_request",
            title = "Material Request",
            subtitle = "Indents for cement, steel, aggregates",
            icon = Icons.Outlined.Inventory2,
            requiredPermission = Permission.REQUEST_MATERIAL
        ),
        QuickActionItem(
            id = "new_task",
            title = "Create Task",
            subtitle = "Assign milestone or site activity",
            icon = Icons.Outlined.TaskAlt,
            requiredPermission = Permission.MANAGE_TASKS
        ),
        QuickActionItem(
            id = "calculation",
            title = "Civil Calculation",
            subtitle = "Concrete, steel, brickwork takeoff",
            icon = Icons.Outlined.Calculate,
            requiredPermission = Permission.USE_CALCULATORS
        ),
        QuickActionItem(
            id = "expense",
            title = "Record Expense",
            subtitle = "Petty cash, vendor invoices, vouchers",
            icon = Icons.Outlined.CurrencyRupee,
            requiredPermission = Permission.SUBMIT_EXPENSE
        )
    )

    // Filter actions by user's role permissions
    val visibleActions = allActions.filter { user.role.hasPermission(it.requiredPermission) }

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
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = "Quick Create",
                style = CmsTheme.typography.screenTitle
            )
            Text(
                text = "Permitted actions for ${user.role.displayName}",
                style = CmsTheme.typography.cardSubtitle
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                visibleActions.forEach { action ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CmsTheme.shapes.tile)
                            .background(CmsTheme.colors.card)
                            .border(CmsTokens.Strokes.CardBorder, CmsTheme.colors.border, CmsTheme.shapes.tile)
                            .clickable {
                                onActionSelected(action.id)
                                onDismiss()
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CmsTheme.shapes.iconTile)
                                .background(CmsTheme.colors.cardElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = null,
                                tint = CmsTheme.colors.accent,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = action.title,
                                style = CmsTheme.typography.cardTitle
                            )
                            Text(
                                text = action.subtitle,
                                style = CmsTheme.typography.cardSubtitle
                            )
                        }
                    }
                }
            }
        }
    }
}
