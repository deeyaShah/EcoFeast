package com.college.ecofeast.ui

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.Search
import com.college.ecofeast.ui.nearby.NearbyMapView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.college.ecofeast.ui.auth.EcoFeastSnackbarHost
import com.college.ecofeast.ui.auth.FeedbackCategory
import com.college.ecofeast.ui.auth.FeedbackMessage
import com.college.ecofeast.ui.auth.showFeedback
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.college.ecofeast.data.model.FoodListing
import com.college.ecofeast.ui.nearby.NearbyUiState
import com.college.ecofeast.ui.nearby.NearbyViewModel
import com.college.ecofeast.ui.theme.DeepText
import com.college.ecofeast.ui.theme.PrimaryForestGreen
import com.college.ecofeast.ui.theme.SecondaryText
import com.college.ecofeast.ui.theme.SoftGreen
import com.college.ecofeast.ui.theme.WarmIvory
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Polished Nearby Food Discovery Screen with Compact Layout, Privacy Protection,
 * Filter Bottom Sheet, and Card Entrance & Interaction Animations.
 */
@Composable
fun NearbyScreen(
    modifier: Modifier = Modifier,
    isGuest: Boolean = false,
    onSignInClick: () -> Unit = {},
    onClaimAsGuest: (String) -> Unit = {},
    viewModel: NearbyViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showFilterBottomSheet by remember { mutableStateOf(false) }
    var showClaimConfirmDialog by remember { mutableStateOf(false) }

    // Handle claim feedback notifications
    LaunchedEffect(uiState.claimError, uiState.claimSuccessMessage) {
        val err = uiState.claimError
        val successMsg = uiState.claimSuccessMessage
        if (err != null) {
            snackbarHostState.showFeedback(FeedbackMessage(err, FeedbackCategory.AUTHENTICATION))
            viewModel.clearClaimState()
        } else if (successMsg != null) {
            snackbarHostState.showFeedback(FeedbackMessage(successMsg, FeedbackCategory.SUCCESS))
            viewModel.clearClaimState()
        }
    }

    // Helper: Internal Fresh Location Fetching
    fun fetchFreshLocationInternal() {
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cancelTokenSource = CancellationTokenSource()

            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancelTokenSource.token)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        viewModel.updateReceiverLocation(loc.latitude, loc.longitude)
                    } else {
                        // Fallback 1: lastLocation
                        fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                            if (lastLoc != null) {
                                viewModel.updateReceiverLocation(lastLoc.latitude, lastLoc.longitude)
                            } else {
                                // Fallback 2: Single location update request
                                try {
                                    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
                                    val callback = object : LocationCallback() {
                                        override fun onLocationResult(result: LocationResult) {
                                            val freshLoc = result.lastLocation
                                            if (freshLoc != null) {
                                                viewModel.updateReceiverLocation(freshLoc.latitude, freshLoc.longitude)
                                            } else {
                                                viewModel.setLocationError("Unable to obtain fresh location fix.")
                                            }
                                            fusedClient.removeLocationUpdates(this)
                                        }
                                    }
                                    fusedClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
                                } catch (ex: SecurityException) {
                                    viewModel.setLocationError("Location permission required.")
                                } catch (ex: Exception) {
                                    viewModel.setLocationError("Unable to access location services.")
                                }
                            }
                        }.addOnFailureListener {
                            viewModel.setLocationError("Unable to fetch location.")
                        }
                    }
                }
                .addOnFailureListener {
                    // Fallback to lastLocation
                    fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                        if (lastLoc != null) {
                            viewModel.updateReceiverLocation(lastLoc.latitude, lastLoc.longitude)
                        } else {
                            viewModel.setLocationError("Unable to obtain fresh location fix.")
                        }
                    }.addOnFailureListener {
                        viewModel.setLocationError("Unable to fetch location.")
                    }
                }
        } catch (e: SecurityException) {
            viewModel.setLocationError("Location permission required.")
        } catch (e: Exception) {
            viewModel.setLocationError("Unable to access location services.")
        }
    }

    // Android Location Settings Resolution Launcher (GPS Resolution Dialog)
    val locationSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            fetchFreshLocationInternal()
        } else {
            viewModel.setLocationError("Device location is turned off. You can still browse listings.")
        }
    }

    // Settings Check: Triggers Google System Resolution Dialog if GPS is OFF
    fun checkSettingsAndFetchLocation() {
        viewModel.setLocatingReceiver(true)

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        val settingsBuilder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(context)

        client.checkLocationSettings(settingsBuilder.build())
            .addOnSuccessListener {
                fetchFreshLocationInternal()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                        locationSettingsLauncher.launch(intentSenderRequest)
                    } catch (sendEx: Exception) {
                        viewModel.setLocationError("Please turn on location in phone Settings.")
                    }
                } else {
                    viewModel.setLocationError("Device location services are unavailable.")
                }
            }
    }

    // Location Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        if (granted) {
            checkSettingsAndFetchLocation()
        } else {
            viewModel.setLocationError("Location permission is required to calculate distances.")
        }
    }

    // Main Detect Location Tap Handler
    fun handleDetectLocationTap() {
        if (uiState.isLocatingReceiver) return

        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            checkSettingsAndFetchLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Real-Time Location State Monitoring: BroadcastReceiver + Lifecycle Observer
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

        val locationStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION ||
                    intent?.action == "android.location.MODE_CHANGED") {

                    val isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                            locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true

                    val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

                    if (!isGpsEnabled) {
                        viewModel.clearReceiverLocation()
                    } else if (hasFine || hasCoarse) {
                        if (uiState.receiverLatitude == null && !uiState.isLocatingReceiver) {
                            fetchFreshLocationInternal()
                        }
                    }
                }
            }
        }

        val intentFilter = IntentFilter().apply {
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            addAction("android.location.MODE_CHANGED")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(locationStateReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(locationStateReceiver, intentFilter)
        }

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                        locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true

                val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

                if (!hasFine && !hasCoarse) {
                    viewModel.clearReceiverLocation()
                } else if (!isGpsEnabled) {
                    viewModel.clearReceiverLocation()
                } else if (uiState.receiverLatitude == null && !uiState.isLocatingReceiver) {
                    fetchFreshLocationInternal()
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            try {
                context.unregisterReceiver(locationStateReceiver)
            } catch (e: Exception) {
                // Ignore unregister exceptions
            }
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header & Search Compact Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                // Compact Header with Integrated Location Indicator Chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Available Food Nearby",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepText,
                                fontSize = 18.5.sp
                            )
                        )
                        Text(
                            text = "Community surplus food rescue",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SecondaryText,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Compact Location Status Indicator Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (uiState.receiverLatitude != null) SoftGreen
                                else Color(0xFFF5EFE6)
                            )
                            .border(
                                1.dp,
                                if (uiState.receiverLatitude != null) PrimaryForestGreen.copy(alpha = 0.3f)
                                else Color(0xFFD9D2C9),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { handleDetectLocationTap() }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.isLocatingReceiver) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    color = PrimaryForestGreen,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Locating...",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = PrimaryForestGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                )
                            } else if (uiState.receiverLatitude != null) {
                                Icon(
                                    imageVector = Icons.Filled.NearMe,
                                    contentDescription = null,
                                    tint = PrimaryForestGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Location On",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = PrimaryForestGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.MyLocation,
                                    contentDescription = null,
                                    tint = SecondaryText,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Location Off",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SecondaryText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }
                    }
                }

                if (uiState.locationErrorMessage != null) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = uiState.locationErrorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar Input
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    placeholder = {
                        Text(
                            text = "Search food name, category or address...",
                            color = SecondaryText,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search Icon",
                            tint = PrimaryForestGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear Search",
                                    tint = SecondaryText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PrimaryForestGreen,
                        unfocusedBorderColor = Color(0xFFC8D6B9)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Compact Controls Row: Single Row for Filter Button + List/Map Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Filter Entry Button (shows active filter count if > 0)
                    val activeCount = uiState.activeFilterCount
                    val filterButtonText = if (activeCount > 0) "Filters ($activeCount)" else "Filters"

                    OutlinedButton(
                        onClick = { showFilterBottomSheet = true },
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (activeCount > 0) SoftGreen else Color.White
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (activeCount > 0) PrimaryForestGreen else Color(0xFFC8D6B9)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = null,
                            tint = PrimaryForestGreen,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = filterButtonText,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryForestGreen
                        )
                    }

                    // List View / Map View Segmented Selector beside Filter Button
                    Row(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFEFEFE8))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // List Segment
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (uiState.selectedViewMode == 0) PrimaryForestGreen else Color.Transparent)
                                .clickable { viewModel.selectViewMode(0) }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.FormatListBulleted,
                                    contentDescription = null,
                                    tint = if (uiState.selectedViewMode == 0) Color.White else PrimaryForestGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "List",
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.selectedViewMode == 0) Color.White else PrimaryForestGreen,
                                    fontSize = 12.5.sp
                                )
                            }
                        }

                        // Map Segment (Phase 2 Preview)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (uiState.selectedViewMode == 1) PrimaryForestGreen else Color.Transparent)
                                .clickable { viewModel.selectViewMode(1) }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Map,
                                    contentDescription = null,
                                    tint = if (uiState.selectedViewMode == 1) Color.White else PrimaryForestGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Map",
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.selectedViewMode == 1) Color.White else PrimaryForestGreen,
                                    fontSize = 12.5.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Content Body: List View vs Map View
            if (uiState.selectedViewMode == 1) {
                NearbyMapView(
                    uiState = uiState,
                    viewModel = viewModel,
                    onViewDetailsClick = { listing ->
                        viewModel.selectListing(listing)
                    },
                    onEnableLocationClick = {
                        handleDetectLocationTap()
                    }
                )
            } else {
                // List View: Maximum vertical space for Food Cards
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryForestGreen)
                    }
                } else if (uiState.filteredListings.isEmpty()) {
                    // Clean Empty State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        MapEmptyStateIllustration(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(140.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (uiState.searchQuery.isNotBlank() || uiState.activeFilterCount > 0)
                                "No Matching Food Listings"
                            else
                                "No Available Food Nearby",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryForestGreen,
                                fontSize = 17.5.sp
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (uiState.searchQuery.isNotBlank() || uiState.activeFilterCount > 0)
                                "Try searching for a different keyword or resetting your filters."
                            else
                                "Check back soon as community members and local partners share surplus food.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = SecondaryText,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            ),
                            textAlign = TextAlign.Center
                        )

                        if (uiState.searchQuery.isNotBlank() || uiState.activeFilterCount > 0) {
                            Spacer(modifier = Modifier.height(18.dp))
                            OutlinedButton(
                                onClick = { viewModel.clearFilters() },
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.5.dp, PrimaryForestGreen)
                            ) {
                                Text("Clear Search & Filters", fontWeight = FontWeight.Bold, color = PrimaryForestGreen, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(uiState.filteredListings, key = { it.id }) { listing ->
                            NearbyFoodCard(
                                modifier = Modifier.animateItem(
                                    placementSpec = tween(durationMillis = 250),
                                    fadeInSpec = tween(durationMillis = 250),
                                    fadeOutSpec = tween(durationMillis = 250)
                                ),
                                listing = listing,
                                distanceStr = uiState.distancesMap[listing.id],
                                onViewDetailsClick = { viewModel.selectListing(listing) }
                            )
                        }
                    }
                }
            }
        }

        // Compact Filter & Sorting Bottom Sheet
        if (showFilterBottomSheet) {
            FilterBottomSheet(
                uiState = uiState,
                viewModel = viewModel,
                onDismiss = { showFilterBottomSheet = false }
            )
        }

        // Food Details Modal Bottom Sheet
        if (uiState.selectedListing != null) {
            FoodDetailsBottomSheet(
                listing = uiState.selectedListing!!,
                distanceStr = uiState.distancesMap[uiState.selectedListing!!.id],
                isGuest = isGuest,
                currentUserId = FirebaseAuth.getInstance().currentUser?.uid,
                onDismiss = { viewModel.selectListing(null) },
                onClaimClick = {
                    val currentListing = uiState.selectedListing
                    if (currentListing != null) {
                        if (isGuest) {
                            val listingId = currentListing.id
                            viewModel.selectListing(null)
                            onClaimAsGuest(listingId)
                        } else {
                            showClaimConfirmDialog = true
                        }
                    }
                }
            )
        }

        // Claim Confirmation Dialog
        if (showClaimConfirmDialog && uiState.selectedListing != null) {
            ClaimConfirmationDialog(
                listing = uiState.selectedListing!!,
                isClaiming = uiState.isClaiming,
                onConfirm = {
                    val targetListing = uiState.selectedListing!!
                    viewModel.claimListing(targetListing) {
                        showClaimConfirmDialog = false
                    }
                },
                onDismiss = {
                    if (!uiState.isClaiming) {
                        showClaimConfirmDialog = false
                    }
                }
            )
        }

        // Snackbar Host for Claim Feedback
        EcoFeastSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

