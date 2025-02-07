package com.gapshiko.llistapp.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.gapshiko.llistapp.data.Phone
import com.gapshiko.llistapp.data.PhoneViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun PhoneScreen(phone: Phone, phoneViewModel: PhoneViewModel = viewModel()) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        phoneViewModel.loadImages(phone, context)
    }

    val pagerState = rememberPagerState { phone.image.size }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser
    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser, phone) {
        currentUser?.let { user ->
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    val favorites = document.get("favorites") as? List<String> ?: emptyList()
                    isFavorite = favorites.contains(phone.id)
                }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val imageHeight = maxHeight * 0.4f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    pageSpacing = 8.dp
                ) { page ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = phone.image[page],
                            ),
                            contentDescription = "Image $page",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pagerState.pageCount) { iteration ->
                        val color =
                            if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(RoundedCornerShape(50))
                                .background(color)
                                .size(8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = phone.name, style = MaterialTheme.typography.titleLarge)
        Text(text = "Release date: ${phone.date}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "SoC: ${phone.soc}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "Memory: ${phone.memory}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "Battery: ${phone.battery} mAh", style = MaterialTheme.typography.bodyMedium)

    }
}

