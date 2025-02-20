package com.gapshiko.llistapp.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.gapshiko.llistapp.data.Phone
import com.gapshiko.llistapp.data.Review
import com.gapshiko.llistapp.viewmodel.PhoneViewModel
import com.gapshiko.llistapp.viewmodel.ReviewsViewModel
import com.gapshiko.llistapp.viewmodel.FavoritesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PhoneScreen(
    phone: Phone,
    phoneViewModel: PhoneViewModel = viewModel(),
    reviewsViewModel: ReviewsViewModel = viewModel(),
    favoritesViewModel: FavoritesViewModel = viewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Description", "Reviews")
    val currentUserId = currentUser?.uid

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            favoritesViewModel.loadFavorites(currentUserId)
        }
    }
    val favoriteIds by favoritesViewModel.favoriteIds.collectAsState()
    val isFavorite = favoriteIds.contains(phone.id)

    var showReviewDialog by remember { mutableStateOf(false) }
    var reviewDialogComment by remember { mutableStateOf("") }
    var reviewDialogRating by remember { mutableIntStateOf(0) }
    val reviews by reviewsViewModel.reviews.collectAsState()
    val existingReview = reviews.find { it.userId == currentUser?.uid }

    Scaffold(
        floatingActionButton = {
            when (selectedTabIndex) {
                0 -> {
                    if (currentUserId != null) {
                        FloatingActionButton(
                            onClick = {
                                if (isFavorite) {
                                    favoritesViewModel.removeFavorite(currentUserId, phone.id)
                                } else {
                                    favoritesViewModel.addFavorite(currentUserId, phone.id)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (isFavorite) Color.Yellow else Color.Gray
                            )
                        }
                    }
                }
                1 -> {
                    FloatingActionButton(
                        onClick = {
                            reviewDialogComment = existingReview?.comment ?: ""
                            reviewDialogRating = existingReview?.rating ?: 0
                            showReviewDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = if (existingReview != null) Icons.Filled.Edit else Icons.Filled.Add,
                            contentDescription = if (existingReview != null) "Edit Review" else "Add Review"
                        )
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            when (selectedTabIndex) {
                0 -> PhoneDescription(phone = phone, phoneViewModel = phoneViewModel)
                1 -> ReviewsContent(phoneId = phone.id, reviewsViewModel = reviewsViewModel)
            }
        }
    }
    if (showReviewDialog) {
        ReviewDialog(
            initialComment = reviewDialogComment,
            initialRating = reviewDialogRating,
            onDismiss = { showReviewDialog = false },
            onSubmit = { comment, rating ->
                reviewsViewModel.submitReview(phone.id, comment, rating)
                showReviewDialog = false
            }
        )
    }
}

@Composable
fun PhoneDescription(phone: Phone, phoneViewModel: PhoneViewModel = viewModel()) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        phoneViewModel.loadImages(phone, context)
    }
    val pagerState = rememberPagerState { phone.image.size }
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
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
                        painter = rememberAsyncImagePainter(model = phone.image[page]),
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
                    val color = if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
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
        Spacer(modifier = Modifier.height(16.dp))
        if (phone.name.isNotBlank()) {
            Text(
                text = phone.name,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (phone.date.isNotBlank()) {
            InfoCard(title = "Release date", content = phone.date)
        }
        if (phone.display.isNotEmpty()) {
            InfoCard(title = "Display", content = phone.display.joinToString(separator = "\n"))
        }
        if (phone.memory.isNotEmpty()) {
            InfoCard(title = "Memory", content = phone.memory.joinToString(separator = "\n"))
        }
        if (phone.soc.isNotBlank()) {
            InfoCard(title = "System-On-Chip", content = phone.soc)
        }
        if (phone.mainCamera.isNotEmpty()) {
            InfoCard(title = "Main camera", content = phone.mainCamera.joinToString(separator = "\n"))
        }
        if (phone.frontCam.isNotBlank()) {
            InfoCard(title = "Front camera", content = phone.frontCam)
        }
        val batteryAndCharge = buildString {
            if (phone.battery != 0) append("${phone.battery} mAh")
            if (phone.charge.isNotEmpty()) {
                if (isNotEmpty()) append("\n")
                append("Charge: ${phone.charge.joinToString(separator = ", ")}")
            }
        }
        if (batteryAndCharge.isNotBlank()) {
            InfoCard(title = "Battery", content = batteryAndCharge)
        }
        if (phone.stockOS.isNotBlank()) {
            InfoCard(title = "Stock OS", content = phone.stockOS)
        }
    }
}

@Composable
fun InfoCard(title: String, content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ReviewsContent(phoneId: String, reviewsViewModel: ReviewsViewModel = viewModel()) {
    LaunchedEffect(phoneId) {
        reviewsViewModel.loadReviews(phoneId)
    }
    val reviews by reviewsViewModel.reviews.collectAsState()
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Reviews", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        if (reviews.isNotEmpty()) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(reviews) { review ->
                    ReviewCard(
                        review = review,
                        onDelete = { reviewsViewModel.deleteReview(it) },
                        onEdit = { /* Optionally, inline edit can trigger dialog via parent FAB */ }
                    )
                }
            }
        } else {
            Text(
                text = "There are no reviews",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ReviewDialog(
    initialComment: String,
    initialRating: Int,
    onDismiss: () -> Unit,
    onSubmit: (comment: String, rating: Int) -> Unit
) {
    var comment by remember { mutableStateOf(initialComment) }
    var rating by remember { mutableIntStateOf(initialRating) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Your Review") },
        text = {
            Column {
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                StarRating(
                    rating = rating,
                    onRatingChanged = { rating = it }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(comment, rating) }) {
                Text("Submit")
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
fun StarRating(
    rating: Int,
    onRatingChanged: ((Int) -> Unit)? = null,
    starCount: Int = 5,
    starSize: Dp = 32.dp,
    selectedColor: Color = Color.Yellow,
    unselectedColor: Color = Color.Gray,
    readOnly: Boolean = false
) {
    Row {
        for (i in 1..starCount) {
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Star $i",
                tint = if (i <= rating) selectedColor else unselectedColor,
                modifier = Modifier
                    .size(starSize)
                    .then(
                        if (!readOnly && onRatingChanged != null)
                            Modifier.clickable { onRatingChanged(i) }
                        else Modifier
                    )
            )
        }
    }
}

@Composable
fun ReviewCard(review: Review, onDelete: (Review) -> Unit, onEdit: (Review) -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val formattedDate = remember(review.timestamp) {
        val date = Date(review.timestamp)
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(date)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = review.userName, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            StarRating(
                rating = review.rating,
                readOnly = true,
                starSize = 24.dp,
                selectedColor = Color.Yellow,
                unselectedColor = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = review.comment, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = formattedDate, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            if (currentUser != null && currentUser.uid == review.userId) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Edit",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.clickable { onEdit(review) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Red),
                        modifier = Modifier.clickable { onDelete(review) }
                    )
                }
            }
        }
    }
}
