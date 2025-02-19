package com.gapshiko.llistapp.data

data class Review(
    val id: String = "",
    val phoneId: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val timestamp: Long = 0L
)
