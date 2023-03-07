package ru.mrgrd56.api.storage.storages;

public interface MappedStorage<T> {
    boolean has(String key);
    T get(String key);
    T set(String key, T value);
    T add(String key, T value);
    T update(String key, T value);
    void delete(String key);
}
