package com.powerwatch.app.data.repository

import com.powerwatch.app.data.remote.ApiEnvelope
import com.powerwatch.app.data.remote.CommunityReportDto
import com.powerwatch.app.data.remote.PowerWatchApi
import com.powerwatch.app.data.remote.SubmitCommunityReportRequestDto
import com.powerwatch.app.data.remote.VerifyMeterRequestDto
import com.powerwatch.app.domain.model.AppError
import com.powerwatch.app.domain.model.AppResult
import com.powerwatch.app.domain.model.CommunityReport
import com.powerwatch.app.domain.model.Disco
import com.powerwatch.app.domain.model.MeterVerification
import com.powerwatch.app.domain.model.PowerStatus
import com.powerwatch.app.domain.repository.MeterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.content
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

class MeterRepositoryImpl @Inject constructor(
    private val api: PowerWatchApi,
    private val json: Json
) : MeterRepository {

    override suspend fun getDiscos(): AppResult<List<Disco>> = withContext(Dispatchers.IO) {
        safeCall(
            call = { api.getDiscos() },
            onSuccess = { list -> list.map { Disco(it.code, it.name) } }
        )
    }

    override suspend fun verifyMeter(meterNumber: String, discoCode: String): AppResult<MeterVerification> =
        withContext(Dispatchers.IO) {
            safeCall(
                call = { api.verifyMeter(VerifyMeterRequestDto(meterNumber, discoCode)) },
                onSuccess = { dto ->
                    MeterVerification(
                        verified = dto.verified,
                        customerName = dto.customerName,
                        meterNumberMasked = dto.meterNumberMasked,
                        meterType = dto.meterType,
                        discoCode = dto.discoCode,
                        address = dto.address,
                        outageStatus = dto.outageStatus
                    )
                }
            )
        }

    override suspend fun getCommunityReports(discoCode: String?): AppResult<List<CommunityReport>> =
        withContext(Dispatchers.IO) {
            safeCall(
                call = { api.getCommunityReports(discoCode) },
                onSuccess = { list -> list.map { it.toDomain() } }
            )
        }

    override suspend fun submitCommunityReport(
        discoCode: String,
        areaOrMeterHint: String,
        status: PowerStatus
    ): AppResult<CommunityReport> = withContext(Dispatchers.IO) {
        safeCall(
            call = {
                api.submitCommunityReport(
                    SubmitCommunityReportRequestDto(
                        discoCode = discoCode,
                        areaOrMeterHint = areaOrMeterHint,
                        status = status.name.lowercase()
                    )
                )
            },
            onSuccess = { it.toDomain() }
        )
    }

    private fun CommunityReportDto.toDomain() = CommunityReport(
        id = id,
        discoCode = discoCode,
        areaOrMeterHint = areaOrMeterHint,
        status = if (status.equals("on", ignoreCase = true)) PowerStatus.ON else PowerStatus.OFF,
        reportedAt = reportedAt
    )

    /**
     * Shared plumbing: runs a Retrofit call, maps HTTP/parsing/network
     * failures into [AppError], and never lets an exception escape to
     * the ViewModel layer un-mapped.
     */
    private inline fun <T, R> safeCall(
        call: () -> Response<ApiEnvelope<T>>,
        onSuccess: (T) -> R
    ): AppResult<R> {
        return try {
            val response = call()
            val body = response.body()

            if (response.isSuccessful && body?.success == true && body.data != null) {
                AppResult.Success(onSuccess(body.data))
            } else {
                val errorBody = body?.error ?: parseErrorBody(response.errorBody())
                AppResult.Failure(mapErrorCodeToAppError(response.code(), errorBody?.message))
            }
        } catch (e: SocketTimeoutException) {
            AppResult.Failure(AppError.Timeout)
        } catch (e: IOException) {
            AppResult.Failure(AppError.NoConnection)
        } catch (e: Exception) {
            AppResult.Failure(AppError.Unknown(e.message ?: "Unexpected error"))
        }
    }

    /** Parses just the "error" object out of a failed response body, without needing the generic data type. */
    private fun parseErrorBody(errorBody: ResponseBody?): com.powerwatch.app.data.remote.ApiErrorBody? {
        return try {
            val raw = errorBody?.string() ?: return null
            val root = json.parseToJsonElement(raw).jsonObject
            val errorObj = root["error"]?.jsonObject ?: return null
            com.powerwatch.app.data.remote.ApiErrorBody(
                code = errorObj["code"]?.jsonPrimitive?.content ?: "UNKNOWN",
                message = errorObj["message"]?.jsonPrimitive?.content ?: "Unexpected error."
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun mapErrorCodeToAppError(httpCode: Int, message: String?): AppError {
        val msg = message ?: "The verification could not be completed."
        return when (httpCode) {
            400 -> AppError.Validation(msg)
            404 -> AppError.NotVerified(msg)
            401, 403 -> AppError.Server("Not authorized to reach the verification service.")
            in 500..599 -> AppError.Server(msg)
            else -> AppError.Unknown(msg)
        }
    }
}
