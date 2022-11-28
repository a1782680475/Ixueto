package com.xktech.ixueto.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.xktech.ixueto.data.local.DarkModel
import com.xktech.ixueto.utils.DarkModelUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DarkViewModel @Inject constructor(application: Application) :
    AndroidViewModel(application) {
    var context = application
    val darkModel = liveData {
        emit(DarkModel)
    }

    fun saveDarkModel(isDarkModel: Boolean) {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                DarkModel.model = isDarkModel
            }
            if (context != null) DarkModelUtils.check(context)
        }
    }

    fun saveAutoRuleEnabledState(enable: Boolean) {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                DarkModel.autoRuleEnable = enable
            }
            if (context != null) DarkModelUtils.check(context)
        }
    }

    fun saveAutoRule(rule: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                DarkModel.autoRule = rule
            }
            if (context != null) DarkModelUtils.check(context)
        }
    }

    fun saveAutoDarkStartTime(startMinutes: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                DarkModel.darkStartTime = startMinutes
            }
            if (context != null) DarkModelUtils.check(context)
        }
    }

    fun saveAutoDarkEndTime(endMinutes: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                DarkModel.darkEndTime = endMinutes
            }
            if (context != null) DarkModelUtils.check(context)
        }
    }
}