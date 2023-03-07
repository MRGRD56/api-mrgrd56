package ru.mrgrd56.api.storage.storages;

import org.springframework.stereotype.Component;
import ru.mrgrd56.api.exception.BadRequestException;
import ru.mrgrd56.api.exception.ConflictException;
import ru.mrgrd56.api.exception.NotFoundException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StringMemoryMappedStorage implements MappedStorage<String> {
    private final Map<String, String> values = new ConcurrentHashMap<>();

    @Override
    public boolean has(String key) {
        return values.containsKey(key);
    }

    @Override
    public String get(String key) throws NotFoundException {
        if (!has(key)) {
            throw new NotFoundException("Element does not exist");
        }

        return values.get(key);
    }

    @Override
    public String set(String key, String value) throws BadRequestException {
        if (value == null || value.length() > 2_000) {
            throw new BadRequestException("Invalid value: it must not be null and must not be longer than 2000 characters");
        }

        values.put(key, value);
        return value;
    }

    @Override
    public String add(String key, String value) throws ConflictException {
        if (has(key)) {
            throw new ConflictException("Element with the provided key already exists");
        }

        return set(key, value);
    }

    @Override
    public String update(String key, String value) throws NotFoundException {
        if (!has(key)) {
            throw new NotFoundException("Element does not exist");
        }

        return set(key, value);
    }

    @Override
    public void delete(String key) throws NotFoundException {
        if (values.remove(key) == null) {
            throw new NotFoundException("Element does not exist");
        }
    }
}
