package com.college.ecofeast.ui.donate

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.ecofeast.ui.theme.DeepText
import com.college.ecofeast.ui.theme.PrimaryForestGreen
import com.college.ecofeast.ui.theme.SecondaryText
import com.college.ecofeast.ui.theme.SoftGreen
import com.college.ecofeast.ui.theme.WarmIvory

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DonateWizardScreen(
    uiState: DonateUiState,
    viewModel: DonateViewModel,
    userName: String,
    userPhone: String,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WarmIvory)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Top Header Row matching Storyboard
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.goToPreviousStep() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DeepText
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Donate Food",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepText,
                            fontSize = 20.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Step Progress Indicator (Circles 1 to 5) matching Storyboard
                WizardStepProgressIndicator(
                    currentStep = uiState.wizardStep,
                    onStepClick = { step ->
                        if (step < uiState.wizardStep) {
                            viewModel.jumpToStep(step)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))
            }
        },
        bottomBar = {
            // Wizard Bottom Navigation: Previous & Next/Submit
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WarmIvory)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Button
                OutlinedButton(
                    onClick = { viewModel.goToPreviousStep() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.5.dp, PrimaryForestGreen)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = PrimaryForestGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Previous",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryForestGreen
                        )
                    }
                }

                // Next or Submit Button
                if (uiState.wizardStep < 5) {
                    Button(
                        onClick = { viewModel.goToNextStep() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryForestGreen,
                            contentColor = WarmIvory
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Next",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        containerColor = WarmIvory,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                AnimatedContent(
                    targetState = uiState.wizardStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "WizardStepTransition"
                ) { step ->
                    when (step) {
                        1 -> Step1BasicDetails(
                            foodName = uiState.foodName,
                            foodNameError = uiState.foodNameError,
                            onFoodNameChange = { viewModel.updateFoodName(it) },
                            category = uiState.category,
                            categoryError = uiState.categoryError,
                            onCategoryChange = { viewModel.updateCategory(it) },
                            description = uiState.description,
                            descriptionError = uiState.descriptionError,
                            onDescriptionChange = { viewModel.updateDescription(it) },
                            quantityText = uiState.quantityText,
                            quantityTextError = uiState.quantityTextError,
                            onQuantityTextChange = { viewModel.updateQuantityText(it) },
                            quantityUnit = uiState.quantityUnit,
                            onQuantityUnitChange = { viewModel.updateQuantityUnit(it) }
                        )
                        2 -> Step2FoodPhotos(
                            selectedPhotoPaths = uiState.selectedPhotoPaths,
                            photoError = uiState.photoError,
                            onAddPhotoUri = { viewModel.addPhotoUri(it) },
                            onRemovePhotoPath = { viewModel.removePhotoPath(it) }
                        )
                        3 -> Step3AdditionalDetails(
                            preparationTimestamp = uiState.preparationTimestamp,
                            preparationError = uiState.preparationError,
                            onPreparationTimestampChange = { viewModel.updatePreparationTimestamp(it) },
                            expiryTimestamp = uiState.expiryTimestamp,
                            expiryError = uiState.expiryError,
                            onExpiryTimestampChange = { viewModel.updateExpiryTimestamp(it) },
                            foodCondition = uiState.foodCondition,
                            onFoodConditionChange = { viewModel.updateFoodCondition(it) },
                            allergenNotes = uiState.allergenNotes,
                            onAllergenNotesChange = { viewModel.updateAllergenNotes(it) }
                        )
                        4 -> Step4PickupLocation(
                            pickupAddress = uiState.pickupAddress,
                            pickupAddressError = uiState.pickupAddressError,
                            onPickupAddressChange = { viewModel.updatePickupAddress(it) },
                            latitude = uiState.latitude,
                            longitude = uiState.longitude,
                            onLocationDetected = { lat, lng, addr -> viewModel.updateLocation(lat, lng, addr) },
                            pickupInstructions = uiState.pickupInstructions,
                            onPickupInstructionsChange = { viewModel.updatePickupInstructions(it) }
                        )
                        5 -> Step5ReviewSubmit(
                            foodName = uiState.foodName,
                            category = uiState.category,
                            description = uiState.description,
                            quantityText = uiState.quantityText,
                            quantityUnit = uiState.quantityUnit,
                            selectedPhotoPaths = uiState.selectedPhotoPaths,
                            preparationTimestamp = uiState.preparationTimestamp,
                            expiryTimestamp = uiState.expiryTimestamp,
                            foodCondition = uiState.foodCondition,
                            allergenNotes = uiState.allergenNotes,
                            pickupAddress = uiState.pickupAddress,
                            latitude = uiState.latitude,
                            longitude = uiState.longitude,
                            pickupInstructions = uiState.pickupInstructions,
                            isConfirmed = uiState.isConfirmed,
                            onConfirmedChange = { viewModel.setConfirmed(it) },
                            isSubmitting = uiState.isSubmitting,
                            submitError = uiState.submitError,
                            onSubmitClick = { viewModel.submitListing(userName) },
                            onJumpToStep = { viewModel.jumpToStep(it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun WizardStepProgressIndicator(
    currentStep: Int,
    onStepClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (step in 1..5) {
            val isCompleted = step < currentStep
            val isCurrent = step == currentStep

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> PrimaryForestGreen
                            isCurrent -> PrimaryForestGreen
                            else -> SoftGreen
                        }
                    )
                    .clickable(enabled = isCompleted) { onStepClick(step) },
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Step $step Completed",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = step.toString(),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrent) Color.White else SecondaryText,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            if (step < 5) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.5.dp)
                        .padding(horizontal = 4.dp)
                        .background(if (step < currentStep) PrimaryForestGreen else SoftGreen)
                )
            }
        }
    }
}
