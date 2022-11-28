package com.xktech.ixueto.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ViolationViewModel @Inject constructor() : ViewModel() {
    val count: MutableLiveData<Int> by lazy { MutableLiveData() }
    fun setCount(value: Int?) {
        count.value = value
    }

}