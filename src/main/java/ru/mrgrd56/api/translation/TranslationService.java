package ru.mrgrd56.api.translation;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;
import ru.mrgrd56.api.exception.NotFoundException;
import ru.mrgrd56.api.translation.translator.Translator;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class TranslationService {
    Cache<TranslationRequestHash, String> cache = Caffeine.newBuilder()
            .maximumSize(3000L)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    private final TranslatorProvider translatorProvider;

    public TranslationService(TranslatorProvider translatorProvider) {
        this.translatorProvider = translatorProvider;
    }

    public String translate(String translatorName, String from, String to, String text) {
        Translator translator = translatorProvider.get(translatorName);
        if (translator == null) {
            throw new NotFoundException("Translator not found");
        }

        return cache.get(new TranslationRequestHash(translatorName, from, to, text), k -> {
            return Objects.requireNonNull(translator.translate(text, from, to));
        });
    }

    private record TranslationRequestHash(String translatorName, String from, String to, String text) { }
}
