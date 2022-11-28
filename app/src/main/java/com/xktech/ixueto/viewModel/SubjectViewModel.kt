package com.xktech.ixueto.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.xktech.ixueto.repository.SubjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class SubjectViewModel @Inject constructor(private val subjectRepository: SubjectRepository) :
    ViewModel() {
    fun getSubjects(professionId: Int) = liveData(Dispatchers.IO) {
        try {
            emit(subjectRepository.getRegions(professionId))
        } catch (e: Exception) {

        }
    }

    fun signUp(subjectId: Int) = liveData(Dispatchers.IO) {
        try {
            emit(subjectRepository.signUp(subjectId))
        } catch (e: Exception) {

        }
    }
}