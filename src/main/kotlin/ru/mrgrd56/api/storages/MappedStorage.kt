package ru.mrgrd56.api.storages

interface MappedStorage<T> {
    fun has(key: String): Boolean
    operator fun get(key: String): T
    operator fun set(key: String, value: T): T
    fun add(key: String, value: T): T
    fun update(key: String, value: T): T
    fun delete(key: String)
}
