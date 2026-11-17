package com.college.ecofeast.ui.donate

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.college.ecofeast.ui.theme.DeepText
import com.college.ecofeast.ui.theme.PrimaryForestGreen
import com.college.ecofeast.ui.theme.SecondaryText
import com.college.ecofeast.ui.theme.SoftGreen
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun Step4PickupLocation(
    pickupAddress: String,
    pickupAddressError: String?,
    onPickupAddressChange: (String) -> Unit,
    latitude: Double?,
    longitude: Double?,
    onLocationDetected: (Double, Double, String?) -> Unit,
    pickupInstructions: String,
    onPickupInstructionsChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLocating by remember { mutableStateOf(false) }
    var locationErrorMsg by remember { mutableStateOf<String?>(null) }

    fun fetchLocationInternal() {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val cancelTokenSource = CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancelTokenSource.token)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        val lat = loc.latitude
                        val lng = loc.longitude

                        scope.launch {
                            val addressName = withContext(Dispatchers.IO) {
                                try {
                                    val geocoder = Geocoder(context, Locale.getDefault())
                                    @Suppress("DEPRECATION")
                                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                                    if (!addresses.isNullOrEmpty()) {
                                        val addr = addresses[0]
                                        val featureName = addr.featureName
                                        val locality = addr.locality ?: addr.subAdminArea
                                        val addressLine = addr.getAddressLine(0)
                                        addressLine ?: "$featureName, $locality"
                                    } else null
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            isLocating = false
                            onLocationDetected(lat, lng, addressName)
                        }
                    } else {
                        // Fallback to lastLocation if getCurrentLocation returned null
                        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                            isLocating = false
                            if (lastLoc != null) {
                                onLocationDetected(lastLoc.latitude, lastLoc.longitude, null)
                            } else {
                                locationErrorMsg = "Unable to fetch location automatically. Please enter address manually."
                            }
                        }.addOnFailureListener {
                            isLocating = false
                            locationErrorMsg = "Unable to fetch location. Please enter address manually."
                        }
                    }
                }
                .addOnFailureListener {
                    isLocating = false
                    locationErrorMsg = "Location service error. You can still enter your address manually."
                }
        } catch (e: SecurityException) {
            isLocating = false
            locationErrorMsg = "Location permission is required to detect current location."
        } catch (e: Exception) {
            isLocating = false
            locationErrorMsg = "Unable to access location services."
        }
    }

    // System resolution dialog launcher for turning on location services (GPS)
    val locationSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            fetchLocationInternal()
        } else {
            isLocating = false
            locationErrorMsg = "Device location is turned off. You can still enter your address manually."
        }
    }

    fun checkSettingsAndFetchLocation() {
        isLocating = true
        locationErrorMsg = null

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()
        val settingsBuilder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(context)

        client.checkLocationSettings(settingsBuilder.build())
            .addOnSuccessListener {
                fetchLocationInternal()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                        locationSettingsLauncher.launch(intentSenderRequest)
                    } catch (sendEx: Exception) {
                        isLocating = false
                        locationErrorMsg = "Please enable location services in Settings to use current location."
                    }
                } else {
                    isLocating = false
                    locationErrorMsg = "Location services are disabled on this device. You can enter address manually."
                }
            }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            checkSettingsAndFetchLocation()
        } else {
            isLocating = false
            locationErrorMsg = "Location permission is required to detect current location."
        }
    }

    fun handleLocationTap() {
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

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Pickup Location",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = DeepText,
                fontSize = 22.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Where can people collect the food?",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = SecondaryText,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Pickup Address *
        Text(
            text = "Pickup Address *",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = DeepText,
                fontSize = 14.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = pickupAddress,
            onValueChange = onPickupAddressChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter your address", color = SecondaryText) },
            isError = pickupAddressError != null,
            supportingText = pickupAddressError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = PrimaryForestGreen,
                unfocusedBorderColor = Color(0xFFC8D6B9)
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Use Current Location Action Button matching Storyboard
        OutlinedButton(
            onClick = { handleLocationTap() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.5.dp, PrimaryForestGreen)
        ) {
            if (isLocating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = PrimaryForestGreen,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Detecting location...", fontSize = 14.sp, color = PrimaryForestGreen, fontWeight = FontWeight.Bold)
            } else {
                Icon(
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = null,
                    tint = PrimaryForestGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Use current location", fontSize = 14.sp, color = PrimaryForestGreen, fontWeight = FontWeight.Bold)
            }
        }

        if (locationErrorMsg != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = locationErrorMsg!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Location Pin Preview Card
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
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(PrimaryForestGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (pickupAddress.isNotBlank()) pickupAddress else "No address selected yet",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepText,
                            fontSize = 13.5.sp
                        )
                    )
                    if (latitude != null && longitude != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "GPS Coordinates: %.4f, %.4f".format(latitude, longitude),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SecondaryText,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Pickup Instructions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pickup Instructions",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepText,
                    fontSize = 14.sp
                )
            )
            Text(
                text = "${pickupInstructions.length}/200",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SecondaryText,
                    fontSize = 12.sp
                )
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = pickupInstructions,
            onValueChange = onPickupInstructionsChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(95.dp),
            placeholder = { Text("e.g. Ring the bell, call before coming... (optional)", color = SecondaryText) },
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
