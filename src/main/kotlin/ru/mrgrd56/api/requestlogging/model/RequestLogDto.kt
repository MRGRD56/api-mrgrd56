package ru.mrgrd56.api.requestlogging.model

import java.time.Instant

data class RequestLogDto(
    val time: Instant,
    val method: String,
    val path: String,
    val query: String?,
    val headers: Map<String, String>,
    val body: String?
)