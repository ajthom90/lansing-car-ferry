package dev.ajthom.lansingferry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.ajthom.lansingferry.shared.model.FerryInfo
import dev.ajthom.lansingferry.shared.repository.FerryRepository
import dev.ajthom.lansingferry.shared.repository.FerryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class FerryUiState(
    val ferryInfo: FerryInfo? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class FerryViewModel : ViewModel() {
    private val repository = FerryRepository.create()

    private val _uiState = MutableStateFlow(FerryUiState())
    val uiState: StateFlow<FerryUiState> = _uiState

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val locale = Locale.getDefault().language
            when (val result = repository.getFerryInfo(locale)) {
                is FerryResult.Success -> {
                    _uiState.value = FerryUiState(ferryInfo = result.data)
                }
                is FerryResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                    )
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val locale = Locale.getDefault().language
            when (val result = repository.refresh(locale)) {
                is FerryResult.Success -> {
                    _uiState.value = FerryUiState(ferryInfo = result.data)
                }
                is FerryResult.Error -> {
                    // Keep existing data on refresh failure
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }
}
