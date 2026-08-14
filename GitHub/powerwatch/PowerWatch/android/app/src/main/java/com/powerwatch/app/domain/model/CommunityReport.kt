package com.powerwatch.app.domain.model

/** A user-submitted report about power status in an area. Never authoritative. */
data class CommunityReport(
    val id: String,
    val discoCode: String,
    val areaOrMeterHint: String,
    val status: PowerStatus,
    val reportedAt: String
)

enum class PowerStatus { ON, OFF }
