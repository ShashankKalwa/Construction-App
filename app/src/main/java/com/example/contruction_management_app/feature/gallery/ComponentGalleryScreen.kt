package com.example.contruction_management_app.feature.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.contruction_management_app.core.ui.components.AccentSlider
import com.example.contruction_management_app.core.ui.components.ActionTile
import com.example.contruction_management_app.core.ui.components.BarChart
import com.example.contruction_management_app.core.ui.components.BarItem
import com.example.contruction_management_app.core.ui.components.BottomNav
import com.example.contruction_management_app.core.ui.components.BottomNavItem
import com.example.contruction_management_app.core.ui.components.CategoryChipItem
import com.example.contruction_management_app.core.ui.components.CategoryChipRow
import com.example.contruction_management_app.core.ui.components.CmsPrimaryButton
import com.example.contruction_management_app.core.ui.components.CmsScaffold
import com.example.contruction_management_app.core.ui.components.CmsSecondaryButton
import com.example.contruction_management_app.core.ui.components.CmsStatus
import com.example.contruction_management_app.core.ui.components.CmsTextField
import com.example.contruction_management_app.core.ui.components.ConsumerListRow
import com.example.contruction_management_app.core.ui.components.ControlTile
import com.example.contruction_management_app.core.ui.components.EmptyStateView
import com.example.contruction_management_app.core.ui.components.ErrorStateView
import com.example.contruction_management_app.core.ui.components.GreetingHeader
import com.example.contruction_management_app.core.ui.components.ModeOption
import com.example.contruction_management_app.core.ui.components.ModeSelector
import com.example.contruction_management_app.core.ui.components.PrimaryFab
import com.example.contruction_management_app.core.ui.components.ProjectCard
import com.example.contruction_management_app.core.ui.components.ProjectSwitcher
import com.example.contruction_management_app.core.ui.components.SegmentedControl
import com.example.contruction_management_app.core.ui.components.ShimmerSkeletonBox
import com.example.contruction_management_app.core.ui.components.StatCard
import com.example.contruction_management_app.core.ui.components.StatusBadge
import com.example.contruction_management_app.core.ui.components.UnderlineTabs
import com.example.contruction_management_app.core.ui.components.ValueRowCard
import com.example.contruction_management_app.core.ui.theme.CmsTheme

