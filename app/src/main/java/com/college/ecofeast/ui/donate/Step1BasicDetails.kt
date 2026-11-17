package com.college.ecofeast.ui.donate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.ecofeast.ui.theme.DeepText
import com.college.ecofeast.ui.theme.PrimaryForestGreen
import com.college.ecofeast.ui.theme.SecondaryText

val FOOD_CATEGORIES = listOf(
    "Prepared Meal",
    "Fruits",
    "Vegetables",
    "Bakery",
    "Snacks",
    "Dairy",
    "Beverages",
    "Packaged Food",
    "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step1BasicDetails(
    foodName: String,
    foodNameError: String?,
    onFoodNameChange: (String) -> Unit,
    category: String,
    categoryError: String?,
    onCategoryChange: (String) -> Unit,
    description: String,
    descriptionError: String?,
    onDescriptionChange: (String) -> Unit,
    quantityText: String,
    quantityTextError: String?,
    onQuantityTextChange: (String) -> Unit,
    quantityUnit: String,
    onQuantityUnitChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var categoryExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Basic Details",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = DeepText,
                fontSize = 22.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Tell us about the food you want to donate.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = SecondaryText,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Food Name
        Text(
            text = "Food Name *",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = DeepText,
                fontSize = 14.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = foodName,
            onValueChange = onFoodNameChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. Vegetable Rice", color = SecondaryText) },
            singleLine = true,
            isError = foodNameError != null,
            supportingText = foodNameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = PrimaryForestGreen,
                unfocusedBorderColor = Color(0xFFC8D6B9)
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Food Category Dropdown
        Text(
            text = "Category *",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = DeepText,
                fontSize = 14.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = !categoryExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = if (category.isNotBlank()) category else "Select category",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                isError = categoryError != null,
                supportingText = categoryError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = PrimaryForestGreen,
                    unfocusedBorderColor = Color(0xFFC8D6B9),
                    focusedTextColor = if (category.isBlank()) SecondaryText else DeepText,
                    unfocusedTextColor = if (category.isBlank()) SecondaryText else DeepText
                )
            )

            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                FOOD_CATEGORIES.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item, color = DeepText) },
                        onClick = {
                            onCategoryChange(item)
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Description
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Description *",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepText,
                    fontSize = 14.sp
                )
            )
            Text(
                text = "${description.length}/500",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SecondaryText,
                    fontSize = 12.sp
                )
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            placeholder = { Text("Add a short description about the food...", color = SecondaryText) },
            isError = descriptionError != null,
            supportingText = descriptionError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = PrimaryForestGreen,
                unfocusedBorderColor = Color(0xFFC8D6B9)
            ),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Quantity & Unit (Single Combined Input)
        Text(
            text = "Quantity *",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = DeepText,
                fontSize = 14.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = quantityText,
            onValueChange = onQuantityTextChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter quantity (e.g. 5 portions, 2 kg)", color = SecondaryText) },
            singleLine = true,
            isError = quantityTextError != null,
            supportingText = quantityTextError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = PrimaryForestGreen,
                unfocusedBorderColor = Color(0xFFC8D6B9)
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
    }
}
