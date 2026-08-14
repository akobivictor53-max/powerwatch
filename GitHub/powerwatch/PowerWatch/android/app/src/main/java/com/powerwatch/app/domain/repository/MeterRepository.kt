package com.powerwatch.app.domain.repository

import com.powerwatch.app.domain.model.AppResult
import com.powerwatch.app.domain.model.CommunityReport
import com.powerwatch.app.domain.model.Disco
import com.powerwatch.app.domain.model.MeterVerification
import com.powerwatch.app.domain.model.PowerStatus

/**
 * Domain-facing contract for meter data. The UI/ViewModel layer only knows
 * about this interface, never about Retrofit/HTTP — that's the point of
 * clean architecture's data/domain separation.
 */
interface MeterRepository {
    suspend fun getDiscos(): AppResult<List<Disco>>

    suspend fun verifyMeter(meterNumber: String, discoCode: String): AppResult<MeterVerification>

    suspend fun getCommunityReports(discoCode: String?): AppResult<List<CommunityReport>>

    suspend fun submitCommunityReport(
        discoCode: String,
        areaOrMeterHint: String,
        status: PowerStatus
    ): AppResult<CommunityReport>
}
