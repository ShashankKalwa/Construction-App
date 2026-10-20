package com.example.contruction_management_app.feature.shell

import android.app.Application
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.contruction_management_app.core.auth.SessionDataStore
import com.example.contruction_management_app.core.ui.components.BottomNav
import com.example.contruction_management_app.core.ui.components.BottomNavItem
import com.example.contruction_management_app.core.ui.components.CmsPrimaryButton
import com.example.contruction_management_app.core.ui.components.CmsScaffold
import com.example.contruction_management_app.core.ui.components.CmsSecondaryButton
import com.example.contruction_management_app.core.ui.components.GreetingHeader
import com.example.contruction_management_app.core.ui.components.PrimaryFab
import com.example.contruction_management_app.core.ui.theme.CmsTheme
import com.example.contruction_management_app.core.ui.theme.CmsTokens
import com.example.contruction_management_app.domain.model.User
import com.example.contruction_management_app.feature.dashboard.HomeDashboardScreen
import com.example.contruction_management_app.feature.dashboard.HomeDashboardViewModel
import com.example.contruction_management_app.feature.project.ProjectListScreen
import com.example.contruction_management_app.feature.project.ProjectViewModel
import com.example.contruction_management_app.feature.task.TaskListScreen
import com.example.contruction_management_app.feature.task.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShell(
    user: User,
    onLogout: () -> Unit,
    onOpenGallery: () -> Unit,
    onOpenProjectDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val homeViewModel: HomeDashboardViewModel = viewModel()
    val projectViewModel: ProjectViewModel = viewModel()
    val taskViewModel: TaskViewModel = viewModel()
    val sessionDataStore = remember { SessionDataStore(context) }

    var currentTab by remember { mutableStateOf("home") }
    var showQuickCreate by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    val navItems = listOf(
        BottomNavItem("home", "Home", Icons.Outlined.Home),
        BottomNavItem("projects", "Projects", Icons.Outlined.Folder),
        BottomNavItem("tasks", "Tasks", Icons.Outlined.TaskAlt),
        BottomNavItem("tools", "Tools", Icons.Outlined.Calculate)
    )

    CmsScaffold(
        bottomBar = {
            BottomNav(
                items = navItems,
                currentRoute = currentTab,
                onItemSelected = { currentTab = it.route }
            )
        },
        floatingActionButton = {
            PrimaryFab(onClick = { showQuickCreate = true })
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header with Profile Access & Component Gallery Shortcut
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    GreetingHeader(
                        userName = user.fullName,
                        greeting = "Good Morning,",
                        hasUnreadNotifications = true,
                        onAvatarClick = { showProfileDialog = true }
                    )
                }

                // Gallery preview button
                IconButton(
                    onClick = onOpenGallery,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(36.dp)
                        .clip(CmsTheme.shapes.tile)
                        .background(CmsTheme.colors.card)
                        .border(1.dp, CmsTheme.colors.border, CmsTheme.shapes.tile)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = "Component Gallery",
                        tint = CmsTheme.colors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Body Content based on active tab
            when (currentTab) {
                "home" -> HomeDashboardScreen(
                    viewModel = homeViewModel,
                    user = user,
                    onNavigateToProjectDetail = onOpenProjectDetail,
                    onNavigateToProjectsTab = { currentTab = "projects" },
                    onNavigateToTasksTab = { currentTab = "tasks" },
                    onQuickActionClick = { actionId ->
                        when (actionId) {
                            "calculation" -> currentTab = "tools"
                            else -> showQuickCreate = true
                        }
                    }
                )
                "projects" -> ProjectListScreen(
                    viewModel = projectViewModel,
                    user = user,
                    onProjectClick = onOpenProjectDetail
                )
                "tasks" -> TaskListScreen(
                    activeProjectIdFlow = sessionDataStore.activeProjectIdFlow,
                    taskViewModel = taskViewModel,
                    user = user,
                    onNavigateToProjects = { currentTab = "projects" }
                )
                "tools" -> ToolsTabContent(user = user)
            }
        }
    }

    // Quick Create Bottom Sheet
    if (showQuickCreate) {
        QuickCreateBottomSheet(
            user = user,
            onDismiss = { showQuickCreate = false },
            onActionSelected = { actionId ->
                when (actionId) {
                    "new_task" -> currentTab = "tasks"
                    "calculation" -> currentTab = "tools"
                }
            }
        )
    }

    // Profile & Role Inspector Dialog
    if (showProfileDialog) {
        BasicAlertDialog(
            onDismissRequest = { showProfileDialog = false }
        ) {
            Column(
                modifier = Modifier
                    .clip(CmsTheme.shapes.card)
                    .background(CmsTheme.colors.card)
                    .border(CmsTokens.Strokes.CardBorder, CmsTheme.colors.border, CmsTheme.shapes.card)
                    .padding(24.dp)
            ) {
                Text(
                    text = "Active Session Profile",
                    style = CmsTheme.typography.screenTitle
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Name: ${user.fullName}",
                    style = CmsTheme.typography.body
                )
                Text(
                    text = "Email: ${user.email}",
                    style = CmsTheme.typography.cardSubtitle
                )
                Text(
                    text = "Role: ${user.role.displayName}",
                    style = CmsTheme.typography.cardTitle.copy(color = CmsTheme.colors.accent)
                )
                Text(
                    text = "Company: ${user.organizationName}",
                    style = CmsTheme.typography.cardSubtitle
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Granted Permissions (${user.role.permissions.size}):",
                    style = CmsTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(6.dp))

                user.role.permissions.take(5).forEach { permission ->
                    Text(
                        text = "• ${permission.name}",
                        style = CmsTheme.typography.labelSmall.copy(color = CmsTheme.colors.textSecondary)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CmsSecondaryButton(
                        text = "Close",
                        onClick = { showProfileDialog = false },
                        modifier = Modifier.weight(1f)
                    )
                    CmsPrimaryButton(
                        text = "Sign Out",
                        onClick = {
                            showProfileDialog = false
                            onLogout()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolsTabContent(user: User) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = CmsTheme.spacing.screenPadding),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Engineering & Operations Tools",
                style = CmsTheme.typography.sectionTitle
            )
            Text(
                text = "Authorized calculators for ${user.role.displayName}",
                style = CmsTheme.typography.cardSubtitle
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        item {
            ToolTile(
                title = "Concrete Mix Calculator",
                description = "Volume, cement bags, sand & aggregate estimation (M15, M20, M25)",
                icon = Icons.Outlined.Calculate
            )
        }
        item {
            ToolTile(
                title = "Steel Rebar Takeoff",
                description = "Diameter, cutting length, hook allowance & weight in MT (Fe 500/550)",
                icon = Icons.Outlined.Layers
            )
        }
        item {
            ToolTile(
                title = "Brickwork & Mortar Estimator",
                description = "Standard & modular brick count with cement mortar ratio",
                icon = Icons.Outlined.Apartment
            )
        }
    }
}

@Composable
private fun ToolTile(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CmsTheme.shapes.tile)
            .background(CmsTheme.colors.card)
            .border(CmsTokens.Strokes.CardBorder, CmsTheme.colors.border, CmsTheme.shapes.tile)
            .clickable {}
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CmsTheme.shapes.iconTile)
                .background(CmsTheme.colors.cardElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CmsTheme.colors.accent,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = CmsTheme.typography.cardTitle)
            Text(text = description, style = CmsTheme.typography.cardSubtitle)
        }
    }
}
