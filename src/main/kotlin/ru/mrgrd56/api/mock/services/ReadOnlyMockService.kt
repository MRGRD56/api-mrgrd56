package ru.mrgrd56.api.mock.services

import com.github.javafaker.Faker
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import ru.mrgrd56.api.mock.model.MockItemDto
import ru.mrgrd56.api.mock.model.PageDto
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil


@Service
class ReadOnlyMockService(
    private val faker: Faker
) : ReadableMockService<UUID, MockItemDto> {
    private val items = createItems()

    override fun getItemsPage(pageable: Pageable): PageDto<MockItemDto> {
        val totalItemsCount = items.values.size
        val pageItems = items.values.asSequence()
            .drop(pageable.pageSize * pageable.pageNumber)
            .take(pageable.pageSize)
            .toList()

        return PageDto(
            items = pageItems,
            pageNumber = pageable.pageNumber,
            totalPages = ceil(totalItemsCount.toDouble() / pageable.pageSize).toInt(),
            totalItems = totalItemsCount.toLong()
        )
    }

    override fun getAllItems(): List<MockItemDto> {
        return items.values.toList()
    }

    override fun getItemById(id: UUID): MockItemDto? {
        return items[id]
    }

    private fun createItems(): Map<UUID, MockItemDto> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")

        val minDate = dateFormat.parse("2015-01-14")
        val maxDate = Date()

        val itemsCount = (250..350).random()

        val itemsMap = LinkedHashMap<UUID, MockItemDto>(ceil(itemsCount / 0.75).toInt(), 0.75f)

        repeat(itemsCount) {
            val item = MockItemDto(
                id = UUID.randomUUID(),
                name = faker.commerce().productName(),
                description = faker.lorem().sentence((10..50).random()),
                createdAt = faker.date().between(minDate, maxDate).toInstant(),
                price = faker.number().randomDouble(2, 30L, 1500L)
            )

            itemsMap[item.id] = item
        }

        return Collections.unmodifiableMap(itemsMap)
    }
}