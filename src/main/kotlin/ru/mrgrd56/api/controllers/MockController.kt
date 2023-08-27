package ru.mrgrd56.api.controllers

import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.mrgrd56.api.mock.model.MockItemDto
import ru.mrgrd56.api.mock.model.MockOptions
import ru.mrgrd56.api.mock.model.PageDto
import ru.mrgrd56.api.mock.services.MockService
import ru.mrgrd56.api.mock.utils.mock
import java.util.*

@RestController
@RequestMapping("mock")
class MockController(
    private val mockService: MockService
) {
    @GetMapping
    suspend fun getItemsPage(pageable: Pageable, mockOptions: MockOptions) = mock<PageDto<MockItemDto>>(mockOptions) {
        ResponseEntity.ok(mockService.getItemsPage(pageable))
    }

    @GetMapping("all")
    suspend fun getAllItems(mockOptions: MockOptions) = mock<List<MockItemDto>>(mockOptions) {
        ResponseEntity.ok(mockService.getAllItems())
    }

    @GetMapping("{id}")
    suspend fun getItemById(@PathVariable("id") id: UUID, mockOptions: MockOptions) = mock<MockItemDto>(mockOptions) {
        mockService.getItemById(id)?.let {
            return@mock ResponseEntity.ok(it)
        }

        ResponseEntity.notFound().build()
    }
}