package ru.mrgrd56.api.controllers

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import ru.mrgrd56.api.storages.StringMemoryMappedStorage

@RestController
@RequestMapping("storage/strmem")
class StringMemoryMappedStorageController(private val storage: StringMemoryMappedStorage) {
    @RequestMapping(
        value = ["{key}/get", "{key}"],
        method = [RequestMethod.GET, RequestMethod.POST],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun getValue(@PathVariable key: String): String {
        return storage[key]
    }

    @RequestMapping(
        value = ["{key}/set/{value}"],
        method = [RequestMethod.GET, RequestMethod.POST],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun setValue(@PathVariable key: String, @PathVariable value: String): String {
        return storage.set(key, value)
    }

    @RequestMapping(
        value = ["{key}/add/{value}", "{key}/create/{value}"],
        method = [RequestMethod.GET, RequestMethod.POST],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun addValue(@PathVariable key: String, @PathVariable value: String): String {
        return storage.add(key, value)
    }

    @RequestMapping(
        value = ["{key}/update/{value}"],
        method = [RequestMethod.GET, RequestMethod.POST],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun updateValue(@PathVariable key: String, @PathVariable value: String): String {
        return storage.update(key, value)
    }

    @RequestMapping(
        value = ["{key}/delete", "{key}/remove"],
        method = [RequestMethod.GET, RequestMethod.POST],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun deleteValue(@PathVariable key: String) {
        storage.delete(key)
    }
}
