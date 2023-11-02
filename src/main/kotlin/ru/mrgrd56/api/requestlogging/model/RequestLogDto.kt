package ru.mrgrd56.api.requestlogging.model

import java.time.Instant
import java.util.UUID

data class RequestLogDto(
    val id: UUID,
    val time: Instant,
    val method: String,
    val path: String,
    val query: String?,
    val headers: Map<String, String>,
    val body: String?
)