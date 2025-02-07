package com.gapshiko.llistapp.reps

import com.gapshiko.llistapp.data.Phone
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PhoneRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getPhones(): List<Phone> {
        return db.collection("phones")
            .get()
            .await()
            .toObjects(Phone::class.java)
    }
}