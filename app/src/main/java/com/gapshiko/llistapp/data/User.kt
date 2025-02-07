package com.gapshiko.llistapp.data

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val birthDate: String = "",
    val description: String = "",
    val gender: String = "",
    val registrationDate: String = "",
    val favoritesCount: Int = 0
)