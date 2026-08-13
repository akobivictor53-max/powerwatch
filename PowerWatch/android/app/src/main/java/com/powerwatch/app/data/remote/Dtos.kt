package com.powerwatch.app.data.remote

import kotlinx.serialization.Serializable

/** Generic envelope returned by every PowerWatch backend endpoint. */
@Serializable
data class ApiEnvelope<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiErrorBody? = null,
    val disclaimer: String? = null
)

@Serializable
data class ApiErrorBody(
    val code: String,
    val message: String
)

@Serializable
data class DiscoDto(
    val code: String,
    val name: String
)

@Serializable
data class VerifyMeterRequestDto(
    val meterNumber: String,
    val discoCode: String
)

@Serializable
data class MeterVerificationDto(
    val verified: Boolean,
    val customerName: String? = null,
    val meterNumberMasked: String? = null,
    val meterType: String? = null,
    val discoCode: String? = null,
    val address: String? = null,
    val outageStatus: String? = null
)

@Serializable
data class CommunityReportDto(
    val id: String,
    val discoCode: String,
    val areaOrMeterHint: String,
    val status: String,
    val reportedAt: String
)

@Serializable
data class SubmitCommunityReportRequestDto(
    val discoCode: String,
    val areaOrMeterHint: String,
    val status: String
)
