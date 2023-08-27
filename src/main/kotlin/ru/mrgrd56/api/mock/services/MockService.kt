package ru.mrgrd56.api.mock.services

import com.github.javafaker.Faker
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import ru.mrgrd56.api.mock.model.MockItemDto
import ru.mrgrd56.api.mock.model.PageDto
import java.text.SimpleDateFormat
import java.util.*
import javax.annotation.PostConstruct
import kotlin.math.ceil


@Service
class MockService {
    private val faker = Faker(Locale.US)

    private val items = LinkedHashMap<UUID, MockItemDto>(350)

    @PostConstruct
    private fun initializeItems() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")

        val minDate = dateFormat.parse("2015-01-14")
        val maxDate = Date()

        synchronized(items) {
            repeat((250..350).random()) {
                val item = MockItemDto(
                    id = UUID.randomUUID(),
                    name = faker.commerce().productName(),
                    description = faker.lorem().sentence((10..50).random()),
                    createdAt = faker.date().between(minDate, maxDate).toInstant(),
                    price = faker.number().randomDouble(2, 30L, 1500L)
                )

                items[item.id] = item
            }
        }
    }

    fun getItemsPage(pageable: Pageable): PageDto<MockItemDto> {
        val (totalItemsCount, pageItems) = synchronized(items) {
            items.values.size to items.values.asSequence()
                .drop(pageable.pageSize * pageable.pageNumber)
                .take(pageable.pageSize)
                .toList()
        }

        return PageDto(
            items = pageItems,
            pageNumber = pageable.pageNumber,
            totalPages = ceil(totalItemsCount.toDouble() / pageable.pageSize).toInt(),
            totalItems = totalItemsCount.toLong()
        )
    }

    fun getAllItems(): List<MockItemDto> {
        return synchronized(items) { items.values.toList() }
    }

    fun getItemById(id: UUID): MockItemDto? {
        return synchronized(items) { items[id] }
    }

    // TODO add CRUD operations
}