package com.gapshiko.llistapp.data

data class Phone(
    val id: String = "",
    val name: String = "",
    val image: List<String> = emptyList(),
    val date: String = "",
    val memory: String = "",
    val soc: String = "",
    val battery: Int = 0
)


