package ru.mrgrd56.api.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.mrgrd56.api.translation.TranslationService;

@RestController
public class TranslationController {
    private final TranslationService translationService;

    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @GetMapping("translate/{translator}")
    public String translate(
            @PathVariable String translator,
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam String text) {
        return translationService.translate(translator, from, to, text);
    }
}
