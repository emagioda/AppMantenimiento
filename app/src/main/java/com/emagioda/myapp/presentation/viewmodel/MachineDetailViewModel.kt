package com.emagioda.myapp.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.emagioda.myapp.R
import com.emagioda.myapp.domain.model.MachineDetail
import com.emagioda.myapp.domain.usecase.GetMachineDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MachineDetailUiState(
    val machine: MachineDetail? = null,
    val isLoading: Boolean = true,
    val errorResId: Int? = null
)

class MachineDetailViewModel(
    private val getMachineDetail: GetMachineDetail,
    private val machineId: String
) : ViewModel() {

    var uiState by mutableStateOf(MachineDetailUiState())
        private set

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val machine = getMachineDetail(machineId)
                withContext(Dispatchers.Main) {
                    uiState = if (machine != null) {
                        MachineDetailUiState(
                            machine = machine,
                            isLoading = false
                        )
                    } else {
                        MachineDetailUiState(
                            isLoading = false,
                            errorResId = R.string.machine_detail_error_loading
                        )
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    uiState = MachineDetailUiState(
                        isLoading = false,
                        errorResId = R.string.machine_detail_error_loading
                    )
                }
            }
        }
    }

    class Factory(
        private val getMachineDetail: GetMachineDetail,
        private val machineId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MachineDetailViewModel(getMachineDetail, machineId) as T
        }
    }
}
