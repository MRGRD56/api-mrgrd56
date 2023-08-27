package ru.mrgrd56.api.common.provider

import ru.mrgrd56.api.exception.NotFoundException
import java.util.*

open class BeanProvider<T : Providable> protected constructor(beans: List<T>) {
    private val beansMap: Map<String, T>

    init {
        beansMap = beans.associateBy { it.name }
    }

    @Throws(NotFoundException::class)
    operator fun get(name: String): T {
        return beansMap[name.lowercase(Locale.getDefault())] ?: throw NotFoundException("Bean not found")
    }
}
