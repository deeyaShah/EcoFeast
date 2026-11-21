package com.college.ecofeast.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleEventObserver
import com.college.ecofeast.data.model.FoodListing
import com.college.ecofeast.ui.nearby.MapLocationPrompt
import com.college.ecofeast.ui.theme.DeepText
import com.college.ecofeast.ui.theme.PrimaryForestGreen
import com.college.ecofeast.ui.theme.SecondaryText
import com.college.ecofeast.ui.theme.SoftGreen
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

@Composable
fun HomeMapPreview(
    listings: List<FoodListing>,
    isLocationAvailable: Boolean,
    locationErrorMessage: String?,
    onPreviewClick: () -> Unit,
    onEnableLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    remember {
        try {
            MapLibre.getInstance(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        true
    }

    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onPreviewClick() }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = PrimaryForestGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Available Near You",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepText,
                        fontSize = 17.5.sp
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View Map",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = SecondaryText,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.5.sp
                    )
                )
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = SecondaryText,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Map Preview Card Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(18.dp))
                .clickable { onPreviewClick() },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SoftGreen.copy(alpha = 0.35f)),
            border = BorderStroke(1.dp, Color(0xFFC8D6B9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        MapView(ctx).apply {
                            onCreate(null)
                            getMapAsync { map ->
                                mapLibreMap = map
                                // Disable gestures on Home preview so scrolling works smoothly
                                map.uiSettings.setAllGesturesEnabled(false)
                                map.setStyle(Style.Builder().fromUri("https://tiles.openfreemap.org/styles/liberty")) { _ ->
                                    updateHomeMarkers(map, listings)
                                }

                                map.addOnMapClickListener { _ ->
                                    onPreviewClick()
                                    true
                                }
                            }
                        }
                    },
                    update = { _ ->
                        mapLibreMap?.let { map ->
                            updateHomeMarkers(map, listings)
                        }
                    }
                )

                // Lifecycle observer for MapView disposal
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, _ -> }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                // Location-off prompt overlay if location is unavailable
                if (!isLocationAvailable) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        MapLocationPrompt(
                            message = locationErrorMessage,
                            onEnableLocationClick = onEnableLocationClick
                        )
                    }
                }
            }
        }
    }
}

private fun updateHomeMarkers(
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
