// FavoritesViewModel.kt
package com.gapshiko.llistapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FavoritesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _favoriteIds = MutableStateFlow<List<String>>(emptyList())
    val favoriteIds: StateFlow<List<String>> get() = _favoriteIds

    private var favoriteListenerRegistration: ListenerRegistration? = null

    fun loadFavorites(userId: String) {
        favoriteListenerRegistration?.remove()
        favoriteListenerRegistration = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FavoritesViewModel", "Error loading favorites", error)
                    return@addSnapshotListener
                }
                val favorites = snapshot?.get("favorites") as? List<String> ?: emptyList()
                _favoriteIds.value = favorites
            }
    }

    fun addFavorite(userId: String, phoneId: String) {
        db.collection("users").document(userId)
            .update("favorites", FieldValue.arrayUnion(phoneId))
    }

    fun removeFavorite(userId: String, phoneId: String) {
        db.collection("users").document(userId)
            .update("favorites", FieldValue.arrayRemove(phoneId))
    }

    override fun onCleared() {
        favoriteListenerRegistration?.remove()
        super.onCleared()
    }
}
