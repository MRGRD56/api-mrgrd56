package ru.mrgrd56.api.translation;

import org.springframework.stereotype.Component;
import ru.mrgrd56.api.common.provider.BeanProvider;
import ru.mrgrd56.api.translation.translator.Translator;

import java.util.List;

@Component
public class TranslatorProvider extends BeanProvider<Translator> {
    public TranslatorProvider(List<Translator> translators) {
        super(translators);
    }
}