@Composable
fun ComponentGalleryScreen(
    onNavigateBack: () -> Unit = {}
) {
    // Interactive states for demonstration
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var selectedUnderlineTabIndex by remember { mutableIntStateOf(0) }
    var selectedSegmentIndex by remember { mutableIntStateOf(1) }
    var selectedModeIndex by remember { mutableIntStateOf(1) }
    var sliderValue by remember { mutableFloatStateOf(65f) }
    var inputLength by remember { mutableStateOf("12.5") }
    var inputWidth by remember { mutableStateOf("8.0") }
    var currentNavRoute by remember { mutableStateOf("home") }

    val bottomNavItems = listOf(
        BottomNavItem("home", "Home", Icons.Outlined.Home),
        BottomNavItem("projects", "Projects", Icons.Outlined.Folder),
        BottomNavItem("tasks", "Tasks", Icons.Outlined.TaskAlt),
        BottomNavItem("tools", "Tools", Icons.Outlined.Calculate)
    )

    CmsScaffold(
        bottomBar = {
            BottomNav(
                items = bottomNavItems,
                currentRoute = currentNavRoute,
                onItemSelected = { currentNavRoute = it.route }
            )
        },
        floatingActionButton = {
            PrimaryFab(onClick = {})
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Screen Header
            item {
                GreetingHeader(
                    userName = "Shashank Kumar",
                    greeting = "Dark Industrial Console",
                    hasUnreadNotifications = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                ProjectSwitcher(
                    currentProjectName = "Green Valley Residency",
                    onClick = {}
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Section 1: Color Tokens Palette
            item {
                SectionHeader("1. Color Tokens Palette")
                ColorTokensRow()
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Section 2: Status Badges (Semantic States)
            item {
                SectionHeader("2. Semantic Status Badges")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = CmsTheme.spacing.screenPadding),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CmsStatus.entries) { status ->
                        StatusBadge(status = status)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Section 3: Category Chips
            item {
                SectionHeader("3. Category Chip Row")
                CategoryChipRow(
                    items = listOf(
                        CategoryChipItem("all", "All", Icons.Outlined.Category),
                        CategoryChipItem("projects", "Projects", Icons.Outlined.Apartment),
                        CategoryChipItem("tasks", "Tasks", Icons.Outlined.TaskAlt),
                        CategoryChipItem("materials", "Materials", Icons.Outlined.Inventory2),
                        CategoryChipItem("finance", "Finance", Icons.Outlined.CurrencyRupee)
                    ),
                    selectedIndex = selectedCategoryIndex,
                    onItemSelected = { selectedCategoryIndex = it }
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Section 4: Stat Cards
            item {
                SectionHeader("4. Stat Cards (Metrics & Deltas)")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CmsTheme.spacing.screenPadding),
                    horizontalArrangement = Arrangement.spacedBy(CmsTheme.spacing.gridGap)
                ) {
                    StatCard(
                        label = "Actual Cost",
                        value = "Rs 1.84 Cr",
                        icon = Icons.Outlined.CurrencyRupee,
                        deltaText = "8% under budget",
                        isPositiveDelta = true,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Concrete Poured",
                        value = "742",
                        unit = "m³",
                        icon = Icons.Outlined.Layers,
                        deltaText = "12% over planned",
                        isPositiveDelta = false,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Section 5: Pinned Project Cards (2-column Grid)
            item {
                SectionHeader("5. Project Cards")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CmsTheme.spacing.screenPadding),
                    horizontalArrangement = Arrangement.spacedBy(CmsTheme.spacing.gridGap)
                ) {
                    ProjectCard(
                        title = "Tower A - Slab 4",
                        subtitle = "Sector 14, Gurugram",
                        progress = 0.78f,
                        status = CmsStatus.Active,
                        modifier = Modifier.weight(1f)
                    )
                    ProjectCard(
                        title = "Basement Parking",
                        subtitle = "Phase 2 Excavation",
                        progress = 0.35f,
                        status = CmsStatus.OnHold,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Section 6: Interactive Controls & Tabs
            item {
                SectionHeader("6. Underline Tabs & Segmented Control")
                UnderlineTabs(
                    tabs = listOf("Overview", "Calculators", "BOQ", "Expenses"),
                    selectedTabIndex = selectedUnderlineTabIndex,
                    onTabSelected = { selectedUnderlineTabIndex = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.padding(horizontal = CmsTheme.spacing.screenPadding)) {
                    SegmentedControl(
                        options = listOf("Day", "Week", "Month", "Year"),
                        selectedIndex = selectedSegmentIndex,
                        onOptionSelected = { selectedSegmentIndex = it }
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Section 7: Quick Actions (4-across)
            item {
                SectionHeader("7. Quick Action Tiles")
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

            // Section 8: Calculator Controls (Tiles, ModeSelector, Slider, ValueRow)
            item {
                SectionHeader("8. Calculator Form Controls")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CmsTheme.spacing.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ControlTile(
                        label = "Concrete Mix Ratio",
                        accentValue = "M20 (1:1.5:3)",
                        icon = Icons.Outlined.Layers
                    )

                    ModeSelector(
                        options = listOf(
                            ModeOption("m15", "M15", "1:2:4"),
                            ModeOption("m20", "M20", "1:1.5:3"),
                            ModeOption("m25", "M25", "1:1:2")
                        ),
                        selectedIndex = selectedModeIndex,
                        onOptionSelected = { selectedModeIndex = it }
                    )

                    ValueRowCard(
                        label = "Wastage Allowance",
                        value = "${sliderValue.toInt()}%"
                    )

                    AccentSlider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 0f..20f,
                        startIcon = Icons.Outlined.Straighten,
                        endIcon = Icons.Outlined.Speed
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Section 9: Bar Chart (Canvas + Tap Tooltip)
            item {
                SectionHeader("9. Bar Chart (Actual vs Planned with Tooltip)")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CmsTheme.spacing.screenPadding)
                        .clip(CmsTheme.shapes.card)
                        .background(CmsTheme.colors.card)
                        .border(1.dp, CmsTheme.colors.border, CmsTheme.shapes.card)
                        .padding(14.dp)
                ) {
                    BarChart(
                        items = listOf(
                            BarItem("Mon", planned = 45f, actual = 52f, tooltipText = "52 m³"),
                            BarItem("Tue", planned = 60f, actual = 58f, tooltipText = "58 m³"),
                            BarItem("Wed", planned = 75f, actual = 85f, tooltipText = "85 m³"),
                            BarItem("Thu", planned = 50f, actual = 48f, tooltipText = "48 m³"),
                            BarItem("Fri", planned = 70f, actual = 72f, tooltipText = "72 m³"),
                            BarItem("Sat", planned = 40f, actual = 35f, tooltipText = "35 m³"),
                            BarItem("Sun", planned = 20f, actual = 15f, tooltipText = "15 m³")
                        )
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Section 10: Consumer List Rows
            item {
                SectionHeader("10. Material Consumption Rows")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CmsTheme.spacing.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ConsumerListRow(
                        name = "UltraTech Cement",
                        value = "4,200 bags",
                        percentage = 74,
                        icon = Icons.Outlined.Inventory2
                    )
                    ConsumerListRow(
                        name = "Fe-500D TMT Steel",
                        value = "28.5 MT",
                        percentage = 91,
                        icon = Icons.Outlined.Layers
                    )
                    ConsumerListRow(
                        name = "River Sand (Fine)",
                        value = "180 m³",
                        percentage = 45,
                        icon = Icons.Outlined.Straighten
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Section 11: Form Fields & Buttons
            item {
                SectionHeader("11. Dark Input Fields & Buttons")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CmsTheme.spacing.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CmsTextField(
                        value = inputLength,
                        onValueChange = { inputLength = it },
                        label = "Slab Length",
                        placeholder = "Enter length in meters",
                        unitSuffix = "m"
                    )

                    CmsTextField(
                        value = inputWidth,
                        onValueChange = { inputWidth = it },
                        label = "Slab Width",
                        placeholder = "Enter width in meters",
                        unitSuffix = "m"
                    )

                    CmsTextField(
                        value = "",
                        onValueChange = {},
                        label = "Depth with Validation Error",
                        placeholder = "0.00",
                        unitSuffix = "m",
                        errorMessage = "Depth cannot be zero or negative"
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    CmsPrimaryButton(
                        text = "Calculate & Save to BOQ",
                        onClick = {}
                    )

                    CmsSecondaryButton(
                        text = "Reset Inputs",
                        onClick = {}
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Section 12: State Views (Shimmer, Empty, Error)
            item {
                SectionHeader("12. Loading, Empty & Error States")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CmsTheme.spacing.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Skeleton Shimmer:",
                        style = CmsTheme.typography.cardSubtitle
                    )
                    ShimmerSkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                    )

                    Text(
                        text = "Empty State:",
                        style = CmsTheme.typography.cardSubtitle
                    )
                    EmptyStateView(
                        title = "No Material Requests",
                        message = "All requests have been fulfilled or none submitted today.",
                        actionLabel = "Create Request"
                    )

                    Text(
                        text = "Error State:",
                        style = CmsTheme.typography.cardSubtitle
                    )
                    ErrorStateView(
                        message = "Unable to connect to the backend server. Please verify your network.",
                        onRetryClick = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = CmsTheme.typography.sectionTitle,
        modifier = Modifier.padding(horizontal = CmsTheme.spacing.screenPadding, vertical = 8.dp)
    )
}

@Composable
private fun ColorTokensRow() {
    val tokens = listOf(
        "BG" to CmsTheme.colors.background,
        "Surface" to CmsTheme.colors.surface,
        "Card" to CmsTheme.colors.card,
        "Elevated" to CmsTheme.colors.cardElevated,
        "Accent" to CmsTheme.colors.accent,
        "Success" to CmsTheme.colors.success,
        "Warning" to CmsTheme.colors.warning,
        "Error" to CmsTheme.colors.error,
        "Info" to CmsTheme.colors.info
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = CmsTheme.spacing.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tokens) { (name, color) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CmsTheme.shapes.tile)
                        .background(color)
                        .border(1.dp, CmsTheme.colors.border, CmsTheme.shapes.tile)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = name,
                    style = CmsTheme.typography.labelSmall.copy(fontSize = 10.sp)
                )
            }
        }
    }
}
