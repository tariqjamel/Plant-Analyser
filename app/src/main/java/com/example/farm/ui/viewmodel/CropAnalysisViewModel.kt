package com.example.farm.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farm.data.model.CropAnalysis
import com.example.farm.data.repository.CropAnalysisRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CropAnalysisViewModel @Inject constructor(
    private val repository: CropAnalysisRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<CropAnalysisUiState>(CropAnalysisUiState.Initial)
    val uiState: StateFlow<CropAnalysisUiState> = _uiState.asStateFlow()
    
    private var lastImageUri: Uri? = null
    
    fun analyzeCropImage(imageUri: Uri) {
        lastImageUri = imageUri
        viewModelScope.launch {
            _uiState.value = CropAnalysisUiState.Loading
            
            repository.analyzeCropImage(imageUri)
                .onSuccess { analysis ->
                    _uiState.value = CropAnalysisUiState.Success(analysis)
                }
                .onFailure { error ->
                    _uiState.value = CropAnalysisUiState.Error(error.message ?: "Unknown error occurred")
                }
        }
    }
    
    fun retryAnalysis() {
        lastImageUri?.let { uri ->
            analyzeCropImage(uri)
        }
    }
    
    fun resetState() {
        _uiState.value = CropAnalysisUiState.Initial
        lastImageUri = null
    }
}

sealed class CropAnalysisUiState {
    object Initial : CropAnalysisUiState()
    object Loading : CropAnalysisUiState()
    data class Success(val analysis: CropAnalysis) : CropAnalysisUiState()
    data class Error(val message: String) : CropAnalysisUiState()
} 