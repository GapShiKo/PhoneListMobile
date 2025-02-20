package com.gapshiko.llistapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.ImageRequest
import com.gapshiko.llistapp.data.Phone
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