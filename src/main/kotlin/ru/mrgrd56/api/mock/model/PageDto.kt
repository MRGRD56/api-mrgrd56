package ru.mrgrd56.api.mock.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder("items", "pageNumber", "totalPages", "totalItems", "isLastPage")
data class PageDto<T>(
    val items: List<T>,
    val pageNumber: Int,
    val totalPages: Int,
    val totalItems: Long
) {
    @get:JsonProperty("isLastPage")
    val isLastPage: Boolean
        get() = pageNumber >= totalPages - 1
}
