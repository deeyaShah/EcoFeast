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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.college.ecofeast.data.model.FoodListing
import com.college.ecofeast.ui.auth.WelcomeFoodIllustration
import com.college.ecofeast.ui.theme.DeepText
import com.college.ecofeast.ui.theme.PrimaryForestGreen
import com.college.ecofeast.ui.theme.SecondaryText
import com.college.ecofeast.ui.theme.SoftGreen
import com.college.ecofeast.ui.theme.WarmIvory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyDonationsScreen(
    uiState: DonateUiState,
    viewModel: DonateViewModel,
    onBackClick: () -> Unit,
    onStartNewDonation: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.subScreen == DonateSubScreen.MY_DONATIONS_DETAIL && uiState.selectedListing != null) {
        DonationDetailView(
            listing = uiState.selectedListing,
            viewModel = viewModel,
            onBackClick = { viewModel.navigateTo(DonateSubScreen.MY_DONATIONS_LIST) }
        )
    } else {
        DonationsListView(
            uiState = uiState,
            viewModel = viewModel,
            onBackClick = onBackClick,
            onStartNewDonation = onStartNewDonation,
            modifier = modifier
        )
    }
}

@Composable
private fun DonationsListView(
    uiState: DonateUiState,
    viewModel: DonateViewModel,
    onBackClick: () -> Unit,
    onStartNewDonation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeListings = remember(uiState.userListings) {
        uiState.userListings.filter { it.status == "available" || it.status == "claimed" }
    }
    val pastListings = remember(uiState.userListings) {
        uiState.userListings.filter { it.status == "completed" || it.status == "cancelled" }
    }

    val currentList = if (uiState.selectedListingTab == 0) activeListings else pastListings

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DeepText
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "My Donations",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepText,
                        fontSize = 20.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Rounded Segmented Pill Control for Active / Past
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFEFEFE8))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (uiState.selectedListingTab == 0) PrimaryForestGreen else Color.Transparent)
                        .clickable { viewModel.setSelectedListingTab(0) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Active (${activeListings.size})",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.selectedListingTab == 0) Color.White else PrimaryForestGreen,
                            fontSize = 14.sp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (uiState.selectedListingTab == 1) PrimaryForestGreen else Color.Transparent)
                        .clickable { viewModel.setSelectedListingTab(1) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Past (${pastListings.size})",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.selectedListingTab == 1) Color.White else PrimaryForestGreen,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (uiState.isLoadingListings) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryForestGreen)
                }
            } else if (currentList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    WelcomeFoodIllustration(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(160.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = if (uiState.selectedListingTab == 0) "No active donations yet" else "No past donations",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepText,
                            fontSize = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Start sharing surplus food and make a difference in your community.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = SecondaryText,
                            fontSize = 14.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onStartNewDonation,
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryForestGreen)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create a Listing", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentList, key = { it.id }) { listing ->
                        DonationItemCard(
                            listing = listing,
                            onClick = { viewModel.selectListingForDetails(listing) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DonationItemCard(
    listing: FoodListing,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFC8D6B9))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (listing.photoUrls.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(listing.photoUrls.first()),
                    contentDescription = null,
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SoftGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Restaurant, contentDescription = null, tint = PrimaryForestGreen, modifier = Modifier.size(32.dp))
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = listing.foodName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepText,
                            fontSize = 16.5.sp
                        ),
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when (listing.status) {
                                    "available" -> SoftGreen
                                    "claimed" -> Color(0xFFFFF3E0)
                                    "completed" -> Color(0xFFE3F2FD)
                                    else -> Color(0xFFFFEBEE)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = listing.status.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = when (listing.status) {
                                    "available" -> PrimaryForestGreen
                                    "claimed" -> Color(0xFFE65100)
                                    "completed" -> Color(0xFF1565C0)
                                    else -> Color(0xFFC62828)
                                },
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${listing.category} • ${listing.quantity.toInt()} ${listing.quantityUnit}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SecondaryText,
                        fontSize = 13.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (listing.status == "claimed") "Claimed by: ${listing.claimantName?.ifBlank { "Receiver" } ?: "Receiver"}" else "Available for pickup",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (listing.status == "claimed") Color(0xFFE65100) else SecondaryText,
                        fontWeight = if (listing.status == "claimed") FontWeight.Medium else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun DonationDetailView(
    listing: FoodListing,
    viewModel: DonateViewModel,
    onBackClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault()) }
    var showCancelConfirmDialog by remember { mutableStateOf(false) }
    var showConfirmHandoverDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    if (showCancelConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmDialog = false },
            title = { Text("Cancel Listing?", fontWeight = FontWeight.Bold, color = DeepText) },
            text = { Text("Are you sure you want to cancel this food listing? Nearby receivers will no longer see it.", color = SecondaryText) },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelConfirmDialog = false
                        viewModel.withdrawDonation(listing.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Cancel", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmDialog = false }) {
                    Text("Keep Active", color = PrimaryForestGreen, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showConfirmHandoverDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmHandoverDialog = false },
            title = { Text("Confirm Handover?", fontWeight = FontWeight.Bold, color = DeepText) },
            text = { Text("Please only confirm after you have physically handed over the food to the receiver.", color = SecondaryText, lineHeight = 20.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmHandoverDialog = false
                        viewModel.confirmHandover(listing.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryForestGreen)
                ) {
                    Text("Confirm Handover", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmHandoverDialog = false }) {
                    Text("Cancel", color = SecondaryText, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showWithdrawDialog) {
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = { Text("Withdraw Donation?", fontWeight = FontWeight.Bold, color = DeepText) },
            text = { Text("Are you sure you want to withdraw this donation? The claim will be cancelled.", color = SecondaryText, lineHeight = 20.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showWithdrawDialog = false
                        viewModel.withdrawDonation(listing.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Withdraw", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }) {
                    Text("Keep", color = PrimaryForestGreen, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Sticky Top Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WarmIvory)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DeepText)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Donation Details",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepText,
                            fontSize = 20.sp
                        )
                    )
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Menu", tint = DeepText)
                }
            }

            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Banner Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    if (listing.photoUrls.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(listing.photoUrls.first()),
                            contentDescription = "Food Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SoftGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Restaurant, contentDescription = null, tint = PrimaryForestGreen, modifier = Modifier.size(64.dp))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when (listing.status) {
                                    "available" -> PrimaryForestGreen
                                    "claimed" -> Color(0xFFE65100)
                                    "completed" -> Color(0xFF1565C0)
                                    else -> Color(0xFFC62828)
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = listing.status.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = listing.foodName,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepText,
                            fontSize = 22.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Restaurant, contentDescription = null, tint = PrimaryForestGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${listing.category}  •  ${listing.quantity.toInt()} ${listing.quantityUnit}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = DeepText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        )
                    }

                    if (listing.status == "claimed") {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Claimed by: ${listing.claimantName?.ifBlank { "Receiver" } ?: "Receiver"}",
                                style = MaterialTheme.typography.bodyMedium.copy(color = DeepText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = PrimaryForestGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = dateFormat.format(Date(listing.preparationDateTime)),
                            style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText, fontSize = 13.5.sp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = PrimaryForestGreen, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = listing.pickupAddress.ifBlank { "Location on map" },
                            style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText, fontSize = 13.5.sp, lineHeight = 19.sp)
                        )
                    }

                    // Two-Party Pickup Confirmation Progress Card (if claimed)
                    if (listing.status == "claimed") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SoftGreen),
                            border = BorderStroke(1.dp, Color(0xFFC8D6B9))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Pickup & Handover Status",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = PrimaryForestGreen, fontSize = 14.sp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (listing.receiverPickupConfirmed) Icons.Filled.CheckCircle else Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = if (listing.receiverPickupConfirmed) PrimaryForestGreen else SecondaryText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (listing.receiverPickupConfirmed) "Receiver pickup confirmed ✓" else "Waiting for receiver pickup confirmation",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (listing.receiverPickupConfirmed) PrimaryForestGreen else DeepText,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.5.sp
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (listing.donorHandoverConfirmed) Icons.Filled.CheckCircle else Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = if (listing.donorHandoverConfirmed) PrimaryForestGreen else SecondaryText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (listing.donorHandoverConfirmed) "Your handover confirmed ✓" else "Waiting for your handover confirmation",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (listing.donorHandoverConfirmed) PrimaryForestGreen else DeepText,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.5.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DeepText, fontSize = 15.sp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = listing.description.ifBlank { "No description provided." },
                        style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText, fontSize = 14.sp, lineHeight = 20.sp)
                    )

                    if (!listing.allergenNotes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Allergen Notes",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DeepText, fontSize = 15.sp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = listing.allergenNotes,
                            style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText, fontSize = 14.sp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Functional Action Buttons for Active / Claimed Listing
                    if (listing.status == "available") {
                        Button(
                            onClick = { showCancelConfirmDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Cancel,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Cancel Listing",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else if (listing.status == "claimed") {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (!listing.donorHandoverConfirmed) {
                                Button(
                                    onClick = { showConfirmHandoverDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryForestGreen)
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Confirm Handover", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }

                            if (!listing.donorHandoverConfirmed && !listing.receiverPickupConfirmed) {
                                OutlinedButton(
                                    onClick = { showWithdrawDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Withdraw Donation", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(90.dp).navigationBarsPadding())
                }
            }
        }
    }
}
