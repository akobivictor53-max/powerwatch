package com.powerwatch.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Talks ONLY to our own backend — never directly to Sogo. The Android app
 * has no knowledge of Sogo's endpoint, auth scheme, or secret key.
 */
interface PowerWatchApi {

    @GET("api/discos")
    suspend fun getDiscos(): Response<ApiEnvelope<List<DiscoDto>>>

    @POST("api/meters/verify")
    suspend fun verifyMeter(@Body body: VerifyMeterRequestDto): Response<ApiEnvelope<MeterVerificationDto>>

    @GET("api/community/reports")
    suspend fun getCommunityReports(
        @Query("discoCode") discoCode: String?
    ): Response<ApiEnvelope<List<CommunityReportDto>>>

    @POST("api/community/reports")
    suspend fun submitCommunityReport(
        @Body body: SubmitCommunityReportRequestDto
    ): Response<ApiEnvelope<CommunityReportDto>>
}
