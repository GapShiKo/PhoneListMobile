package com.gapshiko.llistapp.data

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ReviewsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> get() = _reviews

    private var reviewListenerRegistration: ListenerRegistration? = null

    fun loadReviews(phoneId: String) {
        reviewListenerRegistration?.remove()
        reviewListenerRegistration = db.collection("reviews")
            .whereEqualTo("phoneId", phoneId)
            .orderBy("timestamp")
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    Log.e("ReviewsViewModel", "Error listening to reviews", error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Review::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                Log.d("ReviewsViewModel", "Received ${list.size} reviews")
                _reviews.value = list
            }
    }

    fun submitReview(phoneId: String, comment: String, rating: Int) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val existingReview = _reviews.value.find { it.userId == currentUser.uid && it.phoneId == phoneId }
        if (existingReview != null) {
            db.collection("reviews").document(existingReview.id)
                .update(
                    mapOf(
                        "comment" to comment,
                        "rating" to rating,
                        "timestamp" to System.currentTimeMillis()
                    )
                ).addOnSuccessListener {
                    _reviews.value = _reviews.value.map {
                        if (it.id == existingReview.id)
                            it.copy(comment = comment, rating = rating, timestamp = System.currentTimeMillis())
                        else it
                    }
                }
        } else {
            val review = Review(
                phoneId = phoneId,
                userId = currentUser.uid,
                userName = currentUser.displayName ?: currentUser.email ?: "Anonymous",
                rating = rating,
                comment = comment,
                timestamp = System.currentTimeMillis()
            )
            db.collection("reviews").add(review)
                .addOnSuccessListener { docRef ->
                    val newReview = review.copy(id = docRef.id)
                    _reviews.value += newReview
                }
        }
    }

    fun deleteReview(review: Review) {
        db.collection("reviews").document(review.id)
            .delete()
            .addOnSuccessListener {
                _reviews.value = _reviews.value.filterNot { it.id == review.id }
            }
    }

    override fun onCleared() {
        reviewListenerRegistration?.remove()
        super.onCleared()
    }
}
