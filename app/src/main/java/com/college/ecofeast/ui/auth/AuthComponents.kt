package com.college.ecofeast.ui.auth

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.ecofeast.R
import com.college.ecofeast.ui.theme.DeepText
import com.college.ecofeast.ui.theme.PrimaryForestGreen
import com.college.ecofeast.ui.theme.SecondaryText
import com.college.ecofeast.ui.theme.WarmIvory

// Shared Vector Icons
val ArrowBackIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "ArrowBack",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(20f, 11f)
            lineTo(7.83f, 11f)
            lineTo(13.42f, 5.41f)
            lineTo(12f, 4f)
            lineTo(4f, 12f)
            lineTo(12f, 20f)
            lineTo(13.41f, 18.59f)
            lineTo(7.83f, 13f)
            lineTo(20f, 13f)
            close()
        }
    }.build()
}

val PersonIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Person",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(PrimaryForestGreen)) {
            moveTo(12f, 12f)
            curveTo(14.21f, 12f, 16f, 10.21f, 16f, 8f)
            curveTo(16f, 5.79f, 14.21f, 4f, 12f, 4f)
            curveTo(9.79f, 4f, 8f, 5.79f, 8f, 8f)
            curveTo(8f, 10.21f, 9.79f, 12f, 12f, 12f)
            close()
            moveTo(12f, 14f)
            curveTo(9.33f, 14f, 4f, 15.34f, 4f, 18f)
            lineTo(4f, 20f)
            lineTo(20f, 20f)
            lineTo(20f, 18f)
            curveTo(20f, 15.34f, 14.67f, 14f, 12f, 14f)
            close()
        }
    }.build()
}

val EmailIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Email",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(PrimaryForestGreen)) {
            moveTo(20f, 4f)
            lineTo(4f, 4f)
            curveTo(2.9f, 4f, 2.01f, 4.9f, 2.01f, 6f)
            lineTo(2f, 18f)
            curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f)
            lineTo(20f, 20f)
            curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
            lineTo(22f, 6f)
            curveTo(22f, 4.9f, 21.1f, 4f, 20f, 4f)
            close()
            moveTo(20f, 8f)
            lineTo(12f, 13f)
            lineTo(4f, 8f)
            lineTo(4f, 6f)
            lineTo(12f, 11f)
            lineTo(20f, 6f)
            lineTo(20f, 8f)
            close()
        }
    }.build()
}

val PhoneIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Phone",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(PrimaryForestGreen)) {
            moveTo(6.62f, 10.79f)
            curveTo(8.06f, 13.62f, 10.38f, 15.94f, 13.21f, 17.38f)
            lineTo(15.41f, 15.18f)
            curveTo(15.69f, 14.9f, 16.08f, 14.82f, 16.43f, 14.93f)
            curveTo(17.55f, 15.3f, 18.75f, 15.5f, 20f, 15.5f)
            curveTo(20.55f, 15.5f, 21f, 15.95f, 21f, 16.5f)
            lineTo(21f, 20f)
            curveTo(21f, 20.55f, 20.55f, 21f, 20f, 21f)
            curveTo(10.61f, 21f, 3f, 13.39f, 3f, 4f)
            curveTo(3f, 3.45f, 3.45f, 3f, 4f, 3f)
            lineTo(7.5f, 3f)
            curveTo(8.05f, 3f, 8.5f, 3.45f, 8.5f, 4f)
            curveTo(8.5f, 5.25f, 8.7f, 6.45f, 9.07f, 7.57f)
            curveTo(9.18f, 7.92f, 9.1f, 8.31f, 8.82f, 8.59f)
            lineTo(6.62f, 10.79f)
            close()
        }
    }.build()
}

val LockIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Lock",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(PrimaryForestGreen)) {
            moveTo(18f, 8f)
            lineTo(17f, 8f)
            lineTo(17f, 6f)
            curveTo(17f, 3.24f, 14.76f, 1f, 12f, 1f)
            curveTo(9.24f, 1f, 7f, 3.24f, 7f, 6f)
            lineTo(7f, 8f)
            lineTo(6f, 8f)
            curveTo(4.9f, 8f, 4f, 8.9f, 4f, 10f)
            lineTo(4f, 20f)
            curveTo(4f, 21.1f, 4.9f, 22f, 6f, 22f)
            lineTo(18f, 22f)
            curveTo(19.1f, 22f, 20f, 21.1f, 20f, 20f)
            lineTo(20f, 10f)
            curveTo(20f, 8.9f, 19.1f, 8f, 18f, 8f)
            close()
            moveTo(9f, 6f)
            curveTo(9f, 4.34f, 10.34f, 3f, 12f, 3f)
            curveTo(13.66f, 3f, 15f, 4.34f, 15f, 6f)
            lineTo(15f, 8f)
            lineTo(9f, 8f)
            lineTo(9f, 6f)
            close()
            moveTo(12f, 17f)
            curveTo(10.9f, 17f, 10f, 16.1f, 10f, 15f)
            curveTo(10f, 13.9f, 10.9f, 13f, 12f, 13f)
            curveTo(13.1f, 13f, 14f, 13.9f, 14f, 15f)
            curveTo(14f, 16.1f, 13.1f, 17f, 12f, 17f)
            close()
        }
    }.build()
}

val VisibilityIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Visibility",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(SecondaryText)) {
            moveTo(12f, 4.5f)
            curveTo(7f, 4.5f, 2.73f, 7.61f, 1f, 12f)
            curveTo(2.73f, 16.39f, 7f, 19.5f, 12f, 19.5f)
            curveTo(17f, 19.5f, 21.27f, 16.39f, 23f, 12f)
            curveTo(21.27f, 7.61f, 17f, 4.5f, 12f, 4.5f)
            close()
            moveTo(12f, 17f)
            curveTo(9.24f, 17f, 7f, 14.76f, 7f, 12f)
            curveTo(7f, 9.24f, 9.24f, 7f, 12f, 7f)
            curveTo(14.76f, 7f, 17f, 9.24f, 17f, 12f)
            curveTo(17f, 14.76f, 14.76f, 17f, 12f, 17f)
            close()
            moveTo(12f, 9f)
            curveTo(10.34f, 9f, 9f, 10.34f, 9f, 12f)
            curveTo(9f, 13.66f, 10.34f, 15f, 12f, 15f)
            curveTo(13.66f, 15f, 15f, 13.66f, 15f, 12f)
            curveTo(15f, 10.34f, 13.66f, 9f, 12f, 9f)
            close()
        }
    }.build()
}

val VisibilityOffIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "VisibilityOff",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(SecondaryText)) {
            moveTo(12f, 7f)
            curveTo(14.76f, 7f, 17f, 9.24f, 17f, 12f)
            curveTo(17f, 12.64f, 16.87f, 13.26f, 16.64f, 13.82f)
            lineTo(19.57f, 16.75f)
            curveTo(21.07f, 15.5f, 22.27f, 13.86f, 23f, 12f)
            curveTo(21.27f, 7.61f, 17f, 4.5f, 12f, 4.5f)
            curveTo(10.73f, 4.5f, 9.51f, 4.75f, 8.39f, 5.21f)
            lineTo(10.82f, 7.64f)
            curveTo(11.2f, 7.23f, 11.58f, 7f, 12f, 7f)
            close()
            moveTo(2f, 4.27f)
            lineTo(4.28f, 6.55f)
            lineTo(4.73f, 7f)
            curveTo(3.08f, 8.3f, 1.78f, 10.02f, 1f, 12f)
            curveTo(2.73f, 16.39f, 7f, 19.5f, 12f, 19.5f)
            curveTo(13.55f, 19.5f, 15.03f, 19.2f, 16.38f, 18.66f)
            lineTo(16.82f, 19.1f)
            lineTo(19.73f, 22f)
            lineTo(21f, 20.73f)
            lineTo(3.27f, 3f)
            lineTo(2f, 4.27f)
            close()
            moveTo(7.53f, 9.8f)
            lineTo(9.08f, 11.35f)
            curveTo(9.03f, 11.56f, 9f, 11.78f, 9f, 12f)
            curveTo(9f, 13.66f, 10.34f, 15f, 12f, 15f)
            curveTo(12.22f, 15f, 12.44f, 14.97f, 12.65f, 14.92f)
            lineTo(14.2f, 16.47f)
            curveTo(13.52f, 16.81f, 12.78f, 17f, 12f, 17f)
            curveTo(9.24f, 17f, 7f, 14.76f, 7f, 12f)
            curveTo(7f, 11.22f, 7.19f, 10.48f, 7.53f, 9.8f)
            close()
        }
    }.build()
}

/**
 * Header section rendering the custom auth_background artwork with top back button overlay.
 * Crops the upper forest-green portion containing food illustrations and organic wave bottom transition.
 */
@Composable
fun AuthHeaderArtwork(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    height: Dp = 150.dp
) {
    val authBgBitmap = ImageBitmap.imageResource(id = R.drawable.auth_background)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            val srcOffset = IntOffset(0, 0)
            // Crop top 38% containing the green artwork and wavy transition
            val srcSize = IntSize(authBgBitmap.width, (authBgBitmap.height * 0.3808f).toInt())
            val dstOffset = IntOffset(0, 0)
            val dstSize = IntSize(size.width.toInt(), size.height.toInt())

            drawImage(
                image = authBgBitmap,
                srcOffset = srcOffset,
                srcSize = srcSize,
                dstOffset = dstOffset,
                dstSize = dstSize
            )
        }

        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 12.dp, top = 8.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = ArrowBackIcon,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}

