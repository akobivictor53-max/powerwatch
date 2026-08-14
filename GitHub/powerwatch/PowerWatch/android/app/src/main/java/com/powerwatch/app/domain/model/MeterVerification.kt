package com.powerwatch.app.domain.model

/**
 * Result of verifying a meter, containing ONLY fields the backend actually
 * returned (which in turn only reflects what Sogo actually returned).
 * Any field the provider didn't supply is null — the UI must not
 * fabricate a value for it.
 */
data class MeterVerification(
    val verified: Boolean,
    val customerName: String?,
    val meterNumberMasked: String?,
    val meterType: String?,
    val discoCode: String?,
    val address: String?,
    /** Only populated if a legitimate, disclosed data source provides it. */
    val outageStatus: String?
)
