package ru.mrgrd56.api.translation.translator;

public interface Translator {
    String getName();

    String translate(String text, String from, String to);
}
