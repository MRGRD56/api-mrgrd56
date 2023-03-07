package ru.mrgrd56.api.translation;

import org.springframework.stereotype.Component;
import ru.mrgrd56.api.translation.translator.Translator;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TranslatorProvider {
    private final Map<String, Translator> translatorsMap;

    public TranslatorProvider(List<Translator> translators) {
        this.translatorsMap = translators.stream()
                .collect(Collectors.toMap(Translator::getName, Function.identity()));
    }

    public Translator getTranslator(String name) {
        return translatorsMap.get(name.toLowerCase());
    }
}
