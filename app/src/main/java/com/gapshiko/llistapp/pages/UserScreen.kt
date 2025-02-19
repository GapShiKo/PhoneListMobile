package com.gapshiko.llistapp.pages

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

@Composable
fun UserScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            navController.navigate("login") {
                popUpTo("profile") { inclusive = true }
            }
        }
    }

    if (currentUser == null) return

    var name by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var favorites by remember { mutableStateOf(emptyList<String>()) }
    var registration by remember { mutableStateOf("") }

    LaunchedEffect(currentUser) {
        currentUser.let { user ->
            db.collection("users").document(user.uid).get().addOnSuccessListener { document ->
                name = document.getString("name").orEmpty()
                birthDate = document.getString("birthDate").orEmpty()
                description = document.getString("description").orEmpty()
                gender = document.getString("gender").orEmpty()
                favorites = document.get("favorites") as? List<String> ?: emptyList()
                registration = document.getString("registrationDate").orEmpty()
            }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text(text = "User Profile", style = MaterialTheme.typography.titleLarge)
        }

        item {
            ProfileCard("Login", currentUser.email.toString())
        }
        item {
            ProfileCard("Name", name)
        }
        item {
            ProfileCard("Birth date", birthDate)
        }
        item {
            ProfileCard("Gender", gender)
        }
        item {
            ProfileCard("Registration date", registration)
        }
        item {
            ProfileCard("Description", description)
        }
        item {
            ProfileCard("Favorites count", favorites.size.toString())
        }

        item {
            Button(
                onClick = {
                    auth.signOut()
                    navController.navigate("login")
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("Log out")
            }
        }

        item {
            Button(
                onClick = { navController.navigate("editProfile") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("Edit Profile")
            }
        }

        item {
            Button(
                onClick = { navController.navigate("deleteAccount") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("Delete Account")
            }
        }
    }
}

@Composable
fun ProfileCard(title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    initialDate: Long? = null
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance().apply { timeInMillis = millis }
                        val formattedDate = "${calendar[Calendar.DAY_OF_MONTH]}/" +
                                "${calendar[Calendar.MONTH] + 1}/" +
                                "${calendar[Calendar.YEAR]}"
                        onDateSelected(formattedDate)
                    }
                    onDismiss()
                }
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("Male", "Female")

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    name = document.getString("name").orEmpty()
                    birthDate = document.getString("birthDate").orEmpty()
                    description = document.getString("description").orEmpty()
                    gender = document.getString("gender").orEmpty()
                }
        }
    }

    val initialDate = remember(birthDate) {
        if (birthDate.isNotEmpty()) {
            val parts = birthDate.split("/")
            if (parts.size == 3) {
                val day = parts[0].toInt()
                val month = parts[1].toInt() - 1
                val year = parts[2].toInt()
                Calendar.getInstance().apply { set(year, month, day) }.timeInMillis
            } else null
        } else null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = birthDate,
                    onValueChange = {},
                    label = { Text("Birth Date") },
                    readOnly = true,
                    enabled = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            showDatePicker = true
                        }
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = !genderExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = {},
                    label = { Text("Gender") },
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false }
                ) {
                    genderOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                gender = option
                                genderExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                currentUser?.let { user ->
                    db.collection("users").document(user.uid)
                        .update(
                            "name", name,
                            "birthDate", birthDate,
                            "description", description,
                            "gender", gender
                        )
                        .addOnSuccessListener {
                            Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Save Changes")
        }
    }

    if (showDatePicker) {
        CustomDatePickerDialog(
            onDateSelected = { date ->
                birthDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            initialDate = initialDate
        )
    }
}

@Composable
fun DeleteAccountScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser
    val context = LocalContext.current

    var showConfirmationDialog by remember { mutableStateOf(true) }

    if (currentUser == null) {
        LaunchedEffect(Unit) {
            navController.navigate("login") {
                popUpTo("profile") { inclusive = true }
            }
        }
    }

    if (showConfirmationDialog) {
        ConfirmationDialog(
            onConfirm = {
                currentUser?.let { user ->
                    db.collection("users").document(user.uid).delete()
                        .addOnSuccessListener {
                            user.delete().addOnSuccessListener {
                                auth.signOut()
                                Toast.makeText(
                                    context,
                                    "Account deleted successfully.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigate("login")
                            }.addOnFailureListener { e ->
                                Toast.makeText(
                                    context,
                                    "Error deleting account from Authentication: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "Error deleting account from Firestore: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            },
            onDismiss = {
                showConfirmationDialog = false
                navController.navigate("profile")
            }
        )
    }
}

@Composable
fun ConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Are you sure you want to delete your account?") },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No")
            }
        }
    )
}