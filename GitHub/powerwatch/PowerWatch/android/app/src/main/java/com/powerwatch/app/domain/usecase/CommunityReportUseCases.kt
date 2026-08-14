package com.powerwatch.app.domain.usecase

import com.powerwatch.app.domain.model.AppResult
import com.powerwatch.app.domain.model.CommunityReport
import com.powerwatch.app.domain.model.PowerStatus
import com.powerwatch.app.domain.repository.MeterRepository
import javax.inject.Inject

class GetCommunityReportsUseCase @Inject constructor(
    private val repository: MeterRepository
) {
    suspend operator fun invoke(discoCode: String?): AppResult<List<CommunityReport>> =
        repository.getCommunityReports(discoCode)
}

class SubmitCommunityReportUseCase @Inject constructor(
    private val repository: MeterRepository
) {
    suspend operator fun invoke(
        discoCode: String,
        areaOrMeterHint: String,
        status: PowerStatus
    ): AppResult<CommunityReport> = repository.submitCommunityReport(discoCode, areaOrMeterHint, status)
}
