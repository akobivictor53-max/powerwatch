package com.powerwatch.app.domain.usecase

import com.powerwatch.app.domain.model.AppError
import com.powerwatch.app.domain.model.AppResult
import com.powerwatch.app.domain.model.MeterVerification
import com.powerwatch.app.domain.repository.MeterRepository
import javax.inject.Inject

/**
 * Encapsulates the "verify a meter" business rule: basic client-side
 * shape validation (fast feedback, no network round-trip for obvious
 * typos), then delegates to the repository.
 */
class VerifyMeterUseCase @Inject constructor(
    private val repository: MeterRepository
) {
    suspend operator fun invoke(meterNumber: String, discoCode: String): AppResult<MeterVerification> {
        val trimmed = meterNumber.trim()

        if (discoCode.isBlank()) {
            return AppResult.Failure(AppError.Validation("Please select a Disco."))
        }
        if (!trimmed.matches(Regex("^\\d{10,13}$"))) {
            return AppResult.Failure(AppError.Validation("Meter number must be 10-13 digits."))
        }

        return repository.verifyMeter(trimmed, discoCode)
    }
}
