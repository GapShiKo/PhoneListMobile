package com.gapshiko.llistapp.pages

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gapshiko.llistapp.viewmodel.FavoritesViewModel
import com.gapshiko.llistapp.viewmodel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson

@Composable
fun FavScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = viewModel(),
    favoritesViewModel: FavoritesViewModel = viewModel()
) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    LaunchedEffect(Unit) {
        homeViewModel.loadPhones()
        currentUser?.uid?.let { favoritesViewModel.loadFavorites(it) }
    }
    val favoriteIds by favoritesViewModel.favoriteIds.collectAsState()
    val phones by homeViewModel.phones.collectAsState()
    val favorites = phones.filter { it.id in favoriteIds }

    if (currentUser == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("To see favorites list, please log in")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("login") }) {
                Text("Login")
            }
        }
    } else {
        if (phones.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (favorites.isEmpty()){
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("You don't have anything in favorites")
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(favorites) { phone ->
                    PhoneItem(phone = phone) {
                        val phoneJson = Uri.encode(Gson().toJson(phone))
                        navController.navigate("phoneDetail/$phoneJson")
                    }
                }
            }
        }
    }
}

