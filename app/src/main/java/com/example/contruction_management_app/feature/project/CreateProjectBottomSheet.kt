package com.example.contruction_management_app.feature.project

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.contruction_management_app.core.ui.components.CmsPrimaryButton
import com.example.contruction_management_app.core.ui.components.CmsSecondaryButton
import com.example.contruction_management_app.core.ui.components.CmsTextField
import com.example.contruction_management_app.core.ui.theme.CmsTheme
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: (name: String, code: String, location: String, budget: BigDecimal) -> Unit,
    isSubmitting: Boolean = false,
    errorMessage: String? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var budgetInput by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var codeError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var budgetError by remember { mutableStateOf<String?>(null) }

    // Live preview of INR notation
    val budgetPreview by remember(budgetInput) {
        derivedStateOf {
            val num = budgetInput.toDoubleOrNull()
            if (num != null && num > 0) {
                when {
                    num >= 10000000 -> String.format("₹%.2f Crore", num / 10000000.0)
                    num >= 100000 -> String.format("₹%.2f Lakhs", num / 100000.0)
                    else -> String.format("₹%,.0f", num)
                }
            } else {
                null
            }
        }
    }

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
            Text(
                text = "Create Construction Site",
                style = CmsTheme.typography.screenTitle
            )
            Text(
                text = "Register a new project, location, and overall financial budget",
                style = CmsTheme.typography.cardSubtitle
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Project Name
            CmsTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = "Project Name *",
                placeholder = "e.g. Skyline Business Park Tower A",
                leadingIcon = Icons.Outlined.Apartment,
                errorMessage = nameError,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Project Code
            CmsTextField(
                value = code,
                onValueChange = {
                    code = it.uppercase()
                    codeError = null
                },
                label = "Project Code / Site ID *",
                placeholder = "e.g. SBP-02",
                leadingIcon = Icons.Outlined.Tag,
                errorMessage = codeError,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Location
            CmsTextField(
                value = location,
                onValueChange = {
                    location = it
                    locationError = null
                },
                label = "Site Location *",
                placeholder = "e.g. Whitefield, Bangalore",
                leadingIcon = Icons.Outlined.LocationOn,
                errorMessage = locationError,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Budget INR
            CmsTextField(
                value = budgetInput,
                onValueChange = {
                    if (it.isEmpty() || it.all { char -> char.isDigit() || char == '.' }) {
                        budgetInput = it
                        budgetError = null
                    }
                },
                label = "Total Project Budget (INR) *",
                placeholder = "e.g. 25000000",
                leadingIcon = Icons.Outlined.CurrencyRupee,
                unitSuffix = "INR",
                errorMessage = budgetError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Dynamic INR Conversion Preview Chip
            if (budgetPreview != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Formatted: $budgetPreview",
                    style = CmsTheme.typography.labelSmall.copy(color = CmsTheme.colors.accent),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    style = CmsTheme.typography.labelSmall.copy(color = CmsTheme.colors.error)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CmsSecondaryButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    enabled = !isSubmitting
                )

                CmsPrimaryButton(
                    text = if (isSubmitting) "Creating..." else "Create Site",
                    onClick = {
                        var isValid = true
                        if (name.isBlank()) {
                            nameError = "Project name is required"
                            isValid = false
                        }
                        if (code.isBlank()) {
                            codeError = "Project code is required"
                            isValid = false
                        }
                        if (location.isBlank()) {
                            locationError = "Site location is required"
                            isValid = false
                        }
                        val budgetVal = budgetInput.toDoubleOrNull()
                        if (budgetVal == null || budgetVal <= 0) {
                            budgetError = "Enter a valid positive budget amount"
                            isValid = false
                        }

                        if (isValid) {
                            onSubmit(
                                name.trim(),
                                code.trim(),
                                location.trim(),
                                BigDecimal(budgetInput.trim())
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isSubmitting
                )
            }
        }
    }
}
