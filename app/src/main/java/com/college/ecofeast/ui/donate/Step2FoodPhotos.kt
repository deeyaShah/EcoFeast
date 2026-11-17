package com.college.ecofeast.ui.donate

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.college.ecofeast.ui.theme.DeepText
import com.college.ecofeast.ui.theme.PrimaryForestGreen
import com.college.ecofeast.ui.theme.SecondaryText
import com.college.ecofeast.ui.theme.SoftGreen
import java.io.File

@Composable
fun Step2FoodPhotos(
    selectedPhotoPaths: List<String>,
    photoError: String?,
    onAddPhotoUri: (Uri) -> Unit,
    onRemovePhotoPath: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val uriSaver = Saver<Uri?, String>(
        save = { it?.toString() ?: "" },
        restore = { if (!it.isNullOrBlank()) Uri.parse(it) else null }
    )
    var tempCameraUri by rememberSaveable(stateSaver = uriSaver) { mutableStateOf<Uri?>(null) }
    var showCameraPermissionDialog by remember { mutableStateOf(false) }

    // Android Photo Picker launcher (Gallery)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onAddPhotoUri(uri)
        }
    }

    // Camera Intent launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val photoFile = File(context.cacheDir, "camera_capture.jpg")
        if (success && photoFile.exists() && photoFile.length() > 0) {
            val uri = Uri.fromFile(photoFile)
            onAddPhotoUri(uri)
        } else if (success && tempCameraUri != null) {
            onAddPhotoUri(tempCameraUri!!)
        }
    }

    fun launchCameraIntent() {
        try {
            val photoFile = File(context.cacheDir, "camera_capture.jpg")
            if (photoFile.exists()) photoFile.delete()
            photoFile.createNewFile()

            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, photoFile)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Camera Permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCameraIntent()
        } else {
            showCameraPermissionDialog = true
        }
    }

    fun handleCameraTap() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launchCameraIntent()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (showCameraPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showCameraPermissionDialog = false },
            title = {
                Text(
                    text = "Camera Permission Required",
                    fontWeight = FontWeight.Bold,
                    color = DeepText
                )
            },
            text = {
                Text(
                    text = "Camera permission is needed to capture fresh food photos directly inside EcoFeast. You can also pick existing photos from your Gallery.",
                    color = SecondaryText,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCameraPermissionDialog = false
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryForestGreen)
                ) {
                    Text("Grant Permission", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCameraPermissionDialog = false }) {
                    Text("Cancel", color = SecondaryText)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Add Food Photos",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = DeepText,
                fontSize = 22.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Upload clear photos of the food (1 to 5 photos).",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = SecondaryText,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Dashed Photo Dropzone Card matching Storyboard Panel 3
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SoftGreen.copy(alpha = 0.35f)),
            border = BorderStroke(1.5.dp, PrimaryForestGreen.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 14.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SoftGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddAPhoto,
                        contentDescription = null,
                        tint = PrimaryForestGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tap to add photos\nor choose from gallery",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = DeepText,
                        fontSize = 13.5.sp,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.height(42.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryForestGreen),
                        enabled = selectedPhotoPaths.size < 5
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gallery", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = { handleCameraTap() },
                        modifier = Modifier.height(42.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, PrimaryForestGreen),
                        enabled = selectedPhotoPaths.size < 5
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.AddAPhoto,
                                contentDescription = null,
                                tint = PrimaryForestGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Camera", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = PrimaryForestGreen)
                        }
                    }
                }
            }
        }

        if (photoError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = photoError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Selected Photos Count & Previews List
        Text(
            text = "Selected Photos (${selectedPhotoPaths.size}/5)",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = DeepText,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedPhotoPaths.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No photos added yet. At least 1 photo is required.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SecondaryText,
                        fontSize = 13.sp
                    )
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(selectedPhotoPaths) { path ->
                    Box(
                        modifier = Modifier.size(86.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(File(path)),
                            contentDescription = "Food Photo Thumbnail",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop
                        )

                        // Delete 'X' Badge matching Storyboard
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.75f))
                                .clickable { onRemovePhotoPath(path) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Remove Photo",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                if (selectedPhotoPaths.size < 5) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(86.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add More Photos",
                                tint = PrimaryForestGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
