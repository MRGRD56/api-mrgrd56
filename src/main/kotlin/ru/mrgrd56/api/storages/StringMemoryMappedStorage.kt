package ru.mrgrd56.api.storages

import org.springframework.stereotype.Component
import ru.mrgrd56.api.exception.BadRequestException
import ru.mrgrd56.api.exception.ConflictException
import ru.mrgrd56.api.exception.NotFoundException
import java.util.concurrent.ConcurrentHashMap

@Component
class StringMemoryMappedStorage : MappedStorage<String> {
    private val values: MutableMap<String, String?> = ConcurrentHashMap()
    override fun has(key: String): Boolean {
        return values.containsKey(key)
    }

    @Throws(NotFoundException::class)
    override fun get(key: String): String {
        if (!has(key)) {
            throw NotFoundException("Element does not exist")
        }

        return values[key]!!
    }

    @Throws(BadRequestException::class)
    override operator fun set(key: String, value: String): String {
        if (value.length > 2000) {
            throw BadRequestException("Invalid value: it must not be null and must not be longer than 2000 characters")
        }

        values[key] = value
        return value
    }

    @Throws(ConflictException::class)
    override fun add(key: String, value: String): String {
        if (has(key)) {
            throw ConflictException("Element with the provided key already exists")
        }

        return set(key, value)
    }

    @Throws(NotFoundException::class)
    override fun update(key: String, value: String): String {
        if (!has(key)) {
            throw NotFoundException("Element does not exist")
        }

        return set(key, value)
    }

    @Throws(NotFoundException::class)
    override fun delete(key: String) {
        if (values.remove(key) == null) {
            throw NotFoundException("Element does not exist")
        }
    }
}
