package com.college.ecofeast.ui.donate

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.ecofeast.ui.theme.DeepText
import com.college.ecofeast.ui.theme.PrimaryForestGreen
import com.college.ecofeast.ui.theme.SecondaryText
import com.college.ecofeast.ui.theme.SoftGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

val FOOD_CONDITIONS = listOf(
    "Freshly Prepared",
    "Refrigerated",
    "Frozen",
    "Packaged / Sealed"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Step3AdditionalDetails(
    preparationTimestamp: Long,
    preparationError: String?,
    onPreparationTimestampChange: (Long) -> Unit,
    expiryTimestamp: Long?,
    expiryError: String?,
    onExpiryTimestampChange: (Long?) -> Unit,
    foodCondition: String,
    onFoodConditionChange: (String) -> Unit,
    allergenNotes: String,
    onAllergenNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault()) }

    fun showDateTimePicker(initialTimestamp: Long, onResult: (Long) -> Unit) {
        val calendar = Calendar.getInstance().apply { timeInMillis = initialTimestamp }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        onResult(calendar.timeInMillis)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Additional Details",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = DeepText,
                fontSize = 22.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Add more information about the food.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = SecondaryText,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Preparation Date & Time *
        Text(
            text = "Preparation Date & Time *",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = DeepText,
                fontSize = 14.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showDateTimePicker(preparationTimestamp) { selectedTs ->
                        onPreparationTimestampChange(selectedTs)
                    }
                },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, if (preparationError != null) MaterialTheme.colorScheme.error else Color(0xFFC8D6B9))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = PrimaryForestGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = dateFormat.format(Date(preparationTimestamp)),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = DeepText,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
        if (preparationError != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = preparationError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Best Before / Expiry Date (Optional)
        Text(
            text = "Best Before / Expiry Date",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = DeepText,
                fontSize = 14.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showDateTimePicker(expiryTimestamp ?: (System.currentTimeMillis() + 86400000)) { selectedTs ->
                        onExpiryTimestampChange(selectedTs)
                    }
                },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, if (expiryError != null) MaterialTheme.colorScheme.error else Color(0xFFC8D6B9))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = PrimaryForestGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (expiryTimestamp != null) dateFormat.format(Date(expiryTimestamp)) else "Select date (optional)",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (expiryTimestamp != null) DeepText else SecondaryText,
                        fontSize = 14.5.sp
                    )
                )
            }
        }
        if (expiryError != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = expiryError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Food Condition Chips
        Text(
            text = "Food Condition *",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = DeepText,
                fontSize = 14.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FOOD_CONDITIONS.forEach { condition ->
                val isSelected = foodCondition == condition
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) PrimaryForestGreen else Color.White)
                        .clickable { onFoodConditionChange(condition) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = condition,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isSelected) Color.White else DeepText,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. Allergen / Ingredient Notes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Allergen / Ingredient Notes",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepText,
                    fontSize = 14.sp
                )
            )
            Text(
                text = "${allergenNotes.length}/200",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SecondaryText,
                    fontSize = 12.sp
                )
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = allergenNotes,
            onValueChange = onAllergenNotesChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(95.dp),
            placeholder = { Text("e.g. Contains nuts, dairy, etc. (optional)", color = SecondaryText) },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = PrimaryForestGreen,
                unfocusedBorderColor = Color(0xFFC8D6B9)
            ),
            maxLines = 3
        )
    }
}
