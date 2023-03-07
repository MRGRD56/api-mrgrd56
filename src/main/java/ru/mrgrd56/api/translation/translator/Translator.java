package ru.mrgrd56.api.translation.translator;

import ru.mrgrd56.api.common.provider.Providable;

public interface Translator extends Providable {
    String translate(String text, String from, String to);
}
