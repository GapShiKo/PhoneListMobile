package com.gapshiko.llistapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.ImageRequest
import com.gapshiko.llistapp.data.Phone
import com.gapshiko.llistapp.reps.PhoneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val phoneRepository = PhoneRepository()
    private val _phones = MutableStateFlow<List<Phone>>(emptyList())
    val phones: StateFlow<List<Phone>> = _phones

    fun loadPhones(context: Context) {
        viewModelScope.launch {
            val phoneList = phoneRepository.getPhones()
            _phones.value = phoneList

            phoneList.forEach { phone ->
                phone.image.forEach { imageUrl ->
                    val request = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .build()
                    context.imageLoader.enqueue(request)
                }
            }
        }
    }
}
