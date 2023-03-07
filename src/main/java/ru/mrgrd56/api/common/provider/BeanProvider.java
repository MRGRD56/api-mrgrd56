package ru.mrgrd56.api.common.provider;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BeanProvider<T extends Providable> {
    private final Map<String, T> beansMap;

    protected BeanProvider(List<T> beans) {
        this.beansMap = beans.stream()
                .collect(Collectors.toMap(Providable::getName, Function.identity()));
    }

    public T get(String name) {
        return beansMap.get(name.toLowerCase());
    }
}
