package ru.mrgrd56.api.translation

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class TranslationService(private val translatorProvider: TranslatorProvider) {
    private val cache: Cache<TranslationRequestHash, String> = Caffeine.newBuilder()
            .maximumSize(3000L)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build()

    fun translate(translatorName: String, from: String, to: String, text: String): String {
        return cache.get(TranslationRequestHash(translatorName, from, to, text)) {
            translatorProvider[translatorName].translate(text, from, to)!!
        }
    }

    private data class TranslationRequestHash(
        val translatorName: String,
        val from: String,
        val to: String,
        val text: String
    )
}
