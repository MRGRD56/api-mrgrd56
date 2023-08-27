package ru.mrgrd56.api.mock.services

import org.springframework.data.domain.Pageable
import ru.mrgrd56.api.mock.model.PageDto

interface ReadableMockService<ID, T> {
    fun getItemsPage(pageable: Pageable): PageDto<T>

    fun getAllItems(): List<T>

    fun getItemById(id: ID): T?
}