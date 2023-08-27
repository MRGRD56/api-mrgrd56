package ru.mrgrd56.api.mock.model

import java.time.Instant
import java.util.*

data class MockItemDto(
    val id: UUID,
    val name: String,
    val description: String?,
    val createdAt: Instant,
    val price: Double
)
