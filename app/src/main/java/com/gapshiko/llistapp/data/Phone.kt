package com.gapshiko.llistapp.data

data class Phone(
    val id: String = "",
    val name: String = "",
    val image: List<String> = emptyList(),
    val date: String = "",
    val memory: String = "",
    val soc: String = "",
    val battery: Int = 0,
    val charge: List<String> = emptyList(),
    val display: List<String> = emptyList(),
    val frontCam: String = "",
    val mainCamera: List<String> = emptyList(),
    val stockOS: String = ""
)
