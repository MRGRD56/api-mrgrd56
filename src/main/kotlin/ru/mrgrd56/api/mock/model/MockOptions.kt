package ru.mrgrd56.api.mock.model

data class MockOptions(
    private val withDelay: Boolean?
) {
    val isWithDelay = withDelay == true
}
