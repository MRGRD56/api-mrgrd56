package ru.mrgrd56.api.translation.translator

import ru.mrgrd56.api.common.provider.Providable

interface Translator : Providable {
    fun translate(text: String?, from: String?, to: String?): String?
}
