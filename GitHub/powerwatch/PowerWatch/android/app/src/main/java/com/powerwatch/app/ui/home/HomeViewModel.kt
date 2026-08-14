package com.powerwatch.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerwatch.app.domain.model.AppResult
import com.powerwatch.app.domain.usecase.GetCommunityReportsUseCase
import com.powerwatch.app.domain.usecase.GetDiscosUseCase
import com.powerwatch.app.domain.usecase.SubmitCommunityReportUseCase
import com.powerwatch.app.domain.usecase.VerifyMeterUseCase
import com.powerwatch.app.domain.model.PowerStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getDiscos: GetDiscosUseCase,
    private val verifyMeter: VerifyMeterUseCase,
    private val getCommunityReports: GetCommunityReportsUseCase,
    private val submitCommunityReport: SubmitCommunityReportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDiscos()
    }

    private fun loadDiscos() {
        viewModelScope.launch {
            when (val result = getDiscos()) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        discos = result.data,
                        selectedDiscoCode = result.data.firstOrNull()?.code
                    )
                }
                is AppResult.Failure -> {
                    // Non-fatal: user can still type a meter number once Discos load,
                    // or retry. We surface this softly rather than blocking the screen.
                    _uiState.update { it.copy(discos = emptyList()) }
                }
            }
        }
    }

    fun onMeterNumberChanged(value: String) {
        // Only allow digits, and cap length defensively.
        val digitsOnly = value.filter { it.isDigit() }.take(13)
        _uiState.update { it.copy(meterNumberInput = digitsOnly) }
    }

    fun onDiscoSelected(discoCode: String) {
        _uiState.update { it.copy(selectedDiscoCode = discoCode) }
        loadCommunityReports(discoCode)
    }

    fun onVerifyClicked() {
        val state = _uiState.value
        val discoCode = state.selectedDiscoCode

        if (discoCode == null) {
            _uiState.update { it.copy(verifyStatus = VerifyStatus.Error("Please select a Disco first.")) }
            return
        }

        _uiState.update { it.copy(verifyStatus = VerifyStatus.Loading) }

        viewModelScope.launch {
            when (val result = verifyMeter(state.meterNumberInput, discoCode)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(verifyStatus = VerifyStatus.Success(result.data))
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(verifyStatus = VerifyStatus.Error(result.error.message))
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(verifyStatus = VerifyStatus.Idle) }
    }

    private fun loadCommunityReports(discoCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(communityReportsLoading = true) }
            when (val result = getCommunityReports(discoCode)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(communityReports = result.data, communityReportsLoading = false)
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(communityReportsLoading = false)
                }
            }
        }
    }

    fun submitReport(areaHint: String, status: PowerStatus) {
        val discoCode = _uiState.value.selectedDiscoCode ?: return
        viewModelScope.launch {
            when (submitCommunityReport(discoCode, areaHint, status)) {
                is AppResult.Success -> loadCommunityReports(discoCode)
                is AppResult.Failure -> Unit // Silently ignore; non-critical optional feature.
            }
        }
    }
}