/**
 * Shared Food Details Modal Bottom Sheet.
 * Displays food information, protects donor phone numbers, and supports Instant Claim.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoodDetailsBottomSheet(
    listing: FoodListing,
    distanceStr: String?,
    isGuest: Boolean,
    currentUserId: String?,
    onDismiss: () -> Unit,
    onClaimClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault()) }
    val context = LocalContext.current

    val isExpired = listing.expiryDateTime != null && listing.expiryDateTime <= System.currentTimeMillis()
    val isAvailable = listing.status == "available" && !isExpired
    val isDonor = !currentUserId.isNullOrBlank() && listing.donorId == currentUserId

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = WarmIvory,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(bottom = 32.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DeepText)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Food Details",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepText,
                        fontSize = 20.sp
                    )
                )
            }

            // Banner Image with Gentle Crossfade
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
            ) {
                if (listing.photoUrls.isNotEmpty()) {
                    val bannerPainter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(context)
                            .data(listing.photoUrls.first())
                            .crossfade(true)
                            .crossfade(250)
                            .build()
                    )
                    Image(
                        painter = bannerPainter,
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

                // Available Status Overlay Badge
                val badgeText = when {
                    isExpired -> "Expired"
                    listing.status == "available" -> "Available"
                    else -> listing.status.replaceFirstChar { it.uppercase() }
                }
                val badgeBgColor = when {
                    isExpired -> Color(0xFFD9774A)
                    listing.status == "available" -> PrimaryForestGreen
                    listing.status == "claimed" -> Color(0xFFE6B84A)
                    listing.status == "completed" -> Color(0xFF1565C0)
                    else -> Color(0xFFC62828)
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeBgColor)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details Content Body
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
                        fontSize = 21.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Restaurant, contentDescription = null, tint = PrimaryForestGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${listing.category}  •  ${listing.quantity.toInt()} ${listing.quantityUnit}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = DeepText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = PrimaryForestGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Prepared: ${dateFormat.format(Date(listing.preparationDateTime))}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText, fontSize = 13.5.sp)
                    )
                }

                if (listing.expiryDateTime != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = Color(0xFFD9774A), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Best Before: ${dateFormat.format(Date(listing.expiryDateTime))}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFD9774A), fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = PrimaryForestGreen, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = listing.pickupAddress.ifBlank { "Pickup address on map" },
                            style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText, fontSize = 13.5.sp, lineHeight = 19.sp)
                        )
                        if (distanceStr != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = distanceStr,
                                style = MaterialTheme.typography.bodySmall.copy(color = PrimaryForestGreen, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
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

                if (!listing.pickupInstructions.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Pickup Instructions",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DeepText, fontSize = 15.sp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = listing.pickupInstructions,
                        style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText, fontSize = 14.sp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Donor Info Card (Protected: Phone number removed, only display name shown)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftGreen.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, Color(0xFFC8D6B9))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PrimaryForestGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Shared by: ${listing.donorName.ifBlank { "EcoFeast Donor" }}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepText,
                                    fontSize = 14.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "EcoFeast Community Donor",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SecondaryText,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                if (isDonor) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SoftGreen)
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🌿 Your Donation Listing",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryForestGreen,
                                fontSize = 14.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(23.dp),
                        border = BorderStroke(1.5.dp, PrimaryForestGreen)
                    ) {
                        Text("Close Details", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = PrimaryForestGreen)
                    }
                } else if (isAvailable) {
                    Button(
                        onClick = onClaimClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryForestGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isGuest) "Sign In to Claim Food" else "Claim Food Instant",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close Details", fontSize = 14.sp, color = SecondaryText, fontWeight = FontWeight.Medium)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFEEEEEE))
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isExpired) "This food listing has expired" else "This listing is ${listing.status}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SecondaryText,
                                fontSize = 14.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(23.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryForestGreen)
                    ) {
                        Text("Close Details", fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Confirmation dialog shown before instant claim execution.
 */
