package ru.mrgrd56.api.controllers;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.mrgrd56.api.storage.storages.StringMemoryMappedStorage;

@RestController
@RequestMapping("storage/strmem")
public class StringMemoryMappedStorageController {
    private final StringMemoryMappedStorage storage;

    public StringMemoryMappedStorageController(StringMemoryMappedStorage storage) {
        this.storage = storage;
    }

    @RequestMapping(value = {"{key}/get", "{key}"},
            method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_PLAIN_VALUE)
    public String getValue(@PathVariable String key) {
        return storage.get(key);
    }

    @RequestMapping(value = "{key}/set/{value}",
            method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_PLAIN_VALUE)
    public String setValue(@PathVariable String key, @PathVariable String value) {
        return storage.set(key, value);
    }

    @RequestMapping(value = {"{key}/add/{value}", "{key}/create/{value}"},
            method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_PLAIN_VALUE)
    public String addValue(@PathVariable String key, @PathVariable String value) {
        return storage.add(key, value);
    }

    @RequestMapping(value = "{key}/update/{value}",
            method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_PLAIN_VALUE)
    public String updateValue(@PathVariable String key, @PathVariable String value) {
        return storage.update(key, value);
    }

    @RequestMapping(value = {"{key}/delete", "{key}/remove"},
            method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_PLAIN_VALUE)
    public void deleteValue(@PathVariable String key) {
        storage.delete(key);
    }
}