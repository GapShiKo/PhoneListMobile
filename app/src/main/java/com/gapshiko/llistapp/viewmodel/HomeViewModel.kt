package com.gapshiko.llistapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gapshiko.llistapp.data.Phone
import com.gapshiko.llistapp.reps.PhoneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val phoneRepository = PhoneRepository()
    private val _phones = MutableStateFlow<List<Phone>>(emptyList())
    val phones: StateFlow<List<Phone>> = _phones

    fun loadPhones() {
        viewModelScope.launch {
            if (_phones.value.isEmpty()) {
                val phoneList = phoneRepository.getPhones()
                _phones.value = phoneList
            }
        }
    }
}
