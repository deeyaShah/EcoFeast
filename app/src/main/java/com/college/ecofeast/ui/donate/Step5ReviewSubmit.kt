package com.college.ecofeast.ui.donate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.college.ecofeast.ui.theme.DeepText
import com.college.ecofeast.ui.theme.PrimaryForestGreen
import com.college.ecofeast.ui.theme.SecondaryText
import com.college.ecofeast.ui.theme.SoftGreen
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun Step5ReviewSubmit(
    foodName: String,
    category: String,
    description: String,
    quantityText: String,
    quantityUnit: String,
    selectedPhotoPaths: List<String>,
    preparationTimestamp: Long,
    expiryTimestamp: Long?,
    foodCondition: String,
    allergenNotes: String,
    pickupAddress: String,
    latitude: Double?,
    longitude: Double?,
    pickupInstructions: String,
    isConfirmed: Boolean,
    onConfirmedChange: (Boolean) -> Unit,
    isSubmitting: Boolean,
    submitError: String?,
    onSubmitClick: () -> Unit,
    onJumpToStep: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault()) }
    val formattedQuantity = remember(quantityText, quantityUnit) {
        formatQuantityDisplay(quantityText, quantityUnit)
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Review & Submit",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = DeepText,
                fontSize = 22.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Check your details before submitting.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = SecondaryText,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 1: Food Details Card
        ReviewSectionCard(
            title = "Food Details",
            onEditClick = { onJumpToStep(1) }
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (selectedPhotoPaths.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(File(selectedPhotoPaths.first())),
                        contentDescription = "Main Food Image",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = foodName.ifBlank { "Untitled Food" },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepText,
                            fontSize = 16.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Category: ${category.ifBlank { "Uncategorized" }}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SecondaryText,
                            fontSize = 13.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Quantity: $formattedQuantity",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DeepText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    )
                }
            }

            if (description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"$description\"",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SecondaryText,
                        fontSize = 12.5.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SECTION 2: Food Photos Card
        ReviewSectionCard(
            title = "Food Photos (${selectedPhotoPaths.size})",
            onEditClick = { onJumpToStep(2) }
        ) {
            if (selectedPhotoPaths.isEmpty()) {
                Text(
                    text = "No photos attached.",
                    style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText, fontSize = 12.5.sp)
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(selectedPhotoPaths) { path ->
                        Image(
                            painter = rememberAsyncImagePainter(File(path)),
                            contentDescription = "Thumbnail",
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SECTION 3: Additional Details Card
        ReviewSectionCard(
            title = "Additional Details",
            onEditClick = { onJumpToStep(3) }
        ) {
            SummaryDetailRow(label = "Preparation Date", value = dateFormat.format(Date(preparationTimestamp)))
            val expiryDisplay = if (expiryTimestamp != null) {
                dateFormat.format(Date(expiryTimestamp))
            } else {
                "Missing Expiry (Please edit Step 3)"
            }
            SummaryDetailRow(label = "Expiry Date & Time", value = expiryDisplay)
            SummaryDetailRow(label = "Food Condition", value = foodCondition)
            if (allergenNotes.isNotBlank()) {
                SummaryDetailRow(label = "Allergen Notes", value = allergenNotes)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SECTION 4: Pickup Location Card
        ReviewSectionCard(
            title = "Pickup Location",
            onEditClick = { onJumpToStep(4) }
        ) {
            SummaryDetailRow(label = "Pickup Address", value = pickupAddress.ifBlank { "Not specified" })
            if (latitude != null && longitude != null) {
                SummaryDetailRow(label = "GPS Pin", value = "%.4f, %.4f".format(latitude, longitude))
            }
            if (pickupInstructions.isNotBlank()) {
                SummaryDetailRow(label = "Instructions", value = pickupInstructions)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Submit Error Banner if submission failed
        if (submitError != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = submitError,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Mandatory Safety Confirmation Checkbox Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SoftGreen.copy(alpha = 0.55f)),
            border = BorderStroke(1.dp, PrimaryForestGreen.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onConfirmedChange(!isConfirmed) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isConfirmed,
                    onCheckedChange = onConfirmedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = PrimaryForestGreen,
                        uncheckedColor = SecondaryText
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "I confirm that the food is safe to consume and all information is accurate.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepText,
                        fontSize = 12.5.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Submit Action Button
        Button(
            onClick = onSubmitClick,
            enabled = isConfirmed && !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryForestGreen,
                contentColor = Color.White,
                disabledContainerColor = PrimaryForestGreen.copy(alpha = 0.4f),
                disabledContentColor = Color.White.copy(alpha = 0.6f)
            )
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Publishing Listing...", fontSize = 15.5.sp, fontWeight = FontWeight.Bold)
            } else {
                Text("Submit Listing", fontSize = 15.5.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ReviewSectionCard(
    title: String,
    onEditClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFC8D6B9))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryForestGreen,
                        fontSize = 14.5.sp
                    )
                )
                TextButton(
                    onClick = onEditClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit $title",
                        modifier = Modifier.size(13.dp),
                        tint = PrimaryForestGreen
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Edit",
                        color = PrimaryForestGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            content()
        }
    }
}

@Composable
private fun SummaryDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = SecondaryText,
                fontSize = 12.5.sp
            ),
            modifier = Modifier.weight(0.45f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                color = DeepText,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            ),
            modifier = Modifier.weight(0.55f)
        )
    }
}

/**
 * Formats quantity and unit display cleanly without duplicate unit strings like "2 kg kg".
 */
private fun formatQuantityDisplay(quantityText: String, quantityUnit: String): String {
    val trimmedQty = quantityText.trim()
    val trimmedUnit = quantityUnit.trim()
    if (trimmedQty.isEmpty()) return ""
    if (trimmedUnit.isEmpty()) return trimmedQty

    val lowerQty = trimmedQty.lowercase(Locale.getDefault())
    val lowerUnit = trimmedUnit.lowercase(Locale.getDefault())

    if (lowerQty.endsWith(lowerUnit)) {
        return trimmedQty
    }
    val regexWithWord = Regex("""^\d+(?:\.\d+)?\s+[a-zA-Z]+.*$""")
    if (regexWithWord.matches(trimmedQty)) {
        return trimmedQty
    }
    return "$trimmedQty $trimmedUnit"
}