@Composable
private fun ClaimConfirmationDialog(
    listing: FoodListing,
    isClaiming: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = {
            if (!isClaiming) onDismiss()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = PrimaryForestGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Confirm Instant Claim",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepText,
                        fontSize = 18.sp
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "You are reserving this complete food listing. No donor approval is required.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SecondaryText,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftGreen.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, Color(0xFFC8D6B9))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = listing.foodName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepText,
                                fontSize = 15.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${listing.category} • ${listing.quantity.toInt()} ${listing.quantityUnit}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PrimaryForestGreen,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "📍 Pickup: ${listing.pickupAddress.ifBlank { "Location specified by donor" }}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SecondaryText,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        )
                        if (listing.expiryDateTime != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "⏰ Best Before: ${dateFormat.format(Date(listing.expiryDateTime))}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFD9774A),
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }

                if (!listing.allergenNotes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Allergens: ${listing.allergenNotes}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFD9774A),
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                }

                if (!listing.pickupInstructions.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pickup Info: ${listing.pickupInstructions}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SecondaryText,
                            fontSize = 12.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFF3E0))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "⚠️ By confirming, you commit to picking up this food promptly and safely. Partial claims are not supported.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFE65100),
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp
                        )
                    )
                }

                if (isClaiming) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = PrimaryForestGreen,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Reserving food listing...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = PrimaryForestGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isClaiming,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryForestGreen,
                    contentColor = WarmIvory
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirm Claim", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isClaiming
            ) {
                Text("Go Back", color = SecondaryText, fontWeight = FontWeight.Medium)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Filter & Sorting Modal Bottom Sheet containing all filter categories,
 * condition filters, sorting modes, and clear/reset options.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FilterBottomSheet(
    uiState: NearbyUiState,
    viewModel: NearbyViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = WarmIvory,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter & Sort Food",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepText,
                        fontSize = 19.sp
                    )
                )

                if (uiState.activeFilterCount > 0) {
                    TextButton(onClick = { viewModel.clearFilters() }) {
                        Text(
                            text = "Reset All",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Section 1: Food Category
                Text(
                    text = "Food Category",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepText,
                        fontSize = 14.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    viewModel.categories.forEach { category ->
                        val isSelected = category.equals(uiState.selectedCategory, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) PrimaryForestGreen else Color.White)
                                .border(
                                    BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) PrimaryForestGreen else Color(0xFFC8D6B9)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { viewModel.selectCategory(category) }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else DeepText,
                                    fontSize = 12.5.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 2: Food Condition
                Text(
                    text = "Food Condition",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepText,
                        fontSize = 14.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    viewModel.conditions.forEach { condition ->
                        val isSelected = condition.equals(uiState.selectedCondition, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) PrimaryForestGreen else Color.White)
                                .border(
                                    BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) PrimaryForestGreen else Color(0xFFC8D6B9)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { viewModel.selectCondition(condition) }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = condition,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else DeepText,
                                    fontSize = 12.5.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 3: Sort Order
                Text(
                    text = "Sort By",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepText,
                        fontSize = 14.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    viewModel.sortOptions.forEach { sort ->
                        val isSelected = sort.equals(uiState.sortBy, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) PrimaryForestGreen else Color.White)
                                .border(
                                    BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) PrimaryForestGreen else Color(0xFFC8D6B9)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { viewModel.selectSortBy(sort) }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sort,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else DeepText,
                                    fontSize = 12.5.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryForestGreen)
                ) {
                    Text(
                        text = if (uiState.activeFilterCount > 0) "Apply Filters (${uiState.activeFilterCount})" else "Apply Filters",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Polished Food Card with Press Interaction Feedback, Coil Image Crossfade,
 * Distance Animation, and Quantity Badge.
 */
@Composable
private fun NearbyFoodCard(
    modifier: Modifier = Modifier,
    listing: FoodListing,
    distanceStr: String?,
    onViewDetailsClick: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault()) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "CardPressScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onViewDetailsClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFC8D6B9))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Image Section with Quantity Badge & Coil Gentle Crossfade
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(165.dp)
            ) {
                if (listing.photoUrls.isNotEmpty()) {
                    val imagePainter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(context)
                            .data(listing.photoUrls.first())
                            .crossfade(true)
                            .crossfade(250)
                            .build()
                    )
                    Image(
                        painter = imagePainter,
                        contentDescription = listing.foodName,
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
                        Icon(
                            imageVector = Icons.Filled.Restaurant,
                            contentDescription = null,
                            tint = PrimaryForestGreen,
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }

                // TOP-LEFT QUANTITY BADGE
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryForestGreen)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${listing.quantity.toInt()} ${listing.quantityUnit}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.5.sp
                        )
                    )
                }

                // Top-Right Food Condition Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.90f))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = listing.foodCondition,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepText,
                            fontSize = 11.5.sp
                        )
                    )
                }
            }

            // Card Information Details
            Column(
                modifier = Modifier.padding(14.dp)
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
                            fontSize = 17.sp
                        ),
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = listing.category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryForestGreen,
                            fontSize = 12.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                // Pickup Address Row
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = PrimaryForestGreen,
                        modifier = Modifier
                            .size(15.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = listing.pickupAddress.ifBlank { "Pickup address specified on detail view" },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SecondaryText,
                            fontSize = 12.5.sp,
                            lineHeight = 17.sp
                        ),
                        maxLines = 2,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Smoothly Animated Distance Row
                AnimatedVisibility(
                    visible = distanceStr != null,
                    enter = fadeIn(animationSpec = tween(200)) + expandVertically(animationSpec = tween(200)),
                    exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(animationSpec = tween(200))
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.NearMe,
                                contentDescription = null,
                                tint = PrimaryForestGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = distanceStr ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = PrimaryForestGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Date Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = SecondaryText,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dateFormat.format(Date(listing.preparationDateTime)),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SecondaryText,
                            fontSize = 12.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // View Details Action Button
                Button(
                    onClick = onViewDetailsClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryForestGreen)
                ) {
                    Text(
                        text = "View Details",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
