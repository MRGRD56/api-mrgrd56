package ru.mrgrd56.api.utils

inline infix fun <T> T?.ifNull(action: () -> Unit) {
    if (this == null) {
        action()
    }
}

inline infix fun <C, R> C?.ifNullOrBlank(defaultValue: () -> R): R where R : CharSequence, C : R =
    if (isNullOrBlank()) defaultValue() else this
