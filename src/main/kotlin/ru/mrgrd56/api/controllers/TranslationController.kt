package ru.mrgrd56.api.controllers

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.mrgrd56.api.translation.TranslationService

@RestController
class TranslationController(private val translationService: TranslationService) {
    @GetMapping(value = ["translate/{translator}"], produces = [MediaType.TEXT_PLAIN_VALUE])
    fun translate(
        @PathVariable translator: String?,
        @RequestParam from: String?,
        @RequestParam to: String?,
        @RequestParam text: String?
    ): String {
        return translationService.translate(translator!!, from!!, to!!, text!!)
    }
}
