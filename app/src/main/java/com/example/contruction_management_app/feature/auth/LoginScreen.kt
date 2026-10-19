package com.example.contruction_management_app.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.contruction_management_app.core.ui.components.CmsPrimaryButton
import com.example.contruction_management_app.core.ui.components.CmsTextField
import com.example.contruction_management_app.core.ui.theme.CmsTheme
import com.example.contruction_management_app.core.ui.theme.CmsTokens
import com.example.contruction_management_app.domain.model.Role

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onOpenGallery: () -> Unit = {},
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("engineer@apexinfra.com") }
    var password by remember { mutableStateOf("password123") }
    var selectedRole by remember { mutableStateOf(Role.CIVIL_ENGINEER) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CmsTheme.colors.background)
            .verticalScroll(rememberScrollState())
            .padding(CmsTheme.spacing.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Industrial Logo & App Header
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CmsTheme.shapes.tile)
                .background(CmsTheme.colors.accentTint)
                .border(1.dp, CmsTheme.colors.accent, CmsTheme.shapes.tile),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Apartment,
                contentDescription = "CMS Logo",
                tint = CmsTheme.colors.accent,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Construction Management System",
            style = CmsTheme.typography.screenTitle.copy(fontSize = 20.sp),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Industrial Console • Secure Workspace",
            style = CmsTheme.typography.cardSubtitle
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Login Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CmsTheme.shapes.card)
                .background(CmsTheme.colors.card)
                .border(CmsTokens.Strokes.CardBorder, CmsTheme.colors.border, CmsTheme.shapes.card)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Sign In to Your Account",
                style = CmsTheme.typography.sectionTitle
            )

            CmsTextField(
                value = email,
                onValueChange = { email = it },
                label = "Company Email",
                placeholder = "name@company.com",
                leadingIcon = Icons.Outlined.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            CmsTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "••••••••",
                leadingIcon = Icons.Outlined.Lock,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(8.dp))

            CmsPrimaryButton(
                text = "Sign In",
                onClick = {
                    viewModel.login(email, selectedRole)
                    onLoginSuccess()
                }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Demo Role Switcher Section (Mandatory for rapid role-based testing)
        Text(
            text = "Explore as Demo Role (Instant Switch):",
            style = CmsTheme.typography.cardSubtitle.copy(color = CmsTheme.colors.textSecondary)
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Role.entries.forEach { role ->
                val isSelected = role == selectedRole
                val bgColor = if (isSelected) CmsTheme.colors.accentTint else CmsTheme.colors.card
                val borderColor = if (isSelected) CmsTheme.colors.accent else CmsTheme.colors.border
                val textColor = if (isSelected) CmsTheme.colors.accent else CmsTheme.colors.textSecondary

                Box(
                    modifier = Modifier
                        .clip(CmsTheme.shapes.pill)
                        .background(bgColor)
                        .border(1.dp, borderColor, CmsTheme.shapes.pill)
                        .clickable {
                            selectedRole = role
                            email = "${role.id}@apexinfra.com"
                            viewModel.login(email, role)
                            onLoginSuccess()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = role.displayName,
                        style = CmsTheme.typography.labelSmall.copy(
                            color = textColor,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Link to Component Gallery
        Text(
            text = "View Design Component Gallery →",
            style = CmsTheme.typography.button.copy(color = CmsTheme.colors.accent),
            modifier = Modifier
                .clickable { onOpenGallery() }
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
