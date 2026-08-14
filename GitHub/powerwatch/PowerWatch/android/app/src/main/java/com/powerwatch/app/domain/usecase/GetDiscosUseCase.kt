package com.powerwatch.app.domain.usecase

import com.powerwatch.app.domain.model.AppResult
import com.powerwatch.app.domain.model.Disco
import com.powerwatch.app.domain.repository.MeterRepository
import javax.inject.Inject

class GetDiscosUseCase @Inject constructor(
    private val repository: MeterRepository
) {
    suspend operator fun invoke(): AppResult<List<Disco>> = repository.getDiscos()
}