@Composable
fun PrimaryAuthButton(
    text: String,
    loadingText: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryForestGreen,
            contentColor = Color.White,
            disabledContainerColor = PrimaryForestGreen.copy(alpha = 0.85f),
            disabledContentColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        )
    ) {
        if (isLoading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = loadingText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        } else {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholderText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SecondaryText,
                    fontSize = 15.sp
                )
            )
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedBorderColor = PrimaryForestGreen,
            unfocusedBorderColor = Color(0xFFC8D6B9),
            disabledBorderColor = Color(0xFFC8D6B9),
            focusedLeadingIconColor = PrimaryForestGreen,
            unfocusedLeadingIconColor = SecondaryText,
            disabledLeadingIconColor = SecondaryText,
            focusedTrailingIconColor = PrimaryForestGreen,
            unfocusedTrailingIconColor = SecondaryText,
            disabledTrailingIconColor = SecondaryText,
            focusedTextColor = DeepText,
            unfocusedTextColor = DeepText,
            disabledTextColor = DeepText,
            cursorColor = PrimaryForestGreen
        )
    )
}

/**
 * Country code model for phone authentication.
 */
data class Country(
    val name: String,
    val code: String,
    val flag: String
)

val defaultCountries = listOf(
    Country("India", "+91", "🇮🇳"),
    Country("United States", "+1", "🇺🇸"),
    Country("United Kingdom", "+44", "🇬🇧"),
    Country("Canada", "+1", "🇨🇦"),
    Country("Australia", "+61", "🇦🇺"),
    Country("United Arab Emirates", "+971", "🇦🇪"),
    Country("Singapore", "+65", "🇸🇬"),
    Country("Germany", "+49", "🇩🇪"),
    Country("France", "+33", "🇫🇷"),
    Country("Japan", "+81", "🇯🇵"),
    Country("China", "+86", "🇨🇳"),
    Country("Brazil", "+55", "🇧🇷"),
    Country("Mexico", "+52", "🇲🇽"),
    Country("South Africa", "+27", "🇿🇦"),
    Country("New Zealand", "+64", "🇳🇿")
)

/**
 * Searchable dialog to select a country code.
 */
@Composable
fun CountrySelectorDialog(
    countries: List<Country>,
    selectedCountry: Country,
    onCountrySelected: (Country) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCountries = remember(searchQuery, countries) {
        if (searchQuery.isBlank()) {
            countries
        } else {
            countries.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.code.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WarmIvory,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Select Country Code",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepText,
                        fontSize = 20.sp
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                AuthTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholderText = "Search country or code..."
                )
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredCountries.size) { index ->
                        val country = filteredCountries[index]
                        val isSelected = country == selectedCountry

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) PrimaryForestGreen.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable {
                                    onCountrySelected(country)
                                    onDismiss()
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = country.flag,
                                fontSize = 22.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = country.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = DeepText,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 15.sp
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = country.code,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = PrimaryForestGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = PrimaryForestGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

/**
 * Phone number input field with country code selector box.
 */
@Composable
fun PhoneInputRow(
    phoneValue: String,
    onPhoneChange: (String) -> Unit,
    enabled: Boolean = true,
    selectedCountry: Country = defaultCountries[0],
    onCountryClick: () -> Unit = {},
    onDone: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Country Code Box
        Box(
            modifier = Modifier
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFC8D6B9), RoundedCornerShape(16.dp))
                .clickable(enabled = enabled, onClick = onCountryClick)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = selectedCountry.flag,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = selectedCountry.code,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = DeepText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "▾",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SecondaryText,
                        fontSize = 12.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Phone Number Input Field
        AuthTextField(
            value = phoneValue,
            onValueChange = { input ->
                // Filter digits only, limit to 10 digits
                val digits = input.filter { it.isDigit() }
                if (digits.length <= 10) {
                    onPhoneChange(digits)
                }
            },
            placeholderText = "Phone Number",
            enabled = enabled,
            modifier = Modifier.weight(1f),
            leadingIcon = {
                Icon(
                    imageVector = PhoneIcon,
                    contentDescription = null,
                    tint = PrimaryForestGreen
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDone() }
            )
        )
    }
}

/**
 * Formatted 6-digit OTP input row with highlighted individual digit boxes.
 */
@Composable
fun OtpDigitInputRow(
    otpValue: String,
    onOtpChange: (String) -> Unit,
    enabled: Boolean = true,
    length: Int = 6,
    onDone: () -> Unit = {}
) {
    BasicTextField(
        value = otpValue,
        onValueChange = { input ->
            val digits = input.filter { it.isDigit() }
            if (digits.length <= length) {
                onOtpChange(digits)
            }
        },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDone() }
        ),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(length) { index ->
                    val char = if (index < otpValue.length) otpValue[index].toString() else ""
                    val isFocused = index == otpValue.length || (index == length - 1 && otpValue.length == length)

                    Box(
                        modifier = Modifier
                            .size(width = 46.dp, height = 54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(
                                width = if (isFocused) 2.dp else 1.dp,
                                color = if (isFocused) PrimaryForestGreen else Color(0xFFC8D6B9),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepText,
                                fontSize = 20.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun EcoFeastSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { data ->
        Snackbar(
            snackbarData = data,
            containerColor = PrimaryForestGreen,
            contentColor = WarmIvory,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
