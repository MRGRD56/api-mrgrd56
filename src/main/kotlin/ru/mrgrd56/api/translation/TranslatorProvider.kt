package ru.mrgrd56.api.translation

import org.springframework.stereotype.Component
import ru.mrgrd56.api.common.provider.BeanProvider
import ru.mrgrd56.api.translation.translator.Translator

@Component
class TranslatorProvider(translators: List<Translator>) : BeanProvider<Translator>(translators)
