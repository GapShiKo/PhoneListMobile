package com.gapshiko.llistapp.pages

import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults.outlinedTextFieldColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.Coil
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.gapshiko.llistapp.data.Phone
import com.gapshiko.llistapp.viewmodel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlin.math.roundToInt

data class FilterParams(
    val batteryMin: Int = 0,
    val year: Int? = null,
    val ramMin: Int = 0,
    val os: String? = null,
    val chargingSpeedMin: Int = 0,
    val screenSizeMin: Float = 0f,
    val cameraMPMin: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, homeViewModel: HomeViewModel = viewModel()) {
    val phones by homeViewModel.phones.collectAsState()
    val auth = FirebaseAuth.getInstance()
    var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }
    var searchQuery by remember { mutableStateOf("") }

    var showFilterDialog by remember { mutableStateOf(false) }
    var filterParams by remember { mutableStateOf(FilterParams()) }

    val availableYears = remember(phones) {
        phones.mapNotNull {
            it.date.split("/").last().toIntOrNull()
        }.distinct().sortedDescending()
    }

    val availableRams = remember(phones) {
        phones.flatMap { phone ->
            phone.memory.mapNotNull { config ->
                config.split("/").firstOrNull()?.trim()?.toIntOrNull()
            }
        }.distinct().sorted()
    }

    val availableOS = remember(phones) {
        phones.map { phone ->
            phone.stockOS.split(",").first().trim()
        }.distinct().sorted()
    }

    val availableChargingSpeeds = remember(phones) {
        phones.mapNotNull { phone ->
            phone.charge.firstOrNull()?.let {
                """(\d+)W""".toRegex().find(it)?.groups?.get(1)?.value?.toIntOrNull()
            }
        }.distinct().sorted()
    }

    val availableScreenSizes = remember(phones) {
        phones.mapNotNull { phone ->
            phone.display.getOrNull(1)?.let {
                """([\d.]+)\s*inches""".toRegex().find(it)?.groups?.get(1)?.value?.toFloatOrNull()
            }
        }.distinct().sorted()
    }

    val availableCameraMPs = remember(phones) {
        phones.mapNotNull { phone ->
            phone.mainCamera.firstOrNull()?.let {
                """(\d+)\s*MP""".toRegex().find(it)?.groups?.get(1)?.value?.toIntOrNull()
            }
        }.distinct().sorted()
    }

    val filteredPhones = remember(phones, searchQuery, filterParams) {
        phones.filter { phone ->
            val matchesSearch = phone.name.contains(searchQuery, ignoreCase = true)
            val batteryMatch = phone.battery >= filterParams.batteryMin

            val yearMatch = filterParams.year?.let { year ->
                phone.date.split("/").last().toIntOrNull() == year
            } ?: true

            val ramMatch = phone.memory.mapNotNull { config ->
                config.split("/").firstOrNull()?.trim()?.toIntOrNull()
            }.any { it >= filterParams.ramMin }

            val osMatch = filterParams.os?.let { os ->
                phone.stockOS.startsWith(os, ignoreCase = true)
            } ?: true

            val chargingMatch = filterParams.chargingSpeedMin <= (phone.charge.firstOrNull()
                ?.let { """(\d+)W""".toRegex().find(it)?.groups?.get(1)?.value?.toIntOrNull() } ?: 0)

            val screenSizeMatch = filterParams.screenSizeMin <= (phone.display.getOrNull(1)
                ?.let { """([\d.]+)\s*inches""".toRegex().find(it)?.groups?.get(1)?.value?.toFloatOrNull() } ?: 0f)

            val cameraMPMatch = filterParams.cameraMPMin <= (phone.mainCamera.firstOrNull()
                ?.let { """(\d+)\s*MP""".toRegex().find(it)?.groups?.get(1)?.value?.toIntOrNull() } ?: 0)

            matchesSearch && batteryMatch && yearMatch && ramMatch &&
                    osMatch && chargingMatch && screenSizeMatch && cameraMPMatch
        }
    }

    DisposableEffect(Unit) {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            isLoggedIn = firebaseAuth.currentUser != null
        }
        auth.addAuthStateListener(authStateListener)
        onDispose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            homeViewModel.loadPhones()
        }
    }

    if (phones.isNotEmpty()) {
        PreloadImages(phones)
    }

    if (isLoggedIn) {
        if (phones.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = "Filter"
                        )
                    }
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search phones...") },
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search icon",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = outlinedTextFieldColors(
                            focusedBorderColor = Color.Gray,
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                }

                if (showFilterDialog) {
                    FilterDialog(
                        onDismiss = { showFilterDialog = false },
                        onApply = { newParams ->
                            filterParams = newParams
                            showFilterDialog = false
                        },
                        currentParams = filterParams,
                        availableYears = availableYears,
                        availableRams = availableRams,
                        availableOS = availableOS,
                        availableChargingSpeeds = availableChargingSpeeds,
                        availableScreenSizes = availableScreenSizes,
                        availableCameraMPs = availableCameraMPs
                    )
                }

                if (filteredPhones.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No results found")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 2.dp)
                    ) {
                        items(filteredPhones) { phone ->
                            PhoneItem(phone = phone) {
                                val phoneJson = Uri.encode(Gson().toJson(phone))
                                navController.navigate("phoneDetail/$phoneJson")
                            }
                        }
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("To see the list, please, log in")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("login") }) {
                Text("Login")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    onDismiss: () -> Unit,
    onApply: (FilterParams) -> Unit,
    currentParams: FilterParams,
    availableYears: List<Int>,
    availableRams: List<Int>,
    availableOS: List<String>,
    availableChargingSpeeds: List<Int>,
    availableScreenSizes: List<Float>,
    availableCameraMPs: List<Int>
) {
    val minCharging = availableChargingSpeeds.minOrNull() ?: 0
    val minScreenSize = availableScreenSizes.minOrNull() ?: 0f
    val minCameraMP = availableCameraMPs.minOrNull() ?: 0

    val batteryMin by remember { mutableIntStateOf(currentParams.batteryMin) }
    var selectedYear by remember { mutableStateOf(currentParams.year) }
    var selectedRamIndex by remember {
        mutableIntStateOf(
            availableRams.indexOf(currentParams.ramMin).takeIf { it >= 0 } ?: 0
        )
    }
    val ramMinValue = if (availableRams.isNotEmpty()) availableRams[selectedRamIndex] else 0

    var selectedOS by remember { mutableStateOf(currentParams.os) }
    var chargingSpeedMin by remember {
        mutableIntStateOf(if (currentParams.chargingSpeedMin == 0) minCharging else currentParams.chargingSpeedMin)
    }
    var screenSizeMin by remember {
        mutableFloatStateOf(if (currentParams.screenSizeMin.toInt() == 0) minScreenSize else currentParams.screenSizeMin)
    }
    var cameraMPMin by remember {
        mutableIntStateOf(if (currentParams.cameraMPMin == 0) minCameraMP else currentParams.cameraMPMin)
    }

    var yearExpanded by remember { mutableStateOf(false) }
    var osExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Options") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                ExposedDropdownMenuBox(
                    expanded = yearExpanded,
                    onExpandedChange = { yearExpanded = !yearExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedYear?.toString() ?: "Any",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Release Year") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = yearExpanded,
                        onDismissRequest = { yearExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Any") },
                            onClick = {
                                selectedYear = null
                                yearExpanded = false
                            }
                        )
                        availableYears.forEach { year ->
                            DropdownMenuItem(
                                text = { Text("$year") },
                                onClick = {
                                    selectedYear = year
                                    yearExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = osExpanded,
                    onExpandedChange = { osExpanded = !osExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedOS ?: "Any",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Operating System") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = osExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = osExpanded,
                        onDismissRequest = { osExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Any") },
                            onClick = {
                                selectedOS = null
                                osExpanded = false
                            }
                        )
                        availableOS.forEach { os ->
                            DropdownMenuItem(
                                text = { Text(os) },
                                onClick = {
                                    selectedOS = os
                                    osExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (availableRams.isNotEmpty()) {
                    Text("RAM (GB) min: $ramMinValue", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = selectedRamIndex.toFloat(),
                        onValueChange = { selectedRamIndex = it.roundToInt() },
                        valueRange = 0f..(availableRams.size - 1).toFloat(),
                        steps = if (availableRams.size > 1) availableRams.size - 2 else 0
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text("Charging Speed (W) min: $chargingSpeedMin", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = chargingSpeedMin.toFloat(),
                    onValueChange = { chargingSpeedMin = it.toInt() },
                    valueRange = minCharging.toFloat()..(availableChargingSpeeds.maxOrNull()?.toFloat() ?: 100f),
                    steps = ((availableChargingSpeeds.maxOrNull() ?: 100) - minCharging) / 5
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("Screen Size (inches) min: ${"%.1f".format(screenSizeMin)}", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = screenSizeMin,
                    onValueChange = { screenSizeMin = (it * 10).roundToInt() / 10f },
                    valueRange = minScreenSize..(availableScreenSizes.maxOrNull() ?: 10f),
                    steps = ((availableScreenSizes.maxOrNull() ?: 10f) - minScreenSize).let {
                        if (it > 0) (it / 0.2f).toInt() else 0
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("Main Camera (MP) min: $cameraMPMin", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = cameraMPMin.toFloat(),
                    onValueChange = { cameraMPMin = it.toInt() },
                    valueRange = minCameraMP.toFloat()..(availableCameraMPs.maxOrNull()?.toFloat() ?: 200f),
                    steps = ((availableCameraMPs.maxOrNull() ?: 200) - minCameraMP) / 2
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onApply(
                    FilterParams(
                        batteryMin = batteryMin,
                        year = selectedYear,
                        ramMin = ramMinValue,
                        os = selectedOS,
                        chargingSpeedMin = chargingSpeedMin,
                        screenSizeMin = screenSizeMin,
                        cameraMPMin = cameraMPMin
                    )
                )
            }) {
                Text("Apply")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PhoneItem(phone: Phone, onPhoneClick: (Phone) -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onPhoneClick(phone) }
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            val imageUrl = phone.image.firstOrNull()
            val painter = rememberAsyncImagePainter(model = imageUrl)
            val backgroundColor = if (painter.state is AsyncImagePainter.State.Loading) Color.Gray else Color.White
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .padding(1.dp),
                    contentScale = ContentScale.Fit
                )
                if (painter.state is AsyncImagePainter.State.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = phone.name, style = MaterialTheme.typography.titleLarge)
                Text(text = "Release: ${phone.date}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Memory: ${phone.memory.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Battery: ${phone.battery} mAh", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun PreloadImages(phones: List<Phone>) {
    val context = LocalContext.current
    val imageLoader = Coil.imageLoader(context)
    LaunchedEffect(phones) {
        phones.forEach { phone ->
            phone.image.firstOrNull()?.let { imageUrl ->
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .build()
                imageLoader.enqueue(request)
            }
        }
    }
}
