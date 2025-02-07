package com.gapshiko.llistapp.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.launch

class PhoneViewModel : ViewModel() {
    fun loadImages(phone: Phone, context: Context) {
        viewModelScope.launch {
            phone.image.forEach { imageUrl ->
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .build()
                context.imageLoader.enqueue(request)
            }
        }
    }
}