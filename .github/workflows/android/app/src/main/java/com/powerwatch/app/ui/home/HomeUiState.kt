package com.powerwatch.app.ui.home

import com.powerwatch.app.domain.model.CommunityReport
import com.powerwatch.app.domain.model.Disco
import com.powerwatch.app.domain.model.MeterVerification

/** Everything the Home screen needs to render, in one immutable snapshot. */
data class HomeUiState(
    val discos: List<Disco> = emptyList(),
    val selectedDiscoCode: String? = null,
    val meterNumberInput: String = "",
    val verifyStatus: VerifyStatus = VerifyStatus.Idle,
    val communityReports: List<CommunityReport> = emptyList(),
    val communityReportsLoading: Boolean = false
)

sealed class VerifyStatus {
    data object Idle : VerifyStatus()
    data object Loading : VerifyStatus()
    data class Success(val result: MeterVerification) : VerifyStatus()
    data class Error(val message: String) : VerifyStatus()
}
