package com.gapshiko.llistapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.gson.Gson
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gapshiko.llistapp.components.BottomNavigationBar
import com.gapshiko.llistapp.data.Phone
import com.gapshiko.llistapp.pages.DeleteAccountScreen
import com.gapshiko.llistapp.pages.EditProfileScreen
import com.gapshiko.llistapp.pages.FavScreen
import com.gapshiko.llistapp.pages.HomeScreen
import com.gapshiko.llistapp.pages.LoginScreen
import com.gapshiko.llistapp.pages.PhoneScreen
import com.gapshiko.llistapp.pages.RegisterScreen
import com.gapshiko.llistapp.pages.UserScreen
import com.gapshiko.llistapp.ui.theme.ListProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ListProjectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") { LoginScreen(navController) }
            composable("register") { RegisterScreen(navController) }
            composable("profile") { UserScreen(navController) }
            composable("home") { HomeScreen(navController) }
            composable("editProfile") { EditProfileScreen(navController) }
            composable("deleteAccount") { DeleteAccountScreen(navController)}
            composable("fav") { FavScreen(navController) }

            composable(
                "phoneDetail/{phoneJson}",
                arguments = listOf(navArgument("phoneJson") { type = NavType.StringType })
            ) { backStackEntry ->
                val phoneJson = backStackEntry.arguments?.getString("phoneJson")
                val phone = Gson().fromJson(phoneJson, Phone::class.java)
                PhoneScreen(phone)
            }
        }
    }
}