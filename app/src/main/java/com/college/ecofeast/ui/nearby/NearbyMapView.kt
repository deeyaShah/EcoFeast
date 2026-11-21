package com.college.ecofeast.ui.nearby

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.college.ecofeast.data.model.FoodListing
import com.college.ecofeast.ui.theme.PrimaryForestGreen
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

@Composable
fun NearbyMapView(
    uiState: NearbyUiState,
    viewModel: NearbyViewModel,
    onViewDetailsClick: (FoodListing) -> Unit,
    onEnableLocationClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Initialize MapLibre context
    remember {
        try {
            MapLibre.getInstance(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        true
    }

    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var selectedMapPreviewListing by remember { mutableStateOf<FoodListing?>(null) }
    var hasCenteredOnUser by remember { mutableStateOf(false) }

    // Keep map preview synced or cleared if filtered listings change
    LaunchedEffect(uiState.filteredListings) {
        if (selectedMapPreviewListing != null && !uiState.filteredListings.any { it.id == selectedMapPreviewListing?.id }) {
            selectedMapPreviewListing = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    onCreate(null)
                    getMapAsync { map ->
                        mapLibreMap = map
                        map.setStyle(Style.Builder().fromUri("https://tiles.openfreemap.org/styles/liberty")) { style ->
                            updateMapMarkers(map, uiState.filteredListings)
                        }

                        map.setOnMarkerClickListener { marker ->
                            val listingId = marker.snippet
                            val listing = uiState.filteredListings.find { it.id == listingId }
                            if (listing != null) {
                                selectedMapPreviewListing = listing
                                map.animateCamera(
                                    CameraUpdateFactory.newLatLng(
                                        LatLng(marker.position.latitude, marker.position.longitude)
                                    ),
                                    500
                                )
                            }
                            true
                        }

                        map.addOnMapClickListener { latLng ->
                            selectedMapPreviewListing = null
                            false
                        }
                    }
                }
            },
            update = { mapView ->
                mapLibreMap?.let { map ->
                    updateMapMarkers(map, uiState.filteredListings)
                }

                if (!hasCenteredOnUser && uiState.receiverLatitude != null && uiState.receiverLongitude != null) {
                    mapLibreMap?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(uiState.receiverLatitude, uiState.receiverLongitude),
                            14.0
                        ),
                        1000
                    )
                    hasCenteredOnUser = true
                }
            }
        )

        // Lifecycle observer for MapView
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                // Handle lifecycle if needed
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        // Compact Floating Location Prompt when receiver location is unavailable
        AnimatedVisibility(
            visible = uiState.receiverLatitude == null && !uiState.isLocatingReceiver,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            MapLocationPrompt(
                message = uiState.locationErrorMessage,
                onEnableLocationClick = onEnableLocationClick
            )
        }

        // Recenter Button
        if (uiState.receiverLatitude != null && uiState.receiverLongitude != null) {
            FloatingActionButton(
                onClick = {
                    mapLibreMap?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(uiState.receiverLatitude, uiState.receiverLongitude),
                            15.0
                        ),
                        800
                    )
                },
                containerColor = Color.White,
                contentColor = PrimaryForestGreen,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = "Recenter on My Location",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Floating Food Preview Card at bottom of map
        AnimatedVisibility(
            visible = selectedMapPreviewListing != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            selectedMapPreviewListing?.let { listing ->
                MapFoodPreviewCard(
                    listing = listing,
                    distanceStr = uiState.distancesMap[listing.id],
                    onClose = { selectedMapPreviewListing = null },
                    onViewDetails = {
                        onViewDetailsClick(listing)
                    }
                )
            }
        }
    }
}

private fun updateMapMarkers(
    map: MapLibreMap,
    listings: List<FoodListing>
) {
    map.clear()
    for (listing in listings) {
        if (listing.latitude != null && listing.longitude != null) {
            val latLng = LatLng(listing.latitude, listing.longitude)
            val markerOptions = MarkerOptions()
                .position(latLng)
                .title(listing.foodName)
                .snippet(listing.id)
            map.addMarker(markerOptions)
        }
    }
}

@Composable
private fun MapFoodPreviewCard(
    listing: FoodListing,
    distanceStr: String?,
    onClose: () -> Unit,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food Image
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEFEFE8))
            ) {
                if (listing.photoUrls.isNotEmpty()) {
                    AsyncImage(
                        model = listing.photoUrls.first(),
                        contentDescription = listing.foodName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(
                modifier = Modifier.weight(1.8f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = listing.category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PrimaryForestGreen,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    if (distanceStr != null) {
                        Surface(
                            color = PrimaryForestGreen.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = distanceStr,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PrimaryForestGreen,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = listing.foodName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF2C3E50)
                    ),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${listing.quantity.toInt()} ${listing.quantityUnit} • ${listing.pickupAddress.ifBlank { "Pickup location" }}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray,
                        fontSize = 13.sp
                    ),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Actions (Close & View Details button)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close Preview",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onViewDetails,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryForestGreen),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "View",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
