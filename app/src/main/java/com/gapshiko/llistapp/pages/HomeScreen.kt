package com.gapshiko.llistapp.pages

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors
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
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.gapshiko.llistapp.data.Phone
import com.gapshiko.llistapp.viewmodel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, homeViewModel: HomeViewModel = viewModel()) {
    val context = LocalContext.current
    val phones by homeViewModel.phones.collectAsState()
    val auth = FirebaseAuth.getInstance()
    var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredPhones = remember(phones, searchQuery) {
        if (searchQuery.isBlank()) {
            phones
        } else {
            phones.filter { phone ->
                phone.name.contains(searchQuery, ignoreCase = true)
            }
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
            homeViewModel.loadPhones(context)
        }
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
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
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
                        modifier = Modifier.weight(1f)
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
            if (auth.currentUser == null) {
                Text("To see the list, please, log in")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate("login") }) {
                    Text("Login")
                }
            } else {
                FavoritesList(navController)
            }
        }
    }
}

@Composable
fun PhoneItem(phone: Phone, onPhoneClick: (Phone) -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onPhoneClick(phone) }
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageUrl = phone.image.firstOrNull()
            val painter = rememberAsyncImagePainter(model = imageUrl)

            val backgroundColor = if (painter.state is AsyncImagePainter.State.Loading) {
                Color.Gray
            } else {
                Color.White
            }

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
                        .padding(vertical = 1.dp),
                    contentScale = ContentScale.Fit
                )

                if (painter.state is AsyncImagePainter.State.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(text = phone.name, style = MaterialTheme.typography.titleLarge)
                Text(text = "Release: ${phone.date}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "SoC: ${phone.soc}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Memory: ${phone.memory}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Battery: ${phone.battery}mAh", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}



