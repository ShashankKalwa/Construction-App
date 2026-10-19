package com.example.contruction_management_app.feature.shell

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.contruction_management_app.core.ui.components.ActionTile
import com.example.contruction_management_app.core.ui.components.BottomNav
import com.example.contruction_management_app.core.ui.components.BottomNavItem
import com.example.contruction_management_app.core.ui.components.CategoryChipItem
import com.example.contruction_management_app.core.ui.components.CategoryChipRow
import com.example.contruction_management_app.core.ui.components.CmsPrimaryButton
import com.example.contruction_management_app.core.ui.components.CmsScaffold
import com.example.contruction_management_app.core.ui.components.CmsSecondaryButton
import com.example.contruction_management_app.core.ui.components.CmsStatus
import com.example.contruction_management_app.core.ui.components.ConsumerListRow
import com.example.contruction_management_app.core.ui.components.GreetingHeader
import com.example.contruction_management_app.core.ui.components.PrimaryFab
import com.example.contruction_management_app.core.ui.components.ProjectCard
import com.example.contruction_management_app.core.ui.components.ProjectSwitcher
import com.example.contruction_management_app.core.ui.components.StatCard
import com.example.contruction_management_app.core.ui.components.StatusBadge
import com.example.contruction_management_app.core.ui.theme.CmsTheme
import com.example.contruction_management_app.core.ui.theme.CmsTokens
import com.example.contruction_management_app.domain.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShell(
    user: User,
    onLogout: () -> Unit,
    onOpenGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf("home") }
    var showQuickCreate by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }

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
                modifier = Modifier.fillMaxWidth(),
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

            // Project Switcher
            ProjectSwitcher(
                currentProjectName = "Green Valley Residency",
                onClick = {}
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Body Content based on active tab
            when (currentTab) {
                "home" -> HomeTabContent(
                    user = user,
                    selectedCategoryIndex = selectedCategoryIndex,
                    onSelectCategory = { selectedCategoryIndex = it }
                )
                "projects" -> ProjectsTabContent()
                "tasks" -> TasksTabContent()
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
                // Handled in corresponding workflows
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
private fun HomeTabContent(
    user: User,
    selectedCategoryIndex: Int,
    onSelectCategory: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Category Chips
        item {
            CategoryChipRow(
                items = listOf(
                    CategoryChipItem("all", "All", Icons.Outlined.Category),
                    CategoryChipItem("projects", "Projects", Icons.Outlined.Apartment),
                    CategoryChipItem("tasks", "Tasks", Icons.Outlined.TaskAlt),
                    CategoryChipItem("materials", "Materials", Icons.Outlined.Inventory2),
                    CategoryChipItem("finance", "Finance", Icons.Outlined.CurrencyRupee)
                ),
                selectedIndex = selectedCategoryIndex,
                onItemSelected = onSelectCategory
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Summary StatCards
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CmsTheme.spacing.screenPadding),
                horizontalArrangement = Arrangement.spacedBy(CmsTheme.spacing.gridGap)
            ) {
                StatCard(
                    label = "Project Budget",
                    value = "Rs 2.5 Cr",
                    icon = Icons.Outlined.CurrencyRupee,
                    deltaText = "73% budget spent",
                    isPositiveDelta = true,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Overall Progress",
                    value = "68%",
                    icon = Icons.Outlined.Engineering,
                    deltaText = "On schedule",
                    isPositiveDelta = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Pinned Projects Section
        item {
            Text(
                text = "Pinned Sites & Buildings",
                style = CmsTheme.typography.sectionTitle,
                modifier = Modifier.padding(horizontal = CmsTheme.spacing.screenPadding)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CmsTheme.spacing.screenPadding),
                horizontalArrangement = Arrangement.spacedBy(CmsTheme.spacing.gridGap)
            ) {
                ProjectCard(
                    title = "Building A (12 Flats)",
                    subtitle = "RCC Framing Phase",
                    progress = 0.82f,
                    status = CmsStatus.Active,
                    modifier = Modifier.weight(1f)
                )
                ProjectCard(
                    title = "Building B (12 Flats)",
                    subtitle = "Brickwork & Masonry",
                    progress = 0.54f,
                    status = CmsStatus.Active,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Quick Actions Section
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
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                ActionTile(
                    label = "Material Req",
                    icon = Icons.Outlined.Inventory2,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                ActionTile(
                    label = "Calculator",
                    icon = Icons.Outlined.Calculate,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                ActionTile(
                    label = "Add Expense",
                    icon = Icons.Outlined.CurrencyRupee,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Top Material Consumption
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
            }
        }
    }
}

@Composable
private fun ProjectsTabContent() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = CmsTheme.spacing.screenPadding),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Active Projects",
                style = CmsTheme.typography.sectionTitle
            )
        }
        item {
            ProjectCard(
                title = "Green Valley Residency",
                subtitle = "Residential • 2 Buildings • 24 Apartments",
                progress = 0.68f,
                status = CmsStatus.Active
            )
        }
        item {
            ProjectCard(
                title = "Apex Tech Park - Tower 2",
                subtitle = "Commercial Complex • 8 Floors",
                progress = 0.35f,
                status = CmsStatus.OnHold
            )
        }
    }
}

@Composable
private fun TasksTabContent() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = CmsTheme.spacing.screenPadding),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Site Tasks & Milestones",
                style = CmsTheme.typography.sectionTitle
            )
        }
        item {
            TaskRow(
                title = "Pour 4th Floor Slab Concrete",
                assignedTo = "Subcontractor: R.K. Builders",
                status = CmsStatus.Active,
                deadline = "Today, 5:00 PM"
            )
        }
        item {
            TaskRow(
                title = "Inspect Column Rebar Spacing",
                assignedTo = "Civil Engineer: Anand Verma",
                status = CmsStatus.Completed,
                deadline = "Yesterday"
            )
        }
        item {
            TaskRow(
                title = "Receive 20 MT TMT Steel Delivery",
                assignedTo = "Storekeeper: Ramesh Yadav",
                status = CmsStatus.PendingSync,
                deadline = "Tomorrow, 10:00 AM"
            )
        }
    }
}

@Composable
private fun TaskRow(
    title: String,
    assignedTo: String,
    status: CmsStatus,
    deadline: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CmsTheme.shapes.tile)
            .background(CmsTheme.colors.card)
            .border(CmsTokens.Strokes.CardBorder, CmsTheme.colors.border, CmsTheme.shapes.tile)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = CmsTheme.typography.cardTitle)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = assignedTo, style = CmsTheme.typography.cardSubtitle)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Due: $deadline",
                style = CmsTheme.typography.labelSmall.copy(color = CmsTheme.colors.accent)
            )
        }
        StatusBadge(status = status)
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
                text = "Authorized tools for ${user.role.displayName}",
                style = CmsTheme.typography.cardSubtitle
            )
        }
        item {
            ToolTile(
                title = "Concrete Mix Calculator",
                description = "Volume, cement bags, sand & aggregate estimation",
                icon = Icons.Outlined.Calculate
            )
        }
        item {
            ToolTile(
                title = "Steel Rebar Takeoff",
                description = "Diameter, cutting length, hook allowance & weight in MT",
                icon = Icons.Outlined.Layers
            )
        }
        item {
            ToolTile(
                title = "Brickwork & Mortar Estimator",
                description = "Standard & modular brick count with wastage",
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
